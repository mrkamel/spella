package spelling2g

/**
 * Implements a trie. The full string is not stored in each trie node to
 * minimize memory usage. Instead, it can be retrieved by traversing up
 * the trie and concatenating the characters of each node.
 */

class TrieNode(parent: TrieNode? = null, char: Char? = null) {
    val parent = parent
    val char = char
    var isWordEnd = false
    var score = 0.0
    val children = HashMap<Char, TrieNode>()

    fun insert(phrase: String, score: Double) {
        if (phrase.length == 0) return

        var node = this

        for ((index, char) in phrase.withIndex()) {
            node = node.children.getOrPut(char) { TrieNode(parent = node, char = char) }

            if (phrase.length <= index + 1 || phrase[index + 1] == ' ') {
                node.isWordEnd = true
            }
        }

        node.score = score
    }

    fun lookup(string: String): TrieNode? {
        var res = this

        for (char in string) {
            res = res.children[char] ?: return null
        }

        return res
    }

    fun getPhrase(): String {
        val str = StringBuilder()
        var curNode: TrieNode? = this

        while (curNode != null) {
            str.append(curNode.char ?: "")
            curNode = curNode.parent
        }

        return str.toString().reversed()
    }

    fun root(): TrieNode {
        return parent?.root() ?: this
    }
}
