package se.sics.test;

import com.google.common.primitives.Ints;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.config.Config;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class PingerParent extends ComponentDefinition {

    public PingerParent() {
        TAddress baseSelf = config().getValue("pingpong.self", TAddress.class);

        Component timer = create(JavaTimer.class, Init.NONE);
        Component network = create(NettyNetwork.class, new NettyInit(baseSelf));
        int num = config().getValue("pingpong.pinger.num", Integer.class);
        for (int i = 0; i < num; i++) {
            byte[] id = Ints.toByteArray(i);
            Config.Builder cbuild = config().modify(id());
            cbuild.setValue("pingpong.self", baseSelf.withVirtual(id));
            Component pinger = create(Pinger.class, Init.NONE, cbuild.finalise());

            connect(pinger.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);

            connect(pinger.getNegative(Network.class), network.getPositive(Network.class), 
                    new IdChannelSelector(id), Channel.TWO_WAY);
        }

    }

}
