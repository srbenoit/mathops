package dev.mathops.app.db.config;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.config.ServerConfig;

/**
 * A mutable model of a database server configuration that can be edited through a GUI.
 */
public final class MutableServerConfig {

    /** The id. */
    private String id;

    /** The database installation type. */
    private EDbInstallationType type;

    /** The host. */
    private String host;

    /** The port. */
    private int port;

    /** The DBA username (null if not configured). */
    private String dbaUser;

    /**
     * Constructs a new {@code MutableServerConfig}.
     *
     * @param theId      the server ID
     * @param theType    the database installation type (must not be null)
     * @param theHost    the host name (must not be null or blank)
     * @param thePort    the TCP port (must be positive, less than 65536)
     * @param theDbaUser the DBA username, if configured
     */
    public MutableServerConfig(final String theId, final EDbInstallationType theType, final String theHost,
                               final int thePort, final String theDbaUser) {

        update(theId, theType, theHost, thePort, theDbaUser);
    }

    /**
     * Constructs a new {@code MutableServerConfig} from a {@code ServerConfig}.
     *
     * @param source the source {@code ServerConfig}
     */
    MutableServerConfig(final ServerConfig source) {

        this.id = source.id;
        this.type = source.type;
        this.host = source.host;
        this.port = source.port;
        this.dbaUser = source.dbaUser;
    }

    /**
     * Gets the ID.
     *
     * @return the ID
     */
    public String getId() {

        return this.id;
    }

    /**
     * Gets the database installation type.
     *
     * @return the database installation type
     */
    public EDbInstallationType getType() {

        return this.type;
    }

    /**
     * Gets the hostname.
     *
     * @return the hostname
     */
    public String getHost() {

        return this.host;
    }

    /**
     * Gets the TCP port.
     *
     * @return the port
     */
    public int getPort() {

        return this.port;
    }

    /**
     * Gets the DBA username.
     *
     * @return the DBA username (could be null or blank)
     */
    public String getBbaUser() {

        return this.dbaUser;
    }


    /**
     * Updates the field values.
     *
     * @param theId      the server ID
     * @param theType    the database installation type (must not be null)
     * @param theHost    the host name (must not be null or blank)
     * @param thePort    the TCP port (must be positive, less than 65536)
     * @param theDbaUser the DBA username, if configured
     */
    public void update(final String theId, final EDbInstallationType theType, final String theHost,
                       final int thePort, final String theDbaUser) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Server ID may not be null or blank.");
        }
        if (theType == null) {
            throw new IllegalArgumentException("Database installation type may not be null.");
        }
        if (theHost == null || theHost.isBlank()) {
            throw new IllegalArgumentException("Host name may not be null or blank.");
        }
        if (thePort < 1 || thePort > 65535) {
            throw new IllegalArgumentException("Invalid TCP port number");
        }

        this.id = theId;
        this.type = theType;
        this.host = theHost;
        this.port = thePort;
        this.dbaUser = theDbaUser;
    }

    /**
     * Generate an immutable {@code ServerConfig} from this object.
     *
     * @return the generated {@code ServerConfig}
     */
    public ServerConfig toServerConfig() {

        return new ServerConfig(this.id, this.type, this.host, this.port, this.dbaUser);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String portStr = Integer.toString(this.port);

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("MutableServerConfig{id='", this.id, "',type='", this.type, "',host='",
                this.host, "',port='", portStr, "'");

        if (this.dbaUser != null) {
            builder.add(",dbaUser='", this.dbaUser, "'");
        }
        builder.add("}");

        return builder.toString();
    }
}
