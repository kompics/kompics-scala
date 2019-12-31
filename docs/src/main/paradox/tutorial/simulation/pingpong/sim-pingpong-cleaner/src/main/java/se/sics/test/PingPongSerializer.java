package se.sics.test;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import se.sics.kompics.network.netty.serialization.Serializer;

public class PingPongSerializer implements Serializer {

    private static final byte PING = 1;
    private static final byte PONG = 2;

    @Override
    public int identifier() {
        return 200;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        if (o instanceof Ping) {
            Ping ping = (Ping) o;
            buf.writeByte(PING); // 1 byte
            // total 1 bytes
        } else if (o instanceof Pong) {
            Pong pong = (Pong) o;
            buf.writeByte(PONG); // 1 byte
            // total 1 bytes
        }
    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        byte type = buf.readByte(); // 1 byte
        switch (type) {
            case PING:
                return new Ping(); // 1 bytes total, check
            case PONG:
                return new Pong(); // 1 bytes total, check

        }
        return null;
    }
}
