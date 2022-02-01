package jexamples.simulation.basic;

import se.sics.kompics.KompicsEvent;

public class SimpleEvent implements KompicsEvent {
  public final long num;

  public SimpleEvent() {
    this.num = 0L;
  }

  public SimpleEvent(long param) {
    this.num = param;
  }
}
