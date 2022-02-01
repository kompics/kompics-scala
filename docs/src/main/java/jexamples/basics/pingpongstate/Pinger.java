package jexamples.basics.pingpongstate;

import se.sics.kompics.Kompics;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.Handler;
import se.sics.kompics.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// #header_and_port
// #header_only
public class Pinger extends ComponentDefinition {
  // #header_only

  Positive<PingPongPort> ppp = requires(PingPongPort.class);
  // #header_and_port
  private long counter = 0;

  Handler<Start> startHandler =
      new Handler<Start>() {
        public void handle(Start event) {
          trigger(new Ping(), ppp);
        }
      };
  Handler<Pong> pongHandler =
      new Handler<Pong>() {
        public void handle(Pong event) {
          counter++;
          logger.info("Got Pong #{}!", counter);
          if (counter < 100) {
            trigger(new Ping(), ppp);
          } else {
            Kompics.asyncShutdown();
          }
        }
      };

  {
    subscribe(startHandler, control);
    subscribe(pongHandler, ppp);
  }
  // #header_and_port
  // #header_only
}
// #header_only
// #header_and_port
