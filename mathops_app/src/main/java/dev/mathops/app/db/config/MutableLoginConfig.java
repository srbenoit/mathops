package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.LoginConfig;

/**
 * A mutable model of a database server configuration that can be edited through a GUI.
 */
public final class MutableLoginConfig {

    /** The login ID. */
    private String id;

    /** The username. */
    private String user;

    /** The password. */
    private String password;

    /**
     * Constructs a new {@code MutableLoginConfig}.
     *
     * @param theId the login ID (must not be null or blank)
     * @param theUser the login ID (must not be null or blank)
     * @param thePassword the login ID (can be null or blank)
     */
    public MutableLoginConfig(final String theId, final String theUser, final String thePassword) {

        update(theId, theUser, thePassword);
    }

    /**
     * Constructs a new {@code MutableLoginConfig} from a {@code LoginConfig}.
     *
     * @param source the source {@code LoginConfig}
     */
    public MutableLoginConfig(final LoginConfig source) {

        this.id = source.id;
        this.user = source.user;
        this.password = source.password;
    }

    /**
     * Gets the login ID.
     *
     * @return the login ID
     */
    public String getId() {

        return this.id;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUser() {

        return this.user;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {

        return this.password;
    }

    /**
     * Updates the field values.
     *
     * @param theId the login ID (must not be null or blank)
     * @param theUser the login ID (must not be null or blank)
     * @param thePassword the login ID (can be null or blank)
     */
    public void update(final String theId, final String theUser, final String thePassword) {

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
     * Generate an immutable {@code LoginConfig} from this object.
     *
     * @return the generated {@code LoginConfig}
     */
    LoginConfig toLoginConfig() {

        return new LoginConfig(this.id, this.user, this.password);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableLoginConfig{id='", this.id, "',user='", this.user, "',password='",
                this.password, "'}");
    }
}
