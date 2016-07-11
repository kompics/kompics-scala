package se.sics.kompics.sl

import scala.language.implicitConversions
import se.sics.kompics.KompicsEvent
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
    
    def raise[E <: KompicsEvent, N <: Number](count: Int, op: Operation1[E, N], d: Distribution[N]): StochasticProcessBuilder = {
        val sp = new StochasticProcessBuilder();
        sp.and(count, op, d);
    }
    
    def raise[E <: KompicsEvent, N <: Number, N2 <: Number](count: Int, op: Operation2[E, N, N2], d: Distribution[N], d2: Distribution[N2]): StochasticProcessBuilder = {
        val sp = new StochasticProcessBuilder();
        sp.and(count, op, d, d2);
    }
}