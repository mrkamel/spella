package spelling2g

class Correction(string: String, distance: Double, score: Double) : Comparable<Correction> {
    var string = string
    var distance = distance
    var score = score

	override operator fun compareTo(other: Correction): Int {
		if (distance < other.distance) return -1
		if (distance > other.distance) return 1

		if (score > other.score) return -1
		if (score < other.score) return 1
		
		return 0
	}

    override fun equals(other: Any?): Boolean {
        return other is Correction && string == other.string && distance == other.distance && score == other.score
    }
}