package spelling2g

import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level

fun main(args: Array<String>) {
    val tries = Tries()

    args.forEach {
        tries.addFile(it)
    }

    embeddedServer(Netty, port = 8888, host = "0.0.0.0") {
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
