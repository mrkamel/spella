// Implements a levenshtein automaton as described in
// https://julesjacobs.com/2015/06/17/disqus-levenshtein-simple-and-fast.html

package spelling2g

data class State(val indices: List<Int>, val values: List<Int>)

class Automaton(string: String, maxEdits: Int) {
    val string = string
    var maxEdits = maxEdits
    val transliterableString: TransliterableString by lazy { string.toTransliterableString() }

    /**
     * Returns all available corrections which have distance <= maxEdits.
     * Matches are determined by whether or not the respective trie node is
     * a word end. That means partial matches delimited by whitespace are
     * also considered as matches.
     */

    fun correct(nodeList: TrieNodeList): List<Correction> {
        return correctRecursive(nodeList, start())
    }

    public fun correctRecursive(nodeList: TrieNodeList, state: State): List<Correction> {
        val node = nodeList.head
        val nodes = nodeList.tail
        var res = ArrayList<Correction>()
        val distance = state.values.last()

        if (node.isWordEnd && isMatch(state)) {
            res.add(
                Correction(
                    value = nodeList.getPhrase().toTransliterableString(),
                    original = transliterableString,
                    distance = distance,
                    score = node.score + nodes.sumOf { it.score },
                    nodeList = nodeList,
                )
            )
        }

        for ((char, newNode) in node.children) {
            var newState = step(state, char, node.char)

            if (canMatch(newState)) {
                res.addAll(correctRecursive(TrieNodeList(newNode, nodes), newState))
            }
        }

        // Additionally try to split the current word and continue correcting
        // from the root node.

        if (node.isWordEnd) {
            var newState = step(state, ' ', node.char)

            if (canMatch(newState)) {
                res.addAll(correctRecursive(TrieNodeList(node.root(), nodes + listOf(node)), newState))
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

        // Handle transposition

        if (i > 0 && prevChar != null && string[i - 1] == curChar && string[i] == prevChar) return 0

        // Handle transliteration

        var ascii: String? = Transliterator.map(curChar)
        if (ascii != null && i > 0 && string[i - 1] == ascii[0] && string[i] == ascii[1]) return 0

        return 1
    }
}
