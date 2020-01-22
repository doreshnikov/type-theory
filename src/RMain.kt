import parser.Parser
import reduction.Reductor

fun main() {

    readLine()!!.split(" ").map { it.toInt() }.toList().let { it ->
        val n = it[0]
        val k = it[1]
        val line = readLine()
        val expression = Parser(line!!).parse()!!

        System.out.bufferedWriter().use { out ->
            out.write(expression.toString())
            out.write("\n")
            Reductor(expression).process(n, k, out)
        }
    }

}