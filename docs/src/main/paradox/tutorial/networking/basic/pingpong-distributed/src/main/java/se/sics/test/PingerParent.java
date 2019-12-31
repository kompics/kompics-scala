package se.sics.test;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class PingerParent extends ComponentDefinition {

    public PingerParent(Init init) {
        Component timer = create(JavaTimer.class, Init.NONE);
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component pinger = create(Pinger.class, new Pinger.Init(init.self, init.ponger));

        connect(pinger.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);

        connect(pinger.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
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
