package sexamples.virtualnetworking.pingpongselectors

import com.google.common.primitives.Ints;
import se.sics.kompics.sl._
import se.sics.kompics.Channel;
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}

// #pinger-parent
class PingerParent extends ComponentDefinition {

  val baseSelf = cfg.getValue[TAddress]("pingpong.pinger.addr");

  val timer = create[JavaTimer];
  val network = create[NettyNetwork](new NettyInit(baseSelf));

  val num = cfg.getValue[Int]("pingpong.pinger.num");
  for (i <- 0 until num) {
    val id = Ints.toByteArray(i);
    val cbuild = this.config().modify(this.id());
    cbuild.setValue("pingpong.pinger.addr", baseSelf.copy(id = Some(id)));
    val pinger = create[Pinger](Init.none[Pinger], cbuild.finalise());

    connect[Timer](timer -> pinger);

    // must use the Java API here
    connect(pinger.getNegative(classOf[Network]),
            network.getPositive(classOf[Network]),
            new IdChannelSelector(id),
            Channel.TWO_WAY);
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
