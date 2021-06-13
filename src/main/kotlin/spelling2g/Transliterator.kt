package spelling2g

/*
 * Transliterates string to the respective ascii version.
 */

object Transliterator {
    private var mapping = hashMapOf<Char, String>(
        'Ä' to "Ae",
        'ä' to "ae",
        'Ö' to "Oe",
        'ö' to "oe",
        'Ü' to "Ue",
        'ü' to "ue",
        'ß' to "ss"
    )

    fun map(string: String): String {
        var res = StringBuilder()

        for(char in string) {
            var mapped = mapping[char]
            res.append(mapped ?: char)
        }

        return res.toString()
    }

    fun map(char: Char): String? {
        return mapping[char]
    }
}