package se.sics.kompics.sl

import scala.language.implicitConversions
import se.sics.kompics.simulator.SimulationScenario
import se.sics.kompics.simulator.adaptor._
import se.sics.kompics.simulator.adaptor.distributions._
import se.sics.kompics.simulator.adaptor.distributions.extra._

/**
  * The Kompics Simulator DSL for Scala
  *
  * @example {{{
  * case object SimpleSimulation {
  *
  *   import Distributions._
  *   // needed for the distributions, but needs to be initialised after setting the seed
  *   implicit val random = JSimulationScenario.getRandom();
  *
  *   private def intToAddress(i: Int): Address = {
  *     try {
  *       new TAddress(new InetSocketAddress(InetAddress.getByName("192.193.0." + i), 10000));
  *     } catch {
  *       case ex: UnknownHostException => throw new RuntimeException(ex);
  *     }
  *   }
  *
  *   val startResultSetterOp = Op { (self: Integer) =>
  *     val selfAddr = intToAddress(self);
  *     StartNode(selfAddr, Init[ResultSetter](selfAddr))
  *   };
  *   val startPongerOp = Op { (self: Integer) =>
  *     val selfAddr = intToAddress(self)
  *     StartNode(selfAddr, Init[PongerParent](selfAddr))
  *   };
  *   val startPingerOp = Op { (self: Integer, ponger: Integer) =>
  *     val selfAddr = intToAddress(self);
  *     val pongerAddr = intToAddress(ponger);
  *     StartNode(selfAddr, Init[PingerParent](selfAddr, pongerAddr))
  *   };
  *
  *   val scenario = raise(5, startPongerOp, 1.toN)
  *     .arrival(constant(1000.millis))
  *     .andThen(1000.millis)
  *     .afterTermination(raise(5, startPingerOp, 6.toN, 1.toN).arrival(constant(1000.millis)))
  *     .inParallel(raise(1, startResultSetterOp, 1.toN).arrival(constant(1000.millis)))
  *     .andThen(10000.millis)
  *     .afterTermination(Terminate);
  * }
  * }}}
  */
package object simulator {

  /**
    * Allow the creation of [[se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution BasicIntSequentialDistribution]] via `i.toN()`
    *
    * @param i the starting point of the sequence
    */
  implicit class IntDistributions(val i: Int) {

    /**
      * Create a [[se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution BasicIntSequentialDistribution]] via `i.toN()`
      *
      * @return the new sequential distribution
      */
    def toN(): BasicIntSequentialDistribution = {
      new BasicIntSequentialDistribution(i)
    }
  }

  /**
    * Automatically convert a [[StochasticProcess]] to a [[StochasticProcessChain]]
    *
    * @param sp the starting process of the chain
    *
    * @return the new chain
    *
    * @see [[StochasticProcessChain!.startWith startWith]]
    */
  implicit def stochasticProcessToChain(sp: StochasticProcess): StochasticProcessChain = {
    val spc = new StochasticProcessChain();
    spc.startWith(sp);
    spc
  }

  /**
    * Automatically convert a [[StochasticProcessChain]] into a [[se.sics.kompics.simulator.SimulationScenario SimulationScenario]]
    *
    * @param spc the original process chain
    * @return the new simulation scenario
    *
    * @see [[StochasticProcessChain!.get get]]
    */
  implicit def chainToScenario(spc: StochasticProcessChain): SimulationScenario = spc.get()

  /**
    * Raise a number of events produced by an operation
    *
    * @tparam E the type of event to produce
    * @param count the number of events produce
    * @param op the operation that creates the events
    *
    * @return a new [[StochasticProcessBuilder]] with the raise operation appended
    */
  def raise[E <: KompicsEvent](count: Int, op: Operation[E]): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op);
  }

  /**
    * Raise a number of events produced by an operation
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first operation argument
    * @param count the number of events produce
    * @param op the operation that creates the events
    * @param d the distribution for the first operation argument
    *
    * @return a new [[StochasticProcessBuilder]] with the raise operation appended
    */
  def raise[E <: KompicsEvent, N <: Number](count: Int,
                                            op: Operation1[E, N],
                                            d: Distribution[N]): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op, d);
  }

  /**
    * Raise a number of events produced by an operation
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first operation argument
    * @tparam N2 the type of the second operation argument
    * @param count the number of events produce
    * @param op the operation that creates the events
    * @param d the distribution for the first operation argument
    * @param d2 the distribution for the second operation argument
    *
    * @return a new [[StochasticProcessBuilder]] with the raise operation appended
    */
  def raise[E <: KompicsEvent, N <: Number, N2 <: Number](count: Int,
                                                          op: Operation2[E, N, N2],
                                                          d: Distribution[N],
                                                          d2: Distribution[N2]): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op, d, d2);
  }

  /**
    * Raise a number of events produced by an operation
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first operation argument
    * @tparam N2 the type of the second operation argument
    * @tparam N3 the type of the third operation argument
    * @param count the number of events produce
    * @param op the operation that creates the events
    * @param d the distribution for the first operation argument
    * @param d2 the distribution for the second operation argument
    * @param d3 the distribution for the third operation argument
    *
    * @return a new [[StochasticProcessBuilder]] with the raise operation appended
    */
  def raise[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number](
      count: Int,
      op: Operation3[E, N, N2, N3],
      d: Distribution[N],
      d2: Distribution[N2],
      d3: Distribution[N3]
  ): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op, d, d2, d3);
  }

  /**
    * Raise a number of events produced by an operation
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first operation argument
    * @tparam N2 the type of the second operation argument
    * @tparam N3 the type of the third operation argument
    * @tparam N4 the type of the fourth operation argument
    * @param count the number of events produce
    * @param op the operation that creates the events
    * @param d the distribution for the first operation argument
    * @param d2 the distribution for the second operation argument
    * @param d3 the distribution for the third operation argument
    * @param d4 the distribution for the fourth operation argument
    *
    * @return a new [[StochasticProcessBuilder]] with the raise operation appended
    */
  def raise[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number](
      count: Int,
      op: Operation4[E, N, N2, N3, N4],
      d: Distribution[N],
      d2: Distribution[N2],
      d3: Distribution[N3],
      d4: Distribution[N4]
  ): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op, d, d2, d3, d4);
  }

  /**
    * Raise a number of events produced by an operation
    *
    * @tparam E the type of event to produce
    * @tparam N the type of the first operation argument
    * @tparam N2 the type of the second operation argument
    * @tparam N3 the type of the third operation argument
    * @tparam N4 the type of the fourth operation argument
    * @tparam N5 the type of the fifth operation argument
    * @param count the number of events produce
    * @param op the operation that creates the events
    * @param d the distribution for the first operation argument
    * @param d2 the distribution for the second operation argument
    * @param d3 the distribution for the third operation argument
    * @param d4 the distribution for the fourth operation argument
    * @param d5 the distribution for the fifth operation argument
    *
    * @return a new [[StochasticProcessBuilder]] with the raise operation appended
    */
  def raise[E <: KompicsEvent, N <: Number, N2 <: Number, N3 <: Number, N4 <: Number, N5 <: Number](
      count: Int,
      op: Operation5[E, N, N2, N3, N4, N5],
      d: Distribution[N],
      d2: Distribution[N2],
      d3: Distribution[N3],
      d4: Distribution[N4],
      d5: Distribution[N5]
  ): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op, d, d2, d3, d4, d5);
  }
}
