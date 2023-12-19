package dev.mathops.db.config;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.generalized.connection.JdbcGeneralConnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

/**
 * Represents a server machine with a database product running.
 *
 * <p>
 * There should exist one {@code ServerConfig} object for each unique database product installation on a server machine.
 * There could be multiple database products running on a single machine, in which case each would have its own object.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;server id='...' type='...' schema='...' host='...' port='...' db='...' dba='...'/&gt;
 * </pre>
 */
public final class ServerConfig {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "server";

    /** The server ID attribute. */
    private static final String ID_ATTR = "id";

    /** The server type attribute. */
    private static final String TYPE_ATTR = "type";

    /** The schema attribute. */
    private static final String SCHEMA_ATTR = "schema";

    /** The host attribute. */
    private static final String HOST_ATTR = "host";

    /** The port attribute. */
    private static final String PORT_ATTR = "port";

    /** The ID attribute. */
    private static final String DB_ATTR = "db";

    /** The ID attribute. */
    private static final String DBA_ATTR = "dba";

    /** A character encoding. */
    private static final String ENC = "UTF-8";

    /** A common integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** The least valid TCP port number. */
    private static final int MIN_TCP_PORT = 1;

    /** The greatest valid TCP port number. */
    private static final int MAX_TCP_PORT = 65535;

    /** The server ID. */
    public final String id;

    /** The server installation type. */
    public final EDbInstallationType type;

    /** The schema type. */
    public final ESchemaType schema;

    /** The server host name (or IP address). */
    public final String host;

    /** The TCP port on which the server accepts JDBC connections. */
    public final int port;

    /** The database, if required by the database driver. */
    public final String db;

    /** The DBA username ({@code null} if not configured). */
    public final String dbaUser;

    /**
     * Constructs a new {@code ServerConfig}.
     *
     * @param theId      the server ID
     * @param theType    the database installation type
     * @param theSchema  the schema type
     * @param theHost    the host name
     * @param thePort    the TCP port
     * @param theDb      the database, if required by the database driver
     * @param theDbaUser the DBA username (null if not configured)
     * @throws IllegalArgumentException if the type, schema, or host is null, or the TCP port is invalid
     */
    public ServerConfig(final String theId, final EDbInstallationType theType, final ESchemaType theSchema,
                        final String theHost, final int thePort, final String theDb, final String theDbaUser) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("ID may not be null or blank.");
        }
        if (theType == null) {
            throw new IllegalArgumentException("Database installation type may not be null.");
        }
        if (theSchema == null) {
            throw new IllegalArgumentException("Schema may not be null");
        }
        if (theHost == null || theHost.isBlank()) {
            throw new IllegalArgumentException("Host name may not be null or blank.");
        }
        if (thePort < MIN_TCP_PORT || thePort > MAX_TCP_PORT) {
            throw new IllegalArgumentException("Invalid TCP port number");
        }

        this.id = theId;
        this.type = theType;
        this.schema = theSchema;
        this.host = theHost;
        this.port = thePort;
        this.db = theDb;
        this.dbaUser = theDbaUser;
    }

    /**
     * Constructs a new {@code ServerConfig} from its XML representation.
     *
     * @param theElem the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    ServerConfig(final EmptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {

            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id.isBlank()) {
                throw new IllegalArgumentException("ID may not be blank.");
            }

            final String typeStr = theElem.getRequiredStringAttr(TYPE_ATTR);
            this.type = EDbInstallationType.forName(typeStr);
            if (this.type == null) {
                final String msg = Res.fmt(Res.SRV_CFG_BAD_TYPE, typeStr);
                throw new ParsingException(theElem, msg);
            }

            final String schemaStr = theElem.getRequiredStringAttr(SCHEMA_ATTR);
            this.schema = ESchemaType.forName(schemaStr);
            if (this.schema == null) {
                final String msg = Res.fmt(Res.SRV_CFG_BAD_SCHEMA, schemaStr);
                throw new ParsingException(theElem, msg);
            }

            this.host = theElem.getRequiredStringAttr(HOST_ATTR);
            if (this.host.isBlank()) {
                throw new IllegalArgumentException("Hostname may not be blank.");
            }

            this.port = theElem.getIntegerAttr(PORT_ATTR, ZERO).intValue();
            if (this.port < MIN_TCP_PORT || this.port > MAX_TCP_PORT) {
                throw new IllegalArgumentException("Invalid TCP port number");
            }

            this.db = theElem.getStringAttr(DB_ATTR);
            this.dbaUser = theElem.getStringAttr(DBA_ATTR);
        } else {
            final String msg = Res.get(Res.SRV_CFG_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Builds the JDBC URL used to create the JDBC connection to the server.
     *
     * @param theUser     the username for this connection
     * @param thePassword the password for this connection
     * @return the URL
     */
    private String buildJdbcUrl(final String theUser, final String thePassword) {

        final HtmlBuilder url = new HtmlBuilder(80);

        url.add("jdbc:");

        try {
            if (this.type == EDbInstallationType.INFORMIX) {
                url.add("informix-sqli://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, this.db, ":INFORMIXSERVER=", this.db, ";user=", theUser, ";password=",
                        thePassword, "; IFX_LOCK_MODE_WAIT=5; CLIENT_LOCALE=en_US.8859-1;");
            } else if (this.type == EDbInstallationType.ORACLE) {
                url.add("oracle:thin:", theUser, CoreConstants.SLASH, URLEncoder.encode(thePassword, ENC), "@",
                        this.host, CoreConstants.COLON, Integer.toString(this.port), CoreConstants.SLASH, this.db);
            } else if (this.type == EDbInstallationType.POSTGRESQL) {
                url.add("postgresql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, this.db, "?user=", theUser, "&password=", thePassword);
            } else if (this.type == EDbInstallationType.MYSQL) {
                url.add("mysql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, this.db, "?user=", theUser, "&password=", thePassword);
            } else if (this.type == EDbInstallationType.CASSANDRA) {
                // TODO: This is not JDBC
            }
        } catch (final UnsupportedEncodingException ex) {
            Log.warning(ex);
        }

        return url.toString();
    }

    /**
     * Creates a new JDBC connection using this configuration.
     *
     * @param theUser     the username for this connection
     * @param thePassword the password for this connection
     * @return the new connection
     * @throws SQLException if the connection could not be opened
     */
    public AbstractGeneralConnection openConnection(final String theUser, final String thePassword) throws SQLException {

        try {
            final String url = buildJdbcUrl(theUser, thePassword);

            final Properties props = new Properties();

            final AbstractGeneralConnection conn;

            if (this.type == EDbInstallationType.INFORMIX) {
                props.setProperty("CLIENT_LOCALE", "EN_US.8859-1");
                conn = new JdbcGeneralConnection(DriverManager.getConnection(url, props));
            } else {
                // Log.info("Connect URL is " + url);
                conn = new JdbcGeneralConnection(DriverManager.getConnection(url));
            }

            // TODO: Add non-JDBC connections for non-JDBC database products

            final String productName = conn.getDatabaseProductName();
            Log.info("Connected to ", this.db, CoreConstants.SPC, productName);

            return conn;
        } catch (final SQLException | IllegalArgumentException ex) {
            final String exMsg = ex.getMessage();
            Log.warning(exMsg);
            final String portStr = Integer.toString(this.port);
            final String msg = Res.fmt(Res.SRV_CFG_CANT_CONNECT, this.db, this.host, portStr);
            throw new SQLException(msg, ex);
        }
    }

    /**
     * Tests whether this {@code ServerConfig} is equal to another object. To be equal, the other object must be a
     * {@code ServerConfig} and must have the same type, schema, host, port, and ID.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final ServerConfig test) {
            equal = test.type == this.type &&  test.schema == this.schema && test.host.equals(this.host)
                    && test.port == this.port && Objects.equals(test.db, this.db);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {

        return this.type.hashCode() + this.schema.hashCode() + this.host.hashCode() + this.port
                + EqualityTests.objectHashCode(this.db);
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        final String portStr = Integer.toString(this.port);
        htm.add(this.type.name, " server implementing the ", this.schema.name, " schema at ", this.host,
                CoreConstants.COLON, portStr);

        if (this.db != null) {
            htm.add(" with id ", this.db);
        }

        return htm.toString();
    }
}
