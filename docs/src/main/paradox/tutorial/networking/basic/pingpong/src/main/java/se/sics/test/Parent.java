package se.sics.test;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Component;
import se.sics.kompics.Init;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.kompics.network.netty.NettyInit;

public class Parent extends ComponentDefinition {
	public Parent(Init init) {
		Component timer = create(JavaTimer.class, Init.NONE);
		Component network = create(NettyNetwork.class, new NettyInit(init.self));
		Component pinger = create(Pinger.class, new Pinger.Init(init.self));
		Component ponger = create(Ponger.class, new Ponger.Init(init.self));
		Component pinger2 = create(Pinger.class, new Pinger.Init(init.self));


		connect(pinger.getNegative(Timer.class), timer.getPositive(Timer.class));
		connect(pinger2.getNegative(Timer.class), timer.getPositive(Timer.class));

		connect(pinger.getNegative(Network.class), network.getPositive(Network.class));
		connect(pinger2.getNegative(Network.class), network.getPositive(Network.class));
		connect(ponger.getNegative(Network.class), network.getPositive(Network.class));
	}
	public static class Init extends se.sics.kompics.Init<Parent> {
		public final TAddress self;
		public Init(TAddress self) {
			this.self = self;
		}
	}
}

