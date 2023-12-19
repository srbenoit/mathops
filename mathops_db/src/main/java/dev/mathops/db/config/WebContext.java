package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a web server host, with a map from path to data profile.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;web host='...'&gt;
 *   &lt;site path='...' profile='...'&gt;
 * &lt;/web&gt;
 * </pre>
 */
public final class WebContext {

    /** The web server host. */
    public final String host;

    /** A map from path to data profile ID. */
    private final Map<String, DataProfile> paths;

    /**
     * Constructs a new {@code WebContext}.
     *
     * @param theHost  the host
     * @param thePaths a map from path name to data profile
     * @throws IllegalArgumentException if any argument is null
     */
    WebContext(final String theHost, final Map<String, DataProfile> thePaths) {

        if (theHost == null) {
            throw new IllegalArgumentException("Host may not be null");
        }
        if (thePaths == null) {
            throw new IllegalArgumentException("Paths map may not be null");
        }

        this.host = theHost;

        final int count = thePaths.size();
        this.paths = new HashMap<>(count);
        for (final Map.Entry<String, DataProfile> entry : thePaths.entrySet()) {
            final String path = entry.getKey();
            final DataProfile profile = entry.getValue();

            if (path != null && profile != null) {
                this.paths.put(path, profile);
            }
        }
    }

    /**
     * Gets the list of paths for which data profiles are configured.
     *
     * @return the list of paths
     */
    public List<String> getPaths() {

        final Collection<String> keys = this.paths.keySet();

        return new ArrayList<>(keys);
    }

    /**
     * Gets the data profile configured for a specified path.
     * @param path the path
     * @return the data profile; {@code null} if none is configured for the specified path)
     */
    public DataProfile getProfile(final String path) {

        return this.paths.get(path);
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

        if (obj instanceof final WebContext test) {
            equal = test.host.equals(this.host) && test.paths.equals(this.paths);
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

        return this.host.hashCode() + this.paths.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("WebContext{host='", this.host, "', paths={", this.paths, "}}");
    }
}
