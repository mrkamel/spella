package spelling2g

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AutomatonTest : DescribeSpec({
    describe("correct") {
        it("returns all applicable corrections with correct distance, score and original") {
            val trieNode = TrieNode().also {
                it.insert("some phrase", 1.0)
                it.insert("some phrases", 2.0)
                it.insert("same phrases", 3.0)
                it.insert("other", 4.0)
            }

            val corrections = Automaton("some phrase", maxEdits = 2).correct(TrieNodeList(trieNode)).sorted()

            corrections.filter { it.isTerminal }.associate { it.value.string to listOf(it.distance, it.score) }.shouldBe(
                mapOf(
                    "some phrase" to listOf(0, 1.0),
                    "some phrases" to listOf(1, 2.0),
                    "same phrases" to listOf(2, 3.0),
                )
            )

            corrections.filter { !it.isTerminal }.associate { it.value.string to listOf(it.distance, it.score) }.shouldBe(
                mapOf(
                    "some phrase " to listOf(1, 1.0),
                    "same phrase" to listOf(1, 0.0),
                    "some phras" to listOf(1, 0.0),
                    "some phrases " to listOf(2, 2.0),
                    "some phrase s" to listOf(2, 1.0),
                    "some phrase o" to listOf(2, 1.0),
                    "same phras" to listOf(2, 0.0),
                    "some phra" to listOf(2, 0.0),
                )
            )

            corrections.map { it.original.string }.distinct().shouldBe(listOf("some phrase"))
        }

        it("includes partial corrections") {
            val trieNode = TrieNode().also {
                it.insert("processing", 1.0)
                it.insert("processor", 2.0)
                it.insert("procedure", 3.0)
                it.insert("proceed", 4.0)
                it.insert("other", 5.0)
            }

            val corrections = Automaton("process", maxEdits = 1).correct(TrieNodeList(trieNode)).sorted()

            corrections.associate { it.value.string to listOf(it.distance, it.score) }.shouldBe(
                mapOf(
                    "process" to listOf(0, 0.0),
                    "proces" to listOf(1, 0.0),
                    "processi" to listOf(1, 0.0),
                    "processo" to listOf(1, 0.0),
                )
            )
        }

        it("starts from the head of the given node list") {
            val trieNode = TrieNode().also {
                it.insert("preprocess", 1.0)
                it.insert("postprocess", 2.0)
                it.insert("preprocessor", 3.0)
                it.insert("preprocessing", 4.0)
                it.insert("other", 5.0)
            }

            val corrections = Automaton("process", maxEdits = 2).correct(TrieNodeList(trieNode.lookup("pre")!!)).sorted()

            corrections.filter { it.isTerminal }.associate { it.value.string to listOf(it.distance, it.score) }.shouldBe(
                mapOf(
                    "preprocess" to listOf(0, 1.0),
                    "preprocessor" to listOf(2, 3.0),
                )
            )
        }

        it("restarts from the root when neccessary and adds the previous node the the tail") {
        }

        it("adds distance one for deletions") {
        }

        it("adds distance one for insertions") {
        }

        it("adds distance one for transpositions") {
        }

        it("adds distance one for transliterations") {
        }
    }
})
