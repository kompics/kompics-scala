package jexamples.basics.pingpongdirect;

import se.sics.kompics.Channel;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Component;
import se.sics.kompics.Init;

public class Parent extends ComponentDefinition {
  Component pinger = create(Pinger.class, Init.NONE);
  Component ponger = create(Ponger.class, Init.NONE);
  Component pinger2 = create(Pinger.class, Init.NONE);

  {
    connect(
        pinger.getNegative(PingPongPort.class),
        ponger.getPositive(PingPongPort.class),
        Channel.TWO_WAY);
    connect(
        pinger2.getNegative(PingPongPort.class),
        ponger.getPositive(PingPongPort.class),
        Channel.TWO_WAY);
  }
}
