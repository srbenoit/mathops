package dev.mathops.db.config.edit;

import dev.mathops.db.config.DatabaseConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A mutable model of a database configuration that can be edited through a GUI.  This class constrains the model to be
 * valid at all times, and can generate a {@code DatabaseConfig} at any time.
 */
public final class MutableDatabaseConfig {

    /** The list of mutable server configuration objects. */
    private final List<MutableServerConfig> servers;

    /** A map from profile ID to the mutable data profile configuration object. */
    private Map<String, MutableDataProfile> profiles;

    /** A map from code context name to data profile ID that represents all code contexts. */
    private final Map<String, String> codeContexts;

    /**
     * Constructs a new, empty {@code MutableDatabaseConfig}.
     */
    public MutableDatabaseConfig() {

        this.servers = new ArrayList<>(10);
        this.profiles = new HashMap<>(10);
        this.codeContexts = new HashMap<>(10);
    }

    /**
     * Constructs a new {@code MutableDatabaseConfig} from a {@code DatabaseConfig}.
     *
     * @param source the source {@code DatabaseConfig}
     */
    public MutableDatabaseConfig(final DatabaseConfig source) {

        this.servers = new ArrayList<>(10);
        this.profiles = new HashMap<>(10);
        this.codeContexts = new HashMap<>(10);
    }

    /**
     * Gets the data profile with a specified ID.
     * @param id the ID
     * @return the associated data profile; {@code null} if none has the specified ID
     */
    MutableDataProfile getDataProfile(final String id) {

        return this.profiles.get(id);
    }
}
