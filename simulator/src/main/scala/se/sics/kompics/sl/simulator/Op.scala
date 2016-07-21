package se.sics.kompics.sl.simulator

import se.sics.kompics.KompicsEvent
import se.sics.kompics.simulator.adaptor._

object Op {

    class FunctionOperation[E <: KompicsEvent](generator: Unit => E) extends Operation[E] {
        override def generate(): E = {
            generator(());
        }
    }
    class FunctionOperation1[E <: KompicsEvent, N <: Number](generator: N => E) extends Operation1[E, N] {
        override def generate(n: N): E = {
            generator(n);
        }
    }
    class FunctionOperation2[E <: KompicsEvent, N <: Number, N2 <: Number](generator: (N, N2) => E) extends Operation2[E, N, N2] {
        override def generate(n: N, n2: N2): E = {
            generator(n, n2);
        }
    }

    class FunctionOperation3[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number](generator: (N, N2, N3) => E) extends Operation3[E, N, N2, N3] {
        override def generate(n: N, n2: N2, n3: N3): E = {
            generator(n, n2, n3);
        }
    }

    class FunctionOperation4[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number](generator: (N, N2, N3, N4) => E) extends Operation4[E, N, N2, N3, N4] {
        override def generate(n: N, n2: N2, n3: N3, n4: N4): E = {
            generator(n, n2, n3, n4);
        }
    }

    class FunctionOperation5[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number, N5 <: Number](generator: (N, N2, N3, N4, N5) => E) extends Operation5[E, N, N2, N3, N4, N5] {
        override def generate(n: N, n2: N2, n3: N3, n4: N4, n5: N5): E = {
            generator(n, n2, n3, n4, n5);
        }
    }


    def apply[E <: KompicsEvent](generator: Unit => E): Operation[E] = new FunctionOperation(generator)
    def apply[E <: KompicsEvent, N <: Number](generator: N => E): Operation1[E, N] = new FunctionOperation1(generator)
    def apply[E <: KompicsEvent, N <: Number, N2 <: Number](generator: (N, N2) => E): Operation2[E, N, N2] = new FunctionOperation2(generator)
    def apply[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number](generator: (N, N2, N3) => E): Operation3[E, N, N2, N3] = new FunctionOperation3(generator)
    def apply[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number](generator: (N, N2, N3, N4) => E): Operation4[E, N, N2, N3, N4] = new FunctionOperation4(generator)
    def apply[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number, N5 <: Number](generator: (N, N2, N3, N4, N5) => E): Operation5[E, N, N2, N3, N4, N5] = new FunctionOperation5(generator)

}
