package reduction

import parser.Expression
import java.io.BufferedWriter

class Reductor(private var expression: Expression) {

    private fun leftmostRedex(node: Expression): Expression.Application? {
        return when (node) {
            is Expression.Application -> {
                if (node.left is Expression.Abstraction && node.right !is Expression.REDUCED) node
                else leftmostRedex(node.left) ?: leftmostRedex(node.right)
            }
            is Expression.Abstraction -> return leftmostRedex(node.right)
            else -> return null
        }
    }

    private fun replace(node: Expression, variable: Expression.Variable, sub: Expression): Expression {
        return when (node) {
            is Expression.Application -> {
                Expression.Application(
                    replace(node.left, variable, sub),
                    replace(node.right, variable, sub)
                )
            }
            is Expression.Abstraction -> {
                val rename = Expression.Variable()
                val right = replace(node.right, node.left, rename)
                Expression.Abstraction(
                    rename,
                    replace(right, variable, sub)
                )
            }
            is Expression.Variable -> {
                if (node == variable) sub
                else node
            }
            else -> node
        }
    }

    private fun normalize(node: Expression): Expression {
        return when (node) {
            is Expression.Application -> {
                if (node.right is Expression.REDUCED) {
                    normalize(node.left)
                } else {
                    Expression.Application(
                        normalize(node.left),
                        normalize(node.right)
                    )
                }
            }
            is Expression.Abstraction -> {
                Expression.Abstraction(
                    node.left,
                    normalize(node.right)
                )
            }
            else -> node
        }
    }

    private fun reduce(): Boolean {
        val redex = leftmostRedex(expression) ?: return false
        val variable = (redex.left as Expression.Abstraction).left
        val sub = redex.right.clone()

        redex.left = replace((redex.left as Expression.Abstraction).right, variable, sub)
        redex.right = Expression.REDUCED
        expression = normalize(expression)
        return true
    }

    fun process(n: Int, k: Int, out: BufferedWriter) {
        for (i in 1..n) {
            if (!reduce()) break
            if (i % k == 0) {
                out.write(expression.toString())
                out.write("\n")
            }
        }
    }

}