package jexamples.simulation.pingpong;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class PingerParent extends ComponentDefinition {

  Positive<Network> network = requires(Network.class);
  Positive<Timer> timer = requires(Timer.class);

  public PingerParent() {
    // create all components except timer and network
    Component pinger = create(Pinger.class, Init.NONE);

    // connect required internal components to network and timer
    connect(pinger.getNegative(Timer.class), timer, Channel.TWO_WAY);
    connect(pinger.getNegative(Network.class), network, Channel.TWO_WAY);
  }
}
