package dev.mathops.db.config;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;

import java.util.Map;

/**
 * An immutable set of login credentials (username and password) for a specified database server.
 */
public final class LoginConfig {

    /** The element tag used in the XML representation of the configuration. */
    public static final String ELEM_TAG = "login";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** The server ID attribute. */
    private static final String SERVER_ATTR = "server";

    /** The user attribute. */
    private static final String USER_ATTR = "user";

    /** The password attribute. */
    private static final String PASSWORD_ATTR = "password";

    /** The ID of the configuration (unique among all loaded configurations). */
    public final String id;

    /** The ID of the server configuration to which this login applies. */
    public final String server;

    /** The login username. */
    public String user;

    /** The login password. */
    public String password;

    /**
     * Constructs a new {@code LoginConfig}.
     *
     * @param theId       the login configuration ID
     * @param theServer   the server configuration to which this login applies
     * @param theUser     the login username
     * @param thePassword the login password
     */
    public LoginConfig(final String theId, final String theServer, final String theUser,
                       final String thePassword) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Login ID may not be null or blank.");
        }
        if (theServer == null || theId.isBlank()) {
            throw new IllegalArgumentException("Server ID may not be null or blank");
        }
        if (theUser == null || theUser.isBlank()) {
            throw new IllegalArgumentException("Login user name may not be null or blank.");
        }

        this.id = theId;
        this.server = theServer;
        this.user = theUser;
        this.password = thePassword;
    }

    /**
     * Constructs a new {@code LoginConfig} from its XML representation.
     *
     * @param theElem the XML element from which to extract configuration settings.
     * @param servers a map from server ID to server configuration
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    LoginConfig(final EmptyElement theElem, final Map<String, ServerConfig> servers) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id == null || this.id.isBlank()) {
                throw new ParsingException(theElem, "Login ID may not be null or blank.");
            }

            this.server = theElem.getRequiredStringAttr(SERVER_ATTR);
            if (!servers.containsKey(this.server)) {
                final String msg = SimpleBuilder.concat("Server '", this.server,
                        "' referenced in login is not defined");
                throw new ParsingException(theElem, msg);
            }

            this.user = theElem.getRequiredStringAttr(USER_ATTR);
            if (this.user == null || this.user.isBlank()) {
                throw new ParsingException(theElem, "Login user name may not be null or blank.");
            }

            // NOTE: Password is allowed to be blank - applications should prompt for the password at runtime, to
            // prevent having to store plaintext passwords in configuration files
            this.password = theElem.getStringAttr(PASSWORD_ATTR);
        } else {
            final String msg = Res.get(Res.LOGIN_CFG_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
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

        builder.add("LoginConfig{id='", this.id, "',server='", this.server, "',user='", this.user, "',password='",
                this.password, "'");

        return builder.toString();
    }
}
