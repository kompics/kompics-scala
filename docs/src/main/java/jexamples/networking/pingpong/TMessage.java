package jexamples.networking.pingpong;

import se.sics.kompics.network.Address;
import se.sics.kompics.network.Header;
import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Transport;

public abstract class TMessage implements Msg<TAddress, THeader> {

  public final THeader header;

  public TMessage(TAddress src, TAddress dst, Transport protocol) {
    this.header = new THeader(src, dst, protocol);
  }

  @Override
  public THeader getHeader() {
    return this.header;
  }

  @SuppressWarnings("deprecation")
  @Override
  public TAddress getSource() {
    return this.header.src;
  }

  @SuppressWarnings("deprecation")
  @Override
  public TAddress getDestination() {
    return this.header.dst;
  }

  @SuppressWarnings("deprecation")
  @Override
  public Transport getProtocol() {
    return this.header.proto;
  }
}
