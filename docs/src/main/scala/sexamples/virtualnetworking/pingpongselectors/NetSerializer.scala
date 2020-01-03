package sexamples.virtualnetworking.pingpongselectors

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
        addressToBinary(addr, buf); // 7-11 bytes
        // total 8-12 bytes
      }
      case header: THeader => {
        buf.writeByte(HEADER); // mark which type we are serialising (1 byte)
        headerToBinary(header, buf); // 15-23 bytes
        // total 16-24 bytes
      }
      case msg: TMessage => {
        buf.writeByte(MSG); // mark which type we are serialising (1 byte)
        headerToBinary(msg.header, buf); // 15-23 bytes
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
        val header = headerFromBinary(buf); // 15-23 bytes
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
    addressToBinary(header.src, buf); // 7-11 bytes
    addressToBinary(header.dst, buf); // 7-11 bytes
    buf.writeByte(header.proto.ordinal()); // 1 byte is enough
    // total of 15-23 bytes
  }

  private def headerFromBinary(buf: ByteBuf): THeader = {
    val src = addressFromBinary(buf); // 7-11 bytes
    val dst = addressFromBinary(buf); // 7-11 bytes
    val protoOrd = buf.readByte(); // 1 byte
    val proto = Transport.values()(protoOrd);
    return THeader(src, dst, proto); // total of 15-23 bytes, check
  }

  private def addressToBinary(addr: TAddress, buf: ByteBuf): Unit = {
    buf.writeBytes(addr.getIp().getAddress()); // 4 bytes IP (assume it's IPv4)
    buf.writeShort(addr.getPort()); // we only need 2 bytes here
    addr.id match {
      case Some(id) => {
        buf.writeByte(id.length); // 1 byte - we only want to use 4 bytes for ids, so let's not waste space
        buf.writeBytes(id); // should be 4 bytes
        // total of 11 bytes
      }
      case None => {
        buf.writeByte(-1); //  we'll use this a null-marker (while a length of 0 indicates an empty array)
        // total of 7 bytes
      }
    }
  }

  private def addressFromBinary(buf: ByteBuf): TAddress = {
    val ipBytes = Array.ofDim[Byte](4);
    buf.readBytes(ipBytes); // 4 bytes
    val ip = InetAddress.getByAddress(ipBytes);
    val port = buf.readUnsignedShort(); // 2 bytes
    val idLength = buf.readByte(); // 1 byte
    if (idLength >= 0) {
      val id = Array.ofDim[Byte](idLength); // should be 4 bytes
      buf.readBytes(id);
      return TAddress(ip, port, id); // total of 11, check
    } else {
      return TAddress(ip, port); // total of 7, check
    }
  }
}
