package spella

data class TransliterableString(val string: String) {
    val transliteratedString: String by lazy { Transliterator.map(string) }
    val wordCount: Int by lazy { 1 + string.count { it == ' ' } }
}

fun String.toTransliterableString(): TransliterableString {
    return TransliterableString(this)
}
