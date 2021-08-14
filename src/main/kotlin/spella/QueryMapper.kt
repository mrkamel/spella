package spella

/**
 * The QueryMapper takes an input string, splits it by whitespace and greedily
 * searches for the longest correction. Uses intermediary nodes as an
 * optimization to avoid correcting the prefix words again when a longer one
 * gets corrected.
 */

class QueryMapper(string: String, language: String, tries: Tries, allowedDistances: List<Int>) {
    val string = string
    val language = language
    val tries = tries
    val allowedDistances = allowedDistances
    private val trieNode = tries[language]
    private val wordCorrectionCache = HashMap<String, Correction?>()
    private val trieNodeList: TrieNodeList by lazy { TrieNodeList(trieNode!!) }

    fun map(maxLookahead: Int = 5): Correction {
        if (trieNode == null) return Correction(
            value = string.toTransliterableString(),
            original = string.toTransliterableString(),
            distance = 0,
            score = 0.0,
        )

        val words = string.split(" ").filter { it.length > 0 }
        val corrections = ArrayList<Correction>()
        var i = 0

        while (i < words.size) {
            val max = Math.min(maxLookahead, words.size - i)
            val correction = correctPhrase(words, i, i + max - 1, trieNodeList)
                ?: Correction(
                    words[i].toTransliterableString(),
                    words[i].toTransliterableString(),
                    distance = 0,
                    score = 0.0,
                )

            corrections.add(correction)

            i += correction.original.wordCount
        }

        return Correction(
            value = corrections.map { it.value.string }.joinToString(", ").toTransliterableString(),
            original = string.toTransliterableString(),
            distance = corrections.sumOf { it.distance },
            score = corrections.sumOf { it.score }
        )
    }

    /**
     * Returns the best correction that matches the edit distance criteria by recursively
     * and greedily correcting the list of words up until a word can not be corrected
     * anymore. This guarantees that every single word has the specified max edit
     * distance at most. Otherwise we'd need to increase the max edit distance the
     * longer the string we try to correct, which is not optimal performance wise and we
     * wouldn't be able to guarantee a max edit distance per word.
     */

    private fun correctPhrase(
        words: List<String>,
        firstIndex: Int,
        lastIndex: Int,
        trieNodeList: TrieNodeList,
        phrase: Boolean = false,
        maxRestarts: Int = Int.MAX_VALUE,
    ): Correction? {
        val word = words[firstIndex]
        val maxEdits = maxEdits(word)
        val wordCorrection = correctWord(word, maxEdits)
        val string = if (phrase) " $word" else word
        var bestCorrection: Correction? = null
        var curMaxRestarts = maxRestarts

        Automaton(string = string, maxEdits = maxEdits).correct(trieNodeList).forEach { correction ->
            // Skip the correction if it exceeds the current maximum number of restarts
            if (correction.numRestarts > curMaxRestarts) return@forEach

            // Skip the phrase correction if the word correction distance is better
            if (wordCorrection != null && correction.distance > wordCorrection.distance) return@forEach

            var currentCorrection = correction

            if (firstIndex < lastIndex) {
                correctPhrase(
                    words,
                    firstIndex + 1,
                    lastIndex,
                    correction.trieNodeList!!,
                    phrase = true,
                    maxRestarts = correction.numRestarts
                )?.let{ longerCorrection ->
                    currentCorrection = Correction(
                        value = longerCorrection.value,
                        original = "${correction.original.string}${longerCorrection.original.string}".toTransliterableString(),
                        distance = longerCorrection.distance + correction.distance,
                        score = longerCorrection.score,
                        isTerminal = longerCorrection.isTerminal,
                        trieNodeList = longerCorrection.trieNodeList,
                    )
                }
            }

            if (currentCorrection.isTerminal) {
                bestCorrectionOf(bestCorrection ?: currentCorrection, currentCorrection).let {
                    bestCorrection = it
                    curMaxRestarts = it.numRestarts
                }
            }
        }

        return bestCorrection
    }

    /**
     * Returns the max number of edits for the given word.
     */

    private fun maxEdits(word: String): Int {
        allowedDistances.forEachIndexed { index, allowedDistance ->
            if (word.length <= allowedDistance) {
                return index
            }
        }

        return allowedDistances.size
    }

    /**
     * Lookup and cache the best correction of a single word.
     */

    private fun correctWord(word: String, maxEdits: Int): Correction? {
        return wordCorrectionCache.getOrPut(word) {
            Automaton(string = word, maxEdits = maxEdits).correct(trieNodeList).minOrNull()
        }
    }

    /**
     * Returns the best correction out of two corrections. The criteria for choosing
     * the best correction are:
     * 1. The number of words corrected (more is better)
     * 2. The correction itself with its sort order
     */

    private fun bestCorrectionOf(correction1: Correction, correction2: Correction): Correction {
        if (correction1.original.wordCount > correction2.original.wordCount) return correction1
        if (correction1.original.wordCount < correction2.original.wordCount) return correction2

        if (correction1 < correction2) return correction1

        return correction2
    }
}
