// Implements a levenshtein automaton as described in
// https://julesjacobs.com/2015/06/17/disqus-levenshtein-simple-and-fast.html

package spelling2g

data class State(val indices: List<Int>, val values: List<Int>)

class Automaton(string: String, maxEdits: Int) {
    val string = string
    var maxEdits = maxEdits
    val transliterableString: TransliterableString by lazy { string.toTransliterableString() }

    /*
     * Returns all available corrections which have distance <= maxEdits.
     * Matches are determined by whether or not the respective trie node is
     * a word end. That means partial matches delimited by whitespace are
     * also considered as matches.
     */

    fun correct(node: TrieNode): List<Correction> {
        return correctRecursive(node, start())
    }

    public fun correctRecursive(node: TrieNode, state: State): List<Correction> {
        var res = ArrayList<Correction>()
        var distance = state.values.last()

        if (node.isWordEnd && isMatch(state)) {
            res.add(
                Correction(
                    value    = node.getPhrase().toTransliterableString(),
                    original = transliterableString,
                    distance = distance,
                    score    = node.score,
                    node     = node
                )
            )
        }

        for ((char, newNode) in node.children) {
            var newState = step(state, char, node.char)

            if (canMatch(newState)) {
                res.addAll(correctRecursive(newNode, newState))
            }
        }

        // Additionally try to split the current word and continue correcting
        // from root node.

        if (node.isWordEnd) {
            var newState = step(state, ' ', node.char)

            if (canMatch(newState)) {
                val corrections = correctRecursive(node.root(), newState).map { correction ->
                    // When splitting, the already corrected prefix needs to
                    // be prepended to the corrections and the individual scores
                    // may not be respected.

                    Correction(
                        "${node.getPhrase()} ${correction.value.string}".toTransliterableString(),
                        correction.original,
                        distance = correction.distance,
                        score = 0.0,
                        node = correction.node
                    )
                }

                res.addAll(corrections)
            }
        }

        return res
    }

    private fun start(): State {
        return State(
            indices = ArrayList<Int>(maxEdits).apply { addAll(0..maxEdits) },
            values = ArrayList<Int>(maxEdits).apply { addAll(0..maxEdits) }
        )
    }

    private fun step(curState: State, curChar: Char, prevChar: Char?): State {
        var indices = curState.indices
        var values = curState.values

        var newIndices = ArrayList<Int>(maxEdits)
        var newValues = ArrayList<Int>(maxEdits)

        if (indices.size > 0 && indices[0] == 0 && values[0] < maxEdits) {
            newIndices.add(0)
            newValues.add(values[0] + 1)
        }

        indices.forEachIndexed { j, i ->
            if (i == string.length) return@forEachIndexed

            var cost = calculateCost(i, curChar, prevChar)
            var value = values[j] + cost

            if (newIndices.size > 0 && newIndices.last() == i) {
                value = Math.min(value, newValues.last() + 1)
            }

            if (j + 1 < indices.size && indices[j + 1] == i + 1) {
                value = Math.min(value, values[j + 1] + 1)
            }

            if (value <= maxEdits) {
                newIndices.add(i + 1)
                newValues.add(value)
            }
        }

        return State(indices = newIndices, values = newValues)
    }

    private fun isMatch(state: State): Boolean {
        return state.indices.size > 0 && state.indices.last() == string.length
    }

    private fun canMatch(state: State): Boolean {
        return state.indices.size > 0
    }

    private fun calculateCost(i: Int, curChar: Char, prevChar: Char?): Int {
        if (string[i] == curChar) return 0

        // transposition
        if (i > 0 && prevChar != null && string[i - 1] == curChar && string[i] == prevChar) return 0

        // transliteration
        var ascii: String? = Transliterator.map(curChar)
        if (ascii != null && i > 0 && string[i - 1] == ascii[0] && string[i] == ascii[1]) return 0

        return 1
    }
}