package se.sics.test;
import com.google.common.base.Function;
import se.sics.kompics.Component;
import se.sics.kompics.Negative;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Main {
  static Function<Ping, Pong> pingToPongMapper = new Function<Ping, Pong> () {
    public Pong apply(Ping ping) {
      return new Pong(ping.id);
    }
  };
  public static void main(String[] args) { // answerRequestExample
    TestContext<Pinger> tc = TestContext.newInstance(Pinger.class);
    Component pinger = tc.getComponentUnderTest();
    Negative<PingPongPort> pingerPort = pinger.getNegative( PingPongPort.class);
    tc.setComparator(Ping.class, Ping.comparator);
    // setup done
    tc.body()
        // treat ping(1) as a request and trigger pong(2) on the same port
        .answerRequest(Ping.class, pingerPort, pingToPongMapper, pingerPort)
        // treat ping(2) as a normal event
        .expect(Ping.class, pingerPort, Direction.OUT)
        // treat ping(3) as a request and trigger pong(3) on the same port
        .answerRequest(Ping.class, pingerPort, pingToPongMapper, pingerPort)
    ;
    assertTrue(tc.check());
    assertEquals(2, Pinger.pongsReceived);
  }
}