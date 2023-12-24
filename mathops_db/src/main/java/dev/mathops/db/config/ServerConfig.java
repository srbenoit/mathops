package dev.mathops.db.config;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;
import dev.mathops.db.generalized.connection.JdbcGeneralConnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
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
 * &lt;server id='...' type='...' host='...' port='...' dba='...'/&gt;
 * </pre>
 */
public final class ServerConfig {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "server";

    /** The server ID attribute. */
    private static final String ID_ATTR = "id";

    /** The server type attribute. */
    private static final String TYPE_ATTR = "type";

    /** The host attribute. */
    private static final String HOST_ATTR = "host";

    /** The port attribute. */
    private static final String PORT_ATTR = "port";

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

    /** The server host name (or IP address). */
    public final String host;

    /** The TCP port on which the server accepts JDBC connections. */
    public final int port;

    /** The DBA username ({@code null} if not configured). */
    public final String dbaUser;

    /**
     * Constructs a new {@code ServerConfig}.
     *
     * @param theId      the server ID
     * @param theType    the database installation type
     * @param theHost    the host name
     * @param thePort    the TCP port
     * @param theDbaUser the DBA username (null if not configured)
     * @throws IllegalArgumentException if the id, type, or host is null, or the TCP port is invalid
     */
    public ServerConfig(final String theId, final EDbInstallationType theType, final String theHost, final int thePort,
                        final String theDbaUser) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("ID may not be null or blank.");
        }
        if (theType == null) {
            throw new IllegalArgumentException("Database installation type may not be null.");
        }
        if (theHost == null || theHost.isBlank()) {
            throw new IllegalArgumentException("Host name may not be null or blank.");
        }
        if (thePort < MIN_TCP_PORT || thePort > MAX_TCP_PORT) {
            throw new IllegalArgumentException("Invalid TCP port number");
        }

        this.id = theId;
        this.type = theType;
        this.host = theHost;
        this.port = thePort;
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

            this.host = theElem.getRequiredStringAttr(HOST_ATTR);
            if (this.host.isBlank()) {
                throw new IllegalArgumentException("Hostname may not be blank.");
            }

            this.port = theElem.getIntegerAttr(PORT_ATTR, ZERO).intValue();
            if (this.port < MIN_TCP_PORT || this.port > MAX_TCP_PORT) {
                throw new IllegalArgumentException("Invalid TCP port number");
            }

            this.dbaUser = theElem.getStringAttr(DBA_ATTR);
        } else {
            final String msg = Res.get(Res.SRV_CFG_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Creates a new JDBC connection using this configuration.
     *
     * @param theDb       the database configuration
     * @param theUser     the username for this connection
     * @param thePassword the password for this connection
     * @return the new connection
     * @throws SQLException if the connection could not be opened
     */
    public AbstractGeneralConnection openConnection(final DbConfig theDb, final String theUser,
                                                    final String thePassword) throws SQLException {

        final String db = theDb.db;

        try {
            final String url = buildJdbcUrl(theDb, theUser, thePassword);

            final Properties props = new Properties();

            final AbstractGeneralConnection conn;

            final Connection jdbcConn;
            if (this.type == EDbInstallationType.INFORMIX) {
                props.setProperty("CLIENT_LOCALE", "EN_US.8859-1");
                jdbcConn = DriverManager.getConnection(url, props);
            } else {
                // Log.info("Connect URL is " + url);
                jdbcConn = DriverManager.getConnection(url);
            }

            conn = new JdbcGeneralConnection(theDb.use, jdbcConn);

            // TODO: Add non-JDBC connections for non-JDBC database products

            final String productName = conn.getDatabaseProductName();
            Log.info("Connected to ", db, CoreConstants.SPC, productName);

            return conn;
        } catch (final SQLException | IllegalArgumentException ex) {
            final String exMsg = ex.getMessage();
            Log.warning(exMsg);
            final String portStr = Integer.toString(this.port);
            final String msg = Res.fmt(Res.SRV_CFG_CANT_CONNECT, db, this.host, portStr);
            throw new SQLException(msg, ex);
        }
    }

    /**
     * Builds the JDBC URL used to create the JDBC connection to the server.
     *
     * @param theDb       the database configuration
     * @param theUser     the username for this connection
     * @param thePassword the password for this connection
     * @return the URL
     */
    private String buildJdbcUrl(final DbConfig theDb, final String theUser, final String thePassword) {

        final HtmlBuilder url = new HtmlBuilder(80);

        url.add("jdbc:");

        final String db = theDb.db;
        try {
            if (this.type == EDbInstallationType.INFORMIX) {
                url.add("informix-sqli://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, db, ":INFORMIXSERVER=", db, ";user=", theUser, ";password=",
                        thePassword, "; IFX_LOCK_MODE_WAIT=5; CLIENT_LOCALE=en_US.8859-1;");
            } else if (this.type == EDbInstallationType.ORACLE) {
                url.add("oracle:thin:", theUser, CoreConstants.SLASH, URLEncoder.encode(thePassword, ENC), "@",
                        this.host, CoreConstants.COLON, Integer.toString(this.port), CoreConstants.SLASH, db);
            } else if (this.type == EDbInstallationType.POSTGRESQL) {
                url.add("postgresql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, db, "?user=", theUser, "&password=", thePassword);
            } else if (this.type == EDbInstallationType.MYSQL) {
                url.add("mysql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, db, "?user=", theUser, "&password=", thePassword);
            } else if (this.type == EDbInstallationType.CASSANDRA) {
                // TODO: This is not JDBC
            }
        } catch (final UnsupportedEncodingException ex) {
            Log.warning(ex);
        }

        return url.toString();
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
            equal = test.id.equals(this.id) && test.type == this.type
                    && test.host.equals(this.host) && test.port == this.port
                    && Objects.equals(test.dbaUser, this.dbaUser);

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

        return this.id.hashCode() + this.type.hashCode() + this.host.hashCode() + this.port
                + Objects.hashCode(this.dbaUser);
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
        htm.add(this.type.name, " server ID '", this.id, "' at ", this.host, CoreConstants.COLON, portStr);

        if (this.dbaUser != null) {
            htm.add(" with DBA ", this.dbaUser);
        }

        return htm.toString();
    }
}
