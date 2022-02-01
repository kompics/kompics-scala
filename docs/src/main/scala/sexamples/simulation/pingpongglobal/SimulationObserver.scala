package sexamples.simulation.pingpongglobal

import se.sics.kompics.sl._
import se.sics.kompics.simulator.util.GlobalView
import se.sics.kompics.network.Network
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}

import java.util.UUID

object SimulationObserver {
  class CheckTimeout(_spt: SchedulePeriodicTimeout) extends Timeout(_spt);
}
class SimulationObserver(init: Init[SimulationObserver]) extends ComponentDefinition {
  import SimulationObserver._;

  val timer = requires[Timer];
  val network = requires[Network];

  val Init(minPings: Int, minDeadNodes: Int) = init;

  private var timerId: Option[UUID] = None;

  ctrl uponEvent {
    case _: Start => {
      val period = cfg.getValue[Long]("pingpong.simulation.checktimeout");
      val spt = new SchedulePeriodicTimeout(period, period);
      val timeout = new CheckTimeout(spt);
      spt.setTimeoutEvent(timeout);
      trigger(spt -> timer);
      timerId = Some(timeout.getTimeoutId());
    }
  }

  timer uponEvent {
    case _: CheckTimeout => {
      val gv = cfg.getValue[GlobalView]("simulation.globalview");

      // Java API only :(
      if (gv.getValue("simulation.pongs", classOf[Integer]) > minPings) {
        log.info(s"Terminating simulation as the minimum pings=$minPings is achieved");
        gv.terminate();
      }
      if (gv.getDeadNodes().size() > minDeadNodes) {
        log.info(s"Terminating simulation as the min dead nodes=$minDeadNodes is achieved");
        gv.terminate();
      }
    }
  }

  override def tearDown(): Unit = {
    timerId match {
      case Some(id) => {
        trigger(new CancelPeriodicTimeout(id) -> timer);
      }
      case None => () // nothing to cancel
    }

  }

}
