package spelling2g

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.slf4j.LoggerFactory

class Spella : CliktCommand(name = "spella", treatUnknownOptionsAsArgs = true) {
    val bind by option(help = "The address to listen on").default("localhost")
    val port by option(help = "The port to listen on").int().default(8888)
    val arguments by argument("files").multiple()
    val logger = LoggerFactory.getLogger("spella")

    override fun run() {
        val tries = Tries()

        arguments.forEach {
            logger.info("loading $it")

            tries.addFile(it)
        }

        Server(tries = tries).start(bind, port)
    }
}

fun main(args: Array<String>) = Spella().main(args)
