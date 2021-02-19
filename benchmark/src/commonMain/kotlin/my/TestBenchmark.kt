package my

import Reducer
import TransducerContext
import conj
import kotlinx.benchmark.*
import showDoubledString

//TODO increase size

@State(Scope.Benchmark)
@Warmup(20)
@Measurement(iterations = 20)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class TestBenchmark {

    lateinit var list: List<Int>
    lateinit var listList: List<List<Int>>
    lateinit var strList: List<String>
    lateinit var seq: Sequence<Int>
    lateinit var rangeList: List<Int>
    lateinit var conjLambdaInt: Reducer<MutableList<Int>, Int>
    lateinit var conjLambdaString: Reducer<MutableList<String>, String>

    @Setup
    fun setup() {
        list = listOf(1, 2, 3)
        listList = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
        strList = listOf("123", "456", "78")
        seq = IntRange(0, 1000).asSequence()
        rangeList = IntRange(0, 1000).toList()
        conjLambdaInt = { a: MutableList<Int>, b: Int -> conj(a, b) }
        conjLambdaString = { a: MutableList<String>, b: String -> conj(a, b) }
    }

    //============================================ TRIVIAL ============================================

    @Benchmark
    fun trivialFullInlined(): List<String> {
        var exit = false
        val acc = mutableListOf<String>()
        var count = 0

        for (e in list) {
            if (exit) break
            val p1 = { a: MutableList<String>, b: String -> conj(a, b) }
            val p2 = e.showDoubledString()
            if (!p2.startsWith("3")) {
                count++
                if (count > 2) {
                    acc
                } else if (count == 2) {
                    exit = true
                    p1(acc, p2)
                } else {
                    p1(acc, p2)
                }
            } else {
                acc
            }
        }

        return acc
    }

    @Benchmark
    fun trivialLambdaInlined(): List<String> {
        var exit = false
        val acc = mutableListOf<String>()
        var count = 0

        for (e in list) {
            if (exit) break
            ({ st: Reducer<MutableList<String>, String> ->
                {
                    { acc2: MutableList<String>, arg2: String ->
                        {
                            if (!arg2.startsWith("3")) {
                                { acc3: MutableList<String>, arg3: String ->
                                    {
                                        count++
                                        if (count > 2) {
                                            acc3
                                        } else if (count == 2) {
                                            exit = true
                                            st(acc3, arg3)
                                        } else {
                                            st(acc3, arg3)
                                        }
                                    }
                                }(acc2, arg2).invoke()
                            } else {
                                acc2
                            }
                        }
                    }(acc, e.showDoubledString()).invoke()
                }
            }{ a, b -> conj(a, b) }).invoke()
        }

        return acc
    }

    @Benchmark
    fun ctrlAltNtrivialInlined(): List<String> {
        val transducerContext = TransducerContext<MutableList<String>, Int, String> { a, b -> conj(a, b) }
        var count = 0

        val acc = mutableListOf<String>()
        for (e in list) {
            if (transducerContext.exit) break
            val p2 = e.showDoubledString()
            if (!p2.startsWith("3")) {
                count++
                if (count > 2) {
                    acc
                } else if (count == 2) {
                    transducerContext.exit = true
                    transducerContext.step(acc, p2)
                } else {
                    transducerContext.step(acc, p2)
                }
            } else
                acc
        }
        return acc
    }

    @Benchmark
    fun trivialTransducer(): List<String> {
        val transducerContext = TransducerContext<MutableList<String>, Int, String> { a, b -> conj(a, b) }
        val transducerChain = transducerContext.ctx { st ->
            mapping<String, Int> { v -> v.showDoubledString() }(
                filtering<String> { v -> !v.startsWith("3") }(
                    taking<String>(2)(
                        st
                    )
                )
            ) //So Clojure, much smiley =)
        }

        return transducerContext.transduce(list, mutableListOf(), transducerChain)
    }

    @Benchmark
    fun trivialSequence(): List<String> {

        return list
            .asSequence()
            .map { it.showDoubledString() }
            .filter { !it.startsWith("3") }
            .take(2)
            .toList()
    }

    @Benchmark
    fun trivialStandard(): List<String> {

        return list
            .map { it.showDoubledString() }
            .filter { !it.startsWith("3") }
            .take(2)
    }

    //============================================ FLAT MAP ============================================

    @Benchmark
    fun flatMapFullInlined(): List<Int> {
        var exit = false
        val acc = mutableListOf<Int>()
        var count = 0

        for (e in listList) {
            if (exit) break
            val p1 = { a: MutableList<Int>, b: Int -> conj(a, b) }
            for (ee in e) {
                if (exit) break
                count++
                if (count > 8) {
                    acc
                } else if (count == 8) {
                    exit = true
                    p1(acc, ee * 10)
                } else {
                    p1(acc, ee * 10)
                }
            }
        }

        return acc
    }

    @Benchmark
    fun flatMapLambdaInlined(): List<Int> {
        var exit = false
        val acc = mutableListOf<Int>()
        var count = 0

        for (e in listList) {
            if (exit) break
            ({ st: Reducer<MutableList<Int>, Int> ->
                {
                    { acc2: MutableList<Int>, arg2: Iterable<Int> ->
                        {
                            for (ee in arg2) {
                                if (exit) break
                                { acc3: MutableList<Int>, arg3: Int ->
                                    {
                                        { acc4: MutableList<Int>, arg4: Int ->
                                            {
                                                count++
                                                if (count > 8) {
                                                    acc4
                                                } else if (count == 8) {
                                                    exit = true
                                                    st(acc2, ee * 10)
                                                } else {
                                                    st(acc2, ee * 10)
                                                }
                                            }
                                        }(acc3, ee * 10).invoke()
                                    }
                                }(acc2, ee).invoke()
                            }

                            acc2
                        }
                    }(acc, e).invoke()
                }
            }{ a, b -> conj(a, b) }).invoke()
        }

        return acc
    }

    @Benchmark
    fun ctrlAltNflatMapInlined(): List<Int> {
        val transCtx = TransducerContext<MutableList<Int>, Iterable<Int>, Int> { a, b -> conj(a, b) }
        var count = 0

        val acc = mutableListOf<Int>()
        for (e in listList) {
            if (transCtx.exit) break
            for (e in e) {
                if (transCtx.exit) break
                val p2 = e * 10
                count++
                if (count > 8) {
                    acc
                } else if (count == 8) {
                    transCtx.exit = true
                    transCtx.step(acc, p2)
                } else {
                    transCtx.step(acc, p2)
                }
            }
        }
        return acc
    }

    @Benchmark
    fun flatMapTransducer(): List<Int> {
        val transCtx = TransducerContext<MutableList<Int>, Iterable<Int>, Int> { a, b -> conj(a, b) }
        val transChain = transCtx.ctx { st ->
            flatMapping<Int, Int> { el -> el * 10 }(
                taking<Int>(8)(
                    st
                )
            )
        }

        return transCtx.transduce(listList, mutableListOf(), transChain)
    }

    @Benchmark
    fun flatMapSequence(): List<Int> {

        return listList
            .asSequence()
            .flatten()
            .map { it * 10 }
            .take(8)
            .toList()
    }


    @Benchmark
    fun flatMapStandard(): List<Int> {

        return listList
            .flatten()
            .map { it * 10 }
            .take(8)
    }

    //============================================ MAP FLAT ============================================

    /* Only this **handInlined** benchmark was actually written by hand, others were automatically inlined by
       Intellij
    */
    @Benchmark
    fun mapFlatHandInlined(): List<Int> {
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

        return acc
    }

    @Benchmark
    fun mapFlatFullInlined(): List<Int> {
        val exit = false
        val acc = mutableListOf<Int>()

        for (e in strList) {
            if (exit) break
            for (ee in e.toList()) {
                if (exit) break
                val p2 = ee.toInt()
                if (p2 > 3)
                    conj(acc, p2)
                else
                    acc
            }
        }

        return acc
    }

    @Benchmark
    fun mapFlatLambdaHandInlined(): List<Int> {
        val exit = false
        val acc = mutableListOf<Int>()

        for (e in strList) {
            if (exit) break
            { st: Reducer<MutableList<Int>, Int> ->
                { acc2: MutableList<Int>, arg2: String ->
                    {
                        for (ee in arg2.toList()) {
                            if (exit) break
                            { acc3: MutableList<Int>, arg3: Char ->
                                {
                                    { acc4: MutableList<Int>, arg4: Int ->
                                        {
                                            if (arg4 > 3)
                                                st(acc4, arg4)
                                            else
                                                acc4
                                        }
                                    }(acc3, arg3.toInt()).invoke()
                                }
                            }(acc2, ee).invoke()
                        }
                    }
                }(acc, e)
            }{ a, b -> conj(a, b) }.invoke()
        }

        return acc
    }

    @Benchmark
    fun ctrlALtNmapFlatInlined(): List<Int> {
        val ctxBuilder = TransducerContext<MutableList<Int>, String, Int> { a, b -> conj(a, b) }

        val acc = mutableListOf<Int>()
        for (e in strList) {
            if (ctxBuilder.exit) break
            for (e in e.toList()) {
                if (ctxBuilder.exit) break
                val p2 = e.toInt()
                if (p2 > 3)
                    ctxBuilder.step(acc, p2)
                else
                    acc
            }
        }
        return acc
    }

    @Benchmark
    fun mapFlatting(): List<Int> {
        val ctxBuilder = TransducerContext<MutableList<Int>, String, Int> { a, b -> conj(a, b) }
        val chain = ctxBuilder.ctx {
            mapFlatting { s: String -> s.toList() }(
                mapping { el: Char -> el.toInt() }(
                    filtering { el: Int -> el > 3 }(
                        step
                    )
                )
            )
        }

        return ctxBuilder.transduce(strList, mutableListOf(), chain)
    }

    @Benchmark
    fun mapFlatSeq(): List<Int> {

        return strList
            .asSequence()
            .flatMap { it.toList() }
            .map { it.toInt() }
            .filter { it > 3 }
            .toList()
    }

    @Benchmark
    fun mapFlat(): List<Int> {

        return strList
            .flatMap { it.toList() }
            .map { it.toInt() }
            .filter { it > 3 }
    }

    //============================================ HEAVY ============================================

    @Benchmark
    fun heavyFullInlined(): List<Int> {
        var exit = false
        val acc = mutableListOf<Int>()
        var count = 0

        for (e in strList) {
            if (exit) break
            val p1 = { a: MutableList<Int>, b: Int -> conj(a, b) }
            for (ee in e.toList()) {
                if (exit) break
                for (eee in IntRange(0, ee.toInt() * 10)) {
                    if (exit) break
                    if (eee % 2 == 0) {
                        count++
                        if (count > 80) {
                            acc
                        } else if (count == 80) {
                            exit = true
                            p1(acc, eee)
                        } else {
                            p1(acc, eee)
                        }
                    }
                }
            }
        }

        return acc
    }

    @Benchmark
    fun heavyLambdaInlined(): List<Int> {
        var exit = false
        val acc = mutableListOf<Int>()
        var count = 0

        for (e in strList) {
            if (exit) break
            ({ st: Reducer<MutableList<Int>, Int> ->
                { //transduce
                    { acc2: MutableList<Int>, arg2: String ->
                        { //mapFlatting
                            for (ee in arg2.toList()) {
                                if (exit) break
                                { acc3: MutableList<Int>, arg3: Char ->
                                    { // mapping
                                        { acc4: MutableList<Int>, arg4: Int ->
                                            { //mapFlatting
                                                for (eee in IntRange(0, arg4 * 10)) {
                                                    if (exit) break
                                                    { acc5: MutableList<Int>, arg5: Int ->
                                                        { //filtering
                                                            if (arg5 % 2 == 0) {
                                                                { acc6: MutableList<Int>, arg6: Int ->
                                                                    {
                                                                        count++
                                                                        if (count > 80) {
                                                                            acc6
                                                                        } else if (count == 80) {
                                                                            exit = true
                                                                            st(acc6, arg6)
                                                                        } else {
                                                                            st(acc6, arg6)
                                                                        }
                                                                    }
                                                                }(acc5, arg5).invoke()
                                                            }
                                                        }
                                                    }(acc4, eee).invoke()
                                                }

                                                acc4
                                            }
                                        }(acc3, arg3.toInt()).invoke()
                                    }
                                }(acc2, ee).invoke()
                            }

                            acc2
                        }
                    }(acc, e).invoke()
                }
            }{ a, b -> conj(a, b) }).invoke()
        }

        return acc
    }

    @Benchmark
    fun heavyTransduced(): List<Int> {
        val ctxBuilder = TransducerContext<MutableList<Int>, String, Int> { a, b -> conj(a, b) }
        val execChain = ctxBuilder.ctx { st ->
            mapFlatting { el: String -> el.toList() }(
                mapping { el: Char -> el.toInt() }(
                    mapFlatting { el: Int -> IntRange(0, el * 10) }(
                        filtering { el: Int -> el % 2 == 0 }(
                            taking<Int>(80)(
                                st
                            )
                        )
                    )
                )
            )
        }

        return ctxBuilder.transduce(strList, mutableListOf(), execChain)
    }

    @Benchmark
    fun heavySequence(): List<Int> {

        return strList
            .asSequence()
            .flatMap { it.toList() }
            .map { it.toInt() }
            .flatMap { IntRange(0, it * 10) }
            .filter { it % 2 == 0 }
            .take(80)
            .toList()
    }

    @Benchmark
    fun heavyStd(): List<Int> {

        return strList
            .flatMap { it.toList() }
            .map { it.toInt() }
            .flatMap { IntRange(0, it * 10) }
            .filter { it % 2 == 0 }
            .take(80)
    }

    @Benchmark //avg 163.5 ops/ms
    fun simpleTrans(): List<Int> {
        val ctxBuilder = TransducerContext<MutableList<Int>, Int, Int> { a, b -> conj(a, b) }
        val execChain = ctxBuilder.ctx { st ->
            mapping { el: Int -> el * 2 }(
                filtering { el: Int -> el % 3 == 0 }(
                    taking<Int>(1000)(
                        st
                    )
                )
            )
        }

        return ctxBuilder.transduce(rangeList, mutableListOf(), execChain)
    }

    @Benchmark // avg 134 ops/ms
    fun simpleSeq(): List<Int> {
        return seq
            .map { it * 2 }
            .filter { it % 3 == 0 }
            .take(1000)
            .toList()
    }
}