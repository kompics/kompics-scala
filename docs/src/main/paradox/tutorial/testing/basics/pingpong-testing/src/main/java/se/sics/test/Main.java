package se.sics.test;

import se.sics.kompics.Component;
import se.sics.kompics.Positive;
import se.sics.kompics.testing.Direction;
import se.sics.kompics.testing.TestContext;
import static org.junit.Assert.assertTrue;

public class Main {
  public static void main(String[] args) { // basicsExample
    TestContext<Ponger> tc = TestContext.newInstance(Ponger.class);
    Component ponger = tc.getComponentUnderTest();
    Component pinger = tc.create(Pinger.class);
    Positive<PingPongPort> pongerPort = ponger.getPositive( PingPongPort.class);
    tc.connect(pongerPort, pinger.getNegative(PingPongPort.class));
    tc.setComparator(Ping.class, Ping.comparator);
    tc.setComparator(Pong.class, Pong.comparator);
    // setup done
    tc.body()
        .expect(new Ping(8), pongerPort, Direction.IN)          // a
        .expect(new Pong(8), pongerPort, Direction.OUT)         // b
        .trigger(new Ping(0), pongerPort)
        .either()
            .expect(new Pong(1), pongerPort, Direction.OUT)     // c
            .expect(new Pong(2), pongerPort, Direction.OUT)     // d
        .or()
            .expect(new Pong(3), pongerPort, Direction.OUT)     // e
            .expect(new Pong(4), pongerPort, Direction.OUT)     // f
        .end();
    assertTrue(tc.check());
  }
}