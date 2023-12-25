package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;

/**
 * An immutable representation of a set of login credentials (username and password) for a specified database server.
 */
public final class CfgLogin implements Comparable<CfgLogin> {

    /** The element tag used in the XML representation of the configuration. */
    public static final String ELEM_TAG = "login";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** The user attribute. */
    private static final String USERNAME_ATTR = "username";

    /** The password attribute. */
    private static final String PASSWORD_ATTR = "password";

    /** The ID of the configuration (unique among all loaded configurations). */
    public final String id;

    /** The login username. */
    public final String username;

    /** The login password. */
    public final String password;

    /**
     * Constructs a new {@code Login}.
     *
     * @param theId       the login configuration ID
     * @param theUsername the login username
     * @param thePassword the login password
     */
    public CfgLogin(final String theId, final String theUsername, final String thePassword) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Login ID may not be null or blank.");
        }
        if (theUsername == null || theUsername.isBlank()) {
            throw new IllegalArgumentException("Login user name may not be null or blank.");
        }

        this.id = theId;
        this.username = theUsername;
        this.password = thePassword;
    }

    /**
     * Constructs a new {@code LoginConfig} from its XML representation.
     *
     * @param theElem the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgLogin(final EmptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id.isBlank()) {
                throw new ParsingException(theElem, "Login ID may not be blank.");
            }

            this.username = theElem.getRequiredStringAttr(USERNAME_ATTR);
            if (this.username.isBlank()) {
                throw new ParsingException(theElem, "Login user name may not be blank.");
            }

            // NOTE: Password is allowed to be blank, in which case applications should prompt for the password at
            // runtime, to prevent having to store plaintext passwords in configuration files
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

        if (obj instanceof final CfgLogin test) {
            equal = test.id.equals(this.id) && test.username.equals(this.username);
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

        return this.id.hashCode() + this.username.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("LoginConfig{id=", this.id, ",username=", this.username, "}");
    }

    /**
     * Compares this login to another for order. Order is based on username, then by ID.
     *
     * @param o the other login to which to compare
     */
    @Override
    public int compareTo(final CfgLogin o) {

        int result = this.username.compareTo(o.username);

        if (result == 0) {
            result = this.id.compareTo(o.id);
        }

        return result;
    }
}
