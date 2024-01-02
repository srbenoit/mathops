package dev.mathops.app.db.config.model;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.config.CfgDatabaseLayer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * The data model of the database layer.  This object provides a map from named web context (host and path) or named
 * code context to the data profile that will be used to connect to the database for that context. This can be used to
 * select different profiles for various named contexts.
 *
 * <p>
 * A data profile chooses a database server and login for each defined schema.  A database server represents an
 * installation of a database product, such as MySQL or PostgreSQL, on a server machine, and a login represents
 * a username/password combination to connect to that database server product.
 *
 * <p>
 * Typically, each website or logical grouping of code will use a distinct named context. That way, the entire site or
 * application will use a common database (with consistent data), but can be changed as a unit to another database for
 * testing or to provide alternate data.
 *
 * <p>
 * To create a new {@code MutableCfgSite}, the UI would present a field to enter
 * a path, and a dropdown to select a profile, with a button to execute the addition, which would fail if the path
 * were empty or not unique, or if the profile had not been selected.  Creating a new site adds this object to the list
 * of dependencies on the selected profile.
 *
 * <p>
 * If the path is updated and an "apply" button pressed in a GUI, the new path is tested for uniqueness within the
 * owning web context.
 */
public final class CfgDatabaseLayerModel {

    /** A mutable map from instance ID to the mutable instance configuration. */
    private final ObservableMap<String, CfgInstanceModel> instances;

    /** A mutable map from data profile ID to the mutable data profile configuration. */
    private final ObservableMap<String, CfgDataProfileModel> dataProfiles;

    /** A mutable map from web context path to the mutable web context configuration. */
    private final ObservableMap<String, CfgWebContextModel> webContexts;

    /** A mutable map from code context ID to the mutable code context configuration. */
    private final ObservableMap<String, CfgCodeContextModel> codeContexts;

    /**
     * Initializes a new {@code CfgDatabaseLayerModel}.
     */
    private CfgDatabaseLayerModel() {

        this.instances = FXCollections.observableHashMap();
        this.dataProfiles = FXCollections.observableHashMap();
        this.webContexts = FXCollections.observableHashMap();
        this.codeContexts = FXCollections.observableHashMap();
    }

    /**
     * Gets the instances property.
     *
     * @return the instances property
     */
    public ObservableMap<String, CfgInstanceModel> getInstances() {

        return this.instances;
    }

    /**
     * Gets the data profiles property.
     *
     * @return the data profiles property
     */
    public ObservableMap<String, CfgDataProfileModel> getDataProfiles() {

        return this.dataProfiles;
    }

    /**
     * Gets the web contexts property.
     *
     * @return the web contexts property
     */
    public ObservableMap<String, CfgWebContextModel> getWebContexts() {

        return this.webContexts;
    }

    /**
     * Gets the code contexts property.
     *
     * @return the code contexts property
     */
    public ObservableMap<String, CfgCodeContextModel> getCodeContexts() {

        return this.codeContexts;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgDatabaseLayerModel{instances=[", this.instances, "],dataProfiles=[",
                this.dataProfiles, "],webContexts=[", this.webContexts, "],codeContexts=[", this.codeContexts, "]}");
    }
}
