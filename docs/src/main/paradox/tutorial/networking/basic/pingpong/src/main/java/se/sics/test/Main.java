package se.sics.test;

import se.sics.kompics.Kompics;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
	public static void main(String[] args) {
		try {
			InetAddress ip = InetAddress.getLocalHost();
			int port = Integer.parseInt(args[0]);
			TAddress self = new TAddress(ip, port);
			Kompics.createAndStart(Parent.class, new Parent.Init(self), 2);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				System.exit(1);
			}
			Kompics.shutdown();
			System.exit(0);
		} catch (UnknownHostException ex) {
			System.err.println(ex);
			System.exit(1);
		}
	}
}