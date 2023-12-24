package dev.mathops.app.db.config;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.config.DbConfig;
import dev.mathops.db.config.EDbUse;
import dev.mathops.db.config.ESchemaType;

/**
 * A mutable model of a database server configuration that can be edited through a GUI.
 */
public final class MutableDbConfig {

    /** The id. */
    private String id;

    /** The schema type. */
    private ESchemaType schema;

    /** The database use. */
    private EDbUse use;

    /** The database ID (could be null if the database type does not require it). */
    private String db;

    /**
     * Constructs a new {@code MutableDbConfig}.
     *
     * @param theId      the server ID
     * @param theSchema  the schema (must not be null)
     * @param theUse     the database usage (must not be null)
     * @param theDbId    the database ID, if the database type requires this
     */
    public MutableDbConfig(final String theId, final ESchemaType theSchema, final EDbUse theUse, final String theDbId) {

        update(theId, theSchema, theUse, theDbId);
    }

    /**
     * Constructs a new {@code MutableDbConfig} from a {@code DbConfig}.
     *
     * @param source the source {@code DbConfig}
     */
    MutableDbConfig(final DbConfig source) {

        this.id = source.id;
        this.schema = source.schema;
        this.use = source.use;
        this.db = source.db;
    }

    /**
     * Gets the ID.
     *
     * @return the ID
     */
    public String getId() {

        return this.id;
    }

    /**
     * Gets the schema type.
     *
     * @return the schema type
     */
    public ESchemaType getSchema() {

        return this.schema;
    }

    /**
     * Gets the database usage.
     *
     * @return the database usage
     */
    public EDbUse getUse() {

        return this.use;
    }

    /**
     * Gets the database ID.
     *
     * @return the database ID (could be null or blank)
     */
    public String getDb() {

        return this.db;
    }

    /**
     * Updates the field values.
     *
     * @param theId      the server ID
     * @param theSchema  the schema (must not be null)
     * @param theUse     the database usage (must not be null)
     * @param theDbId    the database ID, if the database type requires this
     */
    public void update(final String theId, final ESchemaType theSchema, final EDbUse theUse, final String theDbId) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Server ID may not be null or blank.");
        }
        if (theSchema == null) {
            throw new IllegalArgumentException("Schema may not be null");
        }
        if (theUse == null) {
            throw new IllegalArgumentException("Database usage may not be null.");
        }

        this.id = theId;
        this.schema = theSchema;
        this.use = theUse;
        this.db = theDbId;
    }

    /**
     * Generate an immutable {@code DbConfig} from this object.
     *
     * @return the generated {@code DbConfig}
     */
    public DbConfig toDbConfig() {

        return new DbConfig(this.id, this.schema, this.use, this.db);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add("MutableDbConfig{id='", this.id, "',schema='", this.schema, "',use='", this.use);
        if (this.db != null) {
            builder.add(",dbId='", this.db, "'");
        }
        builder.add("}");

        return builder.toString();
    }
}
