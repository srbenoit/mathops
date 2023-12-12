package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.db.generalized.connection.AbstractGeneralConnection;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Represents DBA login credentials (username and password) for a database.
 */
public final class DbaLoginConfig {

    /** The element tag used in the XML representation of the configuration. */
    public static final String ELEM_TAG = "dbalogin";

    /** The user attribute. */
    private static final String USER_ATTR = "user";

    /** The login username. */
    public final String user;

    /**
     * Constructs a new {@code DbaLoginConfig}.
     *
     * @param theUser the login username
     */
    public DbaLoginConfig(final String theUser) {

        this.user = theUser;
    }

    /**
     * Constructs a new {@code DbaLoginConfig} from its XML representation.
     *
     * @param theElem   the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    DbaLoginConfig(final EmptyElement theElem) throws ParsingException {

        if (ELEM_TAG.equals(theElem.getTagName())) {
            this.user = theElem.getStringAttr(USER_ATTR);
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.LOGIN_CFG_BAD_ELEM_TAG));
        }
    }

    /**
     * Creates a new connection using this configuration.
     *
     * @param theServer   the server to which to connect
     * @param theUser     the username for this connection
     * @param thePassword the password for this connection
     * @return the new connection
     * @throws SQLException if the connection could not be opened
     */
    public AbstractGeneralConnection openConnection(final ServerConfig theServer, final String theUser,
                                                    final String thePassword) throws SQLException {

        return theServer.openConnection(theUser, thePassword);
    }

    /**
     * Creates a new connection using this configuration.
     *
     * @param theServer   the server to which to connect
     * @param password the password
     * @return the new connection
     * @throws SQLException if the connection could not be opened
     */
    public AbstractGeneralConnection openConnection(final ServerConfig theServer, final String password)
            throws SQLException {

        return theServer.openConnection(this.user, password);
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

        if (obj instanceof final DbaLoginConfig test) {
            equal = Objects.equals(test.user, this.user);
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

        return this.user.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("DBA Login as ", this.user);
    }
}
