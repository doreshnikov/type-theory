package parser

interface Expression {

    companion object {
        fun parse(state: Parser.State): Expression? {
            val ret = Application.parse(state) ?: return Abstraction.parse(state) ?: return null
            return Application(ret, Abstraction.parse(state) ?: return ret)
        }

        fun areEqual(left: Expression, right: Expression): Boolean {
            return (left.hashCode() == right.hashCode() && left.toString() == right.toString())
        }
    }

    fun clone(): Expression

    class Variable private constructor(val name: String) : Expression {

        companion object {
            private val variableChars = ('a'..'z') + ('0'..'9') + '\''
            private val factory = hashMapOf<String, Variable>()

            fun parse(state: Parser.State): Variable? {
                var name = ""
                return if (state.get() in 'a'..'z') {
                    while (state.get() in variableChars) {
                        name += state.getAndStep(false)
                    }
                    state.skipSpaces()
                    invoke(name)
                } else null
            }

            operator fun invoke(name: String): Variable {
                return factory.getOrPut(name) { Variable(name) }
            }

            operator fun invoke(): Variable {
                var i = 0
                while ("v$i" in factory) i++
                return invoke("v$i")
            }
        }

        private val hash = name.hashCode()

        override fun clone(): Variable {
            return this
        }

        override fun hashCode(): Int {
            return hash
        }

        override fun toString(): String {
            return name
        }
    }

    abstract class Atom : Expression {

        companion object {
            fun parse(state: Parser.State): Expression? {
                return if (state.get() == '(') {
                    Expression.parse(state.assertAndStep('(')!!).also { state.assertAndStep(')') ?: return null }
                } else {
                    Variable.parse(state)
                }
            }
        }

    }

    class Application(var left: Expression, var right: Expression) : Expression {

        companion object {
            fun parse(state: Parser.State): Expression? {
                var ret = Atom.parse(state) ?: return null
                while (true) {
                    ret = Application(ret, Atom.parse(state) ?: return ret)
                }
            }
        }

        private val data get() = if (right is REDUCED) left.toString() else "($left $right)"
        private val hash get() = data.hashCode()

        override fun clone(): Application {
            return Application(left.clone(), right.clone())
        }

        override fun hashCode(): Int {
            return hash
        }

        override fun toString(): String {
            return data
        }

    }

    class Abstraction(var left: Variable, var right: Expression) : Expression {

        companion object {
            fun parse(state: Parser.State): Expression? {
                state.assertAndStep('\\') ?: return null
                return Variable.parse(state)?.let { variable ->
                    Expression.parse(state.assertAndStep('.') ?: return null)?.let { expression ->
                        return Abstraction(variable, expression)
                    }
                }
            }
        }

        private val data get() = "(\\${left}. $right)"
        private val hash get() = data.hashCode()

        override fun clone(): Abstraction {
            return Abstraction(left.clone(), right.clone())
        }

        override fun hashCode(): Int {
            return hash
        }

        override fun toString(): String {
            return data
        }

    }

    object REDUCED : Expression {

        override fun clone(): Expression {
            return this
        }

    }

}