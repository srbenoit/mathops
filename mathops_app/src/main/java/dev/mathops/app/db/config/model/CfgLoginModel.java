package dev.mathops.app.db.config.model;

import dev.mathops.commons.builder.HtmlBuilder;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

import java.util.HashSet;
import java.util.Set;

/**
 * The data model of a set of login credentials (username and password) for a specified database server.
 *
 * <p>
 * To create a new {@code CfgLoginModel}, the UI would present fields to enter its ID and username, and optionally
 * to provide a password, with a button to execute the addition, which would fail if the selected login ID was
 * duplicated or the username was empty.
 *
 * <p>
 * If the login ID is updated and an "apply" button pressed in a GUI, the new ID is tested for uniqueness before
 * being accepted.  Any other fields can be updated with any valid values.
 *
 * <p>
 * A GUI should support deletion of a login, but this should succeed only of it is not referenced by a data profile.
 */
public final class CfgLoginModel {

    /** The login ID. */
    public final StringPropertyBase id;

    /** The username. */
    public final StringPropertyBase username;

    /** The password. */
    public final StringPropertyBase password;

    /** The set of {@code MutableCfgDataProfile} objects that reference this login. */
    private final transient Set<CfgDataProfileModel> references;

    /**
     * Constructs a new, empty {@code CfgLoginModel}.
     */
    public CfgLoginModel() {

        this.id = new SimpleStringProperty();
        this.username = new SimpleStringProperty();
        this.password = new SimpleStringProperty();
        this.references = new HashSet<>(10);
    }

    /**
     * Gets the ID property.
     *
     * @return the ID property
     */
    public StringPropertyBase getIdProperty() {

        return this.id;
    }

    /**
     * Gets the username property.
     *
     * @return the username property
     */
    public StringPropertyBase getUsernameProperty() {

        return this.username;
    }

    /**
     * Gets the password property.
     *
     * @return the password property
     */
    public StringPropertyBase getPasswordProperty() {

        return this.password;
    }

    /**
     * Indicates that a {@code MutableCfgDataProfile} is referencing this object.
     *
     * @param referencer the referencing {@code MutableCfgDataProfile}
     */
    public void addReference(final CfgDataProfileModel referencer) {

        this.references.add(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgDataProfile} is no longer referencing this object.
     *
     * @param referencer the no-longer referencing {@code MutableCfgDataProfile}
     */
    public void deleteReference(final CfgDataProfileModel referencer) {

        this.references.remove(referencer);
    }

    /**
     * Gets the number of {@code MutableCfgDataProfile} currently referencing this object.
     *
     * @return the number of references
     */
    public int getNumReferences() {

        return this.references.size();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("CfgLoginModel{id='", this.id, "',username='", this.username, "'");
        if (this.password.get() != null) {
            htm.add(", password='['", this.password, "'");
        }
        htm.add("}");

        return htm.toString();
    }
}
