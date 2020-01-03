package sexamples.simulation.basic

import se.sics.kompics.sl._
import se.sics.kompics.sl.simulator._
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import scala.concurrent.duration._

case class SimpleEvent(num: Long = 0L) extends KompicsEvent;

object BasicSimulation {
  import Distributions._
  // needed for the distributions, but needs to be initialised after setting the seed
  implicit val random = JSimulationScenario.getRandom();

  // #gen1
  val simpleEventGen1 = Op { (_: Unit) =>
    SimpleEvent()
  };
  // #gen1

  // #gen2
  val simpleEventGen2 = Op { (param: java.lang.Long) =>
    SimpleEvent(param)
  };
  // #gen2

  val _ignoreThisIsJustForDocs = {
    // #gen1
    /* ... */
    raise(1000, simpleEventGen1).arrival(constant(2.seconds))
    // #gen1
    // #gen2
    /* ... */
    raise(1000, simpleEventGen2, uniform(1000L, 2000L)).arrival(constant(2.seconds))
    // #gen2
    ()
  }

  // #scenario
  val scenario = raise(1000, simpleEventGen1)
    .arrival(constant(2.seconds))
    .andThen(1.second)
    .afterTermination(
      raise(1000, simpleEventGen2, uniform(1000L, 2000L))
        .arrival(constant(2.seconds))
    )
    .andThen(2.seconds)
    .afterTermination(Terminate);
  // #scenario
}
