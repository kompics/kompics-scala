package sexamples.virtualnetworking.pingpongvirtual

import java.net.InetAddress
import java.net.InetSocketAddress
import se.sics.kompics.KompicsEvent
import se.sics.kompics.network.virtual.{Address, Header}
import se.sics.kompics.network.{Msg, Transport}

// #address
object TAddress {
  def apply(addr: InetAddress, port: Int): TAddress = {
    val isa = new InetSocketAddress(addr, port);
    TAddress(isa, None)
  }
  def apply(addr: InetAddress, port: Int, id: Array[Byte]): TAddress = {
    val isa = new InetSocketAddress(addr, port);
    TAddress(isa, Some(id))
  }
}
case class TAddress(isa: InetSocketAddress, id: Option[Array[Byte]]) extends Address {

  override def getIp(): InetAddress = isa.getAddress();
  override def getPort(): Int = isa.getPort();
  override def getId(): Array[Byte] = id.getOrElse(null);
  override def asSocket(): InetSocketAddress = isa;
  override def sameHostAs(other: se.sics.kompics.network.Address): Boolean = {
    /* note that we don't include the id here, since nodes with different
     * ids but the same socket are still on the same machine
     */
    this.isa.equals(other.asSocket())
  }
}
// #address

// #header
case class THeader(src: TAddress, dst: TAddress, proto: Transport) extends Header[TAddress] {
  override def getSource(): TAddress = src;
  override def getDestination(): TAddress = dst;
  override def getProtocol(): Transport = proto;
  override def getDstId(): Array[Byte] = dst.getId();
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
