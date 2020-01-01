package sexamples.networking.pingpongdistributed

import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}

// #pinger-parent
class PingerParent(init: Init[PingerParent]) extends ComponentDefinition {

  val Init(self: TAddress, ponger: TAddress) = init;

  val timer = create[JavaTimer];
  val network = create[NettyNetwork](new NettyInit(self));
  val pinger = create[Pinger](Init[Pinger](self, ponger));

  connect[Timer](timer -> pinger);

  connect[Network](network -> pinger);
}
// #pinger-parent

// #ponger-parent
class PongerParent(init: Init[PongerParent]) extends ComponentDefinition {

  val Init(self: TAddress) = init;

  val network = create[NettyNetwork](new NettyInit(self));
  val ponger = create[Ponger](Init[Ponger](self));

  connect[Network](network -> ponger);
}
// #ponger-parent
