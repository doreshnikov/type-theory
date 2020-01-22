import parser.Parser
import reduction.Reductor

fun main() {

    readLine()!!.split(" ").map { it.toInt() }.toList().let {
        val n = it[0]
        val k = it[1]
        val line = readLine()
        val expression = Parser(line!!).parse()!!

        println(expression)
        Reductor(expression).process(n, k)
    }

}