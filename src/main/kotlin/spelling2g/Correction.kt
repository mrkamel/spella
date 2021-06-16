package spelling2g

/**
 * The Correction class represents a correction and implements Comparable to
 * be able to find the best correction. It takes TransliterableStrings for
 * performance reasons, as those can memoize the transliterations.
 */

class Correction(
    value: TransliterableString,
    original: TransliterableString,
    distance: Int,
    score: Double,
    nodeList: TrieNodeList? = null,
) : Comparable<Correction> {
    val value = value
    val original = original
    val distance = distance
    val score = score
    val nodeList = nodeList
    val matchesTransliterated: Boolean by lazy { value.transliteratedString == original.transliteratedString }

    /**
     * A correction is better/smaller when
     * 1. the distance is less
     * 2. it matches the original when transliterated
     * 3. the score is higher
     */

	override operator fun compareTo(other: Correction): Int {
		if (distance < other.distance) return -1
		if (distance > other.distance) return 1

        if (matchesTransliterated && !other.matchesTransliterated) return -1
        if (!matchesTransliterated && other.matchesTransliterated) return 1

		if (score > other.score) return -1
		if (score < other.score) return 1
		
		return 0
	}

    /**
     * A correction is equal to another correction if the values, distances
     * and scores are equal.
     */

    override fun equals(other: Any?): Boolean {
        return other is Correction && value == other.value && distance == other.distance && score == other.score
    }
}