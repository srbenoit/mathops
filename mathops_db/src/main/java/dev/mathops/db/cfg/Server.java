package dev.mathops.db.cfg;

import dev.mathops.db.EDbProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * A "server" object from the database configuration file.
 */
public final class Server implements Comparable<Server> {

    /** The database type. */
    public final EDbProduct type;

    /** The server hostname. */
    public final String host;

    /** The TCP port on which the server listens for database connections. */
    public final int port;

    /** The databases available on the server. */
    private final List<Database> databases;

    /**
     * Constructs a new {@code Server}.
     *
     * @param theType the database type
     * @param theHost the server hostname
     * @param thePort the TCP port on which the server listens for database connections
     * @throws IllegalArgumentException if the product or hostname is null
     */
    Server(final EDbProduct theType, final String theHost, final int thePort) {

        if (theType == null || theHost == null) {
            final String msg = Res.get(Res.SERVER_NULL_TYPE_HOST);
            throw new IllegalArgumentException(msg);
        }

        this.type = theType;
        this.host = theHost;
        this.port = thePort;

        this.databases = new ArrayList<>(3);
    }

    /**
     * Gets the list of databases provided by the server.
     *
     * @return the list of databases
     */
    public List<Database> getDatabases() {

        return this.databases;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.type.hashCode() + this.host.hashCode() + this.port;
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param o the other object
     * @return true if the objects are equal
     */
    public boolean equals(final Object o) {

        final boolean equal;

        if (o == this) {
            equal = true;
        } else if (o instanceof final Server server) {
            equal = this.type == server.type && this.port == server.port && this.host.equals(server.host);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares two servers for order.  Comparison is based on the server type, then on host and port.
     *
     * @param o the object to be compared
     * @return 0 if this object is equal to {@code o}; -1 if this object precedes {@code o}; +1 if this object succeeds
     *         {@code o}
     */
    @Override
    public int compareTo(final Server o) {

        int result = this.type.compareTo(o.type);

        if (result == 0) {
            result = this.host.compareTo(o.host);

            if (result == 0) {
                result = Integer.compare(this.port, o.port);
            }
        }

        return result;
    }

    /**
     * Generates a string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.host + ":" + this.port;
    }
}
