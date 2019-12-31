package sexamples.basics.pingpong;

import se.sics.kompics.sl._

// #header_and_port
// #header_only
class Pinger extends ComponentDefinition {
// #header_only

  val ppp = requires(PingPongPort);
// #header_and_port

  ctrl uponEvent {
    case _: Start => {
      trigger(Ping -> ppp);
    }
  }

  ppp uponEvent {
    case Pong => {
      log.info(s"Got Pong!");
      trigger(Ping -> ppp);
    }
  }
// #header_and_port
// #header_only
}
// #header_only
// #header_and_port
