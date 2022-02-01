package jexamples.simulation.pingpongglobal;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Transport;

public class TMessage
    implements Msg<TAddress, THeader>, PatternExtractor<Class<Object>, KompicsEvent> {

  public final THeader header;
  public final KompicsEvent payload;

  public TMessage(TAddress src, TAddress dst, Transport protocol, KompicsEvent payload) {
    this.header = new THeader(src, dst, protocol);
    this.payload = payload;
  }

  TMessage(THeader header, KompicsEvent payload) {
    this.header = header;
    this.payload = payload;
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

  @SuppressWarnings("unchecked")
  @Override
  public Class<Object> extractPattern() {
    Class c = payload.getClass();
    return (Class<Object>) c;
  }

  @Override
  public KompicsEvent extractValue() {
    return payload;
  }
}
