package sexamples.basics.pingpongstate

import se.sics.kompics.sl._

// #header_and_port
// #header_only
class Pinger extends ComponentDefinition {
// #header_only

  val ppp = requires(PingPongPort);
// #header_and_port

  private var counter: Long = 0L;

  ctrl uponEvent {
    case _: Start => {
      trigger(Ping -> ppp);
    }
  }

  ppp uponEvent {
    case Pong => {
      counter += 1L;
      log.info(s"Got Pong #${counter}!");
      if (counter < 100L) {
        trigger(Ping -> ppp);
      } else {
        Kompics.asyncShutdown();
      }
    }
  }
// #header_and_port
// #header_only
}
// #header_only
// #header_and_port
