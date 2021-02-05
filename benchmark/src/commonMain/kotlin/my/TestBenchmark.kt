package my

import TransducerContext
import conj
import kotlinx.benchmark.*
import showDoubledString

@State(Scope.Benchmark)
@Warmup(10)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
open class TestBenchmark {

    lateinit var list: List<Int>
    lateinit var listList: List<List<Int>>

    @Setup
    fun setup() {
        list = listOf(1,2,3)
        listList = listOf(listOf(1,2,3), listOf(4,5,6), listOf(7,8,9))
    }

    @Benchmark
    fun trivialTransducer(): MutableList<String> {
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
    fun flatMapTransducer(): MutableList<Int> {
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
}