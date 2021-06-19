package spelling2g

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

    fun map(maxLookahead: Int = 5): Correction? {
        if (trie == null) return null

        val words = string.split(" ").filter { it.length > 0 }
        val corrections = ArrayList<Correction>()
        var i = 0

        while (i < words.size) {
            val max = Math.min(maxLookahead, words.size - i)
            val correction = correct(words, i, i + max - 1, TrieNodeList(trie))
                ?: Correction(words[i].toTransliterableString(), words[i].toTransliterableString(), distance = 0, score = 0.0)

            corrections.add(correction)

            i += correction.original.wordCount
        }

        return Correction(
            value = corrections.map { it.value.string }.joinToString(", ").toTransliterableString(),
            original = string.toTransliterableString(),
            distance = corrections.sumOf { it.distance },
            score = corrections.sumOf { it.score },
        )
    }

    /**
     * Returns the best correction that match the edit distance criteria by recursively
     * and greedily correcting the list of words up until a word can not be corrected
     * anymore. This guarantees that every single word has the specified max edit
     * distance at most. Otherwise we'd need to increase the max edit distance the
     * longer the string we try to correct, which is not optimal performance wise and we
     * wouldn't be able to guarantee a max edit distance per word. As we prefer
     * corrections with less trie restarts, we keep track of the current mininum number
     * of restarts to prune the search space when a path already is above that minimum.
     */

    private fun correct(
        words: List<String>,
        firstIndex: Int,
        lastIndex: Int,
        nodeList: TrieNodeList,
        phrase: Boolean = false,
        maxRestarts: Int = Int.MAX_VALUE,
    ): Correction? {
        val word = words[firstIndex]
        val maxEdits = if (word.length <= 3) 0 else 1
        val string = if (phrase) " $word" else word
        var bestCorrection: Correction? = null
        var curMaxRestarts = maxRestarts

        Automaton(string = string, maxEdits = maxEdits).correct(nodeList).forEach { correction ->
            var currentCorrection = correction

            if (firstIndex < lastIndex && correction.nodeList!!.tail.size <= curMaxRestarts) {
                correct(words, firstIndex + 1, lastIndex, correction.nodeList, true, curMaxRestarts)?.let { cur ->
                    val longerCorrection = Correction(
                        value = cur.value,
                        original = "${correction.original.string}${cur.original.string}".toTransliterableString(),
                        distance = cur.distance + correction.distance,
                        score = cur.score,
                        nodeList = cur.nodeList,
                    )

                    currentCorrection = bestCorrectionOf(currentCorrection, longerCorrection)
                }
            }

            bestCorrectionOf(bestCorrection ?: currentCorrection, currentCorrection).let {
                bestCorrection = it
                curMaxRestarts = it.nodeList!!.tail.size
            }
        }

        return bestCorrection
    }

    /**
     * Returns the best correction out of two corrections. The criteria for choosing
     * the best correction are:
     * 1. number of restarts from the trie root (less is better)
     * 2. The number of words corrected (more is better)
     * 3. The correction itself with its sort order
     */

    private fun bestCorrectionOf(correction1: Correction, correction2: Correction): Correction {
        if (correction1.nodeList!!.tail.size < correction2.nodeList!!.tail.size) return correction1
        if (correction1.nodeList.tail.size > correction2.nodeList.tail.size) return correction2

        if (correction1.original.wordCount > correction2.original.wordCount) return correction1
        if (correction1.original.wordCount < correction2.original.wordCount) return correction2

        if (correction1 < correction2) return correction1

        return correction2
    }
}
