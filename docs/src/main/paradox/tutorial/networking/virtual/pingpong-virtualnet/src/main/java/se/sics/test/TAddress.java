package se.sics.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import se.sics.kompics.network.Address;

public class TAddress implements se.sics.kompics.network.virtual.Address {

    private final InetSocketAddress isa;
    private final byte[] id;

    public TAddress(InetAddress addr, int port) {
        this(addr, port, null);
    }

    public TAddress(InetAddress addr, int port, byte[] id) {
        this.isa = new InetSocketAddress(addr, port);
        this.id = id;
    }

    @Override
    public InetAddress getIp() {
        return this.isa.getAddress();
    }

    @Override
    public int getPort() {
        return this.isa.getPort();
    }

    @Override
    public byte[] getId() {
        return this.id;
    }
    
    public TAddress withVirtual(byte[] id) {
        return new TAddress(isa.getAddress(), isa.getPort(), id);
    }

    @Override
    public InetSocketAddress asSocket() {
        return this.isa;
    }

    @Override
    public boolean sameHostAs(Address other) {
        return this.isa.equals(other.asSocket());
        /* note that we don't include the id here, since nodes with different 
         * ids but the same socket are still on the same machine
         */
    }

    // Not required but strongly recommended
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isa.getHostString());
        sb.append(":");
        sb.append(isa.getPort());
        if (id != null) {
            sb.append(":");
            sb.append(Arrays.toString(id));
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isa, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TAddress other = (TAddress) obj;
        return Objects.equals(this.isa, other.isa) && Objects.deepEquals(this.id, this.id);
    }

}
