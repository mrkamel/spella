package spelling2g

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class Spella : CliktCommand(name = "spella", treatUnknownOptionsAsArgs = true) {
    val bind by option(help = "The address to listen on").default("127.0.0.1")
    val port by option(help = "The port to listen on").int().default(8888)
    val arguments by argument("files").multiple()
    val logger = LoggerFactory.getLogger(this::class.java)

    override fun run() {
        val tries = Tries()

        arguments.forEach {
            logger.info("loading $it")

            tries.addFile(it)
        }

        embeddedServer(Netty, port = port, host = bind) {
            install(ContentNegotiation) {
                gson()
            }

            install(CallLogging) {
                level = Level.INFO

                format { call ->
                    val method = call.request.local.method.value
                    val uri = call.request.local.uri
                    val status = call.response.status()
                    val timingHeader = call.response.headers["X-Timing"]

                    "$method $uri - $status - $timingHeader"
                }
            }

            registerCorrectionsController(tries)
        }.start(wait = true)
    }
}

fun main(args: Array<String>) = Spella().main(args)
