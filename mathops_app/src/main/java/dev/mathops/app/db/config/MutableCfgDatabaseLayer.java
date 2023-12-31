package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.config.CfgCodeContext;
import dev.mathops.db.config.CfgDataProfile;
import dev.mathops.db.config.CfgDatabase;
import dev.mathops.db.config.CfgDatabaseLayer;
import dev.mathops.db.config.CfgInstance;
import dev.mathops.db.config.CfgLogin;
import dev.mathops.db.config.CfgWebContext;
import javafx.beans.property.MapPropertyBase;
import javafx.beans.property.SimpleMapProperty;
import oracle.sql.Mutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A mutable configuration of the database layer.  This object provides a map from named web context (host and path) or
 * named code context to the data profile that will be used to connect to the database for that context. This can be
 * used to select different profiles for various named contexts.
 *
 * <p>
 * A data profile chooses a database server and login for each defined schema.  A database server represents an
 * installation of a database product, such as MySQL or PostgreSQL, on a server machine, and a login represents
 * a username/password combination to connect to that database server product.
 *
 * <p>
 * Typically, each website or logical grouping of code will use a distinct named context. That way, the entire site or
 * application will use a common database (with consistent data), but can be changed as a unit to another database for
 * testing or to provide alternate data.
 *
 * <p>
 * To create a new {@code MutableCfgSite}, the UI would present a field to enter
 * a path, and a dropdown to select a profile, with a button to execute the addition, which would fail if the path
 * were empty or not unique, or if the profile had not been selected.  Creating a new site adds this object to the list
 * of dependencies on the selected profile.
 *
 * <p>
 * If the path is updated and an "apply" button pressed in a GUI, the new path is tested for uniqueness within the
 * owning web context.
 */
public final class MutableCfgDatabaseLayer {

    /** A mutable map from instance ID to the mutable instance configuration. */
    private final MapPropertyBase<String, MutableCfgInstance> instances;

    /** A mutable map from data profile ID to the mutable data profile configuration. */
    private final MapPropertyBase<String, MutableCfgDataProfile> dataProfiles;

    /** A mutable map from web context path to the mutable web context configuration. */
    private final MapPropertyBase<String, MutableCfgWebContext> webContexts;

    /** A mutable map from code context ID to the mutable code context configuration. */
    private final MapPropertyBase<String, MutableCfgCodeContext> codeContexts;

    /**
     * Constructs a new, empty {@code MutableCfgDatabaseLayer}.
     */
    public MutableCfgDatabaseLayer() {

        this.instances = new SimpleMapProperty<>();
        this.dataProfiles = new SimpleMapProperty<>();
        this.webContexts = new SimpleMapProperty<>();
        this.codeContexts = new SimpleMapProperty<>();
    }

    /**
     * Constructs a new {@code MutableCfgDatabaseLayer} from a {@code CfgDatabaseLayer}.
     *
     * @param source   the source {@code CfgDatabaseLayer}
     */
    public MutableCfgDatabaseLayer(final CfgDatabaseLayer source) {

        this();

        for (final CfgInstance instance : source.getInstances()) {
            final MutableCfgInstance mutableInstance = new MutableCfgInstance(instance);
            this.instances.put(instance.id, mutableInstance);
        }
        for (final CfgDataProfile dataProfile : source.getDataProfiles()) {
            final MutableCfgDataProfile mutableDataProfile = new MutableCfgDataProfile(dataProfile);
            this.dataProfiles.put(dataProfile.id, mutableDataProfile);
        }
        for (final CfgWebContext webContext : source.getWebContexts()) {
            final MutableCfgWebContext mutableWebContext = new MutableCfgWebContext(webContext);
            this.webContexts.put(webContext.host, mutableWebContext);
        }
        for (final CfgCodeContext codeContext : source.getCodeContexts()) {
            final MutableCfgCodeContext mutableCodeContext = new MutableCfgCodeContext(codeContext);
            this.codeContexts.put(codeContext.id, mutableCodeContext);
        }
    }

    /**
     * Gets the instances property.
     *
     * @return the instances property
     */
    public MapPropertyBase<String, MutableCfgInstance> getInstancesProperty() {

        return this.instances;
    }

    /**
     * Gets the data profiles property.
     *
     * @return the data profiles property
     */
    public MapPropertyBase<String, MutableCfgDataProfile> getDataProfilesProperty() {

        return this.dataProfiles;
    }

    /**
     * Gets the web contexts property.
     *
     * @return the web contexts property
     */
    public MapPropertyBase<String, MutableCfgWebContext> getWebContextsProperty() {

        return this.webContexts;
    }

    /**
     * Gets the code contexts property.
     *
     * @return the code contexts property
     */
    public MapPropertyBase<String, MutableCfgCodeContext> getCodeContextsProperty() {

        return this.codeContexts;
    }

    /**
     * Generate an immutable {@code CfgDatabaseLayer} from this object.
     *
     * @return the generated {@code CfgDatabaseLayer}
     */
    CfgDatabaseLayer toCfgDatabaseLayer() {

        final List<CfgInstance> instanceMap = new ArrayList<>(10);
        final Map<String, CfgDataProfile> dataProfileMap = new HashMap<>(10);
        final List<CfgWebContext> webContextMap = new ArrayList<>(10);
        final List<CfgCodeContext> codeContextMap = new ArrayList<>(10);

        final Map<String, CfgDatabase> globalDatabases = new HashMap<>(40);
        final Map<String, CfgLogin> globalLogins = new HashMap<>(40);

        for (final MutableCfgInstance mutableInstance : this.instances.values()) {
            final CfgInstance instance = mutableInstance.toCfgInstance();
            instanceMap.add(instance);

            final Map<String, CfgDatabase> instanceDatabases = instance.getDatabaseMap();
            globalDatabases.putAll(instanceDatabases);

            final Map<String, CfgLogin> instanceLogins = instance.getLoginsMap();
            globalLogins.putAll(instanceLogins);
        }

        for (final MutableCfgDataProfile mutableDataProfile : this.dataProfiles.values()) {
            final CfgDataProfile dataProfile = mutableDataProfile.toCfgDataProfile(globalDatabases, globalLogins);
            dataProfileMap.put(dataProfile.id, dataProfile);
        }

        for (final MutableCfgWebContext mutableWebContext : this.webContexts.values()) {
            final CfgWebContext webContext = mutableWebContext.toCfgWebContext(dataProfileMap);
            webContextMap.add(webContext);
        }

        for (final MutableCfgCodeContext mutableCodeContext : this.codeContexts.values()) {
            final CfgCodeContext codeContext = mutableCodeContext.toCfgCodeContext(dataProfileMap);
            codeContextMap.add(codeContext);
        }

        final Collection<CfgDataProfile> dataProfileList = dataProfileMap.values();
        return new CfgDatabaseLayer(instanceMap, dataProfileList, webContextMap, codeContextMap);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableCfgDatabaseLayer{instances=[", this.instances, "],dataProfiles=[",
                this.dataProfiles, "],webContexts=[", this.webContexts, "],codeContexts=[", this.codeContexts, "]}");
    }

    public static void main(final String... args) {

        final CfgDatabaseLayer config = CfgDatabaseLayer.getDefaultInstance();
        final MutableCfgDatabaseLayer mutable = new MutableCfgDatabaseLayer(config);

        Log.fine(mutable);
    }
}
