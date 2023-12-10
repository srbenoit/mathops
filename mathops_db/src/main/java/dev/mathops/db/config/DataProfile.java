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
 *   &lt;schema-login schema='...' login='...'/&gt;
 * &lt;/data-profile&gt;
 * </pre>
 */
public final class DataProfile implements Comparable<DataProfile> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "data-profile";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** The element tag for schema login child elements. */
    private static final String CHILD_TAG = "schema-login";

    /** The schema attribute. */
    private static final String SCHEMA_ATTR = "schema";

    /** The login attribute. */
    private static final String LOGIN_ATTR = "login";

    /** The ID. */
    public final String id;

    /** A map from schema ID to the login to use for that schema. */
    private final EnumMap<ESchemaType, LoginConfig> schemaLogins;

    /**
     * Constructs a new {@code DataProfile}.
     *
     * @param theId         the profile ID
     * @param theSchemaLogins a map from schema ID to database context
     */
    public DataProfile(final String theId, final Map<ESchemaType, LoginConfig> theSchemaLogins) {

        this.id = theId;
        this.schemaLogins = new EnumMap<>(theSchemaLogins);
    }

    /**
     * Constructs a new {@code DataProfile} from its XML representation.
     *
     * @param theLoginMap  the login map
     * @param theElem      the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    DataProfile(final Map<String, LoginConfig> theLoginMap, final NonemptyElement theElem)
            throws ParsingException {

        if (ELEM_TAG.equals(theElem.getTagName())) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);

            final int count = theElem.getNumChildren();
            this.schemaLogins = new EnumMap<>(ESchemaType.class);

            for (int i = 0; i < count; ++i) {
                final INode child = theElem.getChild(i);
                if (child instanceof final EmptyElement childElem) {
                    processChildElement(theLoginMap, childElem);
                } else {
                    Log.warning("Unexpected child of <data-profile> element.");
                }
            }
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.PROF_CFG_BAD_ELEM_TAG));
        }
    }

    /**
     * Processes a child 'schema-login' element.
     *
     * @param theLoginMap  the login map
     * @param theElem      the child element
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    private void processChildElement(final Map<String, LoginConfig> theLoginMap, final EmptyElement theElem)
            throws ParsingException {

        if (CHILD_TAG.equals(theElem.getTagName())) {
            final String schema = theElem.getRequiredStringAttr(SCHEMA_ATTR);
            final ESchemaType schemaType = ESchemaType.forName(schema);
            if (schemaType == null) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.PROF_CFG_BAD_CHILD_SCHEMA, schema));
            }

            final String login = theElem.getRequiredStringAttr(LOGIN_ATTR);

            if (theLoginMap.containsKey(login)) {
                this.schemaLogins.put(schemaType, theLoginMap.get(login));
            } else {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.PROF_CFG_BAD_CHILD_LOGIN, login));
            }
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.PROF_CFG_BAD_CHILD_ELEM_TAG));
        }
    }

    /**
     * Gets the login to use for a particular schema type.
     *
     * @param schemaType the schema type
     * @return the login configuration
     */
    public LoginConfig getLogin(final ESchemaType schemaType) {

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
