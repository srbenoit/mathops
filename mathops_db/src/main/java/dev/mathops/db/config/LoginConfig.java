package dev.mathops.db.config;

import dev.mathops.core.builder.HtmlBuilder;
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

    /** The user attribute. */
    private static final String USER_ATTR = "user";

    /** The password attribute. */
    private static final String PASSWORD_ATTR = "password";

    /** The ID of the configuration (unique among all loaded configurations). */
    public final String id;

    /** The login username. */
    public final String user;

    /** The login password. */
    public final String password;

    /**
     * Constructs a new {@code LoginConfig}.
     *
     * @param theId       the login configuration ID
     * @param theUser     the login username
     * @param thePassword the login password
     */
    public LoginConfig(final String theId, final String theUser,
                       final String thePassword) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Login ID may not be null or blank.");
        }
        if (theUser == null || theUser.isBlank()) {
            throw new IllegalArgumentException("Login user name may not be null or blank.");
        }

        this.id = theId;
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
     * {@code LoginConfig} with the same ID and username as this object.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final LoginConfig test) {
            equal = test.id.equals(this.id) && test.user.equals(this.user);
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

        return this.id.hashCode() + this.user.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("LoginConfig{id='", this.id, "',user='", this.user, "',password='", this.password, "'");

        return builder.toString();
    }
}
