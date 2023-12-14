package dev.mathops.app.db.config;

import java.util.ArrayList;
import java.util.List;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.config.ESchemaType;
import dev.mathops.db.config.LoginConfig;
import dev.mathops.db.config.ServerConfig;

/**
 * A mutable model of a database server configuration that can be edited through a GUI.
 */
public final class MutableServerConfig {

    /** The database installation type. */
    private EDbInstallationType type;

    /** The schema type. */
    private ESchemaType schema;

    /** The host. */
    private String host;

    /** The port. */
    private int port;

    /** The database ID (could be null if the database type does not require it). */
    private String dbId;

    /** The DBA username (null if not configured). */
    private String dbaUser;

    /** The list of mutable data login configuration objects. */
    private final List<MutableLoginConfig> logins;

    /**
     * Constructs a new {@code MutableServerConfig}.
     *
     * @param theType the database installation type (must not be null)
     * @param theSchema the schema (must not be null)
     * @param theHost the host name (must not be null or blank)
     * @param thePort the TCP port (must be positive, less than 65536)
     * @param theDbId the database ID, if the database type requires this
     * @param theDbaUser the DBA username, if configured
     */
    public MutableServerConfig(final EDbInstallationType theType, final ESchemaType theSchema, final String theHost,
                               final int thePort, final String theDbId, final String theDbaUser) {

        this.logins = new ArrayList<>(5);

        update(theType, theSchema, theHost, thePort, theDbId, theDbaUser);
    }

    /**
     * Constructs a new {@code MutableServerConfig} from a {@code ServerConfig}.
     *
     * @param source the source {@code ServerConfig}
     */
    public MutableServerConfig(final ServerConfig source) {

        this.logins = new ArrayList<>(5);

        this.type = source.type;
        this.schema = source.schema;
        this.host = source.host;
        this.port = source.port;
        this.dbId = source.dbId;
        this.dbaUser = source.dbaUser;

        for (final LoginConfig login : source.getLogins()) {
            this.logins.add(new MutableLoginConfig(login));
        }
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
     * Gets the schema type.
     *
     * @return the schema type
     */
    public ESchemaType getSchema() {

        return this.schema;
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
     * Gets the database ID.
     *
     * @return the database ID (could be null or blank)
     */
    public String getDbId() {

        return this.dbId;
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
     * Gets the list of login objects.  The returned list can be edited - results are reflected in this object.
     *
     * @return the list of logins
     */
    public List<MutableLoginConfig>  getLogins() {

        return this.logins;
    }

    /**
     * Updates the field values.
     *
     * @param theType the database installation type (must not be null)
     * @param theSchema the schema (must not be null)
     * @param theHost the host name (must not be null or blank)
     * @param thePort the TCP port (must be positive, less than 65536)
     * @param theDbId the database ID, if the database type requires this
     * @param theDbaUser the DBA username, if configured
     */
    public void update(final EDbInstallationType theType, final ESchemaType theSchema, final String theHost,
                       final int thePort, final String theDbId, final String theDbaUser) {

        if (theType == null) {
            throw new IllegalArgumentException("Database installation type may not be null.");
        }
        if (theSchema == null) {
            throw new IllegalArgumentException("Schema may not be null");
        }
        if (theHost == null || theHost.isBlank()) {
            throw new IllegalArgumentException("Host name may not be null or blank.");
        }
        if (thePort < 1 || thePort > 65535) {
            throw new IllegalArgumentException("Invalid TCP port number");
        }

        this.type = theType;
        this.schema = theSchema;
        this.host = theHost;
        this.port = thePort;
        this.dbId = theDbId;
        this.dbaUser = theDbaUser;
    }

    /**
     * Generate an immutable {@code ServerConfig} from this object.
     *
     * @return the generated {@code ServerConfig}
     */
    public ServerConfig toServerConfig() {

        final int count = this.logins.size();
        final List<LoginConfig> loginConfigs = new ArrayList<>(count);

        for (final MutableLoginConfig login : this.logins) {
            final LoginConfig loginCfg = login.toLoginConfig();
            loginConfigs.add(loginCfg);
        }

        return new ServerConfig(this.type, this.schema, this.host, this.port, this.dbId, this.dbaUser, loginConfigs);
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

        builder.add("MutableServerConfig{type='", this.type, "', schema='", this.schema, "', host='",
                this.host, "', port='", portStr, "'");

        if (this.dbId != null) {
            builder.add(", dbId='", this.dbId, "'");
        }
        if (this.dbaUser != null) {
            builder.add(", dbaUser='", this.dbaUser, "'");
        }
        if (!this.logins.isEmpty()) {
            builder.add(", logins={", this.logins, "}");
        }
        builder.add("}");

        return builder.toString();
    }
}
