package sexamples.networking.pingpong

import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}

class Parent(init: Init[Parent]) extends ComponentDefinition {

  val Init(self: TAddress) = init;

  val timer = create[JavaTimer];
  val network = create[NettyNetwork](new NettyInit(self));
  val pinger = create[Pinger](Init[Pinger](self));
  val pinger2 = create[Pinger](Init[Pinger](self));
  val ponger = create[Ponger](Init[Ponger](self));

  connect[Timer](timer -> pinger);
  connect[Timer](timer -> pinger2);

  connect[Network](network -> pinger);
  connect[Network](network -> pinger2);
  connect[Network](network -> ponger);
}
