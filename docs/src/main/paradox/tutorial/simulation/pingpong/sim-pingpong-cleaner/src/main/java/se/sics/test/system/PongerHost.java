package se.sics.test.system;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.test.TAddress;

public class PongerHost extends ComponentDefinition {
    
    public PongerHost(Init init) {
        TAddress self = config().getValue("pingpong.self", TAddress.class);
        
        Component network = create(NettyNetwork.class, new NettyInit(self));
        Component timer = create(JavaTimer.class, Init.NONE);
        Component pongerParent = create(PongerParent.class, Init.NONE);
        
        connect(pongerParent.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
        connect(pongerParent.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);
    }
}
