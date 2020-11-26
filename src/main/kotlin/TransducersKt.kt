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

inline fun <Acc,T_in,T_out> mapping(crossinline f: (T_out) -> T_in) =
    { step: Reducer<Acc,T_in> ->
        { acc: Acc, arg: T_out -> step(acc, f(arg)) }}

inline fun <T_in,T_out> List<T_out>.mapT(crossinline f: (T_out) -> T_in): List<T_in> {
    val result = mutableListOf<T_in>()
    for (el in this) {
        (mapping<MutableList<T_in>,T_in,T_out>(f))(::conj).invoke(result, el)
    }
    return result
}

fun <T> conj(acc: MutableList<T>, el: T): MutableList<T> =
    acc.apply { this.add(el) }

fun Int.showDoubledString() = (this * this).toString()

fun main() {
    val list = listOf(1,2,3)
    println("Casual: ${map(list) { it.showDoubledString() }}")
    val foo = mapping<List<String>,String,Int>(Int::showDoubledString)
        .invoke(List<String>::plus)
    println("Transduced: ${foldl(list, emptyList(), foo)}")
    println("Ext fun: ${list.mapT { it.showDoubledString() }}")
}