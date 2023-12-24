package dev.mathops.db.config;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;

import java.util.Objects;

/**
 * Represents a database instance on a server machine.
 *
 * <p>
 * There should exist one {@code DbConfig} object for each unique database instance on each server.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;db id='...' schema='...' use='...' db='...'/&gt;
 * </pre>
 */
public final class DbConfig {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "db";

    /** The server ID attribute. */
    private static final String ID_ATTR = "id";

    /** The schema attribute. */
    private static final String SCHEMA_ATTR = "schema";

    /** The server type attribute. */
    private static final String USE_ATTR = "use";

    /** The ID attribute. */
    private static final String DB_ATTR = "db";

    /** The server ID. */
    public final String id;

    /** The schema type. */
    public final ESchemaType schema;

    /** The database usage. */
    public final EDbUse use;

    /** The database, if required by the database driver. */
    public final String db;

    /**
     * Constructs a new {@code DbConfig}.
     *
     * @param theId      the server ID
     * @param theSchema  the schema type
     * @param theUse     the database usage
     * @param theDb      the database name, if needed by the server product
     * @throws IllegalArgumentException if the type, schema, or use is null
     */
    public DbConfig(final String theId, final ESchemaType theSchema, final EDbUse theUse, final String theDb) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("ID may not be null or blank.");
        }
        if (theSchema == null) {
            throw new IllegalArgumentException("Schema may not be null");
        }
        if (theUse == null) {
            throw new IllegalArgumentException("Database usage type may not be null.");
        }

        this.id = theId;
        this.schema = theSchema;
        this.use = theUse;
        this.db = theDb;
    }

    /**
     * Constructs a new {@code DbConfig} from its XML representation.
     *
     * @param theElem the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    DbConfig(final EmptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {

            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id.isBlank()) {
                throw new IllegalArgumentException("ID may not be blank.");
            }

            final String schemaStr = theElem.getRequiredStringAttr(SCHEMA_ATTR);
            this.schema = ESchemaType.forName(schemaStr);
            if (this.schema == null) {
                final String msg = Res.fmt(Res.DB_CFG_BAD_SCHEMA, schemaStr);
                throw new ParsingException(theElem, msg);
            }

            final String useStr = theElem.getRequiredStringAttr(USE_ATTR);
            this.use = EDbUse.forName(useStr);
            if (this.use == null) {
                final String msg = Res.fmt(Res.DB_CFG_BAD_USE, useStr);
                throw new ParsingException(theElem, msg);
            }

            this.db = theElem.getStringAttr(DB_ATTR);
        } else {
            final String msg = Res.get(Res.DB_CFG_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Tests whether this {@code DbConfig} is equal to another object. To be equal, the other object must be a
     * {@code DbConfig} and must have the same type, schema, host, port, and ID.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final DbConfig test) {
            equal = test.id.equals(this.id) &&  test.schema == this.schema && test.use == this.use
                    && Objects.equals(test.db, this.db);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {

        return this.id.hashCode() + this.schema.hashCode() + this.use.hashCode() + Objects.hashCode(this.db);
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("Database id '", this.id, " implementing the ", this.schema.name, " for ", this.use);

        if (this.db != null) {
            htm.add(" with DB name", this.db);
        }

        return htm.toString();
    }
}
