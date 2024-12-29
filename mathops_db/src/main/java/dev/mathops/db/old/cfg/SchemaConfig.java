package dev.mathops.db.old.cfg;

import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.EmptyElement;

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
    /** The use attribute. */
    private static final String USE_ATTR = "use";

    /** The ID of the schema (unique among all loaded schemata). */
    public final String id;

    /** The use of the schema. */
    public final ESchemaUse use;

    /**
     * Constructs a new {@code SchemaConfig} from its XML representation.
     *
     * @param theElem the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    SchemaConfig(final EmptyElement theElem) throws ParsingException {

        final String tagName = theElem.getTagName();
        if (ELEM_TAG.equals(tagName)) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id.indexOf(CoreConstants.COMMA_CHAR) >= 0) {
                final int start = theElem.getStart();
                final int end = theElem.getEnd();
                final String msg = Res.fmt(Res.SCH_BAD_ID, this.id);
                throw new ParsingException(start, end, msg);
            }
            final String attr = theElem.getStringAttr(USE_ATTR);
            this.use = ESchemaUse.forName(attr);

        } else {
            final int start = theElem.getStart();
            final int end = theElem.getEnd();
            final String msg = Res.get(Res.SCH_BAD_ELEM_TAG);
            throw new ParsingException(start, end, msg);
        }
    }

    /**
     * Constructs a new {@code SchemaConfig}.
     *
     * @param theId  the schema ID
     * @param theUse the schema use
     * @throws IllegalArgumentException if the builder class name is not valid
     */
    public SchemaConfig(final String theId, final ESchemaUse theUse) throws IllegalArgumentException {

        this.id = theId;
        this.use = theUse;
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

        htm.add(CoreConstants.QUOTE, this.id, "\"");

        return htm.toString();
    }
}
