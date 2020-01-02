package sexamples.networking.pingpongcleaned

import java.net.InetAddress
import java.net.InetSocketAddress
import se.sics.kompics.KompicsEvent
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
object TMessage {
  def apply(src: TAddress, dst: TAddress, payload: KompicsEvent): TMessage = {
    val header = THeader(src, dst, Transport.TCP);
    TMessage(header, payload)
  }
}
case class TMessage(header: THeader, payload: KompicsEvent) extends Msg[TAddress, THeader] {
  override def getHeader(): THeader = header;
  override def getSource(): TAddress = header.src;
  override def getDestination(): TAddress = header.dst;
  override def getProtocol(): Transport = header.proto;
}
// #message

// #pong
object Pong extends KompicsEvent;
// #pong

// #ping
object Ping extends KompicsEvent;
// #ping
