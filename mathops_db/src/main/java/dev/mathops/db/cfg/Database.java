package dev.mathops.db.cfg;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.EDbProduct;
import dev.mathops.text.builder.HtmlBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A "database" object from the database configuration file.
 */
public final class Database implements Comparable<Database> {

    /** A string used to construct JDBC URLs. */
    private static final String CLIENT_LOCALE = "CLIENT_LOCALE";

    /** A string used to construct JDBC URLs. */
    private static final String IFX_LOCALE = "EN_US.8859-1";

    /** The server that owns the database. */
    public final Server server;

    /** The database ID as defined by the product. */
    public final String id;

    /** The instance name, if needed by the product. */
    public final String instance;

    /** The username of the DBA if administration will be supported. */
    public final String dba;

    /** The logins available on the database. */
    private final List<Login> logins;

    /** The data available on the database. */
    private final List<Data> data;

    /**
     * Constructs a new {@code Database}.
     *
     * @param theServer   the server that owns the database
     * @param theId       the database ID, as defined by the product
     * @param theInstance the instance name, if needed by the product
     * @param theDba      the username of the DBA if administration will be supported
     * @throws IllegalArgumentException if the server or database name is null
     */
    Database(final Server theServer, final String theId, final String theInstance, final String theDba) {

        if (theServer == null || theId == null) {
            final String msg = Res.get(Res.DATABASE_NULL_SERVER_ID);
            throw new IllegalArgumentException(msg);
        }

        this.server = theServer;
        this.id = theId;
        this.instance = theInstance;
        this.dba = theDba;

        this.logins = new ArrayList<>(3);
        this.data = new ArrayList<>(10);
    }

    /**
     * Gets the list of databases provided by the server.
     *
     * @return the list of databases
     */
    public List<Login> getLogins() {

        return this.logins;
    }

    /**
     * Gets the list of data provided by the server.
     *
     * @return the list of data
     */
    public List<Data> getData() {

        return this.data;
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

        final String portStr = Integer.toString(this.server.port);

        if (this.server.type == EDbProduct.INFORMIX) {
            url.add("informix-sqli://", this.server.host, CoreConstants.COLON, portStr, CoreConstants.SLASH,
                    this.id, ":INFORMIXSERVER=", this.instance, ";user=", theUser, ";password=", thePassword,
                    "; IFX_LOCK_MODE_WAIT=5; CLIENT_LOCALE=en_US.8859-1;");
        } else if (this.server.type == EDbProduct.ORACLE) {
            final String encPassword = URLEncoder.encode(thePassword, StandardCharsets.UTF_8);
            url.add("oracle:thin:", theUser, CoreConstants.SLASH, encPassword, "@", this.server.host,
                    CoreConstants.COLON, portStr, CoreConstants.SLASH, this.id);
        } else if (this.server.type == EDbProduct.POSTGRESQL) {
            url.add("postgresql://", this.server.host, CoreConstants.COLON, portStr, CoreConstants.SLASH, this.id,
                    "?user=", theUser, "&password=", thePassword);
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
    Connection openConnection(final String theUser, final String thePassword) throws SQLException {

        try {
            final String url = buildJdbcUrl(theUser, thePassword);

            final Connection conn;

            if (this.server.type == EDbProduct.INFORMIX) {
                final Properties props = new Properties();
                props.setProperty(CLIENT_LOCALE, IFX_LOCALE);
                conn = DriverManager.getConnection(url, props);
            } else {
                // Log.info("Connect URL is " + url);
                conn = DriverManager.getConnection(url);
            }

            final DatabaseMetaData metadata = conn.getMetaData();
            final String productName = metadata.getDatabaseProductName();
            final String msg = this.instance == null ?
                    Res.fmt(Res.DATABASE_CONNECTED_TO_NO_INST, productName, this.id, theUser) :
                    Res.fmt(Res.DATABASE_CONNECTED_TO, this.instance, productName, this.id, theUser);
            Log.info(msg);

            return conn;
        } catch (final SQLException | IllegalArgumentException ex) {
            final String exMsg = ex.getMessage();
            Log.warning(exMsg);
            final String portStr = Integer.toString(this.server.port);
            final String msg = Res.fmt(Res.DATABASE_CANT_CONNECT, this.id, this.instance, this.server.host, portStr);
            throw new SQLException(msg, ex);
        }
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.server.hashCode() + this.id.hashCode();
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
        } else if (o instanceof final Database database) {
            equal = this.id.equals(database.id) && this.server.equals(database.server);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares two databases for order.  Comparison is based only on the database ID.
     *
     * @param o the object to be compared
     * @return 0 if this object is equal to {@code o}; -1 if this object precedes {@code o}; +1 if this object succeeds
     *         {@code o}
     */
    @Override
    public int compareTo(final Database o) {

        return this.id.compareTo(o.id);
    }

    /**
     * Generates a string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.instance == null ? (this.id + " on " + this.server)
                : (this.id + " (" + this.instance + ") on " + this.server);
    }
}
