package se.sics.test.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.test.Ponger;
import se.sics.test.TAddress;

public class PongerParent extends ComponentDefinition {

    Positive<Network> network = requires(Network.class);
    Positive<Timer> timer = requires(Timer.class);
    
    public PongerParent(Init init) {
        //create and connect all components except timer and network
        Component ponger = create(Ponger.class, new Ponger.Init(init.self));

        //connect required internal components to network and timer
        connect(ponger.getNegative(Network.class), network, Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<PongerParent> {

        public final TAddress self;

        public Init(TAddress self) {
            this.self = self;
        }
    }
}