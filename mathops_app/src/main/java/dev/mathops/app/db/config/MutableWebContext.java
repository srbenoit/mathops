package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.DataProfile;
import dev.mathops.db.config.WebContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A mutable model of a web context configuration that can be edited through a GUI.
 */
public final class MutableWebContext {

    /** The web server host. */
    private String host;

    /** A map from site path to data profile. */
    private final Map<String, MutableDataProfile> paths;

    /**
     * Constructs a new {@code MutableWebContext}.
     *
     * @param theHost  the web server host
     * @param thePaths a map from site path name to data profile
     */
    public MutableWebContext(final String theHost, final Map<String, MutableDataProfile> thePaths) {

        if (theHost == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        if (thePaths == null) {
            throw new IllegalArgumentException("Paths map may not be null");
        }

        this.host = theHost;

        final int count = thePaths.size();
        this.paths = new HashMap<>(count);
        for (final Map.Entry<String, MutableDataProfile> entry : thePaths.entrySet()) {
            final String path = entry.getKey();
            final MutableDataProfile profile = entry.getValue();

            if (path != null && profile != null) {
                this.paths.put(path, profile);
            }
        }
    }

    /**
     * Constructs a new {@code MutableWebContext} from a {@code WebContext}.
     *
     * @param source the source {@code WebContext}
     * @param databaseConfig a database configuration that should contain all referenced data profiles
     */
    public MutableWebContext(final WebContext source, final MutableDatabaseConfig databaseConfig) {

        this.host = source.host;

        final List<String> sourcePaths = source.getPaths();
        final int count = sourcePaths.size();
        this.paths = new HashMap<>(count);

        for (final String path : sourcePaths) {
            final DataProfile profile = source.getProfile(path);
            final MutableDataProfile mutableProfile = databaseConfig.getDataProfile(profile.id);

            if (mutableProfile == null) {
                throw new IllegalArgumentException("Referenced profile is not in database configuration.");
            }

            this.paths.put(path, mutableProfile);
        }
    }

    /**
     * Gets the hostname.
     *
     * @return the hostname
     */
    public String getHost() {

        return this.host;
    }

    /**
     * Gets the list of paths for which a profile is configured.
     *
     * @return a copy of the list of paths
     */
    public List<String> getPaths() {

        final Set<String> keys = this.paths.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Tests whether any site in this object references a specified data profile ID.  Used before deleting a data
     * profile to ensure it is not referenced.
     *
     * @param dataProfileId the data profile ID
     * @return true if the data profile ID is referenced; false if not
     */
    boolean isDataProfileIdReferenced(final String dataProfileId) {

        boolean referenced = false;

        for (final MutableDataProfile profile : this.paths.values()) {
            if (profile.getId().equals(dataProfileId)) {
                referenced = true;
                break;
            }
        }

        return referenced;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableWebContext{host='", this.host, "', paths='", this.paths, "}");
    }
}
