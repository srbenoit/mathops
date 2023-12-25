package dev.mathops.app.db.config;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.config.CfgDatabase;
import dev.mathops.db.config.EDbUse;
import dev.mathops.db.config.ESchemaType;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

import java.util.HashSet;
import java.util.Set;

/**
 * A mutable configuration of a database instance on a server machine.
 *
 * <p>
 * To create a new {@code MutableCfgDatabase}, the UI would present fields to enter its ID, select the schema and use,
 * and supply an optional database name, with a button to execute the addition, which would fail if the selected
 * database ID was duplicated.
 *
 * <p>
 * If the database ID is updated and an "apply" button pressed in a GUI, the new ID is tested for uniqueness before
 * being accepted.  Any other fields can be updated with any valid values.
 *
 * <p>
 * A GUI should support deletion of a database, but this should succeed only of it is not referenced by a data profile.
 */
public final class MutableCfgDatabase {

    /** The database ID. */
    public final StringPropertyBase id;

    /** The schema this database provides. */
    public final ObjectPropertyBase<ESchemaType> schema;

    /** The usage for which this database is intended. */
    public final ObjectPropertyBase<EDbUse> use;

    /** The database name. */
    public final StringPropertyBase name;

    /** The set of {@code MutableCfgDataProfile} objects that reference this database. */
    private final transient Set<MutableCfgDataProfile> references;

    /**
     * Constructs a new, empty {@code MutableCfgDatabase}.
     */
    public MutableCfgDatabase() {

        this.id = new SimpleStringProperty();
        this.schema = new SimpleObjectProperty<>();
        this.use = new SimpleObjectProperty<>();
        this.name = new SimpleStringProperty();
        this.references = new HashSet<>(10);
    }

    /**
     * Constructs a new {@code MutableCfgDatabase} from a {@code CfgDatabase}.
     *
     * @param source   the source {@code CfgDatabase}
     */
    public MutableCfgDatabase(final CfgDatabase source) {

        this.id = new SimpleStringProperty(source.id);
        this.schema = new SimpleObjectProperty<>(source.schema);
        this.use = new SimpleObjectProperty<>(source.use);
        this.name = new SimpleStringProperty(source.name);
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
     * Gets the schema property.
     *
     * @return the schema property
     */
    public ObjectPropertyBase<ESchemaType> getSchemaProperty() {

        return this.schema;
    }

    /**
     * Gets the use property.
     *
     * @return the use property
     */
    public ObjectPropertyBase<EDbUse> getUseProperty() {

        return this.use;
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
     * Indicates that a {@code MutableCfgDataProfile} is referencing this object.
     *
     * @param referencer the referencing {@code MutableCfgDataProfile}
     */
    public void addReference(final MutableCfgDataProfile referencer) {

        this.references.add(referencer);
    }

    /**
     * Indicates that a {@code MutableCfgDataProfile} is no longer referencing this object.
     *
     * @param referencer the no-longer referencing {@code MutableCfgDataProfile}
     */
    public void deleteReference(final MutableCfgDataProfile referencer) {

        this.references.remove(referencer);
    }

    /**
     * Gets the number of {@code MutableCf gDataProfile} currently referencing this object.
     *
     * @return the number of references
     */
    public int getNumReferences() {

        return this.references.size();
    }

    /**
     * Generate an immutable {@code CfgDatabase} from this object.
     *
     * @return the generated {@code CfgDatabase}
     */
    CfgDatabase toCfgDatabase() {

        final String idValue = this.id.getValue();
        final ESchemaType schemaValue = this.schema.getValue();
        final EDbUse useValue = this.use.getValue();
        final String nameValue = this.name.getValue();

        return new CfgDatabase(idValue, schemaValue, useValue, nameValue);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("MutableCfgDatabase{id='", this.id, "',schema='", this.schema, "',use='", this.use, "'");
        if (this.name.get() != null) {
            htm.add(", name='['", this.name, "'");
        }
        htm.add("}");

        return htm.toString();
    }
}
