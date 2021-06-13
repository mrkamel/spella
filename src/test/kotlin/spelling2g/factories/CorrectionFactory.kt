package spelling2g

object CorrectionFactory {
    fun build(
        value: TransliterableString = "value".toTransliterableString(),
        original: TransliterableString = "original".toTransliterableString(),
        distance: Int = 1,
        score: Double = 1.0,
        node: TrieNode? = null
    ) : Correction {
        return Correction(value, original, distance, score, node)
    }
}