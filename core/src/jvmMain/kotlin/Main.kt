val conjLambda = {a: MutableList<Int>, b: Int -> conj(a, b) }

fun <In> TransducerContext2<MutableList<Int>>.customTransducer(): Transducer<MutableList<Int>,In,In> =
    { step: Reducer<MutableList<Int>,In> ->
        { acc: MutableList<Int>, el: In ->
            exit = true
            acc
        }}

fun <In> customTransducer(): Transducer<MutableList<Int>,In,In> =
    { step: Reducer<MutableList<Int>,In> ->
        { acc: MutableList<Int>, el: In ->
            acc
        }}


fun TransducerContext2<MutableList<Int>>.testtt() = customTransducer<Int>().invoke() { a, b -> toList(a, b)}

fun main() {
    val list = listOf(1, 2, 3)
   /* val listList = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
    val strList = listOf("123", "456", "78")
    val list2 = list.map { it.showDoubledString() }
        .filter { it.startsWith("4") }
        .take(0)
*/
   fun <T> conj(acc: MutableList<T>, el: T): MutableList<T> =
       acc.apply { this.add(el) }
    val transducerContext = TransducerContext<MutableList<String>, Int, String> { a, b -> conj(a, b) }

    val transChain2 = transducerContext.ctx2 {
        mapping<String, Int> { v -> v.showDoubledString() } (
            filtering<String> { v -> !v.startsWith("3") }(
                taking<String>(2) () { a, b ->
                    conj(a, b)
                }
            )
        )
    }

    /*val res = list
        .transduce(mutableListOf<String>(), TransducerContext2 {
            mapping<String, Int> { v -> v.showDoubledString() } (
            filtering<String> { v -> !v.startsWith("3") }(
            taking<String>(2) () { a, b ->
            conj(a, b)
        }))})*/

    val customTransducer: Transducer<MutableList<Int>,Int,Int> =
    { step: Reducer<MutableList<Int>,Int> ->
        { acc: MutableList<Int>, el: Int ->
            acc
        }}

    /*val res2 = listOf("123", "456", "78")
        .transduce(mutableListOf<Int>(), TransducerContext2 {
            mapFlatting { el: String -> el.toList() }(
            mapping { el: Char -> el.toInt() }(
            mapFlatting { el: Int -> IntRange(0, el * 10) }(
            filtering { el: Int -> el % 2 == 0 }(
            taking<Int>(80)(
            customTransducer<Int>()(
            ::toList
            )
        )))))})

    listOf(1,2).transduce(mutableListOf<Int>(), TransducerContext2<MutableList<Int>,Int>{
        testtt()
    })*/

    val res2 = listOf("123", "456", "78")
        .transduceWithLazyLoadedChain(mutableListOf()) { ctx: TransducerContext2<MutableList<Int>> -> with(ctx) {
            mapFlatting { el: String -> el.toList() }(
            mapping { el: Char -> el.toInt() }(
            mapFlatting { el: Int -> IntRange(0, el * 10) }(
            filtering { el: Int -> el % 2 == 0 }(
            taking<Int>(80)(
            customTransducer<Int>()(
            ::toList
        ))))))}}

    val expected2 = listOf("123", "456", "78")
        .flatMap { it.toList() }
        .map { it.toInt() }
        .flatMap { IntRange(0, it * 10) }
        .filter { it % 2 == 0 }
        .take(80)

    //println("Hooray! Res= ${res}")
    println("""
        Actual Res2: ${res2}
        Exprected:   ${expected2}
    """.trimIndent())

    val transducerChain = transducerContext.ctx {
        mapping<String, Int> { v -> v.showDoubledString() } (
            filtering<String> { v -> !v.startsWith("3") }(
                taking<String>(2)(
                    step
                )
            )
        ) //So Clojure, much smiley =)
    }
    println("Transduced new: ${transducerContext.transduce(list, mutableListOf(), transChain2)}")

    /*
    val transCtx = TransducerContext<MutableList<Int>, Iterable<Int>, Int> { a, b -> conj(a, b) }
    val transChain = transCtx.ctx {
        flatMapping<Int, Int> { el -> el * 10 }(
            taking<Int>(8)(
                step
            )
        )
    }
    println("Flatmap check: ${transCtx.transduce(listList, mutableListOf(), transChain)}")


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
 */

    /*val expected =  strList
        .flatMap { it.toList() }
        .map { it.toInt() }
        .flatMap { IntRange(0, it * 10) }
        .filter { it % 2 == 0 }
        .take(80)

    var exit = false
    val acc = mutableListOf<Int>()
    var count = 0

    for (e in strList) {
        if (exit) break
        ({ st: Reducer<MutableList<Int>, Int> -> { //transduce
            { acc2: MutableList<Int>, arg2: String -> { //mapFlatting
                for (ee in arg2.toList()) {
                    if (exit) break
                    { acc3: MutableList<Int>, arg3: Char -> { // mapping
                        { acc4: MutableList<Int>, arg4: Int -> { //mapFlatting
                            for (eee in IntRange(0, arg4 * 10)) {
                                if (exit) break
                                { acc5: MutableList<Int>, arg5: Int -> { //filtering
                                    if (arg5 % 2 == 0) {
                                        { acc6: MutableList<Int>, arg6: Int -> {
                                            count++
                                            if (count > 80) {
                                                acc6
                                            } else if (count == 80) {
                                                exit = true
                                                st(acc6, arg6)
                                            } else {
                                                st(acc6, arg6)
                                            }
                                        }}(acc5, arg5) .invoke()
                                    }
                                }}(acc4, eee) .invoke()
                            }

                            acc4
                        }}(acc3, arg3.toInt()) .invoke()
                    }}(acc2, ee) .invoke()
                }

                acc2
            }}(acc, e) .invoke()
        }}{a, b -> conj(a, b)}) .invoke()
    }

    println("""
         1: ${expected}
         2: ${acc}
    """.trimIndent())*/

    /*println("""
        Standard: ${stdRes}
        Transduced: ${trnsRes}
        Hand-inlined: ${acc}
        Hand lambda-inlined: ${ac}
    """.trimIndent())*/
}