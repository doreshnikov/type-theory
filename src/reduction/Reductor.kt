package reduction

import parser.Expression

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
        when (node) {
            is Expression.Application -> {
                node.left = replace(node.left, variable, sub)
                node.right = replace(node.right, variable, sub)
            }
            is Expression.Abstraction -> {
                val rename = Expression.Variable()
                node.right = replace(node.right, node.left, rename)
                node.left = rename
                node.right = replace(node.right, variable, sub)
            }
            is Expression.Variable -> {
                return if (node == variable) sub
                else node
            }
        }
        return node
    }

    private fun reduce(): Boolean {
        val redex = leftmostRedex(expression) ?: return false
        val variable = (redex.left as Expression.Abstraction).left
        val sub = redex.right.clone()

        redex.left = replace((redex.left as Expression.Abstraction).right, variable, sub)
        redex.right = Expression.REDUCED
        return true
    }

    fun process(n: Int, k: Int) {
        for (i in 1..n) {
            if (!reduce()) break
            if (i % k == 0) {
                println(expression)
            }
        }
    }

}