package se.sics.test.system;

import se.sics.kompics.Kompics;
import se.sics.kompics.config.Conversions;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.test.NetSerializer;
import se.sics.test.Ping;
import se.sics.test.PingPongSerializer;
import se.sics.test.Pong;
import se.sics.test.TAddress;
import se.sics.test.TAddressConverter;
import se.sics.test.THeader;
import se.sics.test.TMessage;

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
            if (args[0].equalsIgnoreCase("ponger")) {
                Kompics.createAndStart(PongerParent.class, 2);
                System.out.println("Starting Ponger");
                // no shutdown this time...act like a server and keep running until externally exited
            } else if (args[0].equalsIgnoreCase("pinger")) {
                Kompics.createAndStart(PingerParent.class, 2);
                System.out.println("Starting Pinger");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    System.exit(1);
                }
                Kompics.shutdown();
                System.exit(0);
            }
        } else {
            System.err.println("Invalid number of parameters");
            System.exit(1);
        }
    }
}
