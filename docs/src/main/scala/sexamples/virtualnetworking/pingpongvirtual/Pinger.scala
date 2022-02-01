package sexamples.virtualnetworking.pingpongvirtual

import se.sics.kompics.sl._
import se.sics.kompics.network.Network
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}

import java.util.UUID

object Pinger {
  class PingTimeout(_spt: SchedulePeriodicTimeout) extends Timeout(_spt);
}
class Pinger extends ComponentDefinition {
  import Pinger.PingTimeout;

  val self = cfg.getValue[TAddress]("pingpong.pinger.addr");
  val ponger = cfg.getValue[TAddress]("pingpong.pinger.pongeraddr");
  val timeoutPeriod = cfg.getValue[Long]("pingpong.pinger.timeout");

  val net = requires[Network];
  val timer = requires[Timer];

  private var counter: Long = 0L;
  private var timerId: Option[UUID] = None;

  ctrl uponEvent {
    case _: Start => {
      val spt = new SchedulePeriodicTimeout(0, timeoutPeriod);
      val timeout = new PingTimeout(spt);
      spt.setTimeoutEvent(timeout);
      trigger(spt -> timer);
      timerId = Some(timeout.getTimeoutId());
    }
  }

  net uponEvent {
    case TMessage(_, Pong) => {
      counter += 1L;
      log.info(s"Got Pong #${counter}!");
      if (counter > 10) {
        Kompics.asyncShutdown();
      }
    }
  }

  timer uponEvent {
    case _: PingTimeout => {
      trigger(TMessage(self, ponger, Ping) -> net);
    }
  }

  override def tearDown(): Unit = {
    timerId match {
      case Some(id) => {
        trigger(new CancelPeriodicTimeout(id) -> timer);
      }
      case None => () // no cleanup necessary
    }
  }

}
