package spelling2g

import java.io.File

/**
 * The Tries class is used to build one trie per language. It reads the
 * input files and inserts it to the trie for the respective language.
 */

class Tries {
    var tries = HashMap<String, TrieNode>()

    fun addFile(path: String) {
        readFile(path) { lines ->
            lines.map { line -> line.split("\t") }
                 .forEach { (language, phrase, score) ->
                    tries.getOrPut(language) { TrieNode() }.insert(phrase.lowercase(), score.toDouble())
                }
        }
    }

    operator fun get(language: String): TrieNode? {
        return tries[language]
    }

    private fun readFile(path: String, block: (Sequence<String>) -> Unit) {
        File(path).useLines(block = block)
    }
}
