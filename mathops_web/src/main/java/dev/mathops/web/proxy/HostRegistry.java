package dev.mathops.web.proxy;

import dev.mathops.db.Contexts;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A container for all registered services associated with a single host name.
 */
final class HostRegistry {

    /** A path separator. */
    private static final int SLASH_CHAR = (int) '/';

    /** A map from path prefix to the list of registered services. */
    private final SortedMap<String, ServiceRegistration> serviceMap;

    /**
     * Constructs a new {@code HostRegistry}.
     */
    HostRegistry() {

        this.serviceMap = new TreeMap<>();
    }

    /**
     * Adds a new service registration.
     *
     * @param path the path
     * @param reg the service registration (replaces any existing registration under the specified path)
     */
    void registerService(final String path, final ServiceRegistration reg) {

        this.serviceMap.put(path, reg);
    }

    /**
     * Finds the service that a specified path should map to.
     *
     * @param path the path
     * @return the service; {@code null} if there is no service registered that can serve the path
     */
    ServiceRegistration findService(final String path) {

        ServiceRegistration result = null;

        int len = 0;
        final int pathLen = path.length();

        for (final ServiceRegistration test : this.serviceMap.values()) {

            final String testPath = test.getPath();
            final int testPathLen = testPath.length();

            if (testPathLen > len) {

                if ((pathLen > testPathLen && path.startsWith(testPath)
                        && (int) path.charAt(testPathLen) == SLASH_CHAR) || path.equals(testPath)) {
                    result = test;
                    len = testPathLen;
                }
            }
        }

        if (result == null) {
            // Attempt to find a default root site to handle unknown paths
            for (final ServiceRegistration test : this.serviceMap.values()) {
                final String testPath = test.getPath();
                if (Contexts.ROOT_PATH.equals(testPath)) {
                    result = test;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Generates the string representation of the registry.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "HostRegistry{}";
    }
}
