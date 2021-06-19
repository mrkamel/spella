package spelling2g

/**
 * A TrieNodeList basically holds two references. One reference to the last
 * TrieNode used for correction, i.e. the head, and one to the previously used
 * ones, the tail. This helps when calculating corrections where words need
 * to be splitted, and where correction is restarting from the root.
 */

class TrieNodeList(head: TrieNode, tail: List<TrieNode> = listOf<TrieNode>()) {
    val head = head
    val tail = tail

    fun getPhrase(): String {
        val res = ArrayList<String>(tail.size + 1)

        tail.forEach { res.add(it.getPhrase()) }
        res.add(head.getPhrase())

        return res.joinToString(" ")
    }
}
