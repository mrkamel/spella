package spelling2g

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TrieNodeListTest : DescribeSpec({
    describe("getPhrase") {
        it("returns the concatenated phrases of all nodes") {
            val node1 = TrieNode().apply { insert("first phrase", 0.0) }.lookup("first phrase")!!
            val node2 = TrieNode().apply { insert("second phrase", 0.0) }.lookup("second phrase")!!
            val node3 = TrieNode().apply { insert("third phrase", 0.0) }.lookup("third phrase")!!

            TrieNodeList(node2, listOf(node1, node3)).getPhrase().shouldBe("first phrase third phrase second phrase")
        }
    }
})
