package spelling2g

import java.util.PriorityQueue
import kotlin.comparisons.reverseOrder

class TrieNode(parent: TrieNode? = null, char: Char? = null) {
    var parent = parent
    var char = char
    var isTerminal = false
    var score = 0.0
    var children = HashMap<Char, TrieNode>()

    fun insert(phrase: String, score: Double) {
        var node = this

        for (char in phrase) {
            node = node.children.getOrPut(char) { TrieNode(parent = node, char = char) }
        }

        node.isTerminal = true
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
}