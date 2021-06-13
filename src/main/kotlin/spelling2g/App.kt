package spelling2g

import io.javalin.Javalin

fun main(args: Array<String>) {
    val tries = Tries()

    args.forEach {
        tries.addFile(it)
    }

    while (true) {
        print("> ")
        var input = readLine()!!.trim().lowercase()

        var t1 = System.currentTimeMillis()

        var correction = QueryMapper(input, language = "de", tries = tries).map(maxLookahead = 5)
        println("${correction?.value?.string}, distance: ${correction?.distance}, score: ${correction?.score}")

        println(System.currentTimeMillis() - t1)
    }

    /*
        val app = Javalin.create().start(7000)
        app.get("/") { ctx -> ctx.result("Hello World") }
    */
}