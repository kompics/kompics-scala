package jexamples.networking.pingpong;

import se.sics.kompics.network.Transport;

public class Pong extends TMessage {
  public Pong(TAddress src, TAddress dst) {
    super(src, dst, Transport.TCP);
  }
}
