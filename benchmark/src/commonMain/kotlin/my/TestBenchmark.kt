package my

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.State

@State(Scope.Benchmark)
open class TestBenchmark {

    @Benchmark
    fun test() {
        //gyuyuyu
    }
}