package dev.mathops.db.config;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;

import java.util.Objects;

/**
 * An immutable representation of a database instance on a server machine.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;database id='...' schema='...' use='...' name='...'/&gt;
 * </pre>
 */
public final class CfgDatabase {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "database";

    /** The server ID attribute. */
    private static final String ID_ATTR = "id";

    /** The schema attribute. */
    private static final String SCHEMA_ATTR = "schema";

    /** The server type attribute. */
    private static final String USE_ATTR = "use";

    /** The database ID attribute. */
    private static final String NAME_ATTR = "name";

    /** The server ID. */
    public final String id;

    /** The schema type. */
    public final ESchemaType schema;

    /** The database usage. */
    public final EDbUse use;

    /** The database name, if required by the database driver. */
    public final String name;

    /**
     * Constructs a new {@code CfgDatabase}.
     *
     * @param theId      the server ID
     * @param theSchema  the schema type
     * @param theUse     the database usage
     * @param theName    the database name, if needed by the server product
     * @throws IllegalArgumentException if the type, schema, or use is null
     */
    public CfgDatabase(final String theId, final ESchemaType theSchema, final EDbUse theUse, final String theName) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("ID may not be null or blank.");
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
        this.name = theName;
    }

    /**
     * Constructs a new {@code CfgDatabase} from its XML representation.
     *
     * @param theElem the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgDatabase(final EmptyElement theElem) throws ParsingException {

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

            this.name = theElem.getStringAttr(NAME_ATTR);
        } else {
            final String msg = Res.get(Res.DB_CFG_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Tests whether this {@code CfgDatabase} is equal to another object. To be equal, the other object must be a
     * {@code DatabaseConfig} and must have the same type, schema, host, port, and ID.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final CfgDatabase test) {
            equal = test.id.equals(this.id) &&  test.schema == this.schema && test.use == this.use
                    && Objects.equals(test.name, this.name);
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

        return this.id.hashCode() + this.schema.hashCode() + this.use.hashCode() + Objects.hashCode(this.name);
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("CfgDatabase{id=", this.id, ",schema=", this.schema, ",use=", this.use);
        if (this.name != null) {
            htm.add(",name=", this.name);
        }
        htm.add("}");

        return htm.toString();
    }
}
