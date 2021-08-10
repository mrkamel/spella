package spella

import io.javalin.Javalin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

data class CorrectionResponse(
    val text: String,
    val distance: Int,
    val score: Double,
    val took: Long,
)

data class ErrorResponse(
    val message: String,
)

public class Server(tries: Tries) {
    val tries: Tries = tries
    val logger: Logger
    val app: Javalin

    init {
        logger = LoggerFactory.getLogger("spella")

        app = Javalin.create { config ->
            config.showJavalinBanner = false

            config.requestLogger { ctx, ms ->
                logger.info("${ctx.method()} ${ctx.fullUrl()} - ${ctx.status()} ${ms}ms")
            }
        }

        app.get("/corrections") { ctx ->
            val text = ctx.queryParam("text")

            if (text == null) {
                ctx.status(422)
                ctx.json(ErrorResponse("Missing parameter: text"))
                return@get
            }

            val language = ctx.queryParam("language")

            if (language == null) {
                ctx.status(422)
                ctx.json(ErrorResponse("Missing parameter: language"))
                return@get
            }

            val startTime = System.currentTimeMillis()
            val correction = QueryMapper(text, language, tries = tries).map()
            val took = System.currentTimeMillis() - startTime

            ctx.json(
                CorrectionResponse(
                    text = correction.value.string,
                    distance = correction.distance,
                    score = correction.score,
                    took = took,
                )
            )
        }
    }

    fun start(bind: String = "localhost", port: Int = 8888) {
        app.start(bind, port)
    }

    fun stop() {
        app.stop()
    }
}
