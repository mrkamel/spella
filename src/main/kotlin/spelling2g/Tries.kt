package spelling2g

import java.io.File

/**
 * The Tries class is used to build one trie per language. It reads the
 * input files and inserts it to the trie for the respective language.
 */

class Tries {
    val tries = HashMap<String, TrieNode>()

    fun addFile(path: String) {
        readFile(path) { line ->
            var row = line.split("\t")

            insert(row[0], row[1].lowercase(), row[2].toDouble())
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
