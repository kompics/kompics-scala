package se.sics.test;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import java.net.InetAddress;
import java.net.UnknownHostException;
import se.sics.kompics.network.Transport;
import se.sics.kompics.network.netty.serialization.Serializer;

public class NetSerializer implements Serializer {

    private static final byte ADDR = 1;
    private static final byte HEADER = 2;

    @Override
    public int identifier() {
        return 100;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        if (o instanceof TAddress) {
            TAddress addr = (TAddress) o;
            buf.writeByte(ADDR); // mark which type we are serialising (1 byte)
            buf.writeBytes(addr.getIp().getAddress()); // 4 bytes IP (let's hope it's IPv4^^)
            buf.writeShort(addr.getPort()); // we only need 2 bytes here
            // total 7 bytes
        } else if (o instanceof THeader) {
            THeader header = (THeader) o;
            buf.writeByte(HEADER); // mark which type we are serialising (1 byte)
            this.toBinary(header.src, buf); // use this serialiser again (7 bytes)
            this.toBinary(header.dst, buf); // use this serialiser again (7 bytes)
            buf.writeByte(header.proto.ordinal()); // 1 byte is enough
            // total 16 bytes
        }
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        byte type = buf.readByte(); // read the first byte to figure out the type
        switch (type) {
            case ADDR: {
                byte[] ipBytes = new byte[4];
                buf.readBytes(ipBytes);
                try {
                    InetAddress ip = InetAddress.getByAddress(ipBytes); // 4 bytes
                    int port = buf.readUnsignedShort(); // 2 bytes
                    return new TAddress(ip, port); // total of 7, check
                } catch (UnknownHostException ex) {
                    throw new RuntimeException(ex); // let Netty deal with this
                }
            }
            case HEADER: {
                TAddress src = (TAddress) this.fromBinary(buf, Optional.absent()); // We already know what it's going to be (7 bytes)
                TAddress dst = (TAddress) this.fromBinary(buf, Optional.absent()); // same here (7 bytes)
                int protoOrd = buf.readByte(); // 1 byte
                Transport proto = Transport.values()[protoOrd];
                return new THeader(src, dst, proto); // total of 16 bytes, check
            }
        }
        return null; // strange things happened^^
    }
}
