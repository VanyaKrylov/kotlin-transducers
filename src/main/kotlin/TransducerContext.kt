class TransducerContext<Recv>(var step: Reducer<Recv,String>) { //initially step is the terminating reduce function of the chain

    inline fun ctx(
        transduce: TransducerContext<Recv>.(Reducer<Recv,String>) -> Reducer<Recv,Int>
    ): Reducer<Recv, Int> =
        this.transduce(step)

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

    fun main() {

        ctx {
            mapping<String,Int> { v -> v.showDoubledString() }(filtering<String> { v -> v.startsWith("4") }(step))
        }
    }



    /*inline fun <T> statefulFiltering(crossinline pred: (T) -> Boolean) {
        step = { acc: Recv, arg: Any? -> if (pred((arg as T))) step(acc,arg) else acc }
    }*/

    inline fun transduce() {

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