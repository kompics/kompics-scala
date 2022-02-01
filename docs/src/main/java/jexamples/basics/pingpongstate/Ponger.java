package jexamples.basics.pingpongstate;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Negative;
import se.sics.kompics.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// #header_and_port
// #header_only
public class Ponger extends ComponentDefinition {
  // #header_only

  Negative<PingPongPort> ppp = provides(PingPongPort.class);
  // #header_and_port

  private long counter = 0;

  Handler<Ping> pingHandler =
      new Handler<Ping>() {
        public void handle(Ping event) {
          counter++;
          logger.info("Got Ping #{}!", counter);
          trigger(new Pong(), ppp);
        }
      };

  {
    subscribe(pingHandler, ppp);
  }
  // #header_and_port
  // #header_only
}
// #header_only
// #header_and_port
