package se.sics.kompics.sl.simulator

import se.sics.kompics.sl.KompicsEvent
import se.sics.kompics.simulator.adaptor._

/**
  * Operations of various arities, which produce a [[se.sics.kompics.sl.KompicsEvent KompicsEvent]]
  *
  * Arities from zero to five are provided.
  *
  * Use these to map parameters to simulator events using the various [[se.sics.kompics.sl.simulator raise]] functions.
  *
  * @example {{{
  * raise(5,
  *   Op { (self: Integer, other: Integer) =>
  *     val selfAddr = lookupAddress(self); /* Get an address instance for the id */
  *     val otherAddr = lookupAddress(ponger); /* Get an address instance for the id */
  *     StartNode(selfAddr, Init[NewComponent](selfAddr, otherAddr))
  *   },
  *   1.toN, 2.toN)
  * }}}
  *
  * @see [[se.sics.kompics.simulator.adaptor.Operation]]
  */
object Op {

  /**
    * An operation implementation that uses a Scala function internally
    *
    * @see [[se.sics.kompics.simulator.adaptor.Operation]]
    */
  class FunctionOperation[E <: KompicsEvent](generator: Unit => E) extends Operation[E] {
    override def generate(): E = {
      generator(());
    }
  }

  /**
    * An operation implementation that uses a Scala function internally
    *
    * @see [[se.sics.kompics.simulator.adaptor.Operation1]]
    */
  class FunctionOperation1[E <: KompicsEvent, N <: Number](generator: N => E) extends Operation1[E, N] {
    override def generate(n: N): E = {
      generator(n);
    }
  }

  /**
    * An operation implementation that uses a Scala function internally
    *
    * @see [[se.sics.kompics.simulator.adaptor.Operation2]]
    */
  class FunctionOperation2[E <: KompicsEvent, N <: Number, N2 <: Number](generator: (N, N2) => E)
      extends Operation2[E, N, N2] {
    override def generate(n: N, n2: N2): E = {
      generator(n, n2);
    }
  }

  /**
    * An operation implementation that uses a Scala function internally
    *
    * @see [[se.sics.kompics.simulator.adaptor.Operation3]]
    */
  class FunctionOperation3[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number](generator: (N, N2, N3) => E)
      extends Operation3[E, N, N2, N3] {
    override def generate(n: N, n2: N2, n3: N3): E = {
      generator(n, n2, n3);
    }
  }

  /**
    * An operation implementation that uses a Scala function internally
    *
    * @see [[se.sics.kompics.simulator.adaptor.Operation4]]
    */
  class FunctionOperation4[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number](
      generator: (N, N2, N3, N4) => E
  ) extends Operation4[E, N, N2, N3, N4] {
    override def generate(n: N, n2: N2, n3: N3, n4: N4): E = {
      generator(n, n2, n3, n4);
    }
  }

  /**
    * An operation implementation that uses a Scala function internally
    *
    * @see [[se.sics.kompics.simulator.adaptor.Operation5]]
    */
  class FunctionOperation5[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number, N5 <: Number](
      generator: (N, N2, N3, N4, N5) => E
  ) extends Operation5[E, N, N2, N3, N4, N5] {
    override def generate(n: N, n2: N2, n3: N3, n4: N4, n5: N5): E = {
      generator(n, n2, n3, n4, n5);
    }
  }

  /**
    * Zero parameter generator
    *
    * @tparam E the type of event to produce
    * @param generator the function that produces the event
    *
    * @return a [[FunctionOperation]] instance
    */
  def apply[E <: KompicsEvent](generator: Unit => E): Operation[E] = new FunctionOperation(generator);

  /**
    * Single parameter generator
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the parameter
    * @param generator the function that produces the event
    *
    * @return a [[FunctionOperation1]] instance
    */
  def apply[E <: KompicsEvent, N <: Number](generator: N => E): Operation1[E, N] = new FunctionOperation1(generator);

  /**
    * Two parameter generator
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first parameter
    * @tparam N2 the type of the second parameter
    * @param generator the function that produces the event
    *
    * @return a [[FunctionOperation2]] instance
    */
  def apply[E <: KompicsEvent, N <: Number, N2 <: Number](generator: (N, N2) => E): Operation2[E, N, N2] =
    new FunctionOperation2(generator);

  /**
    * Three parameter generator
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first parameter
    * @tparam N2 the type of the second parameter
    * @tparam N3 the type of the third parameter
    * @param generator the function that produces the event
    *
    * @return a [[FunctionOperation3]] instance
    */
  def apply[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number](
      generator: (N, N2, N3) => E
  ): Operation3[E, N, N2, N3] = new FunctionOperation3(generator);

  /**
    * Four parameter generator
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first parameter
    * @tparam N2 the type of the second parameter
    * @tparam N3 the type of the third parameter
    * @tparam N4 the type of the fourth parameter
    * @param generator the function that produces the event
    *
    * @return a [[FunctionOperation4]] instance
    */
  def apply[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number](
      generator: (N, N2, N3, N4) => E
  ): Operation4[E, N, N2, N3, N4] = new FunctionOperation4(generator);

  /**
    * Five parameter generator
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first parameter
    * @tparam N2 the type of the second parameter
    * @tparam N3 the type of the third parameter
    * @tparam N4 the type of the fourth parameter
    * @tparam N5 the type of the fifth parameter
    * @param generator the function that produces the event
    *
    * @return a [[FunctionOperation5]] instance
    */
  def apply[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number, N5 <: Number](
      generator: (N, N2, N3, N4, N5) => E
  ): Operation5[E, N, N2, N3, N4, N5] = new FunctionOperation5(generator);

}
