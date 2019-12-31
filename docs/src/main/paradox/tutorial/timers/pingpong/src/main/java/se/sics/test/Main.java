package se.sics.test;

import se.sics.kompics.Kompics;

public class Main {
	public static void main(String[] args) {
		Kompics.createAndStart(Parent.class, 2);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ex) {
			System.exit(1);
		}
		Kompics.shutdown();
		System.exit(0);
	}
}