package dev.mathops.web.front;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.installation.Installation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
     * Gets the base directory.
     *
     * @return the base directory
     */
    public File getBaseDir() {

        return this.installation.baseDir;
    }

//    /**
//     * Gets the name of the configuration file.
//     *
//     * @return the configuration file
//     */
//    public String getCfgFile() {
//
//        return this.installation.cfgFile;
//    }

    /**
     * Gets the loaded properties, which should contain at least the following.
     *
     * <ul>
     * <li>public-dir (a directory path)
     * </ul>
     *
     * @return the properties
     */
    public Properties getProperties() {

        return this.installation.properties;
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
