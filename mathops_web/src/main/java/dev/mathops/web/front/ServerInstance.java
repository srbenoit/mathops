package dev.mathops.web.front;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.installation.Installation;

import java.util.HashMap;
import java.util.Map;

/**
 * A server instance, with a specified base directory and configuration file name.
 */
public final class ServerInstance {

    /** Server instances, by installation. */
    private static final Map<Installation, ServerInstance> INSTANCES;

    private final Installation installation;

    static {
        INSTANCES = new HashMap<>(2);
    }

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param theInstallation the installation under which the server is running
     */
    private ServerInstance(final Installation theInstallation) {

        this.installation = theInstallation;
    }

    /**
     * Gets the single instance corresponding to a specified configuration file.
     *
     * @param theInstallation the installation under which this instance is running
     * @return the instance
     */
    public static ServerInstance get(final Installation theInstallation) {

        ServerInstance instance;

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            instance = INSTANCES.get(theInstallation);

            if (instance == null) {
                instance = new ServerInstance(theInstallation);
                if (!instance.isLoaded()) {
                    theInstallation.addWarning("Installation not loaded");
                }
                INSTANCES.put(theInstallation, instance);
            }
        }

        return instance;
    }

    /**
     * Tests whether the configuration was loaded successfully and contained all required parameters.
     *
     * @return {@code true} if configuration was loaded
     */
    private boolean isLoaded() {

        return this.installation.isLoaded();
    }

    /**
     * Generates the string representation of the server instance.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(150);

        builder.add("Server instance (", this.installation.toString(), ")");

        return builder.toString();
    }

    /**
     * Gets the installation.
     *
     * @return the installation
     */
    public Installation getInstallation() {

        return this.installation;
    }
}
