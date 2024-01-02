package dev.mathops.app.db.config.model;

import dev.mathops.core.builder.SimpleBuilder;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * The data model of a web context (as specified by a web server host), which defines a list of sites within that web
 * context.
 *
 * <p>
 * To create a new {@code CfgWebContextModel}, the UI would present fields to enter its host name, with a button to
 * execute the addition, which would fail if entered host name is not unique.  The new instance would have no sites.
 *
 * <p>
 * If the host is updated and an "apply" button pressed in a GUI, the new host is tested for uniqueness before being
 * accepted.
 *
 * <p>
 * Once a web context is created, a GUI should allow users to add site configurations within that web context,
 * or delete existing site configurations.
 *
 * <p>
 * A GUI should support deletion of a web context.
 */
public final class CfgWebContextModel {

    /** The host. */
    public final StringPropertyBase host;

    /** A mutable map from site path to the mutable site configuration. */
    private final ObservableMap<String, CfgSiteModel> sites;

    /**
     * Constructs a new, empty {@code CfgWebContextModel}.
     */
    public CfgWebContextModel() {

        this.host = new SimpleStringProperty();
        this.sites = FXCollections.observableHashMap();
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
     * Gets the sites property.
     *
     * @return the sites property
     */
    public ObservableMap<String, CfgSiteModel> getSites() {

        return this.sites;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgWebContextModel{host='", this.host, "',sites=[", this.sites, "]}");
    }
}
