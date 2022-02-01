package jexamples.virtualnetworking.pingpongselectors;

import java.nio.ByteBuffer;
import se.sics.kompics.ChannelSelector;

public class IdChannelSelector extends ChannelSelector<TMessage, ByteBuffer> {

  public IdChannelSelector(byte[] id) {
    super(TMessage.class, ByteBuffer.wrap(id), true);
  }

  @Override
  public ByteBuffer getValue(TMessage event) {
    return ByteBuffer.wrap(event.header.dst.getId());
  }
}
