package spella

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.options.flag
import org.slf4j.LoggerFactory

class Spella : CliktCommand(name = "spella", treatUnknownOptionsAsArgs = true) {
    val bind by option("--bind", help = "The address to listen on").default("localhost")
    val port by option("--port", help = "The port to listen on").int().default(8888)
    val distances by option("--distances", help = "A comma separated list of allowed edit distances. The numbers represent the string lengths (default: 4,9)").default("4,9")
    val arguments by argument("files").multiple()
    val logger = LoggerFactory.getLogger("spella")

    override fun run() {
        val tries = Tries()
        val allowedDistances = distances.split(",").map { it.toInt() }

        arguments.forEach {
            logger.info("loading $it")

            tries.addFile(it)
        }

        Server(tries, allowedDistances).start(bind, port)
    }
}

fun main(args: Array<String>) = Spella().main(args)
