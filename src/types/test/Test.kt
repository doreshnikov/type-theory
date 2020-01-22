package types.test

import parser.Parser
import types.TypeResolver
import types.Type

fun main() {

    val tests = arrayOf(
        "\\x.\\y.x",
        "x",
        "(\\x. x) (\\y. y)",
        "\\a. a' a z8'",
        "\\x.\\y.\\z. x z (y z)",
        "\\f.\\x. f (f x)",
        "\\m.\\n. n m",
        "(\\m.\\n. n m) (\\f.\\x. f (f (f x))) (\\f.\\x. f (f x))",
        "(\\s.(\\m.\\n. n m) s s) (\\f.\\x. f (f (f x)))",
        "\\m.\\n.\\f.\\x. m f (n f x)",
        "(\\x. x x)(\\y. y y)",
        "\\x.\\f.\\g. f x",
        "\\x.\\f.\\g. g x",
        "\\a.\\f.\\g. a f g",
        "\\p.p(\\a.\\b.a)",
        "\\p.p(\\a.\\b.b)",
        "\\a.\\b.\\f. f a b",
        "\\n. n (\\x.\\a.\\b.b) (\\p.\\b.p)",
        "\\x. x x",
        "(\\x. x x)(\\x. x x)",
        "(\\x. x)(\\y. x)",
        "(\\y. x)(\\x. x)",
        "a x (f (\\g. g)f)",
        "a x (f f)",
        "(\\x.x) a b",
        "\\x. y (y x) ",
        "\\f.(\\x.f(x x))(\\x.f(x x))",
        "\\f.\\x. f (f (f (f x)))",
        "(\\x.x) y",
        "x (\\y.y)",
        "(\\x.\\y.x) (\\y.\\x.y)",
        "(\\x.x)(\\x.x)",
        "\\x.(\\y.y)\\z.z",
        "(\\a. b) (\\a. b)",
        "(\\a. b) (\\a. b) (\\a. b)"
    )

    for (line in tests) {
        println(line)
        val parsed = Parser(line).parse()!!
        val solver = TypeResolver(parsed, Type.VariableScope())
        solver.printTree()

        println()
    }

}