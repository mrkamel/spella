package spella

/**
 * A TrieNodeList basically is a linked list holding two references. One reference
 * to the last TrieNode used for correction, i.e. the head, and one to the
 * previously used ones, the tail. This helps when calculating corrections where
 * words need to be splitted, and where correction is restarting from the root.
 */

class TrieNodeList(head: TrieNode, tail: TrieNodeList? = null) {
    val head = head
    val tail = tail
    val size: Int by lazy { 1 + if (tail == null) 0 else tail.size }
    val score: Double by lazy { head.score + if (tail == null) 0.0 else tail.score }

    fun getPhrase(): String {
        val collector = ArrayList<String>()
        var cur: TrieNodeList? = this

        while (cur != null) {
            collector.add(cur.head.getPhrase())
            cur = cur.tail
        }

        return collector.reversed().joinToString(" ")
    }
}
