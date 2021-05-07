typealias Reducer<T,R> = (T, R) -> T
typealias Transducer<Acc,T_in,T_out> = (Reducer<Acc,T_in>) -> Reducer<Acc,T_out>

//@Retention(AnnotationRetention.BINARY)
annotation class SuperInline

class TransducerContext<Recv,In,Out>(var step: Reducer<Recv,Out>) { //initially step is the terminating reduce function of the chain

    var exit: Boolean = false

    @SuperInline
    inline fun ctx(
        @SuperInline apply: TransducerContext<Recv,In,Out>.(Reducer<Recv,Out>) -> Reducer<Recv,In>
    ): Reducer<Recv, In> =
        this.apply(step)

    inline fun ctx2(
        @SuperInline apply: TransducerContext<Recv,In,Out>.() -> Reducer<Recv,In>
    ): Reducer<Recv, In> =
        this.apply()

    @SuperInline
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
                    acc }}
    }

    //Does actually behave as casual flatMap from stdlib
    inline fun <T_in,T_out> mapFlatting(crossinline f: (T_out) -> Iterable<T_in>): Transducer<Recv,T_in,T_out> {
        return { step: Reducer<Recv,T_in> ->
                { acc: Recv, arg: T_out ->
                    for (e in f(arg)) {
                        if (exit) break
                        step(acc, e)
                    }
                    acc }}
    }

    inline fun <In,Out> transduce(arr: List<In>, start: Out, reducer: Reducer<Out,In>): Out {
        val acc = start
        for (e in arr) {
            if (exit) break
            reducer(acc, e)
        }

        return acc
    }
}

class TransducerContext2<Recv,In> {//@SuperInline constructor(@SuperInline val chain: TransducerContext2<Recv,In>.() -> Reducer<Recv,In>) {
    var exit: Boolean = false

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
                acc }}
    }

    //Does actually behave as casual flatMap from stdlib
    inline fun <T_in,T_out> mapFlatting(crossinline f: (T_out) -> Iterable<T_in>): Transducer<Recv,T_in,T_out> {
        return { step: Reducer<Recv,T_in> ->
            { acc: Recv, arg: T_out ->
                for (e in f(arg)) {
                    if (exit) break
                    step(acc, e)
                }
                acc }}
    }
}

/*inline fun <In, Recv> List<In>.transduce(initial: Recv, ctx: TransducerContext2<Recv, In>): Recv {
    val reducer = ctx.chain(ctx)
    for (e in this) {
        if (ctx.exit) break
        reducer(initial, e)
    }

    return initial
}*/

inline fun <In,Recv> List<In>.transduceWithLazyLoadedChain(initial: Recv, operatorChainSupplier: (TransducerContext2<Recv,In>) -> Reducer<Recv,In>): Recv {
    val ctx = TransducerContext2<Recv,In>()
    val operatorChainReducer = operatorChainSupplier(ctx)
    for (e in this) {
        if (ctx.exit) break
        operatorChainReducer(initial, e)
    }

    return initial
}

inline fun <Recv> toList(acc: MutableList<Recv>, v: Recv) = acc.apply { this.add(v) }

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

fun <T> conj(acc: MutableList<T>, el: T): MutableList<T> =
    acc.apply { this.add(el) }

fun Int.showDoubledString() = (this * this).toString()

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

