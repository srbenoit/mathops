package dev.mathops.db.cfg;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.ESchema;

import java.util.EnumMap;
import java.util.Map;

/**
 * A "profile" object from the database configuration file.
 */
public final class Profile {

    /** The profile ID. */
    public final String id;

    /** A map from schema type to the profile schema in this profile. */
    private final Map<ESchema, Schema> schemas;

    /**
     * Constructs a new {@code Profile}.
     *
     * @param theId the profile ID
     */
    Profile(final String theId) {

        if (theId == null || theId.isBlank()) {
            final String msg = Res.get(Res.PROFILE_NULL_ID);
            throw new IllegalArgumentException(msg);
        }

        this.id = theId;
        this.schemas = new EnumMap<>(ESchema.class);
    }

    /**
     * Adds a {@code ProfileSchema}.
     *
     * @param schema        the schema with which the {@code ProfileSchema} is associated
     * @param profileSchema the {@code ProfileSchema}
     */
    void add(final ESchema schema, final Schema profileSchema) {

        this.schemas.put(schema, profileSchema);
    }

    /**
     * Tests whether the profile provides all required schemas (and provides no schema twice).
     *
     * @return an error message if the profile is not valid; null if it is valid
     */
    String validate() {

        String error = null;

        int system = 0;
        int main = 0;
        int extern = 0;
        int analytics = 0;
        int term = 0;

        for (final ESchema type : this.schemas.keySet()) {
            switch (type) {
                case SYSTEM -> ++system;
                case MAIN -> ++main;
                case EXTERN -> ++extern;
                case ANALYTICS -> ++analytics;
                case TERM -> ++term;
                case LEGACY -> {
                    ++system;
                    ++main;
                    ++term;
                }
            }
        }

        if (system == 0 || main == 0 || extern == 0 || analytics == 0 || term == 0) {
            final StringBuilder builder = new StringBuilder(50);
            final String start = Res.get(Res.PROFILE_MISSING_START);
            builder.append(start);
            if (system == 0) {
                builder.append(CoreConstants.SPC).append(ESchema.SYSTEM);
            }
            if (main == 0) {
                builder.append(CoreConstants.SPC).append(ESchema.MAIN);
            }
            if (extern == 0) {
                builder.append(CoreConstants.SPC).append(ESchema.EXTERN);
            }
            if (analytics == 0) {
                builder.append(CoreConstants.SPC).append(ESchema.ANALYTICS);
            }
            if (term == 0) {
                builder.append(CoreConstants.SPC).append(ESchema.TERM);
            }
            final String end = Res.get(Res.PROFILE_MSG_END);
            builder.append(end);
            error = builder.toString();
        } else if (system > 1 || main > 1 || extern > 1 || analytics > 1 || term > 1) {
            final StringBuilder builder = new StringBuilder(50);
            final String start = Res.get(Res.PROFILE_DUP_START);
            builder.append(start);
            if (system > 1) {
                builder.append(CoreConstants.SPC).append(ESchema.SYSTEM);
            }
            if (main > 1) {
                builder.append(CoreConstants.SPC).append(ESchema.MAIN);
            }
            if (extern > 1) {
                builder.append(CoreConstants.SPC).append(ESchema.EXTERN);
            }
            if (analytics > 1) {
                builder.append(CoreConstants.SPC).append(ESchema.ANALYTICS);
            }
            if (term > 1) {
                builder.append(CoreConstants.SPC).append(ESchema.TERM);
            }
            final String end = Res.get(Res.PROFILE_MSG_END);
            builder.append(end);
            error = builder.toString();
        }

        return error;
    }
}
