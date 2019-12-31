package se.sics.test.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.Pinger;
import se.sics.test.TAddress;

public class PingerParent extends ComponentDefinition {

    Positive<Network> network = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);

    public PingerParent() {
        //create and connect all components except timer and network
        Class<? extends ComponentDefinition> p = Pinger.class;
        Component pinger = create(p, Init.NONE);

        //connect required internal components to network and timer
        connect(pinger.getNegative(Network.class), network, Channel.TWO_WAY);
        connect(pinger.getNegative(Timer.class), timer, Channel.TWO_WAY);
    }
}
