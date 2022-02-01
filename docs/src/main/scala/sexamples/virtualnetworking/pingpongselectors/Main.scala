package sexamples.virtualnetworking.pingpongselectors

import se.sics.kompics.sl._
import se.sics.kompics.config.Conversions;
import se.sics.kompics.network.netty.serialization.Serializers
import java.net.{InetAddress, UnknownHostException}

object Main {
  // #registration
  // register
  Serializers.register(NetSerializer, "netS");
  Serializers.register(PingPongSerializer, "ppS");
  // map
  Serializers.register(classOf[TAddress], "netS");
  Serializers.register(classOf[THeader], "netS");
  Serializers.register(classOf[TMessage], "netS");
  Serializers.register(Ping.getClass, "ppS");
  Serializers.register(Pong.getClass, "ppS");
  // conversions
  Conversions.register(TAddressConverter);
  // #registration

  def main(args: Array[String]): Unit = {
    if (args.length == 1) {
      if (args(0).equalsIgnoreCase("ponger")) {
        Kompics.createAndStart(classOf[PongerParent], 2);
        println(s"Starting Ponger");
        Kompics.waitForTermination();
        // will never actually terminate...act like a server and keep running until externally exited
      } else if (args(0).equalsIgnoreCase("pinger")) {
        Kompics.createAndStart(classOf[PingerParent], 2);
        Kompics.waitForTermination();
        System.exit(0);
      } else {
        Console.err.println(s"Invalid argument: ${args(0)}");
        System.exit(1);
      }
    } else {
      Console.err.println("Invalid number of parameters");
      System.exit(1);
    }
  }
}
