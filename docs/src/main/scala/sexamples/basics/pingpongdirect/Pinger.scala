package sexamples.basics.pingpongdirect

import se.sics.kompics.sl._

class Pinger extends ComponentDefinition {

  val ppp = requires(PingPongPort)

  private var counter: Long = 0L;

  ctrl uponEvent {
    case _: Start => {
      trigger(Ping() -> ppp);
    }
  }

  ppp uponEvent {
    case Pong => {
      counter += 1L;
      log.info(s"Got Pong #${counter}!");
      if (counter < 10) {
        trigger(Ping() -> ppp);
      } else {
        Kompics.asyncShutdown();
      }
    }
  }
}
