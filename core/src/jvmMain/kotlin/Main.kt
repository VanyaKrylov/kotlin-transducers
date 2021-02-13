class Main {
}

val conjLambda = {a: MutableList<Int>, b: Int -> conj(a, b) }

fun main() {
    val list = listOf(1, 2, 3)
    val list2 = list.map { it.showDoubledString() }
        .filter { it.startsWith("4") }
        .take(0)

    val transducerContext = TransducerContext<MutableList<String>, Int, String> { a, b -> conj(a, b) }
    val transducerChain = transducerContext.ctx {
        mapping<String, Int> { v -> v.showDoubledString() }(
            filtering<String> { v -> !v.startsWith("3") }(
                taking<String>(2)(
                    step
                )
            )
        ) //So Clojure, much smiley =)
    }
    println("Transduced new: ${transducerContext.transduce(list, mutableListOf(), transducerChain)}")

    val listList = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
    val transCtx = TransducerContext<MutableList<Int>, Iterable<Int>, Int> { a, b -> conj(a, b) }
    val transChain = transCtx.ctx {
        flatMapping<Int, Int> { el -> el * 10 }(
            taking<Int>(8)(
                step
            )
        )
    }
    println("Flatmap check: ${transCtx.transduce(listList, mutableListOf(), transChain)}")

    val strList = listOf("123", "456", "78")
    val stdRes = strList
        .flatMap { it.toList() }
        .map { it.toInt() }
        .filter { it > 3 }

    val ctxBuilder = TransducerContext<MutableList<Int>, String, Int> { a, b -> conj(a, b)}
    val chain = ctxBuilder.ctx { st ->
        mapFlatting { s: String -> s.toList() }(
            mapping { el: Char -> el.toInt() }(
                filtering { el: Int -> el > 3 }(
                    st
                )
            )
        )
    }

    val trnsRes = ctxBuilder.transduce(strList, mutableListOf(), chain)

    val exit = false
    val acc = mutableListOf<Int>()
    for (e in strList) { // -> transduce
        if (exit) break // -> mapFlatting
            for (ee in e.toList()) {
                if (exit) break
                if (ee.toInt() > 3)
                    acc.apply { this.add(ee.toInt()) }
        }
    }

    val exit2 = false
    val ac = mutableListOf<Int>()

    for (e in strList) {
        if (exit) break
        ({ st: Reducer<MutableList<Int>, Int> -> {
                acc2: MutableList<Int>, arg2: String -> {
            for (ee in arg2.toList()) {
                if (exit) break
                {acc3: MutableList<Int>, arg3: Char -> {
                    {acc4: MutableList<Int>, arg4: Int -> {
                        if (arg4 > 3)
                            st(acc4, arg4)
                        else
                            acc4
                    }} (acc3, arg3.toInt()).invoke()
                }} (acc2, ee).invoke()
            }
        }}(ac, e)
        }(conjLambda)).invoke()
    }

    println("""
        Standard: ${stdRes}
        Transduced: ${trnsRes}
        Hand-inlined: ${acc}
        Hand lambda-inlined: ${ac}
    """.trimIndent())
}