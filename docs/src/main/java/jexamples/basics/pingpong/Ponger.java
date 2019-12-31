package jexamples.basics.pingpong;

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

  Handler<Ping> pingHandler =
      new Handler<Ping>() {
        public void handle(Ping event) {
          logger.info("Got Ping!");
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
