typealias Reducer<T,R> = (T, R) -> T

typealias Transducer<T_n,T_m,R> = (Reducer<T_n, R>) -> Reducer<T_m, R>

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

//fun <>mapping(in: Transducer)
fun <R,T> map(arr: Collection<T>, f: (T) -> R,): Collection<R> =
    foldl(arr, emptyList(),) { acc, el -> acc.plus(f(el)) }          // return reduce(arr) { r: Collection<R>?, t -> r?.plus(f(t)) ?: listOf(f(t)) }

fun <R,A,T> mapping(f: (R) -> T) =
    { step: Reducer<A,T> ->
        { acc: A, arg: R -> step(acc, f(arg)) }}


fun Int.showDoubledString() = (this * this).toString()

fun main() {
    val list = listOf(1,2,3)
    println(map(list) {it.showDoubledString()})
}