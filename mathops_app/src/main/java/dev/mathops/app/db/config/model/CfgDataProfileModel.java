package dev.mathops.app.db.config.model;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.persistence.ESchemaType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.HashSet;
import java.util.Set;

/**
 * The data model of a database profile, which provides a mapping from every defined schema types to a schema login
 * configuration.
 *
 * <p>
 * To create a new {@code CfgDataProfileModel}, the UI would present fields to enter its ID, and to select (from the
 * set of defined objects) a database and login for each defined schema, with a button to execute the addition, which
 * would fail if entered ID is not unique.  The new instance would have a schema login for each defined schema.  The
 * button to create a new {@code CfgDataProfileModel} would not be enabled when there are no databases or no logins
 * defined.
 *
 * <p>
 * If the data profile ID is updated and an "apply" button pressed in a GUI, the new ID is tested for uniqueness within
 * the owning database layer configuration before being accepted.
 *
 * <p>
 * Once a data profile is created, the user should be able to update the database or login associated with any schema.
 *
 * <p>
 * A GUI should support deletion of a data profile, but this should succeed only if no web context or code context
 * references the profile.
 */
public final class CfgDataProfileModel {

    /** The instance ID. */
    public final StringPropertyBase id;

    /** A mutable map from schema login ID to the mutable schema login configuration. */
    private final ObservableMap<ESchemaType, CfgSchemaLoginModel> schemaLogins;

    /** The set of {@code MutableCfgWebContext} objects that reference this data profile. */
    private final transient Set<CfgWebContextModel> referencingWebContexts;

    /** The set of {@code MutableCfgCodeContext} objects that reference this data profile. */
    private final transient Set<CfgCodeContextModel> referencingCodeContexts;

    /**
     * Constructs a new, empty {@code CfgDataProfileModel}.
     */
    public CfgDataProfileModel() {

        this.id = new SimpleStringProperty();
        this.schemaLogins = FXCollections.observableHashMap();
        this.referencingWebContexts = new HashSet<>(10);
        this.referencingCodeContexts = new HashSet<>(10);
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
     * Gets the schema logins property.
     *
     * @return the schema logins property
     */
    public ObservableMap<ESchemaType, CfgSchemaLoginModel> getSchemaLogins() {

        return this.schemaLogins;
    }

    /**
     * Indicates that a {@code MutableCfgWebContext} is referencing this object.
     *
     * @param referencer the referencing {@code MutableCfgWebContext}
     */
    public void addReference(final CfgWebContextModel referencer) {

        this.referencingWebContexts.add(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgWebContext} is no longer referencing this object.
     *
     * @param referencer the no-longer referencing {@code MutableCfgWebContext}
     */
    public void deleteReference(final CfgWebContextModel referencer) {

        this.referencingWebContexts.remove(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgCodeContext} is referencing this object.
     *
     * @param referencer the referencing {@code MutableCfgCodeContext}
     */
    public void addReference(final CfgCodeContextModel referencer) {

        this.referencingCodeContexts.add(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgCodeContext} is no longer referencing this object.
     *
     * @param referencer the no-longer referencing {@code MutableCfgCodeContext}
     */
    public void deleteReference(final CfgCodeContextModel referencer) {

        this.referencingCodeContexts.remove(referencer);
    }

    /**
     * Gets the number of {@code MutableCfgWebContext} or {@code MutableCfgCodeContext} currently referencing this
     * object.
     *
     * @return the number of references
     */
    public int getNumReferences() {

        return this.referencingWebContexts.size() + this.referencingCodeContexts.size();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgDataProfileModel{id='", this.id, "',schemaLogins=[", this.schemaLogins, "]}");
    }
}
