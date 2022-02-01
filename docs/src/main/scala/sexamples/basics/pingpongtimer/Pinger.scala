package sexamples.basics.pingpongtimer

import se.sics.kompics.sl._
import se.sics.kompics.timer.{CancelPeriodicTimeout, SchedulePeriodicTimeout, Timeout, Timer}

import java.util.UUID

object Pinger {
  class PingTimeout(_spt: SchedulePeriodicTimeout) extends Timeout(_spt);
}
class Pinger extends ComponentDefinition {
  import Pinger.PingTimeout;

  val ppp = requires(PingPongPort);
  val timer = requires[Timer];

  private var counter: Long = 0L;
  private var timerId: Option[UUID] = None;

  ctrl uponEvent {
    case _: Start => {
      val spt = new SchedulePeriodicTimeout(0, 1000);
      val timeout = new PingTimeout(spt);
      spt.setTimeoutEvent(timeout);
      trigger(spt -> timer);
      timerId = Some(timeout.getTimeoutId());
    }
  }

  ppp uponEvent {
    case Pong => {
      counter += 1L;
      log.info(s"Got Pong #${counter}!");
      if (counter > 10) {
        Kompics.asyncShutdown();
      }
    }
  }

  timer uponEvent {
    case _: PingTimeout => {
      trigger(Ping() -> ppp);
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
