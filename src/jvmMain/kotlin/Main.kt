class Main {
}

fun main() {
    val list = listOf(1,2,3)
    val list2 = list.map { it.showDoubledString() }
        .filter { it.startsWith("4") }
        .take(0)

//    println("Casual: ${ map(list) { it.showDoubledString() } }")
    println("Casual: ${list2}")
    val foo = mapping<List<String>,String,Int>(Int::showDoubledString)
        .invoke(List<String>::plus)
    println("Transduced: ${foldl(list, emptyList(), foo)}")
    println("Ext fun: ${list.mapT { it.showDoubledString() }}")

    val transducerContext = TransducerContext<MutableList<String>,Int,String> { a, b -> conj(a, b) }
    val transducerChain = transducerContext.ctx {
        mapping<String,Int> { v -> v.showDoubledString() }(
            filtering<String> { v -> !v.startsWith("3") }(
                taking<String>(0)(
                    step))) //So Clojure, much smiley =)
    }
    println("Transduced new: ${transducerContext.transduce(list, mutableListOf(), transducerChain)}")

    val listList = listOf(listOf(1,2,3), listOf(4,5,6), listOf(7,8,9))
    val transCtx = TransducerContext<MutableList<Int>,Iterable<Int>,Int> { a, b -> conj(a, b) }
    val transChain = transCtx.ctx {
        flatMapping<Int,Int> { el -> el*10 }(
            taking<Int>(8)(
                step))
    }
    println("Flatmap check: ${transCtx.transduce(listList, mutableListOf(), transChain)}")
}