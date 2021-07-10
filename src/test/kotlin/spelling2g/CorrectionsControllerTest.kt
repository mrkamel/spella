package spelling2g

import com.google.gson.Gson
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication

class CorrectionsControllerTest : DescribeSpec({
    fun Application.setup(tries: Tries) {
        install(ContentNegotiation) {
            gson()
        }

        registerCorrectionsController(tries)
    }

    describe("GET /corrections") {
        it("returns a CorrectionResponse with a http 200 status code") {
            val tries = Tries().also { it.insert("en", "correction", 1.0) }
            val response = withTestApplication({ setup(tries) }) {
                handleRequest(HttpMethod.Get, "/corrections?text=corection&language=en").response
            }

            response.status()?.value.shouldBe(200)

            val correctionResponse = Gson().fromJson(response.content, CorrectionResponse::class.java)

            correctionResponse.asClue {
                it.text.shouldBe("correction")
                it.distance.shouldBe(1)
                it.score.shouldBe(1.0)
                it.took.shouldBeGreaterThanOrEqualTo(0)
            }
        }

        it("returns a http 406 status code for non-json requests") {
            val tries = Tries().also { it.insert("en", "correction", 1.0) }
            val response = withTestApplication({ setup(tries) }) {
                handleRequest(HttpMethod.Get, "/corrections?language=en") {
                    addHeader(HttpHeaders.Accept, ContentType.Text.Plain.toString())
                }.response
            }

            response.status()?.value.shouldBe(406)
        }

        it("returns a ErrorResponse with a http 422 status code when the text is missing") {
            val tries = Tries().also { it.insert("en", "correction", 1.0) }
            val response = withTestApplication({ setup(tries) }) {
                handleRequest(HttpMethod.Get, "/corrections?language=en").response
            }

            response.status()?.value.shouldBe(422)

            val errorResponse = Gson().fromJson(response.content, ErrorResponse::class.java)

            errorResponse.message.shouldBe("Missing parameter: text")
        }

        it("returns a ErrorResponse with a http 422 status code when the language is missing") {
            val tries = Tries().also { it.insert("en", "correction", 1.0) }
            val response = withTestApplication({ setup(tries) }) {
                handleRequest(HttpMethod.Get, "/corrections?text=corection").response
            }

            response.status()?.value.shouldBe(422)

            val errorResponse = Gson().fromJson(response.content, ErrorResponse::class.java)

            errorResponse.message.shouldBe("Missing parameter: language")
        }
    }
})
