package jexamples.virtualnetworking.pingpongselectors;

import java.util.Optional;
import io.netty.buffer.ByteBuf;
import se.sics.kompics.network.netty.serialization.Serializer;

public class PingPongSerializer implements Serializer {

  private static final byte PING = 1;
  private static final byte PONG = 2;

  @Override
  public int identifier() {
    return 200;
  }

  @Override
  public void toBinary(Object o, ByteBuf buf) {
    if (o instanceof Ping) {
      buf.writeByte(PING); // 1 byte
      // total 1 bytes
    } else if (o instanceof Pong) {
      buf.writeByte(PONG); // 1 byte
      // total 1 bytes
    }
  }

  @Override
  public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
    byte type = buf.readByte(); // 1 byte
    switch (type) {
      case PING:
        return Ping.EVENT; // 1 bytes total, check
      case PONG:
        return Pong.EVENT; // 1 bytes total, check
    }
    return null;
  }
}
