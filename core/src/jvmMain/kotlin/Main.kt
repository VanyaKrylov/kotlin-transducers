val conjLambda = {a: MutableList<Int>, b: Int -> conj(a, b) }
val conjLambda2 = {a: MutableList<String>, b: String -> conj(a, b) }

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

interface Foo {
    fun foo()
}

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

  /*  listOf(1,2).transduce(mutableListOf()) { ctx: TransducerContext2<MutableList<Int>> -> with(ctx) {
        testtt()
    } }*/

    val res2 = listOf("123", "456", "78")
        .transduce(mutableListOf<Int>()) {
            mapFlatting { el: String -> el.toList() }(
            mapping { el: Char -> el.toInt() }(
            mapFlatting { el: Int -> IntRange(0, el * 10) }(
            filtering { el: Int -> el % 2 == 0 }(
            taking<Int>(80)(
            customTransducer<Int>()(
            ::toList
        ))))))}

    val res = listOf("123", "456", "78")
        .transduce(mutableListOf<Int>()) {(
                +mapFlatting<String,Char> { it.toList() }
                +mapping { el: Char -> el.toInt() }
                +mapFlatting { el: Int -> IntRange(0, el * 10) }
                +filtering { el: Int -> el % 2 == 0 }
                +taking(10)
                +::toList
            )}

    val res3 = listOf("123", "456", "78")
        .transduce<String,Int> {(
                +mapFlatting { el:String -> el.toList() }
                +mapping { el: Char -> el.toInt() }
                +mapFlatting { el: Int -> IntRange(0, el * 10) }
                +filtering { el: Int -> el % 2 == 0 }
                +taking(8))
        }

    val expected = listOf(1,2,3)
        .map { it.showDoubledString()}
        .filter { it.startsWith("1") }
        .take(3)

    val expected2 = listOf("123", "456", "78")
        .flatMap { it.toList() }
        .map { it.toInt() }
        .flatMap { IntRange(0, it * 10) }
        .filter { it % 2 == 0 }
        .take(80)

    val short = listOf(1,2,3)
        .transduce4<Int,Int> {
            { arg: Reducer<MutableList<Int>, Int> ->
                mapping<Int,Int> { it * 2 }(
                mapFlatting { el: Int -> el .. 10 }(
                taking<Int>(18)(arg)
                ))}
    }.count()

    val expectedShort = listOf(1,2,3)
        .map { it * 2 }

    var v = 2
    var vv = 1
    var vvv = 3
    var v2 = 2
    var vv2 = 1
    var vvv2 = 3

    fun bar(arg: Foo) = arg.foo()

    val longer = (1..100).toMutableList()
        .transduce4<Int,Int> {(
                +mapping<Int,Int> {
                    v = it * 2
                    vv *= it * 2
                    bar(object : Foo {
                        override fun foo() {
                            vv /= it * 2
                        }

                    })
                    it + 2
                }
                +filtering { it > 3 }
                +mapping { it.toString() }
                +mapFlatting { it.toList() }
                +mapping { el: Char -> el.toInt() }
                +mapFlatting { el: Int -> IntRange(0, el * 10) }
                +filtering { el: Int -> el % 2 == 0 }
                +taking(9)
                +mapping { it * 2 }
                +zipping((1..100).toMutableList().lazyTransduce<Int, Int> {(
                        +mapping<Int, Int> {
                            println("[T]INSIDE ZIP: ${it}")
                            it * it
                        }
                        +filtering { it > 50 }
                        +taking(10)
                )})
                +mapping { it.second }
                +mapFlatting {
                    bar(object : Foo {
                        override fun foo() {
                            vvv += it * 2
                        }
                    })

                    v = it * it
                    vv *= v

                    listOf(it)
                }
                +taking(90)
        )}

    val stdLonger = (1..100).toMutableList()
        .asSequence()
        .map {
            v2 = it * 2
            vv2 *= it * 2
            bar(object : Foo {
                override fun foo() {
                    vv2 /= it * 2
                }

            })
            it + 2
        }
        .filter { it > 3 }
        .map { it.toString() }
        .flatMap { it.toList() }
        .map { el: Char -> el.toInt() }
        .flatMap { el: Int -> IntRange(0, el * 10) }
        .filter { el: Int -> el % 2 == 0 }
        .take(9)
        .map { it * 2 }
        .zip((1 .. 100)
            .asSequence()
            .map {
                println("INSIDE ZIP: ${it}")
                it * it
            }
            .filter { it > 50 }
            .take(10))
        .map { it.second }
        .flatMap {
            bar(object : Foo {
                override fun foo() {
                    vvv2 += it * 2
                }
            })

            v2 = it * it
            vv2 *= v2

            listOf(it)
        }
        .take(90)
        .toList()

    println("""
        [!!!] ${longer}, v=${v} vv =${vv} vvv = ${vvv}        
        [!!!] ${stdLonger}, v2=${v2} vv2 =${vv2} vvv2 = ${vvv2}        
    """.trimIndent())

    val new = listOf("123", "456", "789")
        .transduce<String, Int> { (
                +mapFlatting { s: String -> s.toList() }
                +mapping { el: Char -> el.toInt() }
                +filtering { el: Int -> el > 51 }
        ) }

    println("NEW [!_!_!]: ${new}")

    val sum: Reducer<Int, Int> = { acc: Int, el: Int -> acc + el }
    val sumL: Reducer<Long, Long> = { a: Long, b: Long -> a + b }

    val summ = (1..100).toMutableList()
        .transduce(0) {(
                (+mapping<Int, Int> { it * 2 }){ acc: Int, el: Int -> acc + el }
//        +filtering { it < 10 }

     )}

    val summm = (1..100).toMutableList()
        .map { it * 2 }
        .sum()

    println("""
        <==================>
        ${summ}
        ${summm}
        <==================>
    """.trimIndent())

    val z = (1..10).toMutableList()
        .transduce4<Int,Int> {(
                +zipping<Int,Int>(listOf(42,432,5432))
                +mapping { it.first }
        )}

    val zz = (1..10).toMutableList()
        .zip(listOf(42,432,5432))
        .map { it.first }

    println("""
        ZIIIIIIP
        z =  ${z}
        zz = ${zz}
        ZIIIIIIP
    """.trimIndent())

    //println("Hooray! Res= ${res}")
    println("""
        Actual Res2: ${res2}
        [!] Exprected:   ${expected2}
        [!] Actual res:  ${res}
        [!] New (res3):  ${res3}
        Expected:    ${expected}
        
        Short:         ${short}
        Expected short:${expectedShort}
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

    val lazy = listOf(1,2,3,4,5,6).lazyTransduce<Int,Int> {(
            +mapping<Int,Int> { it * 2 }
            +filtering { it > 4 }
    )}

    val iter = lazy.iterator()

    while (iter.hasNext()) {
        println("Lazy: ${iter.next()}")
    }

    val vHi = (0..10000000).map { (it % 10).toLong() }.dropLast(1).toMutableList()
    val vLo = (0..10).map { (it % 10).toLong() }.dropLast(1).toMutableList()

    val cart = vHi
        .transduce5(0L) {((
        +mapFlatting<Long, Long> { d: Long -> vLo.transduce4 { mapping { it * d } } })
        { a: Long, b: Long -> a + b }
        )}

    val cartt = vHi
        .flatMap { d -> vLo.map { it * d } }
        .sum()


    println("Cart:  ${cart}")
    println("Cartt: ${cartt}")

    val flatMapTake = vHi
        .transduce5(0L) {((
        +mapFlatting<Long, Long> { d: Long -> vLo.transduce4 { mapping { it * d } } }
        +taking(20000000))
        { a: Long, b: Long -> a + b }
    )}

    val flatMapTakee = vHi
        .flatMap { d -> vLo.map { it * d } }
        .take(20000000)
        .sum()

    println("""
        1) ${flatMapTake}
        2) ${flatMapTakee}
    """.trimIndent())

    val dotProduct = vHi.transduce5(0L) {(
            (+zipping(vHi) { a: Long, b: Long -> (a * b) } )
            { a: Long, b: Long -> a + b }
    )}

    val dotProductt = vHi
        .zip(vHi) { a, b -> a * b }
        .sum()

    println("""
        dotProduct
        1) ${dotProduct}
        2) ${dotProductt}
    """.trimIndent())

    val vFaZ = (0L..10000L).toMutableList().dropLast(1)

    val flatMapAfterZip = vFaZ.transduce5(0L) {((
            +zipping(vFaZ) { a: Long, b: Long -> (a + b) }
            +mapFlatting<Long, Long> { d: Long -> vFaZ.transduce4 { mapping { it + d } } })
            { a: Long, b: Long -> a + b }
            )}

    val flatMapAfterZipp = vFaZ
        .zip(vFaZ) { a, b -> a + b }
        .flatMap { d -> vFaZ.map { it + d } }
        .sum()

    println("""
        flatMapAfterZip
        1) ${flatMapAfterZip}
        2) ${flatMapAfterZipp}
    """.trimIndent())

    val vZaF = (0L..1000L).toMutableList().dropLast(1)

    val zipAfterFlatMap = vZaF
        .transduce5(0L) {((
        +mapFlatting2<Long, Long> { d -> vZaF.fuser { +mapping<Long, Long> { it + d } } }
        +zipping(vZaF) { a: Long, b: Long -> a + b })
        { a, b -> a + b })}

    val zipAfterFlatMapp = vZaF
        .asSequence()
        .flatMap { d -> vZaF.map { it + d } }
        .zip(vZaF.asSequence()) { a, b -> a + b }
        .sum()

    println("""
        zipAfterFlatMap
        1) ${zipAfterFlatMap}
        2) ${zipAfterFlatMapp}
    """.trimIndent())

    val v_ = (0L..100000000L).map { it % 10 }.dropLast(1)
    val zipFlatFlat = v_.transduce5(0L) {((
            +mapFlatting2<Long, Long> { d -> vLo.fuser { +mapping<Long, Long> { it + d } } }
            +zipping(vLo.transduce4 { +mapFlatting2<Long, Long> { d -> v_.fuser { +mapping<Long, Long> { it + d } } } }) {
                    a: Long, b: Long -> a + b
            }
            +taking(20000000))
            { a, b -> a + b })}

    val zipFlatFlatt = v_.asSequence()
        .flatMap { d -> vLo.map { it + d } }
        .zip(vLo.asSequence().flatMap { d -> v_.map { it + d } }) {
                a, b -> a + b
        }
        .take(20000000)
        .sum()

    println("""
        zipFlatFlat
        1) ${zipFlatFlat}
        2) ${zipFlatFlatt}
    """.trimIndent())

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