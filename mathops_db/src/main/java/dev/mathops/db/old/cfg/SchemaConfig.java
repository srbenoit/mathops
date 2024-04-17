package dev.mathops.db.old.cfg;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.db.old.ISchemaBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/**
 * Represents a particular database schema, with which is associated a factory class that can generate implementations
 * of the various interfaces used to manage each distinct model type.
 * <p>
 * There should exist only one {@code SchemaConfig} object for each unique schema name.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;schema id='...' builder='(fully-qualified class name)' use='...'/&gt;
 * </pre>
 */
public final class SchemaConfig {

    /** The element tag used in the XML representation of the schema. */
    static final String ELEM_TAG = "schema";

    /** The id attribute. */
    private static final String ID_ATTR = "id";

    /** The builder attribute. */
    private static final String BUILDER_ATTR = "builder";

    /** The use attribute. */
    private static final String USE_ATTR = "use";

    /** The ID of the schema (unique among all loaded schemata). */
    public final String id;

    /** The class name of the schema factory class. */
    private final String builderClassName;

    /** The constructor for schema factory that takes a single DbConnection argument. */
    private final Constructor<?> builderConstr;

    /** The use of the schema. */
    public final ESchemaUse use;

    /**
     * Constructs a new {@code SchemaConfig} from its XML representation.
     *
     * @param theElem the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    SchemaConfig(final EmptyElement theElem) throws ParsingException {

        if (ELEM_TAG.equals(theElem.getTagName())) {

            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id.indexOf(CoreConstants.COMMA_CHAR) >= 0) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.fmt(Res.SCH_BAD_ID, this.id));
            }
            this.builderClassName = theElem.getRequiredStringAttr(BUILDER_ATTR);
            try {
                final Class<?> cls = Class.forName(this.builderClassName);
                this.builderConstr = cls.getConstructor();
            } catch (final ClassNotFoundException | NoSuchMethodException ex) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.SCH_BAD_BUILDER, this.builderClassName), ex);
            }
            this.use = ESchemaUse.forName(theElem.getStringAttr(USE_ATTR));

        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.SCH_BAD_ELEM_TAG));
        }
    }

    /**
     * Constructs a new {@code SchemaConfig}.
     *
     * @param theId the schema ID
     * @param theBuilderClassName the fully-qualified name of the builder class
     * @param theUse the schema use
     * @throws IllegalArgumentException if the builder class name is not valid
     */
    public SchemaConfig(final String theId, final String theBuilderClassName, final ESchemaUse theUse)
            throws IllegalArgumentException{

        this.id = theId;
        this.builderClassName = theBuilderClassName;
        this.use = theUse;

        try {
            final Class<?> cls = Class.forName(this.builderClassName);
            this.builderConstr = cls.getConstructor();
        } catch (final ClassNotFoundException | NoSuchMethodException ex) {
            final String msg = Res.fmt(Res.SCH_BAD_BUILDER, this.builderClassName);
            throw new IllegalArgumentException(msg, ex);
        }
    }

    /**
     * Gets the builder associated with the schema.
     *
     * @return the builder
     * @throws SQLException if the builder could not be instantiated
     */
    public ISchemaBuilder getBuilder() throws SQLException {

        try {
            final Object obj = this.builderConstr.newInstance();

            ISchemaBuilder builder = null;

            if (obj instanceof ISchemaBuilder) {
                builder = (ISchemaBuilder) obj;
                builder.load();
            } else {
                Log.warning(Res.fmt(Res.SCH_NOT_IMPLEMENTS, obj.getClass().getName()));
            }

            return builder;
        } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
                       | InvocationTargetException ex) {
            throw new SQLException(Res.get(Res.SCH_CANT_MK_BUILDER), ex);
        }
    }

    /**
     * Tests whether this {@code Schema} is equal to another object. To be equal, the other object must be a
     * {@code Schema} and must have the same ID (since ID is unique).
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final SchemaConfig test) {
            equal = test.id.equals(this.id);
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

        return this.id.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        final String useName = this.use.name;
        htm.add(useName).padToLength(5);

        htm.add(CoreConstants.QUOTE, this.id, "\": (", this.builderClassName, ")");

        return htm.toString();
    }
}
