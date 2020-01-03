package sexamples.virtualnetworking.pingpongvirtual

import com.google.common.primitives.Ints;
import se.sics.kompics.sl._
import se.sics.kompics.Channel;
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}
import se.sics.kompics.network.virtual.VirtualNetworkChannel

// #pinger-parent
class PingerParent extends ComponentDefinition {

  val baseSelf = cfg.getValue[TAddress]("pingpong.pinger.addr");

  val timer = create[JavaTimer];
  val network = create[NettyNetwork](new NettyInit(baseSelf));

  val vnc = VirtualNetworkChannel.connect(network.getPositive(classOf[Network]), this.proxy);

  val num = cfg.getValue[Int]("pingpong.pinger.num");
  for (i <- 0 until num) {
    val id = Ints.toByteArray(i);
    val cbuild = this.config().modify(this.id());
    cbuild.setValue("pingpong.pinger.addr", baseSelf.copy(id = Some(id)));
    val pinger = create[Pinger](Init.none[Pinger], cbuild.finalise());

    connect[Timer](timer -> pinger);

    vnc.addConnection(id, pinger.getNegative(classOf[Network]));
  }
}
// #pinger-parent

// #ponger-parent
class PongerParent extends ComponentDefinition {

  val self = cfg.getValue[TAddress]("pingpong.ponger.addr");

  val network = create[NettyNetwork](new NettyInit(self));
  val ponger = create[Ponger];

  connect[Network](network -> ponger);
}
// #ponger-parent
