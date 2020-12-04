typealias Reducer<T,R> = (T, R) -> T

typealias Transducer<Acc,T_in,T_out> = (Reducer<Acc,T_in>) -> Reducer<Acc,T_out>

fun <R,T> reduce(arr: Collection<T>, f: (R?,T) -> R): R {
    var acc: R? = null
    for (e in arr) {
        acc = f(acc, e)
    }
    return acc!!
}

fun <R,T> foldl(arr: Collection<T>, start: R, f: (R, T) -> R): R {
    var acc = start
    for (e in arr) {
        acc = f(acc, e)
    }

    return acc
}

fun <R,T> map(arr: Collection<T>, f: (T) -> R,): Collection<R> =
    foldl(arr, emptyList(),) { acc, el -> acc.plus(f(el)) }

inline fun <Acc,T_in,T_out> mapping(crossinline f: (T_out) -> T_in): Transducer<Acc,T_in,T_out> =
    { step: Reducer<Acc,T_in> ->
        { acc: Acc, arg: T_out -> step(acc, f(arg)) }}

inline fun <In,Out> List<In>.mapT(crossinline f: (In) -> Out): List<Out> {
    val result = mutableListOf<Out>()
    for (el in this) {
        (mapping<MutableList<Out>,Out,In>(f))(::conj).invoke(result, el)
    }
    return result
}

fun <T> conj(acc: MutableList<T>, el: T): MutableList<T> =
    acc.apply { this.add(el) }

fun Int.showDoubledString() = (this * this).toString()

fun main() {
    val list = listOf(1,2,3)
    val list2 = list.map { it.showDoubledString() }
        .filter { it.startsWith("4") }

//    println("Casual: ${ map(list) { it.showDoubledString() } }")
    println("Casual: ${list2}")
    val foo = mapping<List<String>,String,Int>(Int::showDoubledString)
        .invoke(List<String>::plus)
    println("Transduced: ${foldl(list, emptyList(), foo)}")
    println("Ext fun: ${list.mapT { it.showDoubledString() }}")

    val transducerContext = TransducerContext<MutableList<String>> { a, b -> conj(a, b) }
    val transducerChain = transducerContext.ctx {
        mapping<String,Int> { v -> v.showDoubledString() }(
            filtering<String> { v -> v.startsWith("4") }(step))
    }
    println("Transduced new: ${foldl(list, mutableListOf(), transducerChain)}")
    /*val pairedList: List<Pair<Int,String>> = listOf(1 to "a", 2 to "b", 3 to "see")
    pairedList.flatMap { listOf(it.first.toString(),it.second) }*/

}
