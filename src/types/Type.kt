package types

import parser.Expression

interface Type {

    fun substitute(from: TypeVariable, to: Type): Type

    class TypeVariable(private val id: Int, private val view: String) : Type {
        override fun toString(): String {
            return "t$id"
        }

        override fun substitute(from: TypeVariable, to: Type): Type {
            return if (from == this) {
                to
            } else {
                this
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TypeVariable

            if (id != other.id) return false
            if (view != other.view) return false

            return true
        }

        override fun hashCode(): Int {
            return id + 1
        }
    }

    class TypeImplication(val left: Type, val right: Type) : Type {
        override fun toString(): String {
            return "($left -> $right)"
        }
        
        override fun substitute(from: TypeVariable, to: Type): Type {
            return TypeImplication(left.substitute(from, to), right.substitute(from, to))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TypeImplication

            if (left != other.left) return false
            if (right != other.right) return false

            return true
        }

        override fun hashCode(): Int {
            return (left.hashCode() shl 8) * 239 + (right.hashCode())
        }
    }

    companion object {
        fun noType(): TypeVariable {
            return TypeVariable(-1, "none")
        }
    }

    class VariableScope {

        private val mapper = HashMap<String, TypeVariable>()
        private var lastId = 0

        private fun createNew(expression: Expression): TypeVariable {
            return TypeVariable(lastId++, "[$expression]")
        }

        fun get(expression: Expression): TypeVariable {
            if (expression is Expression.Variable) {
                return mapper.getOrPut(expression.name, { createNew(expression) })
            }
            return createNew(expression)
        }

        fun forget(expression: Expression.Variable) {
            mapper.remove(expression.name)
        }

    }

}