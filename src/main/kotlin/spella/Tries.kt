package spella

import java.io.File

/**
 * The Tries class is used to build one trie per language. It reads the
 * input files and inserts it to the trie for the respective language.
 */

class Tries {
    val tries = HashMap<String, TrieNode>()

    fun addFile(path: String, split: Boolean) {
        readFile(path) { line ->
            var (language, phrase, score) = line.split("\t")
            phrase = phrase.lowercase()

            insert(language, phrase, score.toDouble())

            val words = phrase.split(" ")

            if (!split) return@readFile
            if (words.size == 1) return@readFile

            for (word in words) {
                insert(language, word.lowercase(), 0.0)
            }
        }
    }

    fun insert(language: String, phrase: String, score: Double) {
        tries.getOrPut(language) { TrieNode() }.insert(phrase.lowercase(), score.toDouble())
    }

    operator fun get(language: String): TrieNode? {
        return tries[language]
    }

    private fun readFile(path: String, fn: (String) -> Unit) {
        File(path).forEachLine { line -> fn(line) }
    }
}
