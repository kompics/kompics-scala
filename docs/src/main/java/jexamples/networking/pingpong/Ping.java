package jexamples.networking.pingpong;

import se.sics.kompics.network.Transport;

public class Ping extends TMessage {
  public Ping(TAddress src, TAddress dst) {
    super(src, dst, Transport.TCP);
  }
}
