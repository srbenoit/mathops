package dev.mathops.db.config;

import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a database profile, which provides a mapping from schema to login that covers all defined schemata.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;data-profile id='...'&gt;
 *   ... zero or more &lt;schema-login&gt; child elements ...
 *   &lt;schema-login db='...' login='...'/&gt;
 * &lt;/data-profile&gt;
 * </pre>
 */
public final class DataProfile implements Comparable<DataProfile> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "data-profile";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** The ID. */
    public final String id;

    /** A map from schema type to the schema login configuration for that schema. */
    private final Map<ESchemaType, SchemaLogin> schemaLogins;

    /**
     * Constructs a new {@code DataProfile}.
     *
     * @param theId         the profile ID
     * @param theSchemaLogins a map from schema ID to database context
     */
    public DataProfile(final String theId, final Map<ESchemaType, SchemaLogin> theSchemaLogins) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Data profile ID may not be null or blank.");
        }
        if (theSchemaLogins == null || theSchemaLogins.size() != ESchemaType.values().length
                || theSchemaLogins.containsKey(null)) {
            throw new IllegalArgumentException("Schema logins map must be provided with a login for every schema");
        }

        this.id = theId;
        this.schemaLogins = new EnumMap<>(theSchemaLogins);
    }

    /**
     * Constructs a new {@code DataProfile} from its XML representation.
     *
     * @param theDbMap    the DB map
     * @param theLoginMap the login map
     * @param theElem     the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    DataProfile(final Map<String, DbConfig> theDbMap, final Map<String, LoginConfig> theLoginMap,
                final NonemptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);

            final int count = theElem.getNumChildren();
            this.schemaLogins = new EnumMap<>(ESchemaType.class);

            for (int i = 0; i < count; ++i) {
                final INode child = theElem.getChild(i);
                if (child instanceof final EmptyElement childElem) {
                    final String childTag = theElem.getTagName();

                    if (SchemaLogin.ELEM_TAG.equals(childTag)) {
                        final SchemaLogin login = new SchemaLogin(theDbMap, theLoginMap, theElem);
                        final ESchemaType loginSchema = login.getSchema();
                        this.schemaLogins.put(loginSchema, login);
                    } else {
                        Log.warning("Unexpected child of <data-profile> element.");
                    }
                } else {
                    Log.warning("Unexpected child of <data-profile> element.");
                }
            }

            if (this.schemaLogins.size() != ESchemaType.values().length || this.schemaLogins.containsKey(null)) {
                throw new IllegalArgumentException("Schema login configuration must be provided for every schema");
            }
        } else {
            final String msg = Res.get(Res.PROF_CFG_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Gets the schema login to use for a particular schema type.
     *
     * @param schemaType the schema type
     * @return the schema login configuration
     */
    public SchemaLogin getSchemaLogin(final ESchemaType schemaType) {

        return this.schemaLogins.get(schemaType);
    }

    /**
     * Tests whether this {@code DataProfile} is equal to another object. To be equal, the other object must be a
     * {@code ServerConfig} and must have the same type, host, port, and name.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final DataProfile test) {
            equal = test.id.equals(this.id) && test.schemaLogins.equals(this.schemaLogins);
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

        return this.id.hashCode() + this.schemaLogins.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.id;
    }

    /**
     * Compares this profile to another for order. Order is based on ID.
     *
     * @param o the other profile to which to compare
     */
    @Override
    public int compareTo(final DataProfile o) {

        return this.id.compareTo(o.id);
    }
}
