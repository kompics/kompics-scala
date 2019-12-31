package se.sics.test;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class PingerParent extends ComponentDefinition {

    public PingerParent() {
        TAddress self = config().getValue("pingpong.self", TAddress.class);
        TAddress ponger = config().getValue("pingpong.pinger.pongeraddr", TAddress.class);

        Component timer = create(JavaTimer.class, Init.NONE);
        Component network = create(NettyNetwork.class, new NettyInit(self));
        Component pinger = create(Pinger.class, Init.NONE);

        connect(pinger.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);

        connect(pinger.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);

    }

}
