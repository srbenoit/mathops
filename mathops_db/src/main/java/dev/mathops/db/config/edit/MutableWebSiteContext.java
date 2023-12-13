package dev.mathops.db.config.edit;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.DataProfile;
import dev.mathops.db.config.WebSiteContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A mutable model of a website context configuration that can be edited through a GUI.
 */
public final class MutableWebSiteContext {

    /** The web server host. */
    private String host;

    /** A map from path to data profile. */
    private final Map<String, MutableDataProfile> paths;

    /**
     * Constructs a new {@code MutableWebSiteContext}.
     *
     * @param theHost  the web server host
     * @param thePaths a map from path name to data profile
     */
    public MutableWebSiteContext(final String theHost, final Map<String, MutableDataProfile> thePaths) {

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
     * Constructs a new {@code MutableWebSiteContext} from a {@code WebSiteContext}.
     *
     * @param source the source {@code WebSiteContext}
     * @param databaseConfig a database configuration that should contain all referenced data profiles
     */
    public MutableWebSiteContext(final WebSiteContext source, final MutableDatabaseConfig databaseConfig) {

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
     * @return the list of paths
     */
    public Collection<String> getPaths() {

        final Set<String> keys = this.paths.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableWebSiteContext{host='", this.host, "', paths='", this.paths, "}");
    }
}
