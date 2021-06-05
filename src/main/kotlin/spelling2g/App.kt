
package spelling2g

import io.javalin.Javalin

fun main(args: Array<String>) {
    val tries = Tries()

    args.forEach {
        tries.addFile(it)
    }

    //var trieNode = tries["de"] ?: return

    while (true) {
        print("> ")
        var input = readLine()!!.trim().toLowerCase()
        var t1 = System.currentTimeMillis()

        //var correction = Automaton(readLine()!!.trim().toLowerCase(), maxEdits = 1).correct(trieNode)

        var correction = QueryMapper(input, language = "de", tries = tries).map(maxLookahead = 3)
        println("${correction?.string}, distance: ${correction?.distance}, score: ${correction?.score}")

        println(System.currentTimeMillis() - t1)
    }

    /*
        val app = Javalin.create().start(7000)
        app.get("/") { ctx -> ctx.result("Hello World") }
    */
}