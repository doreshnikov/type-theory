package parser

interface Expression {

    companion object {

        fun parse(state: Parser.State): Expression? {
            val ret = Application.parse(state) ?: return Abstraction.parse(state) ?: return null
            return Application(ret, Abstraction.parse(state) ?: return ret)
        }

    }

    class Variable(val name: String) : Expression {

        companion object {

            private val variableChars = ('a'..'z').plus('0'..'9').plus('\'')

            fun parse(state: Parser.State): Variable? {
                var name = ""
                return if (state.get() in 'a'..'z') {
                    while (state.get() in variableChars) {
                        name += state.getAndStep()
                    }
                    Variable(name)
                } else null
            }

        }

        override fun toString(): String {
            return name
        }

    }

    abstract class Atom : Expression {

        companion object {

            fun parse(state: Parser.State): Expression? {
                return if (state.get() == '(') {
                    Expression.parse(state.step()).also { state.assertAndStep(')') ?: return null }
                } else {
                    Variable.parse(state)
                }
            }

        }

    }

    class Application(val left: Expression, val right: Expression) : Expression {

        companion object {

            fun parse(state: Parser.State): Expression? {
                var ret = Atom.parse(state) ?: return null
                while (true) {
                    ret = Application(ret, Atom.parse(state) ?: return ret)
                }
            }

        }

        override fun toString(): String {
            return "($left $right)"
        }

    }

    class Abstraction(val left: Variable, val right: Expression) : Expression {

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

        override fun toString(): String {
            return "(\\${left}. $right)"
        }

    }

}