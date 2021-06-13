package spelling2g

/**
 * The QueryMapper takes an input string, splits it by whitespace and greedily
 * searches for the longest correction. Uses intermediary nodes as an
 * optimization to avoid correcting the prefix words again when a longer one
 * gets corrected.
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
            var longestCorrection : Correction? = null
            var len = 1
            var max = Math.min(maxLookahead, words.size - i)

            for (u in 1..max) {
                println(">> ${words.slice(i..(i+u-1)).joinToString(" ")}")

                val correction = correct(words, i, i + u -1, trie).minOrNull() ?: break

                // If the current correction has zero distance and the longer one distance > 0,
                // stick with the shorter one unless the shorter one is prefix of the longer one.

                if (longestCorrection == null || correction.distance == 0) {
                    longestCorrection = correction
                } else if (correction.value.string.startsWith(longestCorrection.value.string)) {
                    longestCorrection = correction
                }

                len = u
            }

            corrections.add(
                longestCorrection ?: Correction(
                    value    = words[i].toTransliterableString(),
                    original = words[i].toTransliterableString(),
                    distance = 0,
                    score    = 0.0
                )
            )

            i += len
        }

        return Correction(
            value    = corrections.map { it.value.string }.joinToString(", ").toTransliterableString(),
            original = string.toTransliterableString(),
            distance = corrections.sumOf { it.distance },
            score    = corrections.sumOf { it.score },
        )
    }

    // Recursively correct the list of words to guarantee, that every single word
    // has the specified max edit distance. Otherwise we'd need to increase the
    // max edit distance the longer the string we try to correct.

    private fun correct(words: List<String>, firstIndex: Int, lastIndex: Int, curNode: TrieNode) : List<Correction> {
        var word = words[firstIndex]
        var maxEdits = if (word.length <= 3) 0 else 1

        return Automaton(string = word, maxEdits = maxEdits).correct(curNode).flatMap { correction ->
            if (firstIndex == lastIndex) return@flatMap listOf(correction)

            var res = ArrayList<Correction>()

            correction.node?.children?.get(' ')?.let {
                res.addAll(correct(words, firstIndex + 1, lastIndex, it).map { cur ->
                    Correction(cur.value, cur.original, cur.distance + correction.distance, cur.score, cur.node)
                })
            }

            correction.node?.let {
                res.addAll(correct(words, firstIndex + 1, lastIndex, it).map { cur ->
                    Correction(cur.value, cur.original, cur.distance + correction.distance + 1, cur.score, cur.node)
                })
            }

            res
        }
    }
}