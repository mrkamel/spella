package spelling2g

class QueryMapper(string: String, language: String, tries: Tries) {
    var string = string
    var language = language
    var tries = tries
    var trie = tries[language]

    fun map(maxLookahead: Int = 5) : Correction? {
        if (trie == null) return null

        var words = string.split(" ").filter { it.length > 0 }
        var sumDistance = 0.0
        var sumScore = 0.0
        var res = ArrayList<String>()
        var i = 0

        while (i < words.size) {
            var longest = words[i]
            var distance = 0.0
            var score = 0.0
            var len = 1
            var max = Math.min(maxLookahead, words.size)

            for (u in 1..max) {
                var maxEdits = if (string.length > 4) 2 else 1
                var cur = Automaton(string = words.slice(i..(u - 1)).joinToString(" "), maxEdits = maxEdits).correct(trie!!)

                if (cur == null) break

                longest = cur.string
                distance = cur.distance
                score = cur.score
                len = u
            }

            res.add(longest)
            sumDistance += distance
            sumScore += score
            i += len
        }

        return Correction(res.joinToString(" "), distance = sumDistance, score = sumScore)
    }
}