package sexamples.virtualnetworking.pingpongvirtual

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
      case Ping => {
        buf.writeByte(PING); // 1 byte
        // total 1 bytes
      }
      case Pong => {
        buf.writeByte(PONG); // 1 byte
        // total 1 bytes
      }
    }
  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    val typeFlag = buf.readByte(); // 1 byte
    typeFlag match {
      case PING => {
        return Ping; // 1 bytes total, check
      }
      case PONG => {
        return Pong; // 1 bytes total, check
      }
      case _ => {
        Console.err.println(s"Got invalid byte flag=$typeFlag");
        return null;
      }
    }
  }
}
