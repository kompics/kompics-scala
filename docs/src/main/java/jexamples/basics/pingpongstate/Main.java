package jexamples.basics.pingpongstate;

import se.sics.kompics.Kompics;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    Kompics.createAndStart(Parent.class, 2);
    Kompics.waitForTermination();
  }
}
