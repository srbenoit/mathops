package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.NonemptyElement;

import java.util.Map;

/**
 * Represents a choice of DB and login to use for a particular schemadatabase profile, which provides a mapping from
 * schema to login that covers all defined schemata.
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
public final class SchemaLogin implements Comparable<SchemaLogin> {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "schema-login";

    /** The schema attribute. */
    private static final String SCHEMA_ATTR = "schema";

    /** The schema attribute. */
    private static final String DB_ATTR = "db";

    /** The login attribute. */
    private static final String LOGIN_ATTR = "login";

    /** The schema. */
    public final ESchemaType schema;

    /** The selected DB config. */
    private final DbConfig db;

    /** The selected login config. */
    private final LoginConfig login;

    /**
     * Constructs a new {@code SchemaLogin}.
     *
     * @param theSchema the schema type
     * @param theDb     the selected DB config
     * @param theLogin  the selected login config
     */
    public SchemaLogin(final ESchemaType theSchema, final DbConfig theDb, final LoginConfig theLogin) {

        if (theSchema == null) {
            throw new IllegalArgumentException("Schema type may not be null.");
        }
        if (theDb == null) {
            throw new IllegalArgumentException("DB config may not be null");
        }
        if (theLogin == null) {
            throw new IllegalArgumentException("Login config may not be null");
        }

        this.schema = theSchema;
        this.db = theDb;
        this.login = theLogin;
    }

    /**
     * Constructs a new {@code SchemaLogin} from its XML representation.
     *
     * @param theDbMap    the DB map
     * @param theLoginMap the login map
     * @param theElem     the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    SchemaLogin(final Map<String, DbConfig> theDbMap, final Map<String, LoginConfig> theLoginMap,
                final NonemptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {

            final String schemaStr = theElem.getRequiredStringAttr(SCHEMA_ATTR);
            this.schema = ESchemaType.forName(schemaStr);
            if (this.schema == null) {
                final String msg = Res.fmt(Res.SCH_LOGIN_BAD_SCHEMA, schemaStr);
                throw new ParsingException(theElem, msg);
            }

            final String dbStr = theElem.getRequiredStringAttr(DB_ATTR);
            this.db = theDbMap.get(dbStr);
            if (this.db == null) {
                final String msg = Res.fmt(Res.SCH_LOGIN_BAD_DB, schemaStr);
                throw new ParsingException(theElem, msg);
            }

            final String loginStr = theElem.getRequiredStringAttr(LOGIN_ATTR);
            this.login = theLoginMap.get(loginStr);
            if (this.login == null) {
                final String msg = Res.fmt(Res.SCH_LOGIN_BAD_LOGIN, schemaStr);
                throw new ParsingException(theElem, msg);
            }
        } else {
            final String msg = Res.get(Res.SCH_LOGIN_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
    }

    /**
     * Gets the schema.
     *
     * @return the schema
     */
    public ESchemaType getSchema() {

        return this.schema;
    }

    /**
     * Gets the selected DB configuration.
     *
     * @return the DB configuration
     */
    public DbConfig getDb() {

        return this.db;
    }

    /**
     * Gets the selected login configuration.
     *
     * @return the login configuration
     */
    public LoginConfig getLogin() {

        return this.login;
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

        if (obj instanceof final SchemaLogin test) {
            equal = test.schema == this.schema && test.getDb().id.equals(this.db.id)
                    && test.getLogin().id.equals(this.login.id);
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

        return this.schema.hashCode() + this.db.hashCode() + this.login.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("SchemaLogin{schema='", this.schema, "',db='", this.db.id, "',login='",
                this.login.id, "'");
    }

    /**
     * Compares this profile to another for order. Order is based on schema.
     *
     * @param o the other profile to which to compare
     */
    @Override
    public int compareTo(final SchemaLogin o) {

        return this.schema.compareTo(o.schema);
    }
}
