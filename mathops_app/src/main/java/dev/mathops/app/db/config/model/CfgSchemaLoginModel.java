package dev.mathops.app.db.config.model;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.config.ESchemaType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The data model of a schema login configuration, which defines database and login configurations for a particular
 * schema.
 *
 * <p>
 * To create a new {@code CfgSchemaLoginModel}, the UI would present the schema as a fixed field, and allow the
 * user to select a database and login from those already defined, with a button to execute the addition.  Creating a
 * schema login adds the owning data profile object to the list of those referencing the selected database and the
 * selected login.
 */
public final class CfgSchemaLoginModel {

    /** The owning data profile. */
    private final CfgDataProfileModel owner;

    /** The schema. */
    private final ObjectProperty<ESchemaType> schema;

    /** The database ID. */
    private final StringProperty database;

    /** The database ID. */
    private final StringProperty login;

    /**
     * Constructs a new {@code CfgSchemaLoginModel}.
     *
     * @param theOwner      the owning mutable data profile
     * @param theSchema     the schema
     * @param theDatabaseId the database ID
     * @param theLoginId    the login ID
     */
    public CfgSchemaLoginModel(final CfgDataProfileModel theOwner, final ESchemaType theSchema,
                               final String theDatabaseId, final String theLoginId) {

        if (theOwner == null) {
            throw new IllegalArgumentException("Owning data profile may not be null");
        }
        if (theSchema == null) {
            throw new IllegalArgumentException("Schema may not be null or blank");
        }
        if (theDatabaseId == null) {
            throw new IllegalArgumentException("Database ID may not be null");
        }
        if (theLoginId == null) {
            throw new IllegalArgumentException("Login ID may not be null");
        }

        this.owner = theOwner;
        this.schema = new SimpleObjectProperty<>(theSchema);
        this.database = new SimpleStringProperty(theDatabaseId);
        this.login = new SimpleStringProperty(theLoginId);
    }

    /**
     * Gets the owning mutable web context.
     *
     * @return the owning mutable web context
     */
    public CfgDataProfileModel getOwner() {

        return this.owner;
    }

    /**
     * Gets the schema property.
     *
     * @return the schema property
     */
    public ObjectProperty<ESchemaType> getSchemaProperty() {

        return this.schema;
    }

    /**
     * Gets the database property.
     *
     * @return the database property
     */
    public StringProperty getDatabaseProperty() {

        return this.database;
    }

    /**
     * Gets the login property.
     *
     * @return the login property
     */
    public StringProperty getLoginProperty() {

        return this.login;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgSchemaLoginModel{schema='", this.schema, "',database='", this.database,
                "',login='", this.login, "'}");
    }
}
