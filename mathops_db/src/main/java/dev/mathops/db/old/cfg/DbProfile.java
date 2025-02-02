package dev.mathops.db.old.cfg;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.DbContext;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.EmptyElement;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;

import java.util.EnumMap;
import java.util.Map;

/**
 * Represents a database profile, which provides a mapping from schema to login that covers all defined schemata.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;profile id='...'&gt;
 *   ... zero or more &lt;schema-login&gt; child elements ...
 * &lt;/profile&gt;
 * </pre>
 */
@Deprecated
public final class DbProfile implements Comparable<DbProfile> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "profile";

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

    /** A map from schema ID to database context. */
    private final EnumMap<ESchemaUse, DbContext> dbContexts;

    /**
     * Constructs a new {@code DbProfile} from its XML representation.
     *
     * @param theSchemaMap the schema map
     * @param theLoginMap  the login map
     * @param theElem      the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    DbProfile(final Map<String, SchemaConfig> theSchemaMap,
              final Map<String, LoginConfig> theLoginMap, final NonemptyElement theElem)
            throws ParsingException {

        if (ELEM_TAG.equals(theElem.getTagName())) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);

            final int count = theElem.getNumChildren();
            this.dbContexts = new EnumMap<>(ESchemaUse.class);

            for (int i = 0; i < count; ++i) {
                final INode child = theElem.getChild(i);
                if (child instanceof EmptyElement) {
                    processChildElement((EmptyElement) child, theSchemaMap, theLoginMap);
                } else {
                    Log.warning("Unexpected child of <profile> element.");
                }
            }
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.PROF_CFG_BAD_ELEM_TAG));
        }
    }

    /**
     * Constructs a new {@code DbProfile}.
     *
     * @param theId         the profile ID
     * @param theDbContexts a map from schema ID to database context
     */
    public DbProfile(final String theId, final Map<ESchemaUse, DbContext> theDbContexts) {

        this.id = theId;
        this.dbContexts = new EnumMap<>(theDbContexts);
    }

    /**
     * Processes a child element.
     *
     * @param theElem      the child element
     * @param theSchemaMap the schema map
     * @param theLoginMap  the login map
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    private void processChildElement(final EmptyElement theElem,
                                     final Map<String, SchemaConfig> theSchemaMap,
                                     final Map<String, LoginConfig> theLoginMap)
            throws ParsingException {

        if (CHILD_TAG.equals(theElem.getTagName())) {
            final String schema = theElem.getRequiredStringAttr(SCHEMA_ATTR);

            if (theSchemaMap.containsKey(schema)) {
                final String login = theElem.getRequiredStringAttr(LOGIN_ATTR);

                if (theLoginMap.containsKey(login)) {
                    final SchemaConfig schemaCfg = theSchemaMap.get(schema);
                    this.dbContexts.put(schemaCfg.use,
                            new DbContext(schemaCfg, theLoginMap.get(login)));
                } else {
                    throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                            Res.fmt(Res.PROF_CFG_BAD_CHILD_LOGIN, login));
                }
            } else {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.PROF_CFG_BAD_CHILD_SCHEMA, schema));
            }
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                    Res.get(Res.PROF_CFG_BAD_CHILD_ELEM_TAG));
        }
    }

    /**
     * Gets the database context to use for a particular schema use.
     *
     * @param schemaUse the schema use
     * @return the login configuration
     */
    public DbContext getDbContext(final ESchemaUse schemaUse) {

        return this.dbContexts.get(schemaUse);
    }

    /**
     * Tests whether this {@code ServerConfig} is equal to another object. To be equal, the other object must be a
     * {@code ServerConfig} and must have the same type, host, port, and name.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final DbProfile test) {
            equal = test.id.equals(this.id) && test.dbContexts.equals(this.dbContexts);
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

        return this.id.hashCode() + this.dbContexts.hashCode();
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
    public int compareTo(final DbProfile o) {

        return this.id.compareTo(o.id);
    }
}
