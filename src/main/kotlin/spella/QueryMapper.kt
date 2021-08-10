package spella

import kotlin.math.min

typealias Phrase = ArrayList<String>
typealias Sentence = ArrayList<Phrase>

data class DistanceAndScore(val distance: Double, val score: Double) : Comparable<DistanceAndScore> {
    override operator fun compareTo(other: DistanceAndScore): Int {
        if (distance < other.distance) return -1
        if (distance > other.distance) return 1

        if (score > other.score) return -1
        if (score < other.score) return 1

        return 0
    }
}

class QueryMapper(string: String, language: String, tries: Tries, lookahead: Int = 3) {
    val string = string
    val language = language
    val tries = tries
    val lookahead = lookahead
    private val trie = tries[language]
    private val words: List<String> by lazy { string.split(" ") }

    fun map(): Correction {
        val correctionCache = generateCorrections()

        val sentence = generateSentences().minByOrNull { sentence ->
            val corrections = sentence.map { phrase -> correctionCache[phrase] ?: fallbackCorrection(phrase) }

            DistanceAndScore(
                corrections.sumOf { it.distance } * corrections.size.toDouble(),
                corrections.sumOf { it.score }
            )
        }

        if (sentence == null) {
            return Correction(string.toTransliterableString(), string.toTransliterableString(), 0, 0.0, true)
        }

        val corrections = sentence.map { phrase -> correctionCache[phrase] ?: fallbackCorrection(phrase) }

        return Correction(
            corrections.map { it.value.string }.joinToString(", ").toTransliterableString(),
            string.toTransliterableString(),
            corrections.sumOf { it.distance },
            corrections.sumOf { it.score },
            true
        )
    }

    private fun generateCorrections(): HashMap<Phrase, Correction> {
        val cache = HashMap<Phrase, Correction>()

        for (firstIndex in 0..(words.size - 1)) {
            val correction = Correction("".toTransliterableString(), "".toTransliterableString(), 0, 0.0, true, trie!!)

            generateCorrections(cache, firstIndex, firstIndex, correction)
        }

        return cache
    }

    private fun generateCorrections(cache: HashMap<Phrase, Correction>, firstIndex: Int, lastIndex: Int, prevCorrection: Correction) {
        if (lastIndex - firstIndex + 1 > lookahead) return
        if (lastIndex >= words.size) return

        val phrase = Phrase(words.slice(firstIndex..lastIndex))
        val string = if (firstIndex == lastIndex) phrase.last() else " ${phrase.last()}"
        val word = words[lastIndex]
        val maxEdits = if (word.length <= 3) 0 else if (word.length <= 8) 1 else 2
        val corrections = Automaton(string, maxEdits).correct(prevCorrection.trieNode!!).map { combineCorrections(prevCurrection, it) }
        var bestCorrection = corrections.filter { it.isTerminal }.minOrNull()
        val curCorrection = cache[phrase] ?: bestCorrection

        if (bestCorrection != null) {
            cache[phrase] = if (bestCorrection < curCorrection!!) bestCorrection else curCorrection
        }

        for (correction in corrections) {
            generateCorrections(cache, firstIndex, lastIndex + 1, correction)
        }
    }

    private fun generateSentences(firstIndex: Int = 0): List<Sentence> {
        if (firstIndex >= words.size) return ArrayList<Sentence>().also { it.add(Sentence()) }

        val lastIndex = Math.min(words.size - 1, firstIndex + lookahead - 1)

        return (firstIndex..lastIndex).map { index -> Phrase(words.slice(firstIndex..index)) }.flatMap { phrase ->
            generateSentences(firstIndex + phrase.size).map { sentence ->
                Sentence().also {
                    it.add(phrase)
                    it.addAll(sentence)
                }
            }
        }
    }

    private fun fallbackCorrection(phrase: Phrase): Correction {
        return Correction(
            value = phrase.joinToString("  ").toTransliterableString(),
            original = phrase.joinToString(" ").toTransliterableString(),
            distance = phrase.sumOf { it.length },
            score = 0.0,
            isTerminal = true,
        )
    }

    private fun combineCorrections(prevCorrection: Correction, curCorrection: Correction): Correction {
        return Correction(
            value = curCorrection.value,
            original = "${prevCorrection.original.string}${curCorrection.original.string}".toTransliterableString(),
            distance = curCorrection.distance + prevCorrection.distance,
            score = curCorrection.score,
            isTerminal = curCorrection.isTerminal,
            trieNode = curCorrection.trieNode,
        )
    }
}