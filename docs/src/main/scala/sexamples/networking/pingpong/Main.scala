package sexamples.networking.pingpong

import se.sics.kompics.sl._
import java.net.{InetAddress, UnknownHostException}

object Main {
  def main(args: Array[String]): Unit = {
    val ip = InetAddress.getLocalHost();
    val port = Integer.parseInt(args(0));
    val self = TAddress(ip, port);
    Kompics.createAndStart(classOf[Parent], Init[Parent](self), 2);
    Kompics.waitForTermination();
  }
}
