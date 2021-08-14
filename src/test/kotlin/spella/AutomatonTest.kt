package spella

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainExactly
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

            corrections.filter { it.isTerminal }.map { listOf(it.value.string, it.distance, it.score) }.shouldContainExactly(
                listOf("some phrase", 0, 1.0),
                listOf("some phrases", 1, 2.0),
                listOf("same phrases", 2, 3.0),
            )

            corrections.filter { !it.isTerminal }.map { listOf(it.value.string, it.distance, it.score) }.shouldContainExactly(
                listOf("some phrase ", 1, 1.0),
                listOf("same phrase", 1, 0.0),
                listOf("some phras", 1, 0.0),
                listOf("some phrases ", 2, 2.0),
                listOf("some phrase s", 2, 1.0),
                listOf("some phrase o", 2, 1.0),
                listOf("same phras", 2, 0.0),
                listOf("some phra", 2, 0.0),
            )

            corrections.map { it.original.string }.distinct().shouldContainExactly("some phrase")
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

            corrections.map { listOf(it.value.string, it.distance, it.score) }.shouldContainExactly(
                listOf("process", 0, 0.0),
                listOf("proces", 1, 0.0),
                listOf("processi", 1, 0.0),
                listOf("processo", 1, 0.0),
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

            corrections.filter { it.isTerminal }.map { listOf(it.value.string, it.distance, it.score) }.shouldContainExactly(
                listOf("preprocess", 0, 1.0),
                listOf("preprocessor", 2, 3.0),
            )
        }

        it("adds distance one for deletions") {
            val trieNode = TrieNode().also { it.insert("keyword", 1.0) }
            val correction = Automaton("keyyword", maxEdits = 1).correct(TrieNodeList(trieNode)).map { listOf(it.value.string, it.distance) }.first()

            correction.shouldBe(listOf("keyword", 1))
        }

        it("adds distance one for insertions") {
            val trieNode = TrieNode().also { it.insert("keyword", 1.0) }
            val correction = Automaton("keword", maxEdits = 1).correct(TrieNodeList(trieNode)).map { listOf(it.value.string, it.distance) }.first()

            correction.shouldBe(listOf("keyword", 1))
        }

        it("adds distance one for transpositions") {
            val trieNode = TrieNode().also { it.insert("keyword", 1.0) }
            val correction = Automaton("kewyord", maxEdits = 1).correct(TrieNodeList(trieNode)).map { listOf(it.value.string, it.distance) }.first()

            correction.shouldBe(listOf("keyword", 1))
        }

        it("adds distance one for transliterations") {
            val trieNode = TrieNode().also { it.insert("keywörd", 1.0) }
            val correction = Automaton("keywoerd", maxEdits = 1).correct(TrieNodeList(trieNode)).map { listOf(it.value.string, it.distance) }.first()

            correction.shouldBe(listOf("keywörd", 1))
        }
    }
})
