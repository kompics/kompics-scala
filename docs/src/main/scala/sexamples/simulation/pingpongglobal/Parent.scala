package sexamples.simulation.pingpongglobal

import se.sics.kompics.sl._
import se.sics.kompics.timer.Timer
import se.sics.kompics.timer.java.JavaTimer
import se.sics.kompics.network.Network
import se.sics.kompics.network.netty.{NettyInit, NettyNetwork}

// #pinger-host
class PingerHost extends ComponentDefinition {
  val self = cfg.getValue[TAddress]("pingpong.pinger.addr");

  val timer = create[JavaTimer];
  val network = create[NettyNetwork](new NettyInit(self));

  val pingerParent = create[PingerParent];

  connect[Timer](timer -> pingerParent);
  connect[Network](network -> pingerParent);

}
// #pinger-host

// #ponger-host
class PongerHost extends ComponentDefinition {
  val self = cfg.getValue[TAddress]("pingpong.ponger.addr");

  val timer = create[JavaTimer];
  val network = create[NettyNetwork](new NettyInit(self));

  val pongerParent = create[PongerParent];

  connect[Timer](timer -> pongerParent);
  connect[Network](network -> pongerParent);

}
// #ponger-host

// #pinger-parent
class PingerParent extends ComponentDefinition {

  val timer = requires[Timer];
  val network = requires[Network];

  val pinger = create[Pinger];

  connect[Timer](timer -> pinger);
  connect[Network](network -> pinger);
}
// #pinger-parent

// #ponger-parent
class PongerParent extends ComponentDefinition {

  val timer = requires[Timer];
  val network = requires[Network];

  val ponger = create[Ponger];

  connect[Network](network -> ponger);
}
// #ponger-parent
