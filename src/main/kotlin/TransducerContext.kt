class TransducerContext<Container,T_init,T_fin>(var step: Reducer<Container,Any?>) {

    inline fun ctx(
        transduce: TransducerContext<Container,T_init,T_fin>.(Reducer<Container,Any?>) -> Reducer<Container,Any?>
    ): Reducer<Container,Any?> =
        this.transduce(step)

    inline fun <T_in,T_out> mapping(crossinline f: (T_out) -> T_in): Transducer<Container,T_in,T_out> =
        { step: Reducer<Container,T_in> ->
            { acc: Container, arg: T_out -> step(acc, f(arg)) }}



}