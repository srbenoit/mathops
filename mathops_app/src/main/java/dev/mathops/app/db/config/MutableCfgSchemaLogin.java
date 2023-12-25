package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.CfgDatabase;
import dev.mathops.db.config.CfgLogin;
import dev.mathops.db.config.CfgSchemaLogin;
import dev.mathops.db.config.ESchemaType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;

/**
 * A mutable model of a schema login configuration, which defines database and login configurations for a particular
 * schema.
 *
 * <p>
 * To create a new {@code MutableCfgSchemaLogin}, the UI would present the schema as a fixed field, and allow the
 * user to select a database and login from those already defined, with a button to execute the addition.  Creating a
 * schema login adds the owning data profile object to the list of those referencing the selected database and the
 * selected login.
 */
public final class MutableCfgSchemaLogin {

    /** The owning data profile. */
    private final MutableCfgDataProfile owner;

    /** The schema. */
    private ObjectProperty<ESchemaType> schema;

    /** The database ID. */
    private StringProperty database;

    /** The database ID. */
    private StringProperty login;

    /**
     * Constructs a new {@code MutableCfgSchemaLogin}.
     *
     * @param theOwner      the owning mutable data profile
     * @param theSchema     the schema
     * @param theDatabaseId the database ID
     * @param theLoginId    the login ID
     */
    public MutableCfgSchemaLogin(final MutableCfgDataProfile theOwner, final ESchemaType theSchema,
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
     * Constructs a new {@code MutableCfgSchemaLogin} from a {@code CfgSchemaLogin}.
     *
     * @param theOwner the owning mutable web context
     * @param source   the source {@code CfgSchemaLogin}
     */
    public MutableCfgSchemaLogin(final MutableCfgDataProfile theOwner, final CfgSchemaLogin source) {

        if (theOwner == null) {
            throw new IllegalArgumentException("Owning web context may not be null");
        }

        this.owner = theOwner;
        this.schema = new SimpleObjectProperty<>(source.schema);
        this.database = new SimpleStringProperty(source.database.id);
        this.login = new SimpleStringProperty(source.login.id);
    }

    /**
     * Gets the owning mutable web context.
     *
     * @return the owning mutable web context
     */
    public MutableCfgDataProfile getOwner() {

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
     * Generate an immutable {@code CfgSchemaLogin} from this object.
     *
     * @param databases a map from database ID to the database object
     * @param logins    a map from login ID to the login object
     * @return the generated {@code CfgSchemaLogin}
     */
    CfgSchemaLogin toCfgSchemaLogin(final Map<String, CfgDatabase> databases, final Map<String, CfgLogin> logins) {

        final ESchemaType schemaValue = this.schema.get();
        final String databaseStr = this.database.get();
        final String loginStr = this.login.get();

        final CfgDatabase databaseObj = databases.get(databaseStr);
        final CfgLogin loginObj = logins.get(loginStr);

        return new CfgSchemaLogin(schemaValue, databaseObj, loginObj);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableCfgSchemaLogin{schema='", this.schema, "',database='", this.database,
                "',login='", this.login, "'}");
    }
}
