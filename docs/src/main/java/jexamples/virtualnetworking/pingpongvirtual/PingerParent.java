package jexamples.virtualnetworking.pingpongvirtual;

import com.google.common.primitives.Ints;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.config.Config;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.virtual.VirtualNetworkChannel;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class PingerParent extends ComponentDefinition {

  public PingerParent() {
    TAddress baseSelf = config().getValue("pingpong.pinger.addr", TAddress.class);

    Component timer = create(JavaTimer.class, Init.NONE);
    Component network = create(NettyNetwork.class, new NettyInit(baseSelf));

    VirtualNetworkChannel vnc =
        VirtualNetworkChannel.connect(network.getPositive(Network.class), this.proxy);

    int num = config().getValue("pingpong.pinger.num", Integer.class);
    for (int i = 0; i < num; i++) {
      byte[] id = Ints.toByteArray(i);
      Config.Builder cbuild = config().modify(this.id());
      cbuild.setValue("pingpong.pinger.addr", baseSelf.withVirtual(id));
      Component pinger = create(Pinger.class, Init.NONE, cbuild.finalise());

      connect(pinger.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);

      vnc.addConnection(id, pinger.getNegative(Network.class));
    }
  }
}
