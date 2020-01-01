package jexamples.networking.pingpongdistributed;

import java.util.Optional;
import io.netty.buffer.ByteBuf;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;

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
      Ping ping = (Ping) o;
      buf.writeByte(PING); // 1 byte
      Serializers.toBinary(ping.header, buf); // 1 byte serialiser id + 16 bytes THeader
      // total 18 bytes
    } else if (o instanceof Pong) {
      Pong pong = (Pong) o;
      buf.writeByte(PONG); // 1 byte
      Serializers.toBinary(pong.header, buf); // 1 byte serialiser id + 16 bytes THeader
      // total 18 bytes
    }
  }

  @Override
  public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
    byte type = buf.readByte(); // 1 byte
    switch (type) {
      case PING:
        {
          THeader header =
              (THeader)
                  Serializers.fromBinary(
                      buf, Optional.empty()); // 1 byte serialiser id + 16 bytes THeader
          return new Ping(header); // 18 bytes total, check
        }
      case PONG:
        {
          THeader header =
              (THeader)
                  Serializers.fromBinary(
                      buf, Optional.empty()); // 1 byte serialiser id + 16 bytes THeader
          return new Pong(header); // 18 bytes total, check
        }
    }
    return null;
  }
}
