package jexamples.networking.pingpongcleaned;

import java.net.InetAddress;
import java.net.UnknownHostException;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.Kompics;
import se.sics.kompics.network.netty.serialization.Serializers;

public class Main {

  // #registration
  static {
    // register
    Serializers.register(new NetSerializer(), "netS");
    Serializers.register(new PingPongSerializer(), "ppS");
    // map
    Serializers.register(TAddress.class, "netS");
    Serializers.register(THeader.class, "netS");
    Serializers.register(TMessage.class, "netS");
    Serializers.register(Ping.class, "ppS");
    Serializers.register(Pong.class, "ppS");
    // conversions
    Conversions.register(new TAddressConverter());
  }
  // #registration

  public static void main(String[] args) {
    if (args.length == 1) {
      try {
        if (args[0].equalsIgnoreCase("ponger")) {
          Kompics.createAndStart(PongerParent.class, 2);
          System.out.println("Starting Ponger");
          Kompics.waitForTermination();
          // will never actually terminate...act like a server and keep running until externally
          // exited
        } else if (args[0].equalsIgnoreCase("pinger")) {
          Kompics.createAndStart(PingerParent.class, 2);
          System.out.println("Starting Pinger");
          Kompics.waitForTermination();
          System.exit(0);
        } else {
          System.err.println("Invalid argument: " + args[0]);
          System.exit(1);
        }
      } catch (InterruptedException ex) {
        System.err.println(ex);
        System.exit(1);
      }
    } else {
      System.err.println("Invalid number of parameters");
      System.exit(1);
    }
  }
}
