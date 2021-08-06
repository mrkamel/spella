package spella

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class QueryMapperTest : DescribeSpec({
    describe("map") {
        it("returns the correction") {
            val tries = Tries().also { it.insert("en", "some phrase", 1.0) }

            QueryMapper("some phrse", "en", tries).map().let {
                it.value.string.shouldBe("some phrase")
                it.original.string.shouldBe("some phrse")
                it.score.shouldBe(1.0)
            }
        }
        it("ignores the tries of other languages") {
            val tries = Tries().also {
                it.insert("en", "some phrase", 1.0)
                it.insert("de", "some phrse", 2.0)
            }

            QueryMapper("some phrse", "en", tries).map().let {
                it.value.string.shouldBe("some phrase")
            }
        }

        it("returns the words as is when they can not be corrected") {
            val tries = Tries().also {
                it.insert("en", "some phrase", 1.0)
            }

            QueryMapper("some phrse anoter phrase", "en", tries).map().let {
                it.value.string.shouldBe("some phrase, anoter, phrase")
                it.original.string.shouldBe("some phrse anoter phrase")
                it.distance.shouldBe(1)
                it.score.shouldBe(1.0)
            }
        }

        it("returns the input as is when the trie for the specified language is null") {
            QueryMapper("unkown phrse", "en", Tries()).map().let {
                it.value.string.shouldBe("unkown phrse")
                it.original.string.shouldBe("unkown phrse")
                it.distance.shouldBe(0)
                it.score.shouldBe(0.0)
            }
        }

        it("corrects all the words") {
            val tries = Tries().also {
                it.insert("en", "beach bar", 1.0)
                it.insert("en", "cocktail", 2.0)
                it.insert("en", "summer", 3.0)
            }

            QueryMapper("beahc bar cocktal summmer", "en", tries).map().let {
                it.value.string.shouldBe("beach bar, cocktail, summer")
                it.score.shouldBe(6.0)
                it.distance.shouldBe(3)
            }
        }

        it("supports corrections of parts when at the beginning") {
            val tries = Tries().also { it.insert("en", "another phrase", 1.0) }

            QueryMapper("antoher", "en", tries).map().value.string.shouldBe("another")
            QueryMapper("phrse", "en", tries).map().value.string.shouldBe("phrse")
        }

        it("returns the sum distance and score") {
            val tries = Tries().also {
                it.insert("en", "first phrase", 1.0)
                it.insert("en", "second phrase", 2.0)
            }

            QueryMapper("fist phrase secnd phrase", "en", tries).map().let {
                it.value.string.shouldBe("first phrase, second phrase")
                it.distance.shouldBe(2)
                it.score.shouldBe(3.0)
            }
        }

        it("splits words when neccessary") {
            val tries = Tries().also { it.insert("en", "some phrase", 1.0) }

            QueryMapper("somephrase", "en", tries).map().value.string.shouldBe("some phrase")
        }

        it("joins words when neccessary") {
            val tries = Tries().also { it.insert("en", "skyscraper", 1.0) }

            QueryMapper("skys craper", "en", tries).map().value.string.shouldBe("skyscraper")
        }

        it("uses partial matches") {
            val tries = Tries().also {
                it.insert("en", "beach bar", 1.0)
            }

            QueryMapper("bech", "en", tries).map().value.string.shouldBe("beach")
        }

        it("does not use non-terminal partial matches") {
            val tries = Tries().also {
                it.insert("en", "beachbar", 1.0)
            }

            QueryMapper("bech", "en", tries).map().value.string.shouldBe("bech")
        }

        it("applies a max distance per word") {
            val tries = Tries().also {
                it.insert("en", "skyscraper", 1.0)
                it.insert("en", "beach bar", 2.0)
                it.insert("en", "cocktail", 3.0)
            }

            QueryMapper("skscrapr beahc bar coktail", "en", tries).map().let {
                it.value.string.shouldBe("skscrapr, beach bar, cocktail")
                it.distance.shouldBe(2)
                it.score.shouldBe(5.0)
            }
        }

        it("prefers corrections with less restarts") {
            val tries = Tries().also {
                it.insert("en", "some phrase", 1.0)
                it.insert("en", "some", 2.0)
                it.insert("en", "phrase", 3.0)
            }

            QueryMapper("some phrse", "en", tries).map().let {
                it.value.string.shouldBe("some phrase")
                it.score.shouldBe(1.0)
            }

            QueryMapper("phrase some", "en", tries).map().let {
                it.value.string.shouldBe("phrase, some")
                it.score.shouldBe(5.0)
            }
        }

        it("prefers longer/greedy corrections") {
            val tries = Tries().also {
                it.insert("en", "some long phrase", 1.0)
                it.insert("en", "some long", 2.0)
                it.insert("en", "some", 3.0)
                it.insert("en", "long", 4.0)
                it.insert("en", "phrase", 5.0)
            }

            QueryMapper("somee lnog phrse", "en", tries).map().let {
                it.value.string.shouldBe("some long phrase")
                it.score.shouldBe(1.0)
            }
        }

        it("prefers corrections with smaller distance") {
            val tries = Tries().also {
                it.insert("en", "phrase1", 1.0)
                it.insert("en", "phrase2", 1.0)
            }

            QueryMapper("phrase1", "en", tries).map().value.string.shouldBe("phrase1")
        }

        it("prefers corrections match when transliterated") {
            val tries = Tries().also {
                it.insert("de", "schön", 1.0)
                it.insert("de", "schon", 2.0)
            }

            QueryMapper("schoen", "de", tries).map().value.string.shouldBe("schön")
        }
    }
})
