package se.sics.test;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.net.InetAddress;
import java.net.UnknownHostException;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Transport;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;

public class NetSerializer implements Serializer {

    private static final byte ADDR = 1;
    private static final byte HEADER = 2;
    private static final byte MSG = 3;

    @Override
    public int identifier() {
        return 100;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        if (o instanceof TAddress) {
            TAddress addr = (TAddress) o;
            buf.writeByte(ADDR); // mark which type we are serialising (1 byte)
            addressToBinary(addr, buf); // 6 bytes
            // total 7 bytes
        } else if (o instanceof THeader) {
            THeader header = (THeader) o;
            buf.writeByte(HEADER); // mark which type we are serialising (1 byte)
            headerToBinary(header, buf); // 13 bytes
            // total 14 bytes
        } else if (o instanceof TMessage) {
            TMessage msg = (TMessage) o;
            buf.writeByte(MSG); // mark which type we are serialising (1 byte)
            headerToBinary(msg.header, buf); // 13 bytes
            Serializers.toBinary(msg.payload, buf); // no idea what it is, let the framework deal with it
        }
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        byte type = buf.readByte(); // read the first byte to figure out the type
        switch (type) {
            case ADDR:
                return addressFromBinary(buf);
            case HEADER:
                return headerFromBinary(buf);
            case MSG: {
                THeader header = headerFromBinary(buf); // 13 bytes
                KompicsEvent payload = (KompicsEvent) Serializers.fromBinary(buf, Optional.absent()); // don't know what it is but KompicsEvent is the upper bound
                return new TMessage(header, payload);
            }
        }
        return null; // strange things happened^^
    }

    private void headerToBinary(THeader header, ByteBuf buf) {
        addressToBinary(header.src, buf); // 6 bytes
        addressToBinary(header.dst, buf); // 6 bytes
        buf.writeByte(header.proto.ordinal()); // 1 byte is enough
        // total of 13 bytes
    }

    private THeader headerFromBinary(ByteBuf buf) {
        TAddress src = addressFromBinary(buf); // 6 bytes
        TAddress dst = addressFromBinary(buf); // 6 bytes
        int protoOrd = buf.readByte(); // 1 byte
        Transport proto = Transport.values()[protoOrd];
        return new THeader(src, dst, proto); // total of 13 bytes, check
    }

    private void addressToBinary(TAddress addr, ByteBuf buf) {
        buf.writeBytes(addr.getIp().getAddress()); // 4 bytes IP (let's hope it's IPv4^^)
        buf.writeShort(addr.getPort()); // we only need 2 bytes here
        // total of 6 bytes
    }

    private TAddress addressFromBinary(ByteBuf buf) {
        byte[] ipBytes = new byte[4];
        buf.readBytes(ipBytes); // 4 bytes
        try {
            InetAddress ip = InetAddress.getByAddress(ipBytes);
            int port = buf.readUnsignedShort(); // 2 bytes
            return new TAddress(ip, port); // total of 6, check
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex); // let Netty deal with this
        }
    }
}
