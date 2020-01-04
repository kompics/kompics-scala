package se.sics.test;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;

public class Ponger extends ComponentDefinition {
  public static int pingsReceived = 0;
  Negative<PingPongPort> ppp = provides(PingPongPort.class);
  Handler<Ping> pingHandler = new Handler<Ping>() {
    public void handle(Ping ping) {
      pingsReceived++;
      if (ping.id == 0) {
        trigger(new Pong(1), ppp);
        trigger(new Pong(2), ppp);
      } else {
        // echo the id
        trigger(new Pong(ping.id), ppp);
      }
    }
  };
  {
    subscribe(pingHandler, ppp);
  }
}