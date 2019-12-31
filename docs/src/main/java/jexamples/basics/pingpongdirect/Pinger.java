package jexamples.basics.pingpongdirect;

import se.sics.kompics.Kompics;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.Handler;
import se.sics.kompics.Start;

public class Pinger extends ComponentDefinition {

  Positive<PingPongPort> ppp = requires(PingPongPort.class);

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
          if (counter < 10) {
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
}
