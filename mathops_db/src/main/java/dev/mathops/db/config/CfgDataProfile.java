package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.IElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable representation of a database profile, which provides a mapping from every defined schema types to a
 * schema login configuration.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;data-profile id='...'&gt;
 *   ... one &lt;schema-login&gt; child elements for every schema type ...
 * &lt;/data-profile&gt;
 * </pre>
 */
public final class CfgDataProfile implements Comparable<CfgDataProfile> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "data-profile";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** An empty array used when converting collections of web contexts to arrays. */
    private static final CfgSchemaLogin[] EMPTY_SCHEMA_LOGIN_ARRAY = new CfgSchemaLogin[0];

    /** The ID. */
    public final String id;

    /** A map from schema type to the schema login configuration for that schema. */
    private final Map<ESchemaType, CfgSchemaLogin> schemaLogins;

    /**
     * Constructs a new {@code DataProfile}.
     *
     * @param theId           the profile ID
     * @param theSchemaLogins the child schema logins
     */
    public CfgDataProfile(final String theId, final Collection<CfgSchemaLogin> theSchemaLogins) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Data profile ID may not be null or blank.");
        }
        if (theSchemaLogins == null) {
            throw new IllegalArgumentException("Schema logins map may not be null");
        }

        this.id = theId;
        this.schemaLogins = new EnumMap<>(ESchemaType.class);
        for (final CfgSchemaLogin schemaLogin : theSchemaLogins) {
            if (schemaLogin != null) {
                if (this.schemaLogins.containsKey(schemaLogin.schema)) {
                    throw new IllegalArgumentException("Duplicate schema in child schema login collection");
                }
                this.schemaLogins.put(schemaLogin.schema, schemaLogin);
            }
        }

        if (this.schemaLogins.size() != ESchemaType.values().length) {
            throw new IllegalArgumentException("Schema logins map must be provided with a login for every schema");
        }
    }

    /**
     * Constructs a new {@code DataProfile} from its XML representation.
     *
     * @param theDbMap    the DB map
     * @param theLoginMap the login map
     * @param theElem     the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgDataProfile(final Map<String, CfgDatabase> theDbMap, final Map<String, CfgLogin> theLoginMap,
                   final NonemptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            if (this.id.isBlank()) {
                throw new IllegalArgumentException("Data profile ID may not be blank.");
            }

            final List<IElement> elementChildren = theElem.getElementChildrenAsList();
            this.schemaLogins = new EnumMap<>(ESchemaType.class);

            for (final IElement child : elementChildren) {
                final String childTag = child.getTagName();

                if (child instanceof final EmptyElement childElem) {
                    if (CfgSchemaLogin.ELEM_TAG.equals(childTag)) {
                        final CfgSchemaLogin login = new CfgSchemaLogin(theDbMap, theLoginMap, childElem);
                        this.schemaLogins.put(login.schema, login);
                    } else {
                        Log.warning("Unexpected <", childTag, "> child of <data-profile> element.");
                    }
                } else {
                    Log.warning("Unexpected non-empty <", childTag, "> child of <data-profile> element.");
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
     * Gets of schema logins in the schema login map.
     *
     * @return the array of schema login configurations
     */
    public CfgSchemaLogin[] getSchemaLogins() {

        return this.schemaLogins.values().toArray(EMPTY_SCHEMA_LOGIN_ARRAY);
    }

    /**
     * Gets the schema login to use for a particular schema type.
     *
     * @param schemaType the schema type
     * @return the schema login configuration
     */
    public CfgSchemaLogin getSchemaLogin(final ESchemaType schemaType) {

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

        if (obj instanceof final CfgDataProfile test) {
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

        return SimpleBuilder.concat("CfgDatProfile{schemaLogins=", this.schemaLogins, "}");
    }

    /**
     * Compares this profile to another for order. Order is based on ID.
     *
     * @param o the other profile to which to compare
     */
    @Override
    public int compareTo(final CfgDataProfile o) {

        return this.id.compareTo(o.id);
    }
}
