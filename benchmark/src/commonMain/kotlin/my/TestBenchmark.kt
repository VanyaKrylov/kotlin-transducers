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

    @Setup
    fun setup() {
        list = listOf(1,2,3)
        listList = listOf(listOf(1,2,3), listOf(4,5,6), listOf(7,8,9))
        strList = listOf("123", "456", "78")
    }

    @Benchmark
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
    fun trivialStandard(): List<String> {

        return list
            .map { it.showDoubledString() }
            .filter { !it.startsWith("3") }
            .take(2)
    }

    @Benchmark
    fun flatMapStandard() : List<Int> {
        return listList
            .flatten()
            .map { it * 10 }
            .take(8)
    }

    @Benchmark
    fun mapFlat() : List<Int> {

        return strList
            .flatMap { it.toList() }
            .map { it.toInt() }
            .filter { it > 3 }
    }

    @Benchmark
    fun mapFlatting() : List<Int> {
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
    fun heavyStd() : List<Int> {
        return strList
            .flatMap { it.toList() }
            .map { it.toInt() }
            .flatMap { IntRange(0, it * 10) }
            .filter { it % 2 == 0 }
            .take(80)
    }

    @Benchmark
    fun notSoHeavyCozTransduced() : List<Int> {
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
}