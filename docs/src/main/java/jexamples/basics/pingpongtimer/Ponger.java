package jexamples.basics.pingpongtimer;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Negative;
import se.sics.kompics.Handler;

public class Ponger extends ComponentDefinition {

  Negative<PingPongPort> ppp = provides(PingPongPort.class);

  private long counter = 0;

  Handler<Ping> pingHandler =
      new Handler<Ping>() {
        public void handle(Ping event) {
          counter++;
          logger.info("Got Ping #{}!", counter);
          answer(event, new Pong());
        }
      };

  {
    subscribe(pingHandler, ppp);
  }
}
