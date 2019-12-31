package se.sics.test;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Component;
import se.sics.kompics.Init;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.timer.java.JavaTimer;

public class Parent extends ComponentDefinition {
	Component pinger = create(Pinger.class, Init.NONE);
	Component ponger = create(Ponger.class, Init.NONE);
	Component pinger2 = create(Pinger.class, Init.NONE);
	Component timer = create(JavaTimer.class, Init.NONE);

	{
		connect(pinger.getNegative(PingPongPort.class), ponger.getPositive(PingPongPort.class));
		connect(pinger.getNegative(Timer.class), timer.getPositive(Timer.class));
		connect(pinger2.getNegative(PingPongPort.class), ponger.getPositive(PingPongPort.class));
		connect(pinger2.getNegative(Timer.class), timer.getPositive(Timer.class));
	}
}

