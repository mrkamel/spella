package spella

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TrieNodeListTest : DescribeSpec({
    describe("getPhrase") {
        it("returns the compound head and tail phrase") {
            val trieNode1 = TrieNode().also { it.insert("phrase1", 1.0) }.lookup("phrase1")!!
            val trieNode2 = TrieNode().also { it.insert("phrase2", 2.0) }.lookup("phrase2")!!

            TrieNodeList(trieNode2, TrieNodeList(trieNode1)).getPhrase().shouldBe("phrase1 phrase2")
        }

        it("returns the head phrase when no tail phrase is present") {
            val trieNode = TrieNode().also { it.insert("some phrase", 1.0) }.lookup("some phrase")!!

            TrieNodeList(trieNode).getPhrase().shouldBe("some phrase")
        }
    }
})
