package sexamples.networking.pingpongdistributed

import se.sics.kompics.sl._
import se.sics.kompics.network.Network

class Ponger(init: Init[Ponger]) extends ComponentDefinition {

  val Init(self: TAddress) = init;

  val net = requires[Network];

  private var counter: Long = 0L;

  net uponEvent {
    case ping: Ping => {
      counter += 1L;
      log.info(s"Got Ping #${counter}!");
      trigger(Pong(self, ping.getSource()) -> net);
    }
  }

}
