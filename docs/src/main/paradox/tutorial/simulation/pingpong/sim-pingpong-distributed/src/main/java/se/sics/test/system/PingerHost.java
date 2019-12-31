package se.sics.test.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.test.TAddress;

public class PingerHost extends ComponentDefinition {
    public PingerHost(Init init) {
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component timer = create(JavaTimer.class, Init.NONE);
        Component pingerParent = create(PingerParent.class, new PingerParent.Init(init.self, init.ponger));
        
        connect(pingerParent.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
        connect(pingerParent.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);
    }
    
    public static class Init extends se.sics.kompics.Init<PingerHost> {

        public final TAddress self;
        public final TAddress ponger;

        public Init(TAddress self, TAddress ponger) {
            this.self = self;
            this.ponger = ponger;
        }
    }
}
