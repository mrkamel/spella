package spelling2g

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class CorrectionTest : DescribeSpec({
    describe("compareTo") {
        it("returns -1 or 1 when the distance is smaller or higher") {
            var correction1 = CorrectionFactory.build(distance = 1)
            var correction2 = CorrectionFactory.build(distance = 2)

            correction1.compareTo(correction2).shouldBe(-1)
            correction2.compareTo(correction1).shouldBe(1)
        }

        it("returns -1 or 1 when the score is higher or smaller") {
            var correction1 = CorrectionFactory.build(score = 2.0)
            var correction2 = CorrectionFactory.build(score = 1.0)

            correction1.compareTo(correction2).shouldBe(-1)
            correction2.compareTo(correction1).shouldBe(1)
        }

        it("returns -1 or 1 when the transliteration matches or not matches") {
            var correction1 = CorrectionFactory.build(
                value = "süden".toTransliterableString(),
                original = "sueden".toTransliterableString()
            )

            var correction2 = CorrectionFactory.build(
                value = "value".toTransliterableString(),
                original = "original".toTransliterableString()
            )

            correction1.compareTo(correction2).shouldBe(-1)
            correction2.compareTo(correction1).shouldBe(1)
        }

        it("returns 0 when distance, transliteration check and score") {
            var correction1 = CorrectionFactory.build(
                value = "value1".toTransliterableString(),
                original = "original1".toTransliterableString(),
                distance = 1,
                score = 1.0,
                node = null
            )

            var correction2 = CorrectionFactory.build(
                value = "value2".toTransliterableString(),
                original = "original2".toTransliterableString(),
                distance = 1,
                score = 1.0,
                node = null
            )

            correction1.compareTo(correction2).shouldBe(0)
        }
    }

    describe("equals") {
        it("returns true when value, distance and score is equal") {
            var correction1 = CorrectionFactory.build(
                value = "value".toTransliterableString(),
                original = "original1".toTransliterableString(),
                distance = 1,
                score = 1.0,
                node = null
            )

            var correction2 = CorrectionFactory.build(
                value = "value".toTransliterableString(),
                original = "original2".toTransliterableString(),
                distance = 1,
                score = 1.0,
                node = null
            )

            correction1.equals(correction2).shouldBe(true)
        }

        it("returns false when value is not equal") {
            var correction1 = CorrectionFactory.build(value = "value1".toTransliterableString())
            var correction2 = CorrectionFactory.build(value = "value2".toTransliterableString())

            correction1.equals(correction2).shouldBe(false)
        }

        it("return false when distance is not equal") {
            CorrectionFactory.build(distance = 1).equals(CorrectionFactory.build(distance = 2)).shouldBe(false)
        }

        it("return false when score is not equal") {
            CorrectionFactory.build(score = 1.0).equals(CorrectionFactory.build(score = 2.0)).shouldBe(false)
        }
    }
})