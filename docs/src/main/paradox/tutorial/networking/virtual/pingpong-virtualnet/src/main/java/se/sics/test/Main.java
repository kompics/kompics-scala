package se.sics.test;

import se.sics.kompics.Kompics;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.network.netty.serialization.Serializers;

public class Main {

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

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                if (args[0].equalsIgnoreCase("ponger")) {
                    Kompics.createAndStart(PongerParent.class, 2);
                    System.out.println("Starting Ponger");
                    Kompics.waitForTermination();
                    // no shutdown this time...act like a server and keep running until externally exited
                } else if (args[0].equalsIgnoreCase("pinger")) {
                    Kompics.createAndStart(PingerParent.class, 2);
                    System.out.println("Starting Pinger");
                    
                    Thread.sleep(10000);
                    
                    Kompics.shutdown();
                    System.exit(0);
                }
            } catch (InterruptedException ex) {
                System.exit(1);
            }
        } else {
            System.err.println("Invalid number of parameters");
            System.exit(1);
        }
    }
}
