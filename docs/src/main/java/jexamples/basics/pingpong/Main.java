package jexamples.basics.pingpong;

import se.sics.kompics.Kompics;

public class Main {
  public static void main(String[] args) {
    Kompics.createAndStart(Parent.class);
    try {
      Thread.sleep(10_000);
      Kompics.shutdown();
    } catch (InterruptedException ex) {
      System.err.println(ex);
    }
  }
}
