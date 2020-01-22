package types

import parser.Expression
import java.lang.StringBuilder

class TypeResolver(private val expression: Expression, private val scope: Type.VariableScope) {

    class Equation(val left: Type, val right: Type) {

        fun swap(): Equation {
            return Equation(right, left)
        }

    }

    class EquationSystem(vararg children: EquationSystem?) : ArrayList<Equation>() {

        init {
            children.forEach {
                it?.forEach { eq -> add(eq) }
            }
        }

        constructor(left: Type, right: Type) : this() {
            add(Equation(left, right))
        }

    }

    private val dependencies: List<TypeResolver>

    private var result: Type
    private var system: EquationSystem

    init {
        when (expression) {
            is Expression.Variable -> {
                dependencies = emptyList()
                result = scope.get(expression)
                system = EquationSystem()
            }
            is Expression.Application -> {
                val left = TypeResolver(expression.left, scope)
                val right = TypeResolver(expression.right, scope)
                dependencies = listOf(left, right)
                result = scope.get(expression)
                system = EquationSystem(
                    left.system, right.system,
                    EquationSystem(left.result, Type.TypeImplication(right.result, result))
                )
            }
            is Expression.Abstraction -> {
                val varType = scope.get(expression.left)
                val right = TypeResolver(expression.right, scope).also { scope.forget(expression.left) }
                dependencies = listOf(right)
                result = Type.TypeImplication(varType, right.result)
                system = right.system
            }
            else -> {
                // this actually should not happen
                dependencies = emptyList()
                result = Type.noType()
                system = EquationSystem()
            }
        }
    }

    private fun unification(): EquationSystem? {
        val system = EquationSystem(this.system)

        fun checkDiscard() {
            val remove = arrayListOf<Int>()
            for (i in 0 until system.size) {
                val eq = system[i]
                if (eq.right is Type.TypeVariable && eq.left !is Type.TypeVariable) {
                    system[i] = eq.swap()
                } else if (eq.left == eq.right) {
                    remove.add(i)
                }
            }
            remove.reversed().forEach { system.removeAt(it) }
        }

        fun checkSubstitutionCorrectness(from: Type.TypeVariable, to: Type): Boolean {
            if (to is Type.TypeVariable) {
                return from != to
            } else with(to as Type.TypeImplication) {
                return checkSubstitutionCorrectness(from, left) && checkSubstitutionCorrectness(from, right)
            }
        }

        fun checkSubstitution(from: Type.TypeVariable, to: Type, except: Int): Boolean {
            var success = 0
            for (i in 0 until system.size) {
                if (i == except) {
                    continue
                }
                val eq = system[i]
                val left = eq.left.substitute(from, to)
                val right = eq.right.substitute(from, to)
                if (left != eq.left || right != eq.right) {
                    system[i] = Equation(left, right)
                    success++
                }
            }
            return success > 0
        }

        var finished = false
        while (!finished) {
            checkDiscard()
            var i = 0
            while (i < system.size) {
                val eq = system[i]
                if (eq.left is Type.TypeImplication && eq.right is Type.TypeImplication) {
                    with(system) {
                        add(Equation(eq.left.left, eq.right.left))
                        add(Equation(eq.left.right, eq.right.right))
                        removeAt(i)
                    }
                    break
                }
                if (eq.left is Type.TypeVariable) {
                    if (!checkSubstitutionCorrectness(eq.left, eq.right)) {
                        return null
                    }
                    if (checkSubstitution(eq.left, eq.right, i)) {
                        break
                    }
                }
                i++
            }
            finished = (i == system.size)
        }

        return system
    }

    private fun correctResult(type: Type, solution: EquationSystem): Type {
        var resultType = type
        solution.forEach { resultType = resultType.substitute(it.left as Type.TypeVariable, it.right) }
        return resultType
    }

    private fun processAndCollectGlobalContext(solution: EquationSystem, target: MutableMap<String, Type>) {
        result = correctResult(result, solution)
        dependencies.forEach { it.processAndCollectGlobalContext(solution, target) }
        when (expression) {
            is Expression.Variable -> target[expression.name] = result
            is Expression.Abstraction -> target.remove(expression.left.name)
        }
    }

    private fun output(
        depth: Int, solution: EquationSystem, context: MutableMap<String, Type>,
        target: MutableList<String>
    ) {
        val rule = when (expression) {
            is Expression.Variable -> 1
            is Expression.Application -> 2
            is Expression.Abstraction -> 3
            else -> -1
        }
        val line = StringBuilder()
        repeat(depth) { line.append("*   ") }
        context.map { "${it.key} : ${it.value}" }.joinTo(line)
        if (context.isNotEmpty()) {
            line.append(" ")
        }
        line.append("|- $expression : ${correctResult(result, solution)} [rule #$rule]")
        target.add(line.toString())

        if (expression is Expression.Abstraction) {
            context[expression.left.name] = correctResult((result as Type.TypeImplication).left, solution)
        }
        dependencies.forEach { it.output(depth + 1, solution, context, target) }
        if (expression is Expression.Abstraction) {
            context.remove(expression.left.name)
        }
    }

    fun printTree() {
        val solution = unification().also { it ?: println("Expression has no type") } ?: return
        val contextTarget = HashMap<String, Type>()
        processAndCollectGlobalContext(solution, contextTarget)
        val lineTarget = ArrayList<String>()
        output(0, solution, contextTarget, lineTarget)
        println(lineTarget.joinToString("\n"))
    }

}