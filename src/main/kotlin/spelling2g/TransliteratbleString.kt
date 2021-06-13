package spelling2g

data class TransliterableString(val string: String) {
    val transliteratedString: String by lazy {
        Transliterator.map(string)
    }
}

fun String.toTransliterableString() : TransliterableString {
    return TransliterableString(this)
}