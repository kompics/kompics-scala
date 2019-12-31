package se.sics.test;

import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;

public class PongerParent extends ComponentDefinition {

    public PongerParent() {
            TAddress self = config().getValue("pingpong.self", TAddress.class);
            Component network = create(NettyNetwork.class, new NettyInit(self));
            Component ponger = create(Ponger.class, Init.NONE);

            connect(ponger.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
    }

}
