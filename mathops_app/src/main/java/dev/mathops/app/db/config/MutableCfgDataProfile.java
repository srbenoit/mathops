package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.CfgDataProfile;
import dev.mathops.db.config.CfgDatabase;
import dev.mathops.db.config.CfgLogin;
import dev.mathops.db.config.CfgSchemaLogin;
import dev.mathops.db.config.ESchemaType;
import javafx.beans.property.MapPropertyBase;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A mutable configuration of a database profile, which provides a mapping from every defined schema types to a
 * schema login configuration.
 *
 * <p>
 * To create a new {@code MutableCfgDataProfile}, the UI would present fields to enter its ID, and to select (from the
 * set of defined objects) a database and login for each defined schema, with a button to execute the addition, which
 * would fail if entered ID is not unique.  The new instance would have a schema login for each defined schema.  The
 * button to create a new {@code MutableCfgDataProfile} would not be enabled when there are no databases or no logins
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
public final class MutableCfgDataProfile {

    /** The instance ID. */
    public final StringPropertyBase id;

    /** A mutable map from schema login ID to the mutable schema login configuration. */
    private final MapPropertyBase<ESchemaType, MutableCfgSchemaLogin> schemaLogins;

    /** The set of {@code MutableCfgWebContext} objects that reference this data profile. */
    private final transient Set<MutableCfgWebContext> referencingWebContexts;

    /** The set of {@code MutableCfgCodeContext} objects that reference this data profile. */
    private final transient Set<MutableCfgCodeContext> referencingCodeContexts;

    /**
     * Constructs a new, empty {@code MutableCfgDataProfile}.
     */
    public MutableCfgDataProfile() {

        this.id = new SimpleStringProperty();
        this.schemaLogins = new SimpleMapProperty<>();
        this.referencingWebContexts = new HashSet<>(10);
        this.referencingCodeContexts = new HashSet<>(10);
    }

    /**
     * Constructs a new {@code MutableCfgDataProfile} from a {@code CfgDataProfile}.
     *
     * @param source   the source {@code CfgDataProfile}
     */
    public MutableCfgDataProfile(final CfgDataProfile source) {

        this.id = new SimpleStringProperty(source.id);
        this.schemaLogins = new SimpleMapProperty<>();

        for (final CfgSchemaLogin schemaLogin : source.getSchemaLogins()) {
            final MutableCfgSchemaLogin mutableSchemaLogin = new MutableCfgSchemaLogin(this, schemaLogin);
            this.schemaLogins.put(schemaLogin.schema, mutableSchemaLogin);
        }
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
    public MapPropertyBase<ESchemaType, MutableCfgSchemaLogin> getSchemaLoginsProperty() {

        return this.schemaLogins;
    }

    /**
     * Indicates that a {@code MutableCfgWebContext} is referencing this object.
     *
     * @param referencer the referencing {@code MutableCfgWebContext}
     */
    public void addReference(final MutableCfgWebContext referencer) {

        this.referencingWebContexts.add(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgWebContext} is no longer referencing this object.
     *
     * @param referencer the no-longer referencing {@code MutableCfgWebContext}
     */
    public void deleteReference(final MutableCfgWebContext referencer) {

        this.referencingWebContexts.remove(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgCodeContext} is referencing this object.
     *
     * @param referencer the referencing {@code MutableCfgCodeContext}
     */
    public void addReference(final MutableCfgCodeContext referencer) {

        this.referencingCodeContexts.add(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgCodeContext} is no longer referencing this object.
     *
     * @param referencer the no-longer referencing {@code MutableCfgCodeContext}
     */
    public void deleteReference(final MutableCfgCodeContext referencer) {

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
     * Generate an immutable {@code CfgDataProfile} from this object.
     *
     * @param databases a map from database ID to the database object
     * @param logins    a map from login ID to the login object
     * @return the generated {@code CfgDataProfile}
     */
    CfgDataProfile toCfgDataProfile(final Map<String, CfgDatabase> databases, final Map<String, CfgLogin> logins) {

        final List<CfgSchemaLogin> schemaLoginList = new ArrayList<>(10);

        for (final MutableCfgSchemaLogin mutableSchemaLogin : this.schemaLogins.values()) {
            final CfgSchemaLogin schemaLogin = mutableSchemaLogin.toCfgSchemaLogin(databases, logins);
            schemaLoginList.add(schemaLogin);
        }

        final String idValue = this.id.getValue();

        return new CfgDataProfile(idValue, schemaLoginList);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableCfgDataProfile{id='", this.id, "',schemaLogins=[", this.schemaLogins, "]}");
    }
}
