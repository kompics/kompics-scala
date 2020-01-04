package jexamples.simulation.pingpong;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class PongerParent extends ComponentDefinition {

  Positive<Network> network = requires(Network.class);
  Positive<Timer> timer = requires(Timer.class);

  public PongerParent() {
    // create all components except timer and network
    Component ponger = create(Ponger.class, Init.NONE);

    // connect required internal components to network and timer
    connect(ponger.getNegative(Network.class), network, Channel.TWO_WAY);
  }
}
