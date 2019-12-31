package jexamples.basics.pingpong;

import se.sics.kompics.Channel;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Component;
import se.sics.kompics.Init;

// #create_only
public class Parent extends ComponentDefinition {
  Component pinger = create(Pinger.class, Init.NONE);
  Component ponger = create(Ponger.class, Init.NONE);
  // #create_only
  {
    connect(
        pinger.getNegative(PingPongPort.class),
        ponger.getPositive(PingPongPort.class),
        Channel.TWO_WAY);
  }
  // #create_only
}
// #create_only
