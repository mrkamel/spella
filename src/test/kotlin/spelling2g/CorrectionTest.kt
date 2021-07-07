package spelling2g

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CorrectionTest : DescribeSpec({
    describe("compareTo") {
        it("returns -1 or 1 when the distance is smaller or higher") {
            val correction1 = CorrectionFactory.build(distance = 1)
            val correction2 = CorrectionFactory.build(distance = 2)

            correction1.compareTo(correction2).shouldBe(-1)
            correction2.compareTo(correction1).shouldBe(1)
        }

        it("returns -1 or 1 when the score is higher or smaller") {
            val correction1 = CorrectionFactory.build(score = 2.0)
            val correction2 = CorrectionFactory.build(score = 1.0)

            correction1.compareTo(correction2).shouldBe(-1)
            correction2.compareTo(correction1).shouldBe(1)
        }

        it("returns -1 or 1 when the transliteration matches or not matches") {
            val correction1 = CorrectionFactory.build(
                value = "süden".toTransliterableString(),
                original = "sueden".toTransliterableString()
            )

            val correction2 = CorrectionFactory.build(
                value = "value".toTransliterableString(),
                original = "original".toTransliterableString()
            )

            correction1.compareTo(correction2).shouldBe(-1)
            correction2.compareTo(correction1).shouldBe(1)
        }

        it("returns 0 when distance, transliteration check and score") {
            val correction1 = CorrectionFactory.build(
                value = "value1".toTransliterableString(),
                original = "original1".toTransliterableString(),
                distance = 1,
                score = 1.0,
            )

            val correction2 = CorrectionFactory.build(
                value = "value2".toTransliterableString(),
                original = "original2".toTransliterableString(),
                distance = 1,
                score = 1.0,
            )

            correction1.compareTo(correction2).shouldBe(0)
        }
    }

    describe("equals") {
        it("returns true when value, distance and score is equal") {
            val correction1 = CorrectionFactory.build(
                value = "value".toTransliterableString(),
                original = "original1".toTransliterableString(),
                distance = 1,
                score = 1.0,
            )

            val correction2 = CorrectionFactory.build(
                value = "value".toTransliterableString(),
                original = "original2".toTransliterableString(),
                distance = 1,
                score = 1.0,
            )

            correction1.equals(correction2).shouldBe(true)
        }

        it("returns false when value is not equal") {
            val correction1 = CorrectionFactory.build(value = "value1".toTransliterableString())
            val correction2 = CorrectionFactory.build(value = "value2".toTransliterableString())

            correction1.equals(correction2).shouldBe(false)
        }

        it("return false when distance is not equal") {
            CorrectionFactory.build(distance = 1).equals(CorrectionFactory.build(distance = 2)).shouldBe(false)
        }

        it("return false when score is not equal") {
            CorrectionFactory.build(score = 1.0).equals(CorrectionFactory.build(score = 2.0)).shouldBe(false)
        }
    }

    describe("matchesTransliterated") {
        it("returns true when the transliterated value matches the transliterated original") {
            val correction = CorrectionFactory.build(
                value = "äöüß".toTransliterableString(),
                original = "aeöüss".toTransliterableString(),
            )

            correction.matchesTransliterated.shouldBe(true)
        }

        it("returns false when the transliterated value does not match the transliterated original") {
            val correction = CorrectionFactory.build(
                value = "äöüß".toTransliterableString(),
                original = "aus".toTransliterableString(),
            )

            correction.matchesTransliterated.shouldBe(false)
        }
    }
})
