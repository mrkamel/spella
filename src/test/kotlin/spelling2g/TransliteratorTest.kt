package spelling2g

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class TransliteratorTest : DescribeSpec({
    describe("map with string") {
        it("returns the transliterated string") {
            Transliterator.map("AbcÄüÖßDef").shouldBe("AbcAeueOessDef")
        }
    }

    describe("map with char") {
        it("returns the tranliterated string for it") {
            Transliterator.map('Ä').shouldBe("Ae")
            Transliterator.map('ö').shouldBe("oe")
        }
    }
})
