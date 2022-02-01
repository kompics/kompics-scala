package sexamples.basics.pingpongstate

import se.sics.kompics.sl._

// #header_and_port
// #header_only
class Ponger extends ComponentDefinition {
// #header_only

  val ppp = provides(PingPongPort);
// #header_and_port

  private var counter: Long = 0L;

  ppp uponEvent {
    case Ping => {
      counter += 1L;
      log.info(s"Got Ping #${counter}!");
      trigger(Pong -> ppp);
    }
  }
// #header_and_port
// #header_only
}
// #header_only
// #header_and_port
