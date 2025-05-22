package dev.mathops.db.cfg;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.ESchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A "profile" object from the database configuration file.
 */
public final class Profile implements Comparable<Profile> {

    /** The profile ID. */
    public final String id;

    /** A map from schema type to the corresponding facet. */
    private final Map<ESchema, Facet> facets;

    /**
     * Constructs a new {@code Profile}.
     *
     * @param theId the profile ID
     */
    public Profile(final String theId) {

        if (theId == null || theId.isBlank()) {
            final String msg = Res.get(Res.PROFILE_NULL_ID);
            throw new IllegalArgumentException(msg);
        }

        this.id = theId;
        this.facets = new EnumMap<>(ESchema.class);
    }

    /**
     * Adds a {@code facet}.
     *
     * @param facet the {@code Facet}
     */
    public void addFacet(final Facet facet) {

        this.facets.put(facet.data.schema, facet);
    }

    /**
     * Gets the list of all facets in the profile.
     *
     * @return the list of facets (a copy - altering this list does not affect the profile)
     */
    public List<Facet> getFacets() {

        final Collection<Facet> values = this.facets.values();
        return new ArrayList<>(values);
    }

    /**
     * Gets the facet to use for a specified {@code ESchema}.
     *
     * @param which the {@code ESchema}
     * @return the facet; null if none is defined in this profile
     */
    public Facet getFacet(final ESchema which) {

        return this.facets.get(which);
    }

    /**
     * Gets the login to use for a specified {@code ESchema}.
     *
     * @param which the {@code ESchema}
     * @return the login
     */
    public Login getLogin(final ESchema which) {

        return this.facets.get(which).login;
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
        int legacy = 0;

        for (final ESchema type : this.facets.keySet()) {
            switch (type) {
                case SYSTEM -> ++system;
                case MAIN -> ++main;
                case EXTERN -> ++extern;
                case ANALYTICS -> ++analytics;
                case TERM -> ++term;
                case LEGACY -> ++legacy;
            }
        }

        if (system == 0 || main == 0 || extern == 0 || analytics == 0 || term == 0 || legacy == 0) {
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
            if (legacy == 0) {
                builder.append(CoreConstants.SPC).append(ESchema.LEGACY);
            }
            final String end = Res.get(Res.PROFILE_MSG_END);
            builder.append(end);
            error = builder.toString();
        } else if (system > 1 || main > 1 || extern > 1 || analytics > 1 || term > 1 || legacy > 1) {
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
            if (legacy > 1) {
                builder.append(CoreConstants.SPC).append(ESchema.LEGACY);
            }
            final String end = Res.get(Res.PROFILE_MSG_END);
            builder.append(end);
            error = builder.toString();
        }

        return error;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.id.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param o the other object
     * @return true if the objects are equal
     */
    public boolean equals(final Object o) {

        final boolean equal;

        if (o == this) {
            equal = true;
        } else if (o instanceof final Profile profile) {
            equal = this.id.equals(profile.id);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares two profiles for order.  Comparison is based only on ID.
     *
     * @param o the object to be compared
     * @return 0 if this object is equal to {@code o}; -1 if this object precedes {@code o}; +1 if this object succeeds
     *         {@code o}
     */
    @Override
    public int compareTo(final Profile o) {

        return this.id.compareTo(o.id);
    }
}
