package reduction.test

import parser.Expression
import types.Type
import types.TypeResolver
import kotlin.random.Random

fun randomVariableName() : String {
    return (1..4).joinToString("") { ('a' + Random.nextInt(0, 'z' - 'a')).toString() }
}

fun build(depth: Int, free: List<String>, abstractions: List<String>): Expression {
    if (depth == 0) {
        return Expression.Variable(Random.nextInt(0, free.size + 2 * abstractions.size).let {
            if (it < free.size) {
                free[it]
            } else {
                abstractions[(it - free.size) / 2]
            }
        })
    } else {
        return if (Random.nextBoolean()) {
            Expression.Application(
                build(depth - 1, free, abstractions),
                build(depth - 1, free, abstractions)
            )
        } else {
            var variableName = randomVariableName()
            while (variableName in free || variableName in abstractions) {
                variableName = randomVariableName()
            }
            Expression.Abstraction(
                Expression.Variable(variableName),
                build(depth - 1, free, abstractions.plus(variableName))
            )
        }
    }
}

fun main() {

    println(('a'..'z').take(10))
    while (true) {
        val freeVariables = Random.nextInt(1, 15)
        val expression =
            build(7, ('a'..'z').take(freeVariables).map { it.toString() }, emptyList())
        println(expression)
        try {
            TypeResolver(expression, Type.VariableScope()).printTree()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        } catch (e: Error) {
            e.printStackTrace()
            return
        }
    }

}