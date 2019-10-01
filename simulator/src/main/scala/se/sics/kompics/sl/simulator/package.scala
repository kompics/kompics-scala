package se.sics.kompics.sl

import scala.language.implicitConversions
import se.sics.kompics.simulator.SimulationScenario
import se.sics.kompics.simulator.adaptor._
import se.sics.kompics.simulator.adaptor.distributions._
import se.sics.kompics.simulator.adaptor.distributions.extra._

package object simulator {

  class IntDistributions(val i: Int) extends AnyVal {
    def toN(): BasicIntSequentialDistribution = {
      new BasicIntSequentialDistribution(i)
    }
  }

  implicit def intToDistribution(i: Int): IntDistributions = new IntDistributions(i);

  implicit def stochasticProcessToChain(sp: StochasticProcess): StochasticProcessChain = {
    val spc = new StochasticProcessChain();
    spc.startWith(sp);
    spc
  }

  implicit def chainToScenario(spc: StochasticProcessChain): SimulationScenario = spc.get()

  def raise[E <: KompicsEvent](count: Int, op: Operation[E]): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op);
  }

  def raise[E <: KompicsEvent, N <: Number](count: Int,
                                            op: Operation1[E, N],
                                            d: Distribution[N]): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op, d);
  }

  def raise[E <: KompicsEvent, N <: Number, N2 <: Number](count: Int,
                                                          op: Operation2[E, N, N2],
                                                          d: Distribution[N],
                                                          d2: Distribution[N2]): StochasticProcessBuilder = {
    val sp = new StochasticProcessBuilder();
    sp.and(count, op, d, d2);
  }

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
