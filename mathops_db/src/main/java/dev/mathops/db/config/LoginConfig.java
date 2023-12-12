package dev.mathops.db.config;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;

import java.sql.SQLException;

/**
 * Represents login credentials (username and password) for a database.
 */
public final class LoginConfig {

    /** The element tag used in the XML representation of the configuration. */
    public static final String ELEM_TAG = "login";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** The user attribute. */
    private static final String USER_ATTR = "user";

    /** The password attribute. */
    private static final String PASSWORD_ATTR = "password";

    /** The ID of the configuration (unique among all loaded configurations). */
    public final String id;

    /** The server configuration. */
    public final ServerConfig server;

    /** The login username. */
    public String user;

    /** The login password. */
    public String password;

    /**
     * Constructs a new {@code LoginConfig}.
     *
     * @param theId       the login configuration ID
     * @param theServer   the server configuration
     * @param theUser     the login username
     * @param thePassword the login password
     */
    public LoginConfig(final String theId, final ServerConfig theServer, final String theUser, final String thePassword) {

        this.id = theId;
        this.server = theServer;
        this.user = theUser;
        this.password = thePassword;
    }

    /**
     * Constructs a new {@code LoginConfig} from its XML representation.
     *
     * @param theServer the server configuration
     * @param theElem   the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    LoginConfig(final ServerConfig theServer, final EmptyElement theElem) throws ParsingException {

        this.server = theServer;

        if (ELEM_TAG.equals(theElem.getTagName())) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            this.user = theElem.getStringAttr(USER_ATTR);
            this.password = theElem.getStringAttr(PASSWORD_ATTR);
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.LOGIN_CFG_BAD_ELEM_TAG));
        }
    }

    /**
     * Sets the login parameters used for subsequent connections to the database.
     *
     * @param theUsername the username
     * @param thePassword the password
     */
    public void setLogin(final String theUsername, final String thePassword) {

        this.user = theUsername;
        this.password = thePassword;
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

        return this.server.openConnection(theUser, thePassword);
    }

    /**
     * Creates a new JDBC connection using this configuration.
     *
     * @return the new connection
     * @throws SQLException if the connection could not be opened
     */
    public AbstractGeneralConnection openConnection() throws SQLException {

        return this.server.openConnection(this.user, this.password);
    }

    /**
     * Tests whether this {@code LoginConfig} is equal to another object. To be equal, the other object must be a
     * {@code LoginConfig} with the same ID as this object.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final LoginConfig test) {
            equal = test.id.equals(this.id);
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

        return this.id.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("Login ", this.id, " as ", this.user);

        return builder.toString();
    }
}
