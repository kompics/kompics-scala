package sexamples.simulation.pingpongglobal

import se.sics.kompics.sl._
import se.sics.kompics.sl.simulator._
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import se.sics.kompics.simulator.util.GlobalView
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

  // #setup
  val setupOp = Op { (_: Unit) =>
    Setup { (gv: GlobalView) =>
      gv.setValue("simulation.pongs", 0)
    }.build()
  };
  // #setup

  val startObserverOp = Op { (_: Unit) =>
    StartNode[SimulationObserver, Long](makeAddress(0),
                                        Init[SimulationObserver](100, 2),
                                        Map("pingpong.simulation.checktimeout" -> 2000))
  };

  val startPongerOp = Op { (self: java.lang.Integer) =>
    StartNode[PongerParent, TAddress](makeAddress(self),
                                      Init.none[PongerParent],
                                      Map("pingpong.ponger.addr" -> makeAddress(self)))
  };

  // #kill
  val killPongerOp = Op { (self: java.lang.Integer) =>
    KillNode(makeAddress(self))
  };
  // #kill

  val startPingerOp = Op { (self: java.lang.Integer, ponger: java.lang.Integer) =>
    StartNode[PingerParent, TAddress](makeAddress(self),
                                      Init.none[PingerParent],
                                      Map(
                                        "pingpong.pinger.addr" -> makeAddress(self),
                                        "pingpong.pinger.pongeraddr" -> makeAddress(ponger)
                                      ))
  };
  // #simple-ping
  val simplePingScenario = raise(1, setupOp)
    .arrival(constant(0.seconds))
    .andThen(0.seconds)
    .afterTermination(
      raise(1, startObserverOp).arrival(constant(0.seconds))
    )
    .andThen(1.second)
    .afterTermination(
      raise(5, startPongerOp, 1.toN)
        .arrival(constant(1.second))
    )
    .andThen(1.second)
    .afterTermination(
      raise(5, startPingerOp, 6.toN, 1.toN).arrival(constant(1.second))
    )
    .andThen(10000.seconds)
    .afterTermination(Terminate);
  // #simple-ping

  // #kill-pongers
  val killPongersScenario = raise(1, setupOp)
    .arrival(constant(0.seconds))
    .andThen(0.seconds)
    .afterTermination(
      raise(1, startObserverOp).arrival(constant(0.seconds))
    )
    .andThen(1.second)
    .afterTermination(
      raise(5, startPongerOp, 1.toN)
        .arrival(constant(1.second))
    )
    .andThen(1.second)
    .afterTermination(
      raise(5, startPingerOp, 6.toN, 1.toN).arrival(constant(1.second))
    )
    .andThen(1.second)
    .afterTermination(
      raise(5, killPongerOp, 1.toN).arrival(constant(0.seconds))
    )
    .andThen(10000.seconds)
    .afterTermination(Terminate);
  // #kill-pongers
}
