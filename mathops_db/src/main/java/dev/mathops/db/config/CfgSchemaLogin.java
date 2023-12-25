package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.NonemptyElement;

import java.util.Map;

/**
 * An immutable representation of a schema login configuration, which defines database and login configurations
 * for a particular schema.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;schema-login db='...' login='...'/&gt;
 * </pre>
 */
public final class CfgSchemaLogin implements Comparable<CfgSchemaLogin> {

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
    public final CfgDatabase database;

    /** The selected login config. */
    public final CfgLogin login;

    /**
     * Constructs a new {@code SchemaLogin}.
     *
     * @param theSchema   the schema type
     * @param theDatabase the selected database config
     * @param theLogin    the selected login config
     */
    public CfgSchemaLogin(final ESchemaType theSchema, final CfgDatabase theDatabase, final CfgLogin theLogin) {

        if (theSchema == null) {
            throw new IllegalArgumentException("Schema type may not be null.");
        }
        if (theDatabase == null) {
            throw new IllegalArgumentException("Database config may not be null");
        }
        if (theLogin == null) {
            throw new IllegalArgumentException("Login config may not be null");
        }

        this.schema = theSchema;
        this.database = theDatabase;
        this.login = theLogin;
    }

    /**
     * Constructs a new {@code SchemaLogin} from its XML representation.
     *
     * @param theDatabaseMap the database map
     * @param theLoginMap    the login map
     * @param theElem        the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgSchemaLogin(final Map<String, CfgDatabase> theDatabaseMap, final Map<String, CfgLogin> theLoginMap,
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
            this.database = theDatabaseMap.get(dbStr);
            if (this.database == null) {
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
     * Tests whether this {@code DataProfile} is equal to another object. To be equal, the other object must be a
     * {@code ServerConfig} and must have the same type, host, port, and name.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final CfgSchemaLogin test) {
            equal = test.schema == this.schema && test.database.equals(this.database) && test.login.equals(this.login);
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

        return this.schema.hashCode() + this.database.hashCode() + this.login.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("SchemaLogin{schema=", this.schema, ",database=", this.database.id, ",login=",
                this.login.id);
    }

    /**
     * Compares this schema login to another for order. Order is based on schema.
     *
     * @param o the other schema login to which to compare
     */
    @Override
    public int compareTo(final CfgSchemaLogin o) {

        return this.schema.compareTo(o.schema);
    }
}
