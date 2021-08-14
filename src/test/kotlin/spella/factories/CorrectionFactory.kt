package spella

object CorrectionFactory {
    fun build(
        value: TransliterableString = "value".toTransliterableString(),
        original: TransliterableString = "original".toTransliterableString(),
        distance: Int = 1,
        score: Double = 1.0,
        isTerminal: Boolean = true,
        trieNode: TrieNode? = null,
    ): Correction {
        return Correction(value, original, distance, score, isTerminal, trieNode?.let { TrieNodeList(trieNode) })
    }
}
