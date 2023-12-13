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

    /** The login username. */
    public String user;

    /** The login password. */
    public String password;

    /**
     * Constructs a new {@code LoginConfig}.
     *
     * @param theId       the login configuration ID
     * @param theUser     the login username
     * @param thePassword the login password
     */
    public LoginConfig(final String theId, final String theUser, final String thePassword) {

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
     * @param theElem   the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    LoginConfig(final EmptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            this.user = theElem.getRequiredStringAttr(USER_ATTR);
            this.password = theElem.getStringAttr(PASSWORD_ATTR);

            if (this.id == null || this.id.isBlank()) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(), "Login ID may not be null or blank.");
            }
            if (this.user == null || this.user.isBlank()) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        "Login user name may not be null or blank.");
            }
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
