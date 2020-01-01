package sexamples.networking.pingpongdistributed

import java.net.InetAddress
import java.net.InetSocketAddress
import se.sics.kompics.network.{Address, Header, Msg, Transport}

// #address
object TAddress {
  def apply(addr: InetAddress, port: Int): TAddress = {
    val isa = new InetSocketAddress(addr, port);
    TAddress(isa)
  }
}
case class TAddress(isa: InetSocketAddress) extends Address {

  override def getIp(): InetAddress = isa.getAddress();
  override def getPort(): Int = isa.getPort();
  override def asSocket(): InetSocketAddress = isa;
  override def sameHostAs(other: Address): Boolean = {
    this.isa.equals(other.asSocket())
  }
}
// #address

// #header
case class THeader(src: TAddress, dst: TAddress, proto: Transport) extends Header[TAddress] {
  override def getSource(): TAddress = src;
  override def getDestination(): TAddress = dst;
  override def getProtocol(): Transport = proto;
}
// #header

// #message
abstract class TMessage(val header: THeader) extends Msg[TAddress, THeader] {
  def this(src: TAddress, dst: TAddress, proto: Transport) {
    this(THeader(src, dst, proto))
  }

  override def getHeader(): THeader = header;
  override def getSource(): TAddress = header.src;
  override def getDestination(): TAddress = header.dst;
  override def getProtocol(): Transport = header.proto;
}
// #message

// #pong
object Pong {
  def apply(src: TAddress, dst: TAddress): Pong = new Pong(THeader(src, dst, Transport.TCP));
}
class Pong(_header: THeader) extends TMessage(_header);
// #pong

// #ping
object Ping {
  def apply(src: TAddress, dst: TAddress): Ping = new Ping(THeader(src, dst, Transport.TCP));
}
class Ping(_header: THeader) extends TMessage(_header);
// #ping
