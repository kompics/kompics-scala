package se.sics.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import se.sics.kompics.Kompics;
import se.sics.kompics.network.netty.serialization.Serializers;

public class Main {

    
    static {
        // register
        Serializers.register(new NetSerializer(), "netS");
        Serializers.register(new PingPongSerializer(), "ppS");
        // map
        Serializers.register(TAddress.class, "netS");
        Serializers.register(THeader.class, "netS");
        Serializers.register(Ping.class, "ppS");
        Serializers.register(Pong.class, "ppS");
    }
    
    public static void main(String[] args) {
        try {
            if (args.length == 2) { // start Ponger
                InetAddress ip = InetAddress.getByName(args[0]);
                int port = Integer.parseInt(args[1]);
                TAddress self = new TAddress(ip, port);
                Kompics.createAndStart(PongerParent.class, new PongerParent.Init(self), 2);
                System.out.println("Starting Ponger at " + self);
                // no shutdown this time...act like a server and keep running until externally exited
            } else if (args.length == 4) { // start Pinger
                InetAddress myIp = InetAddress.getByName(args[0]);
                int myPort = Integer.parseInt(args[1]);
                TAddress self = new TAddress(myIp, myPort);
                InetAddress pongerIp = InetAddress.getByName(args[2]);
                int pongerPort = Integer.parseInt(args[3]);
                TAddress ponger = new TAddress(pongerIp, pongerPort);
                Kompics.createAndStart(PingerParent.class, new PingerParent.Init(self, ponger), 2);
                System.out.println("Starting Pinger at" + self + " to " + ponger);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    System.exit(1);
                }
                Kompics.shutdown();
                System.exit(0);
            } else {
                System.err.println("Invalid number of parameters (2 for Ponger, 4 for Pinger)");
                System.exit(1);
            }

        } catch (UnknownHostException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }
}
