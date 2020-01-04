package sexamples.simulation.pingpongglobal

import se.sics.kompics.sl._
import se.sics.kompics.network.Network

class Ponger extends ComponentDefinition {

  val self = cfg.getValue[TAddress]("pingpong.ponger.addr");

  val net = requires[Network];

  private var counter: Long = 0L;

  net uponEvent {
    case TMessage(header, Ping) => {
      counter += 1L;
      log.info(s"Got Ping #${counter}!");
      trigger(TMessage(self, header.getSource(), Pong) -> net);
    }
  }
}
