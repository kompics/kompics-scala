package jexamples.basics.pingpongdirect;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Negative;
import se.sics.kompics.Handler;

public class Ponger extends ComponentDefinition {

  Negative<PingPongPort> ppp = provides(PingPongPort.class);

  private long counter = 0;
// #ping-handler
  Handler<Ping> pingHandler =
      new Handler<Ping>() {
        public void handle(Ping event) {
          counter++;
          logger.info("Got Ping #{}!", counter);
          answer(event, new Pong());
        }
      };
// #ping-handler
  {
    subscribe(pingHandler, ppp);
  }
}
