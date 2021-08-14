package spella

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TrieNodeTest : DescribeSpec({
    describe("insert") {
        it("inserts the specified string") {
            val trieNode = TrieNode()
            trieNode.insert("some phrase", 1.0)

            trieNode.lookup("some phrase")!!.run { getPhrase().shouldBe("some phrase") }
        }

        it("does not do anything if the specified string is empty") {
            val trieNode = TrieNode()
            trieNode.insert("", 1.0)

            trieNode.isTerminal.shouldBe(false)
            trieNode.parent.shouldBeNull()
        }

        it("sets isTerminal to true for the last node") {
            val trieNode = TrieNode()
            trieNode.insert("some phrase", 1.0)

            trieNode.lookup("some")!!.isTerminal.shouldBe(false)
            trieNode.lookup("some phrase")!!.isTerminal.shouldBe(true)
        }

        it("sets the score for the last node") {
            val trieNode = TrieNode()
            trieNode.insert("some phrase", 1.0)

            trieNode.lookup("some")!!.score.shouldBe(0.0)
            trieNode.lookup("some phrase")!!.score.shouldBe(1.0)
        }
    }

    describe("lookup") {
        it("returns the node of the last character when the full phrase can be found") {
            val trieNode = TrieNode()
            trieNode.insert("some phrase", 1.0)
            trieNode.insert("another phrase", 2.0)

            trieNode.lookup("some phrase").shouldNotBeNull().score.shouldBe(1.0)
            trieNode.lookup("another phrase").shouldNotBeNull().score.shouldBe(2.0)
        }

        it("returns null when the phrase is not fully present") {
            val trieNode = TrieNode()
            trieNode.insert("some phrase", 1.0)

            trieNode.lookup("some phrases").shouldBe(null)
            trieNode.lookup("unknown").shouldBe(null)
        }
    }

    describe("getPhrase") {
        it("returns the phrase represented by the node") {
            val trieNode = TrieNode()
            trieNode.insert("some phrase", 1.0)

            trieNode.lookup("some phrase")!!.getPhrase().shouldBe("some phrase")
        }
    }
})
