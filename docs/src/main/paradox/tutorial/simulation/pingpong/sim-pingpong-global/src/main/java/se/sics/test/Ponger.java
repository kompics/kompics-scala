package se.sics.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Positive;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;

public class Ponger extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(Ponger.class);

    Positive<Network> net = requires(Network.class);

    private long counter = 0;
    private final TAddress self;

    public Ponger() {
        this.self = config().getValue("pingpong.self", TAddress.class);
    }

    ClassMatchedHandler<Ping, TMessage> pingHandler = new ClassMatchedHandler<Ping, TMessage>() {
        @Override
        public void handle(Ping content, TMessage context) {
            counter++;
            LOG.info("{}Got Ping #{}! from:{}", new Object[]{self, counter, context.header.src});
            trigger(new TMessage(self, context.getSource(), Transport.TCP, new Pong()), net);
        }
    };

    {
        subscribe(pingHandler, net);
    }
}
