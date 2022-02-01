package se.sics.test;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;

public class Pinger extends ComponentDefinition {
        public static int pongsReceived = 0;
        Positive<PingPongPort> ppp = requires(PingPongPort.class);
        Handler<Start> startHandler = new Handler<Start>(){
                public void handle(Start event) {
                  for (int i = 1; i <= 3; i++) {
                    trigger(new Ping(i), ppp);
                  }
                }
        };
        Handler<Pong> pongHandler = new Handler<Pong>() {
                public void handle(Pong pong) {
                  pongsReceived++;
                }
        };
        {
                subscribe(startHandler, control);
                subscribe(pongHandler, ppp);
        }
}