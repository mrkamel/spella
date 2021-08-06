package spella

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import kong.unirest.Unirest

class ServerTest : DescribeSpec({
    val mapper = jacksonObjectMapper()

    fun withServer(tries: Tries, fn: () -> Unit) {
        val server = Server(tries)

        try {
            server.start()

            fn()
        } finally {
            server.stop()
        }
    }

    describe("GET /corrections") {
        it("returns a CorrectionResponse with a http 200 status code") {
            val tries = Tries().also { it.insert("en", "correction", 1.0) }

            withServer(tries) {
                val response = Unirest.get("http://localhost:8888/corrections?text=corection&language=en").asJson()
                response.status.shouldBe(200)

                val correctionResponse: CorrectionResponse = mapper.readValue(response.body.toString())

                correctionResponse.asClue {
                    it.text.shouldBe("correction")
                    it.distance.shouldBe(1)
                    it.score.shouldBe(1.0)
                    it.took.shouldBeGreaterThanOrEqualTo(0)
                }
            }
        }

        it("returns a ErrorResponse with a http 422 status code when the text is missing") {
            val tries = Tries().also { it.insert("en", "correction", 1.0) }

            withServer(tries) {
                val response = Unirest.get("http://localhost:8888/corrections?language=en").asJson()
                response.status.shouldBe(422)

                val errorResponse: ErrorResponse = mapper.readValue(response.body.toString())
                errorResponse.message.shouldBe("Missing parameter: text")
            }
        }

        it("returns a ErrorResponse with a http 422 status code when the language is missing") {
            val tries = Tries().also { it.insert("en", "correction", 1.0) }

            withServer(tries) {
                val response = Unirest.get("http://localhost:8888/corrections?text=corection").asJson()
                response.status.shouldBe(422)

                val errorResponse: ErrorResponse = mapper.readValue(response.body.toString())
                errorResponse.message.shouldBe("Missing parameter: language")
            }
        }
    }
})
