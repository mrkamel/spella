package spella

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TransliterableStringTest : DescribeSpec({
    describe("transliteratedString") {
        it("returns the transliterated string") {
            TransliterableString("AbcÄüÖßDef").transliteratedString.shouldBe("AbcAeueOessDef")
        }
    }
})
