package sexamples.simulation.pingpong

import io.netty.buffer.ByteBuf
import java.net.{InetAddress, UnknownHostException}
import java.util.Optional
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.Transport
import se.sics.kompics.network.netty.serialization.{Serializer, Serializers}

object NetSerializer extends Serializer {
  private val ADDR: Byte = 1;
  private val HEADER: Byte = 2;
  private val MSG: Byte = 3;

  private val NO_HINT: Optional[AnyRef] = Optional.empty();

  override def identifier(): Int = 100;

  override def toBinary(o: AnyRef, buf: ByteBuf): Unit = {
    o match {
      case addr: TAddress => {
        buf.writeByte(ADDR); // mark which type we are serialising (1 byte)
        addressToBinary(addr, buf); // 6 bytes
        // total 7 bytes
      }
      case header: THeader => {
        buf.writeByte(HEADER); // mark which type we are serialising (1 byte)
        headerToBinary(header, buf); // 13 bytes
        // total 14 bytes
      }
      case msg: TMessage => {
        buf.writeByte(MSG); // mark which type we are serialising (1 byte)
        headerToBinary(msg.header, buf); // 13 bytes
        Serializers.toBinary(msg.payload, buf); // no idea what it is, let the framework deal with it
      }
    }
  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    val typeFlag = buf.readByte(); // read the first byte to figure out the type
    typeFlag match {
      case ADDR => {
        return addressFromBinary(buf);
      }
      case HEADER => {
        return headerFromBinary(buf);
      }
      case MSG => {
        val header = headerFromBinary(buf); // 13 bytes
        val payload = Serializers.fromBinary(buf, NO_HINT).asInstanceOf[KompicsEvent]; // don't know what it is but KompicsEvent is the upper bound
        return TMessage(header, payload);
      }
      case _ => {
        Console.err.println(s"Got invalid byte flag=$typeFlag");
        return null;
      }
    }
  }

  private def headerToBinary(header: THeader, buf: ByteBuf): Unit = {
    addressToBinary(header.src, buf); // 6 bytes
    addressToBinary(header.dst, buf); // 6 bytes
    buf.writeByte(header.proto.ordinal()); // 1 byte is enough
    // total of 13 bytes
  }

  private def headerFromBinary(buf: ByteBuf): THeader = {
    val src = addressFromBinary(buf); // 6 bytes
    val dst = addressFromBinary(buf); // 6 bytes
    val protoOrd = buf.readByte(); // 1 byte
    val proto = Transport.values()(protoOrd);
    return THeader(src, dst, proto); // total of 13 bytes, check
  }

  private def addressToBinary(addr: TAddress, buf: ByteBuf): Unit = {
    buf.writeBytes(addr.getIp().getAddress()); // 4 bytes IP (assume it's IPv4)
    buf.writeShort(addr.getPort()); // we only need 2 bytes here
    // total of 6 bytes
  }

  private def addressFromBinary(buf: ByteBuf): TAddress = {
    val ipBytes = Array.ofDim[Byte](4);
    buf.readBytes(ipBytes); // 4 bytes
    val ip = InetAddress.getByAddress(ipBytes);
    val port = buf.readUnsignedShort(); // 2 bytes
    return TAddress(ip, port); // total of 7, check
  }
}
