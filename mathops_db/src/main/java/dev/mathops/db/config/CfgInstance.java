package dev.mathops.db.config;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;
import dev.mathops.db.generalized.connection.JdbcGeneralConnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Represents an instance of a database product running on a server and listening on a TCP port.  A single server
 * could run multiple products, or multiple instances of the same product, listening on different ports.  Each
 * instance may then have multiple databases.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;instance id='...' type='...' host='...' port='...' name='...' dba='...'/&gt;
 * </pre>
 */
public final class CfgInstance implements Comparable<CfgInstance> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "instance";

    /** The instance ID attribute. */
    private static final String ID_ATTR = "id";

    /** The database product attribute. */
    private static final String PRODUCT_ATTR = "product";

    /** The host attribute. */
    private static final String HOST_ATTR = "host";

    /** The port attribute. */
    private static final String PORT_ATTR = "port";

    /** The instance name attribute. */
    private static final String NAME_ATTR = "name";

    /** The DBA login attribute. */
    private static final String DBA_ATTR = "dba";

    /** A character encoding. */
    private static final String ENC = "UTF-8";

    /** A common integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** The least valid TCP port number. */
    private static final int MIN_TCP_PORT = 1;

    /** The greatest valid TCP port number. */
    private static final int MAX_TCP_PORT = 65535;

    /** An empty array used when converting collections of web contexts to arrays. */
    private static final CfgDatabase[] EMPTY_DATABASE_ARRAY = new CfgDatabase[0];

    /** An empty array used when converting collections of web contexts to arrays. */
    private static final CfgLogin[] EMPTY_LOGIN_ARRAY = new CfgLogin[0];

    /** The instance ID. */
    public final String id;

    /** The database product. */
    public final EDbProduct product;

    /** The server host name (or IP address). */
    public final String host;

    /** The TCP port on which the server accepts JDBC connections. */
    public final int port;

    /** The instance name ({@code null} if not configured). */
    public final String name;

    /** The DBA username ({@code null} if not configured). */
    public final String dbaUser;

    /** A map from database ID to database configuration. */
    private final Map<String, CfgDatabase> databases;

    /** A map from login ID to login configuration. */
    private final Map<String, CfgLogin> logins;

    /**
     * Constructs a new {@code InstanceConfig}.
     *
     * @param theId        the instance ID
     * @param theProduct   the database product
     * @param theHost      the host name
     * @param thePort      the TCP port
     * @param theName      the instance name
     * @param theDbaUser   the DBA username (null if not configured)
     * @param theDatabases the child database configurations
     * @param theLogins    the child login configurations
     * @throws IllegalArgumentException if the id, type, or host is null, or the TCP port is invalid
     */
    public CfgInstance(final String theId, final EDbProduct theProduct, final String theHost, final int thePort,
                       final String theName, final String theDbaUser, final Collection<CfgDatabase> theDatabases,
                       final Collection<CfgLogin> theLogins) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Instance ID may not be null or blank.");
        }
        if (theProduct == null) {
            throw new IllegalArgumentException("Database product type may not be null.");
        }
        if (theHost == null || theHost.isBlank()) {
            throw new IllegalArgumentException("Host name may not be null or blank.");
        }
        if (thePort < MIN_TCP_PORT || thePort > MAX_TCP_PORT) {
            throw new IllegalArgumentException("Invalid TCP port number");
        }

        this.id = theId;
        this.product = theProduct;
        this.host = theHost;
        this.port = thePort;
        this.name = theName;
        this.dbaUser = theDbaUser;

        if (theDatabases == null || theDatabases.isEmpty()) {
            this.databases = new HashMap<>(0);
        } else {
            final int count = theDatabases.size();
            this.databases = new HashMap<>(count);

            for (final CfgDatabase database : theDatabases) {
                if (database != null) {
                    if (this.databases.containsKey(database.id)) {
                        throw new IllegalArgumentException("Duplicate database IDs in child databases collection");
                    }
                    this.databases.put(database.id, database);
                }
            }
        }

        if (theLogins == null || theLogins.isEmpty()) {
            this.logins = new HashMap<>(0);
        } else {
            final int count = theLogins.size();
            this.logins = new HashMap<>(count);

            for (final CfgLogin login : theLogins) {
                if (login != null) {
                    if (this.logins.containsKey(login.id)) {
                        throw new IllegalArgumentException("Duplicate login IDs in child logins collection");
                    }
                    this.logins.put(login.id, login);
                }
            }
        }
    }

    /**
     * Constructs a new {@code InstanceConfig} from its XML representation.
     *
     * @param theElem         the XML element from which to extract configuration settings.
     * @param globalDatabases the global map of database ID to database configuration
     * @param globalLogins    the global map of login ID to login configuration
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgInstance(final NonemptyElement theElem, final Map<String, CfgDatabase> globalDatabases,
                final Map<String, CfgLogin> globalLogins) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {

            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id.isBlank()) {
                throw new IllegalArgumentException("ID may not be blank.");
            }

            final String productStr = theElem.getRequiredStringAttr(PRODUCT_ATTR);
            this.product = EDbProduct.forName(productStr);
            if (this.product == null) {
                final String msg = Res.fmt(Res.INSTANCE_BAD_TYPE, productStr);
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

            this.name = theElem.getStringAttr(NAME_ATTR);
            this.dbaUser = theElem.getStringAttr(DBA_ATTR);

            this.databases = new HashMap<>(10);
            this.logins = new HashMap<>(10);

            for (final IElement child : theElem.getElementChildrenAsList()) {
                if (child instanceof final EmptyElement emptyChild) {
                    final String childTag = child.getTagName();

                    if (CfgDatabase.ELEM_TAG.equals(childTag)) {
                        final CfgDatabase database = new CfgDatabase(emptyChild);
                        if (this.databases.containsKey(database.id)) {
                            throw new IllegalArgumentException("Multiple databases in instance with id '"
                                    + database.id + "'");
                        }
                        if (globalDatabases.containsKey(database.id)) {
                            throw new IllegalArgumentException("Multiple databases with id '" + database.id + "'");
                        }
                        this.databases.put(database.id, database);
                        globalDatabases.put(database.id, database);
                    } else if (CfgLogin.ELEM_TAG.equals(childTag)) {
                        final CfgLogin login = new CfgLogin(emptyChild);
                        if (this.logins.containsKey(login.id)) {
                            throw new IllegalArgumentException("Multiple logins in instance with id '"
                                    + login.id + "'");
                        }
                        if (globalLogins.containsKey(login.id)) {
                            throw new IllegalArgumentException("Multiple logins with id '" + login.id + "'");
                        }
                        this.logins.put(login.id, login);
                        globalLogins.put(login.id, login);
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected non-empty child of <instance> element");
                }
            }
        } else {
            final String msg = Res.get(Res.INSTANCE_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Gets the database configuration map.
     *
     * @return an unmodifiable map from database ID to database configurations
     */
    public  Map<String, CfgDatabase> getDatabaseMap() {

        return Collections.unmodifiableMap(this.databases);
    }

    /**
     * Gets the database configurations in the instance.
     *
     * @return the array of database configurations
     */
    public CfgDatabase[] getDatabases() {

        return this.databases.values().toArray(EMPTY_DATABASE_ARRAY);
    }

    /**
     * Gets a database configuration.
     *
     * @param theId the database ID
     * @return the database configuration
     */
    public CfgDatabase getDatabase(final String theId) {

        return this.databases.get(theId);
    }

    /**
     * Gets the login configuration map.
     *
     * @return an unmodifiable map from login ID to login configurations
     */
    public  Map<String, CfgLogin> getLoginsMap() {

        return Collections.unmodifiableMap(this.logins);
    }

    /**
     * Gets the login configurations in the instance.
     *
     * @return the array of login configurations
     */
    public CfgLogin[] getLogins() {

        return this.logins.values().toArray(EMPTY_LOGIN_ARRAY);
    }

    /**
     * Gets a login configuration.
     *
     * @param theId the login ID
     * @return the login configuration
     */
    public CfgLogin getLogin(final String theId) {

        return this.logins.get(theId);
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
    public AbstractGeneralConnection openConnection(final CfgDatabase theDb, final String theUser,
                                                    final String thePassword) throws SQLException {

        final String dbName = theDb.name;

        try {
            final String url = buildJdbcUrl(theDb, theUser, thePassword);

            final Properties props = new Properties();

            final AbstractGeneralConnection conn;

            final Connection jdbcConn;
            if (this.product == EDbProduct.INFORMIX) {
                props.setProperty("CLIENT_LOCALE", "EN_US.8859-1");
                jdbcConn = DriverManager.getConnection(url, props);
            } else {
                // Log.info("Connect URL is " + url);
                jdbcConn = DriverManager.getConnection(url);
            }

            conn = new JdbcGeneralConnection(theDb.use, jdbcConn);

            // TODO: Add non-JDBC connections for non-JDBC database products

            final String productName = conn.getDatabaseProductName();
            Log.info("Connected to ", dbName, CoreConstants.SPC, productName);

            return conn;
        } catch (final SQLException | IllegalArgumentException ex) {
            final String exMsg = ex.getMessage();
            Log.warning(exMsg);
            final String portStr = Integer.toString(this.port);
            final String msg = Res.fmt(Res.SRV_CFG_CANT_CONNECT, dbName, this.host, portStr);
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
    private String buildJdbcUrl(final CfgDatabase theDb, final String theUser, final String thePassword) {

        final HtmlBuilder url = new HtmlBuilder(80);

        url.add("jdbc:");

        final String dbName = theDb.name;
        try {
            if (this.product == EDbProduct.INFORMIX) {
                url.add("informix-sqli://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, dbName, ":INFORMIXSERVER=", dbName, ";user=", theUser, ";password=",
                        thePassword, "; IFX_LOCK_MODE_WAIT=5; CLIENT_LOCALE=en_US.8859-1;");
            } else if (this.product == EDbProduct.ORACLE) {
                url.add("oracle:thin:", theUser, CoreConstants.SLASH, URLEncoder.encode(thePassword, ENC), "@",
                        this.host, CoreConstants.COLON, Integer.toString(this.port), CoreConstants.SLASH, dbName);
            } else if (this.product == EDbProduct.POSTGRESQL) {
                url.add("postgresql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, dbName, "?user=", theUser, "&password=", thePassword);
            } else if (this.product == EDbProduct.MYSQL) {
                url.add("mysql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, dbName, "?user=", theUser, "&password=", thePassword);
            } else if (this.product == EDbProduct.CASSANDRA) {
                // TODO: This is not JDBC
            }
        } catch (final UnsupportedEncodingException ex) {
            Log.warning(ex);
        }

        return url.toString();
    }

    /**
     * Tests whether this {@code InstanceConfig} is equal to another object. To be equal, the other object must be a
     * {@code InstanceConfig} and must have the same type, schema, host, port, and ID.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final CfgInstance test) {
            equal = test.id.equals(this.id) && test.product == this.product && test.host.equals(this.host)
                    && test.port == this.port && Objects.equals(test.name, this.name)
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

        return this.id.hashCode() + this.product.hashCode() + this.host.hashCode() + this.port
                + Objects.hashCode(this.name) + Objects.hashCode(this.dbaUser);
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

        htm.add("CfgInstance{id=", this.id, ",product=", this.product, ",host=", this.host, ",port=", portStr);
        if (this.name != null) {
            htm.add(",name=", this.name);
        }
        if (this.dbaUser != null) {
            htm.add(",dba=", this.dbaUser);
        }
        htm.add(",databases=['", this.databases, "],logins=[", this.logins, "]}");

        return htm.toString();
    }

    /**
     * Compares this instance configuration to another for order. Order is based on ID.
     *
     * @param o the other instance configuration to which to compare
     */
    @Override
    public int compareTo(final CfgInstance o) {

        return this.id.compareTo(o.id);
    }
}
