package se.sics.test;

import se.sics.kompics.Kompics;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		Kompics.createAndStart(HelloComponent.class);
		Kompics.waitForTermination();
	}
}