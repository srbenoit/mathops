package dev.mathops.db.old.cfg;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.db.EDbProduct;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Represents a database present on a server. Each defined server will have zero or more of these objects.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;db id='...' use='...' schemata='(comma-separated list)'&gt;
 *   ... zero or more &lt;login&gt; child elements ...
 * &lt;/db&gt;
 * </pre>
 */
public final class DbConfig implements Comparable<DbConfig> {

    /** The element tag used in the XML representation of the configuration. */
    private static final String ELEM_TAG = "db";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** The use attribute. */
    private static final String USE_ATTR = "use";

    /** The schemata attribute. */
    private static final String SCHEMATA_ATTR = "schemata";

    /** A colon as part of a JDBC URL. */
    private static final String COLON = CoreConstants.COLON;

    /** A slash as part of a JDBC URL. */
    private static final String SLASH = CoreConstants.SLASH;

    /** A character encoding. */
    private static final String ENC = "UTF-8";

    /** The owning server configuration. */
    public final ServerConfig server;

    /** The database ID. */
    public final String id;

    /** The use. */
    public final EDbUse use;

    /** The schemata. */
    private final List<SchemaConfig> schemata;

    /** The logins. */
    private final List<LoginConfig> logins;

    /**
     * Constructs a new {@code DbConfig} from its XML representation.
     *
     * @param theServer the owning server configuration
     * @param schemaMap the schema configuration map
     * @param loginMap  the login configuration map
     * @param theElem   the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    DbConfig(final ServerConfig theServer, final Map<String, SchemaConfig> schemaMap,
             final Map<String, LoginConfig> loginMap, final NonemptyElement theElem)
            throws ParsingException {

        this.server = theServer;

        if (ELEM_TAG.equals(theElem.getTagName())) {

            this.id = theElem.getRequiredStringAttr(ID_ATTR);

            final String useString = theElem.getRequiredStringAttr(USE_ATTR);
            this.use = EDbUse.forName(useString);
            if (this.use == null) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.DB_CFG_BAD_USE, useString));
            }

            final String[] list =
                    theElem.getRequiredStringAttr(SCHEMATA_ATTR).split(CoreConstants.COMMA);
            this.schemata = new ArrayList<>(list.length);
            for (final String sch : list) {
                final SchemaConfig cfg = schemaMap.get(sch);
                if (cfg == null) {
                    throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                            Res.fmt(Res.DB_CFG_BAD_SCHEMA, sch));
                }
                this.schemata.add(cfg);
            }

            final int count = theElem.getNumChildren();
            this.logins = new ArrayList<>(count);

            for (int i = 0; i < count; ++i) {
                final INode child = theElem.getChild(i);
                if (child instanceof EmptyElement) {
                    final LoginConfig login = new LoginConfig(this, (EmptyElement) child);
                    if (loginMap.get(login.id) != null) {
                        throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                                Res.fmt(Res.DB_CFG_DUP_LOGIN_ID, login.id));
                    }
                    this.logins.add(login);
                    loginMap.put(login.id, login);
                }
            }
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.DB_CFG_BAD_ELEM_TAG));
        }
    }

    /**
     * Gets the list of schemata supported by the database.
     *
     * @return an unmodifiable view of the list of schemata
     */
    public List<SchemaConfig> getSchemata() {

        return Collections.unmodifiableList(this.schemata);
    }

    /**
     * Gets the list of logins configured for the database.
     *
     * @return an unmodifiable view of the list of logins
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
            if (this.server.type == EDbProduct.INFORMIX) {
                url.add("informix-sqli://", this.server.host, COLON, Integer.toString(this.server.port), SLASH,
                        this.id, ":INFORMIXSERVER=", this.server.name, ";user=", theUser, ";password=", thePassword,
                        "; IFX_LOCK_MODE_WAIT=5; CLIENT_LOCALE=en_US.8859-1;");
            } else if (this.server.type == EDbProduct.ORACLE) {
                url.add("oracle:thin:", theUser, SLASH, URLEncoder.encode(thePassword, ENC), "@", this.server.host,
                        COLON, Integer.toString(this.server.port), SLASH, this.id);
            } else if (this.server.type == EDbProduct.POSTGRESQL) {
                url.add("postgresql://", this.server.host, COLON, Integer.toString(this.server.port), SLASH, this.id,
                        "?user=", theUser, "&password=", thePassword);
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
    Connection openConnection(final String theUser, final String thePassword) throws SQLException {

        try {
            final String url = buildJdbcUrl(theUser, thePassword);

            final Properties props = new Properties();

            final Connection conn;

            if (this.server.type == EDbProduct.INFORMIX) {
                props.setProperty("CLIENT_LOCALE", "EN_US.8859-1");
                conn = DriverManager.getConnection(url, props);
            } else {
                // Log.info("Connect URL is " + url);
                conn = DriverManager.getConnection(url);
            }

            Log.info("Connected to ", this.use.name, CoreConstants.SPC, conn.getMetaData().getDatabaseProductName());

            return conn;
        } catch (final SQLException | IllegalArgumentException ex) {
            Log.warning(ex.getMessage());
            throw new SQLException(Res.fmt(Res.DB_CFG_CANT_CONNECT, this.id, this.server.name,
                    this.server.host, Integer.toString(this.server.port)), ex);
        }
    }

    /**
     * Tests whether this {@code DriverConfig} is equal to another object. To be equal, the other object must be a
     * {@code DriverConfig} and must represent the same host name and path.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final DbConfig test) {
            equal = test.id.equals(this.id) && test.use == this.use && test.schemata.equals(this.schemata);
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

        return this.id.hashCode() + this.use.hashCode() + this.schemata.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.id + " on " + this.server.name;
    }

    /**
     * Compares this profile to another for order. Order is based on ID.
     *
     * @param o the other profile to which to compare
     */
    @Override
    public int compareTo(final DbConfig o) {

        int result;

        if (o == null) {
            result = -1;
        } else {
            result = this.server.name.compareTo(o.server.name);

            if (result == 0) {
                result = this.id.compareTo(o.id);
            }
        }

        return result;
    }
}
