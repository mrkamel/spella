// Implements a levenshtein automaton as described at
// https://julesjacobs.com/2015/06/17/disqus-levenshtein-simple-and-fast.html

package spelling2g

data class State(val indices: List<Int>, val values: List<Double>)

object Transliteration {
    private var mapping = hashMapOf<Char, String>(
        'ä' to "ae",
        'ö' to "oe",
        'ü' to "ue",
        'ß' to "ss"
    )

    operator fun get(char: Char) : String? {
        return mapping[char]
    }
}

class Automaton(string: String, maxEdits: Int) {
    var string = string
    var maxEdits = maxEdits

    fun correct(node: TrieNode): Correction? {
        node.lookup(string)?.let {
            return Correction(string = string, distance = 0.0, score = it.score)
        }

        return correctRecursive(node, start())
    }

    public fun correctRecursive(node: TrieNode, state: State): Correction? {
        var res: Correction? = null

        if (node.isTerminal && isMatch(state)) {
            res = Correction(string = node.getPhrase(), distance = state.values.last(), score = node.score)
        }

        for ((char, newNode) in node.children) {
            var newState = step(state, char, node.char)

            if (canMatch(newState)) {
                var candidate = correctRecursive(newNode, newState) ?: continue

                if (res == null || candidate < res) {
                    res = candidate
                }
            }
        }

        return res
    }

    private fun start(): State {
        return State(
            indices = ArrayList<Int>(maxEdits).apply { addAll(0..maxEdits) },
            values = ArrayList<Double>(maxEdits).apply { addAll((0..maxEdits).map { it.toDouble() }) }
        )
    }

    private fun step(curState: State, curChar: Char, prevChar: Char?): State {
        var indices = curState.indices
        var values = curState.values

        var newIndices = ArrayList<Int>(maxEdits)
        var newValues = ArrayList<Double>(maxEdits)

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

    private fun calculateCost(i: Int, curChar: Char, prevChar: Char?): Double {
        if (string[i] == curChar) return 0.0

        // transposition
        if (i > 0 && prevChar != null && string[i - 1] == curChar && string[i] == prevChar) return 0.0

        // transliteration
        var ascii: String? = Transliteration[curChar]
        if (ascii != null && i > 0 && string[i - 1] == ascii[0] && string[i] == ascii[1]) return -0.5

        return 1.0
    }

    private fun isMatch(state: State): Boolean {
        return state.indices.size > 0 && state.indices.last() == string.length
    }

    private fun canMatch(state: State): Boolean {
        return state.indices.size > 0
    }
}