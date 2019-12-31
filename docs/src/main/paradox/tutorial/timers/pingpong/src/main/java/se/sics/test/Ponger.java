package se.sics.test;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Negative;
import se.sics.kompics.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ponger extends ComponentDefinition {

	private static final Logger LOG = LoggerFactory.getLogger(Ponger.class);

	Negative<PingPongPort> ppp = provides(PingPongPort.class);

	private long counter = 0;

	Handler<Ping> pingHandler = new Handler<Ping>(){
		public void handle(Ping event) {
			counter++;
			LOG.info("Got Ping #{}!", counter);
			answer(event, new Pong());
		}
	};
	{
		subscribe(pingHandler, ppp);
	}
}
