package sexamples.networking.pingpongdistributed

import se.sics.kompics.sl._
import se.sics.kompics.network.netty.serialization.Serializers
import java.net.{InetAddress, UnknownHostException}

object Main {

  // #serializers
  // register
  Serializers.register(NetSerializer, "netS");
  Serializers.register(PingPongSerializer, "ppS");
  // map
  Serializers.register(classOf[TAddress], "netS");
  Serializers.register(classOf[THeader], "netS");
  Serializers.register(classOf[Ping], "ppS");
  Serializers.register(classOf[Pong], "ppS");
  // #serializers

  def main(args: Array[String]): Unit = {
    if (args.length == 2) { // start Ponger
      val ip = InetAddress.getByName(args(0));
      val port = Integer.parseInt(args(1));
      val self = TAddress(ip, port);
      Kompics.createAndStart(classOf[PongerParent], Init[PongerParent](self), 2);
      println(s"Starting Ponger at $self");
      Kompics.waitForTermination();
      // will never actually terminate...act like a server and keep running until externally exited
    } else if (args.length == 4) {
      val ip = InetAddress.getByName(args(0));
      val port = Integer.parseInt(args(1));
      val self = TAddress(ip, port);
      val pongerIp = InetAddress.getByName(args(2));
      val pongerPort = Integer.parseInt(args(3));
      val ponger = TAddress(pongerIp, pongerPort);
      Kompics.createAndStart(classOf[PingerParent], Init[PingerParent](self, ponger), 2);
      Kompics.waitForTermination();
      System.exit(0);
    } else {
      System.err.println("Invalid number of parameters (2 for Ponger, 4 for Pinger)");
      System.exit(1);
    }
  }
}
