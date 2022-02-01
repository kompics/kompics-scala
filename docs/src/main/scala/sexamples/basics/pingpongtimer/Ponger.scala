package sexamples.basics.pingpongtimer

import se.sics.kompics.sl._

class Ponger extends ComponentDefinition {

  val ppp = provides(PingPongPort);

  private var counter: Long = 0L;

  ppp uponEvent {
    case ping: Ping => {
      counter += 1L;
      log.info(s"Got Ping #${counter}!");
      answer(ping, Pong);
    }
  }

}
