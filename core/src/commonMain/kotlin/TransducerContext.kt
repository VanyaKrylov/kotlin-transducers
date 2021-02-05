class TransducerContext<Recv,In,Out>(var step: Reducer<Recv,Out>) { //initially step is the terminating reduce function of the chain

    var exit: Boolean = false

    inline fun ctx(
        apply: TransducerContext<Recv,In,Out>.(Reducer<Recv,Out>) -> Reducer<Recv,In>
    ): Reducer<Recv, In> =
        this.apply(step)

    inline fun <T_in,T_out> mapping(crossinline f: (T_out) -> T_in): Transducer<Recv,T_in,T_out> =
        { step: Reducer<Recv,T_in> ->
            { acc: Recv, arg: T_out -> step(acc, f(arg)) }}

    inline fun <T> filtering(crossinline pred: (T) -> Boolean): Transducer<Recv,T,T> =
        { step: Reducer<Recv,T> ->
            { acc: Recv, arg: T ->
                if (pred(arg))
                    step(acc,arg)
                else
                    acc }}

    inline fun <T> taking(n: Int): Transducer<Recv,T,T> {
        var count = 0
        return { step: Reducer<Recv,T> ->
            { acc: Recv, arg: T ->
                count++
                if (count > n) {
                    acc
                }
                else if (count == n) {
                    exit = true
                    step(acc, arg)
                } else {
                    step(acc, arg)
                }}}
    }

    inline fun <T_in,T_out> flatMapping(crossinline f: (T_out) -> T_in): Transducer<Recv,T_in,Iterable<T_out>> {
        return { step: Reducer<Recv,T_in> ->
            { acc: Recv, arg: Iterable<T_out> ->
                for (e in arg) {
                    if (exit) break
                    mapping(f)(step)(acc,e)
                }
                acc
            }}
    }

    inline fun <In,Out> transduce(arr: List<In>, start: Out, reducer: Reducer<Out,In>): Out {
        var acc = start
        for (e in arr) {
            if (exit) break
            acc = reducer(acc, e)
        }

        return acc
    }
}

/*
* In casual execution order the operators are applied top-to-down and left-to-right:
*
* START
* |
* |-> list       (1)
* |->  .map()    (2)
* |->  .filter() (3)
* |->  .sum()    (4)
* V
* END
*
* But with transducers it is down-to-up and right-to-left:
*
*                        END
* (4)  ctx{               ^
* (3)       mapping()   <-|
* (2)       filtering() <-|
* (1)       summing()   <-|
*       }                 |
*                       START
*
* */

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

/*
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

   // println("Transduced new: ${foldl(list, mutableListOf(), transducerChain)}")
    */
/*val pairedList: List<Pair<Int,String>> = listOf(1 to "a", 2 to "b", 3 to "see")
    pairedList.flatMap { listOf(it.first.toString(),it.second) }*//*


}
*/
