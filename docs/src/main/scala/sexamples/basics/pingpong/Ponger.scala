package sexamples.basics.pingpong;

import se.sics.kompics.sl._

// #header_and_port
// #header_only
class Ponger extends ComponentDefinition {
// #header_only

  val ppp = provides(PingPongPort);
// #header_and_port

  ppp uponEvent {
    case Ping => {
      log.info(s"Got Ping!");
      trigger(Pong -> ppp);
    }
  }
// #header_and_port
// #header_only
}
// #header_only
// #header_and_port
