package dev.mathops.app.db.config.model;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.EDbProduct;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * The data model of a database product running on a server and listening on a TCP port.  A single server could run
 * multiple products, or multiple instances of the same product, listening on different ports.  Each instance may then
 * have multiple databases.
 *
 * <p>
 * To create a new {@code CfgInstanceModel}, the UI would present fields to enter its ID, select the database product,
 * enter the hostname and port, and supply an instance name (if needed by the database product) and optional DBA
 * username, with a button to execute the addition, which would fail if any attribute was invalid.  The new instance
 * would have no database or login configurations.
 *
 * <p>
 * If the instance ID is updated and an "apply" button pressed in a GUI, the new ID is tested for uniqueness within
 * the owning database layer configuration before being accepted.  Any other fields can be updated with any valid
 * values.
 *
 * <p>
 * Once an instance is created, a GUI should allow users to add database or login configurations within that instance,
 * or delete existing database or login configurations, if they are not referenced elsewhere.
 *
 * <p>
 * A GUI should support deletion of an instance, but this should succeed only if none of the database or login
 * configurations within that instance are referenced.
 */
public final class CfgInstanceModel {

    /** The instance ID. */
    public final StringPropertyBase id;

    /** The database product. */
    public final ObjectPropertyBase<EDbProduct> product;

    /** The server host name (or IP address). */
    public final StringPropertyBase host;

    /** The TCP port on which the server accepts JDBC connections. */
    public final IntegerPropertyBase port;

    /** The instance name ({@code null} if not configured). */
    public final StringPropertyBase name;

    /** The DBA username ({@code null} if not configured). */
    public final StringPropertyBase dbaUser;

    /** A mutable map from database ID to the mutable database configuration. */
    private final ObservableMap<String, CfgDatabaseModel> databases;

    /** A mutable map from login ID to the mutable login configuration. */
    private final ObservableMap<String, CfgLoginModel> logins;

    /**
     * Constructs a new, empty {@code CfgInstanceModel}.
     */
    public CfgInstanceModel() {

        this.id = new SimpleStringProperty();
        this.product = new SimpleObjectProperty<>();
        this.host = new SimpleStringProperty();
        this.port = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.dbaUser = new SimpleStringProperty();
        this.databases = FXCollections.observableHashMap();
        this.logins = FXCollections.observableHashMap();
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
     * Gets the product property.
     *
     * @return the product property
     */
    public ObjectPropertyBase<EDbProduct> getProductProperty() {

        return this.product;
    }

    /**
     * Gets the host property.
     *
     * @return the host property
     */
    public StringPropertyBase getHostProperty() {

        return this.host;
    }

    /**
     * Gets the port property.
     *
     * @return the port property
     */
    public IntegerPropertyBase getPortProperty() {

        return this.port;
    }

    /**
     * Gets the name property.
     *
     * @return the name property
     */
    public StringPropertyBase getNameProperty() {

        return this.name;
    }

    /**
     * Gets the DBA user property.
     *
     * @return the DBA user property
     */
    public StringPropertyBase getDbaUserProperty() {

        return this.dbaUser;
    }

    /**
     * Gets the databases property.
     *
     * @return the databases property
     */
    public ObservableMap<String, CfgDatabaseModel> getDatabasesProperty() {

        return this.databases;
    }

    /**
     * Gets the logins property.
     *
     * @return the logins property
     */
    public ObservableMap<String, CfgLoginModel> getLoginsProperty() {

        return this.logins;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgInstanceModel{id='", this.id, "',product='", this.product, "',host='",
                this.host, "',port='", this.port, "',name='", this.name, "',dbaUser='", this.dbaUser, "',databases=[",
                this.databases, "],logins=[", this.logins, "]}");
    }
}