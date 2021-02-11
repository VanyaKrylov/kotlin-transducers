package my

import TransducerContext
import conj
import kotlinx.benchmark.*
import showDoubledString

//TODO: sequence

@State(Scope.Benchmark)
@Warmup(10)
@Measurement(iterations = 10)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class TestBenchmark {

    lateinit var list: List<Int>
    lateinit var listList: List<List<Int>>
    lateinit var strList: List<String>
    lateinit var seq: Sequence<Int>
    lateinit var rangeList: List<Int>

    @Setup
    fun setup() {
        list = listOf(1,2,3)
        listList = listOf(listOf(1,2,3), listOf(4,5,6), listOf(7,8,9))
        strList = listOf("123", "456", "78")
        seq = IntRange(0, 1000).asSequence()
        rangeList = IntRange(0, 1000).toList()
    }

    /*@Benchmark
    fun trivialTransducer(): List<String> {
        val transducerContext = TransducerContext<MutableList<String>,Int,String> { a, b -> conj(a, b) }
        val transducerChain = transducerContext.ctx {
            mapping<String,Int> { v -> v.showDoubledString() }(
                filtering<String> { v -> !v.startsWith("3") }(
                    taking<String>(2)(
                        step))) //So Clojure, much smiley =)
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
    }*/

    /*@Benchmark
    fun trivialStandard(): List<String> {

        return list
            .map { it.showDoubledString() }
            .filter { !it.startsWith("3") }
            .take(2)
    }*/

/*    @Benchmark
    fun flatMapTransducer(): List<Int> {
        val transCtx = TransducerContext<MutableList<Int>,Iterable<Int>,Int> { a, b -> conj(a, b) }
        val transChain = transCtx.ctx {
            flatMapping<Int,Int> { el -> el*10 }(
                taking<Int>(8)(
                    step))
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
    }*/


    /*@Benchmark
    fun flatMapStandard() : List<Int> {

        return listList
            .flatten()
            .map { it * 10 }
            .take(8)
    }*/

    @Benchmark
    fun mapFlatting(): List<Int> {
        val ctxBuilder = TransducerContext<MutableList<Int>, String, Int> { a, b -> conj(a, b)}
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

    /*@Benchmark
    fun mapFlat(): List<Int> {

        return strList
            .flatMap { it.toList() }
            .map { it.toInt() }
            .filter { it > 3 }
    }*/

    /*@Benchmark
    fun empty() {
    }*/

    /*@Benchmark
    fun notSoHeavyCozTransduced(): List<Int> {
        val ctxBuilder = TransducerContext<MutableList<Int>, String, Int> { a, b -> conj(a,b) }
        val execChain = ctxBuilder.ctx {
            mapFlatting { el: String -> el.toList() }(
                mapping { el: Char -> el.toInt() }(
                    mapFlatting { el: Int -> IntRange(0, el * 10) }(
                        filtering { el: Int -> el % 2 == 0 }(
                            taking<Int>(80)(
                                step)))))
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
    }*/

    /*@Benchmark
    fun heavyStd() : List<Int> {

        return strList
            .flatMap { it.toList() }
            .map { it.toInt() }
            .flatMap { IntRange(0, it * 10) }
            .filter { it % 2 == 0 }
            .take(80)
    }*/

   /* @Benchmark //avg 163.5 ops/ms
    fun simpleTrans(): List<Int> {
        val ctxBuilder = TransducerContext<MutableList<Int>, Int, Int> { a, b -> conj(a, b)}
        val execChain = ctxBuilder.ctx {
            mapping { el: Int -> el * 2 }(
                filtering { el: Int -> el % 3 ==0 }(
                    taking<Int>(1000)(
                        step)))
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
    }*/
}