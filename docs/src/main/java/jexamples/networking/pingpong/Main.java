package jexamples.networking.pingpong;

import se.sics.kompics.Kompics;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
  public static void main(String[] args) throws InterruptedException, UnknownHostException {
    InetAddress ip = InetAddress.getLocalHost();
    int port = Integer.parseInt(args[0]);
    TAddress self = new TAddress(ip, port);
    Kompics.createAndStart(Parent.class, new Parent.Init(self), 2);
    Kompics.waitForTermination();
  }
}
