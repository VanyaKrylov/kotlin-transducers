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
//                        count++
                        exit = true
                        step(acc, arg)
                    } else {
//                        count++
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

    /*fun main() {

        ctx {
            mapping<String,Int> { v -> v.showDoubledString() }(filtering<String> { v -> v.startsWith("4") }(step))
        }
    }*/



    /*inline fun <T> statefulFiltering(crossinline pred: (T) -> Boolean) {
        step = { acc: Recv, arg: Any? -> if (pred((arg as T))) step(acc,arg) else acc }
    }*/

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