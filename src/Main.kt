import parser.Parser
import types.Type
import types.TypeResolver

fun main() {

    val line = readLine()
    val expression = Parser(line!!).parse()!!
    TypeResolver(expression, Type.VariableScope()).printTree()

}