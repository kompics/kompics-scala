package sexamples.networking.pingpongdistributed

import io.netty.buffer.ByteBuf
import java.net.{InetAddress, UnknownHostException}
import java.util.Optional
import se.sics.kompics.network.Transport
import se.sics.kompics.network.netty.serialization.{Serializer, Serializers}

object PingPongSerializer extends Serializer {
  private val PING: Byte = 1;
  private val PONG: Byte = 2;

  private val NO_HINT: Optional[AnyRef] = Optional.empty();

  override def identifier(): Int = 200;

  override def toBinary(o: AnyRef, buf: ByteBuf): Unit = {
    o match {
      case ping: Ping => {
        buf.writeByte(PING); // 1 byte
        Serializers.toBinary(ping.header, buf); // 1 byte serialiser id + 16 bytes THeader
        // total 18 bytes
      }
      case pong: Pong => {
        buf.writeByte(PONG); // 1 byte
        Serializers.toBinary(pong.header, buf); // 1 byte serialiser id + 16 bytes THeader
        // total 18 bytes
      }
    }
  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    val typeFlag = buf.readByte(); // 1 byte
    typeFlag match {
      case PING => {
        val header = Serializers.fromBinary(buf, NO_HINT).asInstanceOf[THeader]; // 1 byte serialiser id + 16 bytes THeader
        return new Ping(header); // 18 bytes total, check
      }
      case PONG => {
        val header = Serializers.fromBinary(buf, NO_HINT).asInstanceOf[THeader]; // 1 byte serialiser id + 16 bytes THeader
        return new Pong(header); // 18 bytes total, check
      }
      case _ => {
        Console.err.println(s"Got invalid byte flag=$typeFlag");
        return null;
      }
    }
  }
}
