package dev.mathops.db.config.edit;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.DataProfile;
import dev.mathops.db.config.ESchemaType;
import dev.mathops.db.config.LoginConfig;

import java.util.EnumMap;
import java.util.Map;

/**
 * A mutable model of a data profile that can be edited through a GUI.
 */
public final class MutableDataProfile {

    /** The data profile ID. */
    private String id;

    /** The map from schema type to the configured login ID (must have an entry for every schema type). */
    private final Map<ESchemaType, String> schemaLogins;

    /**
     * Constructs a new {@code MutableDataProfileConfig}.
     *
     * @param theId           the data profile ID
     * @param theSchemaLogins a map from schema type to the configured login ID (must have an entry for every type)
     */
    public MutableDataProfile(final String theId, final Map<ESchemaType, String> theSchemaLogins) {

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
     * Constructs a new {@code MutableDataProfileConfig} from a {@code DataProfile}.
     *
     * @param source the source {@code ServerConfig}
     */
    public MutableDataProfile(final DataProfile source) {

        this.id = source.id;
        this.schemaLogins = new EnumMap<>(ESchemaType.class);

        for (final ESchemaType schema : ESchemaType.values()) {
            final LoginConfig login = source.getLogin(schema);

            if (login == null) {
                throw new IllegalArgumentException("Source object must have a login for every schema");
            }

            this.schemaLogins.put(schema, login.id);
        }
    }

    /**
     * Gets the data profile ID.
     *
     * @return the data profile ID
     */
    public String getId() {

        return this.id;
    }

    /**
     * Gets the login configured for a particular schema.
     *
     * @param schema the schema type
     * @return the configured login (never {@code null} or blank)
     */
    public String getSchemaLogin(final ESchemaType schema) {

        if (schema == null) {
            throw new IllegalArgumentException("Schema type may not be null");
        }

        return this.schemaLogins.get(schema);
    }

    /**
     * Sets the login ID associated with a schema type.
     *
     * @param schema the schema type
     * @param loginId the new login ID
     */
    public void setSchemaLogin(final ESchemaType schema, final String loginId) {

        if (schema == null) {
            throw new IllegalArgumentException("Schema type may not be null");
        }
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException("Login ID may not be null or blank");
        }

        this.schemaLogins.put(schema, loginId);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableDataProfile{id='", this.id, "', schemaLogins='", this.schemaLogins, "}");
    }
}
