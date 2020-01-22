package parser

class Parser(line: String) {

    class State(private val line: String) {

        private var index = 0

        init {
            skipSpaces()
        }

        private fun skipSpaces() {
            while (line.getOrNull(index) in arrayOf(' ', '\t', '\n', '\r')) {
                index++
            }
        }

        fun get(): Char? {
            return line.getOrNull(index)
        }

        fun step(): State {
            if (index < line.length) {
                index++.also { skipSpaces() }
            }
            return this
        }

        fun getAndStep(): Char? {
            return get().also { step() }
        }

        fun assertAndStep(c: Char): State? {
            return if (get() == c) step() else null
        }

        override fun toString(): String {
            return "[$line] : $index"
        }

    }

    private val state = State(line)

    fun parse(): Expression? {
        return Expression.parse(state)
    }

}