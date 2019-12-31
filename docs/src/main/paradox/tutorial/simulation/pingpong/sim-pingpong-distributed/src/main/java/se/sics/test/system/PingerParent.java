package se.sics.test.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.Pinger;
import se.sics.test.TAddress;

public class PingerParent extends ComponentDefinition {

    Positive<Network> network = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);

    public PingerParent(Init init) {
        //create and connect all components except timer and network
        Component pinger = create(Pinger.class, new Pinger.Init(init.self, init.ponger));

        //connect required internal components to network and timer
        connect(pinger.getNegative(Network.class), network, Channel.TWO_WAY);
        connect(pinger.getNegative(Timer.class), timer, Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<PingerParent> {

        public final TAddress self;
        public final TAddress ponger;

        public Init(TAddress self, TAddress ponger) {
            this.self = self;
            this.ponger = ponger;
        }
    }
}
