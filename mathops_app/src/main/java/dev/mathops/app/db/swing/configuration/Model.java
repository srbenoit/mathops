package dev.mathops.app.db.swing.configuration;

import dev.mathops.app.db.config.MutableDatabaseConfig;
import dev.mathops.db.config.DatabaseConfig;

/**
 * The "Model" in the "Model/View/Controller" perspective.  The model is created first, then "View" components are
 * created and register with the model to receive notifications of state changes, and "Controller" components are
 * created and add the model as a listener for actions that can alter the model.
 *
 * <p>Some UI components can behave as both "View" and "Controller", but these components still act on a constructed
 * "Model" object.
 */
public final class Model {

    /** The active database configuration. */
    private DatabaseConfig activeConfig;

    /** The mutable database configuration based on the currently loaded configuration. */
    private MutableDatabaseConfig mutableConfig;

    /**
     * Constructs a new {@code Model}.
     *
     * @param theActiveConfig the initial active configuration
     */
    Model(final DatabaseConfig theActiveConfig) {

        this.activeConfig = theActiveConfig;
        this.mutableConfig = new MutableDatabaseConfig(this.activeConfig);
    }

    /**
     * Gets the active configuration.
     *
     * @return the active configuration
     */
    public DatabaseConfig getActiveConfig() {

        return this.activeConfig;
    }

    /**
     * Gets the mutable configuration in its current state.
     *
     * @return the mutable configuration in its current state
     */
    public MutableDatabaseConfig getMutableConfig() {

        return this.mutableConfig;
    }
}
