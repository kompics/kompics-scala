package jexamples.basics.pingpongdirect;

import se.sics.kompics.Kompics;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Kompics.createAndStart(Parent.class, 3);
    Kompics.waitForTermination();
  }
}
