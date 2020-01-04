package jexamples.simulation.pingpong;

import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.Handler;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;

public class Ponger extends ComponentDefinition {

  Positive<Network> net = requires(Network.class);

  private long counter = 0;
  private final TAddress self;

  public Ponger() {
    this.self = config().getValue("pingpong.ponger.addr", TAddress.class);
  }

  ClassMatchedHandler<Ping, TMessage> pingHandler =
      new ClassMatchedHandler<Ping, TMessage>() {
        @Override
        public void handle(Ping content, TMessage context) {
          counter++;
          logger.info("Got Ping #{}!", counter);
          trigger(new TMessage(self, context.getSource(), Transport.TCP, Pong.EVENT), net);
        }
      };

  {
    subscribe(pingHandler, net);
  }
}
