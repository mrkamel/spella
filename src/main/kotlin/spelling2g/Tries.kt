package spelling2g

import java.io.File

class Tries {
    var tries = HashMap<String, TrieNode>()

    fun addFile(path: String) {
        File(path).useLines { lines ->
            lines.map { line -> line.split("\t") }
                 .forEach { (language, phrase, score) ->
                    tries.getOrPut(language) { TrieNode() }.insert(phrase.toLowerCase(), score.toDouble())
                }
        }
    }

    operator fun get(language: String): TrieNode? {
        return tries[language]
    }
}
