package spelling2g

import kotlin.comparisons.compareBy

/**
 * The QueryMapper takes an input string, splits it by whitespace and greedily
 * searches for the longest correction. Uses intermediary nodes, i.e. the
 * TrieNodeLists, as an optimization to avoid correcting the prefix words again
 * when a longer one gets corrected.
 */

class QueryMapper(string: String, language: String, tries: Tries) {
    val string = string
    val language = language
    val tries = tries
    val trie = tries[language]

    fun map(maxLookahead: Int = 5) : Correction? {
        if (trie == null) return null

        var words = string.split(" ").filter { it.length > 0 }
        var corrections = ArrayList<Correction>()
        var i = 0

        while (i < words.size) {
            var max = Math.min(maxLookahead, words.size - i)
            val correction = correct(words, i, i + max - 1, TrieNodeList(trie))

            corrections.add(correction)

            i += correction.original.wordCount
        }

        return Correction(
            value    = corrections.map { it.value.string }.joinToString(", ").toTransliterableString(),
            original = string.toTransliterableString(),
            distance = corrections.sumOf { it.distance },
            score    = corrections.sumOf { it.score },
        )
    }

    /**
     * Find the best correction for a number of words. Candidates are sorted by
     * 1. number of restarts from the trie root (less is better)
     * 2. The number of words corrected (more is better)
     * 3. The correction itself with its sort order
     * In case no correction can be found, the first word is used and returned as
     * correction.
     */

    private fun correct(
        words: List<String>,
        firstIndex: Int,
        lastIndex: Int,
        nodeList: TrieNodeList,
    ) : Correction {
        val correction = correctAll(words, firstIndex, lastIndex, nodeList)
            .minWithOrNull(compareBy({ it.nodeList!!.tail.size }, { -it.original.wordCount }, { it }))

        return correction ?: Correction(
            words[firstIndex].toTransliterableString(),
            words[firstIndex].toTransliterableString(),
            distance = 0,
            score = 0.0,
        )
    }

    /**
     * Returns the longest available corrections that match the edit distance criteria by
     * recursively and greedily correcting the list of words up until a word can not be
     * corrected anymore. This guarantees that every single word has the specified max
     * edit distance at most. Otherwise we'd need to increase the max edit distance the
     * longer the string we try to correct, which is not optimal performance wise and we
     * wouldn't be able to guarantee a max edit distance per word.
     */

     private fun correctAll(
        words: List<String>,
        firstIndex: Int,
        lastIndex: Int,
        nodeList: TrieNodeList,
        phrase: Boolean = false,
     ) : List<Correction> {
         val word = words[firstIndex]
         val maxEdits = if (word.length <= 3) 0 else 1
         val string = if (phrase) " $word" else word

         return Automaton(string = string, maxEdits = maxEdits).correct(nodeList).flatMap { correction ->
            if (firstIndex == lastIndex) return@flatMap listOf(correction)

            val res = arrayListOf(correction)

            res.addAll(correctAll(words, firstIndex + 1, lastIndex, correction.nodeList!!, true).map { cur ->
                Correction(
                    cur.value,
                    "${correction.original.string}${cur.original.string}".toTransliterableString(),
                    cur.distance + correction.distance,
                    cur.score,
                    cur.nodeList,
                )
            })

            res
        }
    }

    /**
     * Optimization
     * 1. Migrate correctAll to iterative instead of recursive
     * 2. Keep the currency best correction continously check if a candidate can keep up with the current maximum
     */
}