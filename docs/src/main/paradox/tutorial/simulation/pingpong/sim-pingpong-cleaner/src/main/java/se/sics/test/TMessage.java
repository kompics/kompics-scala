package se.sics.test;

import se.sics.kompics.KompicsEvent;
import se.sics.kompics.PatternExtractor;
import se.sics.kompics.network.Msg;
import se.sics.kompics.network.Transport;

public class TMessage implements Msg<TAddress, THeader>, PatternExtractor<Class<Object>, KompicsEvent> {

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

    @Override
    public TAddress getSource() {
        return this.header.src;
    }

    @Override
    public TAddress getDestination() {
        return this.header.dst;
    }

    @Override
    public Transport getProtocol() {
        return this.header.proto;
    }

    @Override
    public Class extractPattern() {
        Class c = payload.getClass();
        return (Class<Object>) c;
    }

    @Override
    public KompicsEvent extractValue() {
        return payload;
    }

}
