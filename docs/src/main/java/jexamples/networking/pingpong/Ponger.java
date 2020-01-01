package jexamples.networking.pingpong;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.Handler;
import se.sics.kompics.network.Network;

public class Ponger extends ComponentDefinition {

  Positive<Network> net = requires(Network.class);

  private long counter = 0;
  private final TAddress self;

  public Ponger(Init init) {
    this.self = init.self;
  }

  Handler<Ping> pingHandler =
      new Handler<Ping>() {
        public void handle(Ping event) {
          counter++;
          logger.info("Got Ping #{}!", counter);
          trigger(new Pong(self, event.getSource()), net);
        }
      };

  {
    subscribe(pingHandler, net);
  }

  public static class Init extends se.sics.kompics.Init<Ponger> {
    public final TAddress self;

    public Init(TAddress self) {
      this.self = self;
    }
  }
}
