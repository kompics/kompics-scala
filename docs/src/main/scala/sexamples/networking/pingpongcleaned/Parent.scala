package sexamples.networking.pingpongcleaned

import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}

// #pinger-parent
class PingerParent extends ComponentDefinition {

  val self = cfg.getValue[TAddress]("pingpong.pinger.addr");

  val timer = create[JavaTimer];
  val network = create[NettyNetwork](new NettyInit(self));
  val pinger = create[Pinger];

  connect[Timer](timer -> pinger);

  connect[Network](network -> pinger);
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
