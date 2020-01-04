package sexamples.simulation.pingpong

import java.net.{InetAddress, UnknownHostException}
import java.util.Map
import se.sics.kompics.config.{Conversions, Converter}

object TAddressConverter extends Converter[TAddress] {

  override def convert(o: AnyRef): TAddress = {
    o match {
      case m: Map[String, Any] @unchecked => {
        val hostname = Conversions.convert(m.get("host"), classOf[String]);
        val port: Int = Conversions.convert(m.get("port"), classOf[Integer]);
        val ip = InetAddress.getByName(hostname);
        TAddress(ip, port)
      }
      case s: String => {
        val ipport = s.split(":");
        val ip = InetAddress.getByName(ipport(0));
        val port = Integer.parseInt(ipport(1));
        TAddress(ip, port)
      }
      case _ => null
    }
  }

  override def `type`(): Class[TAddress] = classOf[TAddress];
}
