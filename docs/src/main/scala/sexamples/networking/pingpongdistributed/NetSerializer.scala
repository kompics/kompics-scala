package sexamples.networking.pingpongdistributed

import io.netty.buffer.ByteBuf
import java.net.{InetAddress, UnknownHostException}
import java.util.Optional
import se.sics.kompics.network.Transport
import se.sics.kompics.network.netty.serialization.Serializer

object NetSerializer extends Serializer {
  private val ADDR: Byte = 1;
  private val HEADER: Byte = 2;

  private val NO_HINT: Optional[AnyRef] = Optional.empty();

  override def identifier(): Int = 100;

  override def toBinary(o: AnyRef, buf: ByteBuf): Unit = {
    o match {
      case addr: TAddress => {
        buf.writeByte(ADDR); // mark which type we are serialising (1 byte)
        buf.writeBytes(addr.getIp().getAddress()); // 4 bytes IP (let's assume it's IPv4)
        buf.writeShort(addr.getPort()); // we only need 2 bytes here
        // total 7 bytes
      }
      case header: THeader => {
        buf.writeByte(HEADER); // mark which type we are serialising (1 byte)
        this.toBinary(header.src, buf); // use this serialiser again (7 bytes)
        this.toBinary(header.dst, buf); // use this serialiser again (7 bytes)
        buf.writeByte(header.proto.ordinal()); // 1 byte is enough
        // total 16 bytes
      }
    }
  }

  override def fromBinary(buf: ByteBuf, hint: Optional[AnyRef]): AnyRef = {
    val typeFlag = buf.readByte(); // read the first byte to figure out the type
    typeFlag match {
      case ADDR => {
        val ipBytes = Array.ofDim[Byte](4);
        buf.readBytes(ipBytes);
        val ip = InetAddress.getByAddress(ipBytes); // 4 bytes
        val port = buf.readUnsignedShort(); // 2 bytes
        return TAddress(ip, port); // total of 7, check
      }
      case HEADER => {
        val src = this.fromBinary(buf, NO_HINT).asInstanceOf[TAddress]; // We already know what it's going to be (7 bytes)
        val dst = this.fromBinary(buf, NO_HINT).asInstanceOf[TAddress]; // We already know what it's going to be (7 bytes)
        val protoOrd = buf.readByte(); // 1 byte
        val proto = Transport.values()(protoOrd);
        return THeader(src, dst, proto); // total of 16 bytes, check
      }
      case _ => {
        Console.err.println(s"Got invalid byte flag=$typeFlag");
        return null;
      }
    }
  }
}
