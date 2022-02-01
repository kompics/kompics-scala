package sexamples.simulation.pingpong

import se.sics.kompics.sl._
import se.sics.kompics.sl.simulator._
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import scala.concurrent.duration._
import java.net.InetAddress;

object ScenarioGen {
  import Distributions._
  // needed for the distributions, but needs to be initialised after setting the seed
  implicit val random = JSimulationScenario.getRandom();

  val PORT = 12345;
  val IP_PREFIX = "192.193.0.";

  def makeAddress(num: Int): TAddress = {
    TAddress(InetAddress.getByName(s"${IP_PREFIX}${num}"), PORT);
  }

  val startPongerOp = Op { (self: java.lang.Integer) =>
    StartNode[PongerParent, TAddress](makeAddress(self),
                                      Init.none[PongerParent],
                                      Map("pingpong.ponger.addr" -> makeAddress(self)))
  };
  val startPingerOp = Op { (self: java.lang.Integer, ponger: java.lang.Integer) =>
    StartNode[PingerParent, TAddress](makeAddress(self),
                                      Init.none[PingerParent],
                                      Map(
                                        "pingpong.pinger.addr" -> makeAddress(self),
                                        "pingpong.pinger.pongeraddr" -> makeAddress(ponger)
                                      ))
  };

  val scenario = raise(5, startPongerOp, 1.toN)
    .arrival(constant(1.second))
    .andThen(1.second)
    .afterTermination(
      raise(5, startPingerOp, 6.toN, 1.toN).arrival(constant(1.second))
    )
    .andThen(10.seconds)
    .afterTermination(Terminate);
}
