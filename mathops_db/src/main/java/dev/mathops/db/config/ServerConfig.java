package dev.mathops.db.config;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.generalized.connection.JdbcGeneralConnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 * &lt;server type='...' schema='...' host='...' port='...' id='...'&gt;
 *   ... zero or more &lt;login&gt; child elements ...
 * &lt;/server&gt;
 * </pre>
 */
public final class ServerConfig {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "server";

    /** The server type attribute. */
    private static final String TYPE_ATTR = "type";

    /** The schema attribute. */
    private static final String SCHEMA_ATTR = "schema";

    /** The host attribute. */
    private static final String HOST_ATTR = "host";

    /** The port attribute. */
    private static final String PORT_ATTR = "port";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** A character encoding. */
    private static final String ENC = "UTF-8";

    /** A common integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** The server installation type. */
    public final EDbInstallationType type;

    /** The schema type. */
    public final ESchemaType schema;

    /** The server host name (or IP address). */
    public final String host;

    /** The TCP port on which the server accepts JDBC connections. */
    public final int port;

    /** The database ID, if required by the database driver. */
    public final String id;

    /** The DBA login, if one is configured. */
    public final DbaLoginConfig dbaLogin;

    /** The logins. */
    private final List<LoginConfig> logins;

    /**
     * Constructs a new {@code ServerConfig} from its XML representation.
     *
     * @param theLoginMap  the login map
     * @param theElem      the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    ServerConfig(final Map<String, LoginConfig> theLoginMap, final NonemptyElement theElem) throws ParsingException {

        if (ELEM_TAG.equals(theElem.getTagName())) {

            DbaLoginConfig dba = null;

            this.type = EDbInstallationType.forName(theElem.getRequiredStringAttr(TYPE_ATTR));
            if (this.type == null) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.SRV_CFG_BAD_TYPE, theElem.getRequiredStringAttr(TYPE_ATTR)));
            }

            this.schema = ESchemaType.forName(theElem.getRequiredStringAttr(SCHEMA_ATTR));
            if (this.schema == null) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.SRV_CFG_BAD_SCHEMA, theElem.getRequiredStringAttr(SCHEMA_ATTR)));
            }

            this.host = theElem.getRequiredStringAttr(HOST_ATTR);
            this.port = theElem.getIntegerAttr(PORT_ATTR, ZERO).intValue();
            this.id = theElem.getStringAttr(ID_ATTR);

            final int count = theElem.getNumChildren();
            this.logins = new ArrayList<>(count);

            for (int i = 0; i < count; ++i) {
                final INode child = theElem.getChild(i);
                if (child instanceof final EmptyElement childElem) {
                    final String tag = childElem.getTagName();

                    if (LoginConfig.ELEM_TAG.equals(tag)) {
                        final LoginConfig login = new LoginConfig(this, childElem);

                        if (theLoginMap.containsKey(login.id)) {
                            throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                                    Res.fmt(Res.SRV_CFG_DUP_LOGIN_ID, login.id));
                        }

                        this.logins.add(login);
                        theLoginMap.put(login.id, login);
                    } else if (DbaLoginConfig.ELEM_TAG.equals(tag)) {
                        if (dba == null) {
                            dba = new DbaLoginConfig(childElem);
                        } else {
                            Log.warning("Multiple 'dbalogin' elements are not allowed in one server.");
                        }
                    }
                } else {
                    Log.warning("Unexpected child element of 'server'.");
                }
            }

            this.dbaLogin = dba;
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(),Res.get(Res.SRV_CFG_BAD_ELEM_TAG));
        }
    }

    /**
     * Gets the list of logins.
     *
     * @return the list of logins
     */
    public List<LoginConfig> getLogins() {

        return Collections.unmodifiableList(this.logins);
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
                        CoreConstants.SLASH, this.id, ":INFORMIXSERVER=", this.id, ";user=", theUser, ";password=",
                        thePassword, "; IFX_LOCK_MODE_WAIT=5; CLIENT_LOCALE=en_US.8859-1;");
            } else if (this.type == EDbInstallationType.ORACLE) {
                url.add("oracle:thin:", theUser, CoreConstants.SLASH, URLEncoder.encode(thePassword, ENC), "@",
                        this.host, CoreConstants.COLON, Integer.toString(this.port), CoreConstants.SLASH, this.id);
            } else if (this.type == EDbInstallationType.POSTGRESQL) {
                url.add("postgresql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, this.id, "?user=", theUser, "&password=", thePassword);
            } else if (this.type == EDbInstallationType.MYSQL) {
                url.add("mysql://", this.host, CoreConstants.COLON, Integer.toString(this.port),
                        CoreConstants.SLASH, this.id, "?user=", theUser, "&password=", thePassword);
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
    AbstractGeneralConnection openConnection(final String theUser, final String thePassword) throws SQLException {

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

            Log.info("Connected to ", this.id, CoreConstants.SPC, conn.getDatabaseProductName());

            return conn;
        } catch (final SQLException | IllegalArgumentException ex) {
            Log.warning(ex.getMessage());
            throw new SQLException(Res.fmt(Res.SRV_CFG_CANT_CONNECT, this.id, this.host, Integer.toString(this.port)),
                    ex);
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
                    && test.port == this.port && Objects.equals(test.id, this.id);
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
                + EqualityTests.objectHashCode(this.id);
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add(this.type.name, " server implementing the ", this.schema.name, " schema at ", this.host,
                CoreConstants.COLON, Integer.toString(this.port));

        if (this.id != null) {
            htm.add(" with id ", this.id);
        }

        return htm.toString();
    }
}
