package spelling2g

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing

data class CorrectionResponse(
    val text: String,
    val distance: Int,
    val score: Double,
    val took: Long,
)

data class ErrorResponse(
    val message: String,
)

fun Application.registerCorrectionsController(tries: Tries) {
    routing {
        get("/corrections") {
            val startTime = System.currentTimeMillis()

            val text = call.parameters["text"]
                ?: return@get call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse("Missing parameter: text"))

            val language = call.parameters["language"]
                ?: return@get call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse("Missing parameter: language"))

            val correction = QueryMapper(text, language, tries = tries).map(maxLookahead = 5)
            val took = System.currentTimeMillis() - startTime

            call.response.headers.append("X-Timing", "${took}ms")

            call.respond(
                CorrectionResponse(
                    text = correction.value.string,
                    distance = correction.distance,
                    score = correction.score,
                    took = took,
                )
            )
        }
    }
}
