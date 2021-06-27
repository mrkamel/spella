package spelling2g

import io.kotest.assertions.asClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.spyk
import io.mockk.unmockkAll

class TriesTest : DescribeSpec({
    describe("addFile") {
        val tries = spyk(Tries())

        beforeEach {
            every {
                tries.invoke("readFile").withArguments(listOf("test.dic", any<(Sequence<String>) -> Unit>()))
            } answers {
                secondArg<(Sequence<String>) -> Unit>().invoke(
                    sequenceOf(
                        "en\tsome phrase\t1.0",
                        "de\tandere phrase\t2.0"
                    )
                )
            }
        }

        afterEach { unmockkAll() }

        it("reads and inserts the data from the specified file") {
            tries.addFile("test.dic")

            tries["en"]!!.lookup("some phrase")!!.asClue {
                it.score.shouldBe(1.0)
                it.getPhrase().shouldBe("some phrase")
            }

            tries["de"]!!.lookup("andere phrase")!!.asClue {
                it.score.shouldBe(2.0)
                it.getPhrase().shouldBe("andere phrase")
            }
        }
    }

    describe("insert") {
        it("inserts the phrase into the correct trie") {
            val tries = Tries()
            tries.insert("en", "some phrase", 1.0)
            tries.insert("de", "andere phrase", 2.0)

            tries["en"]!!.lookup("some phrase")!!.asClue {
                it.score.shouldBe(1.0)
                it.getPhrase().shouldBe("some phrase")
            }

            tries["de"]!!.lookup("andere phrase")!!.asClue {
                it.score.shouldBe(2.0)
                it.getPhrase().shouldBe("andere phrase")
            }
        }
    }

    describe("get") {
        it("returns the trie for the specified language") {
            val tries = Tries()
            tries.insert("en", "some phrase", 1.0)
            tries.insert("de", "andere phrase", 2.0)

            tries.get("en").shouldNotBe(null)
            tries.get("de").shouldNotBe(null)
            tries.get("fr").shouldBe(null)
        }
    }
})
