package jexamples.basics.pingpong;

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

  Handler<Start> startHandler =
      new Handler<Start>() {
        public void handle(Start event) {
          trigger(new Ping(), ppp);
        }
      };
  Handler<Pong> pongHandler =
      new Handler<Pong>() {
        public void handle(Pong event) {
          logger.info("Got Pong!");
          trigger(new Ping(), ppp);
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
