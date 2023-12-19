package dev.mathops.app.db.config;

import dev.mathops.db.config.DataProfile;
import dev.mathops.db.config.DatabaseConfig;
import dev.mathops.db.config.ESchemaType;
import dev.mathops.db.config.LoginConfig;
import dev.mathops.db.config.ServerConfig;
import dev.mathops.db.config.WebContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A mutable model of a database configuration that can be edited through a GUI.  This class constrains the model to be
 * valid at all times, and can generate a {@code DatabaseConfig} at any time.
 */
public final class MutableDatabaseConfig {

    /** The map from server configuration ID to the mutable server configuration object. */
    private final Map<String, MutableServerConfig> serverConfigs;

    /** The map from login configuration ID to the mutable login configuration object. */
    private final Map<String, MutableLoginConfig> loginConfigs;

    /** A map from profile ID to the mutable data profile configuration object. */
    private Map<String, MutableDataProfile> dataProfiles;

    /** A map from web host to the associated website context. */
    private final Map<String, MutableWebContext> webContexts;

    /** A map from code context name to data profile ID that represents all code contexts. */
    private final Map<String, String> codeContexts;

    /**
     * Constructs a new, empty {@code MutableDatabaseConfig}.
     */
    private MutableDatabaseConfig() {

        this.serverConfigs = new LinkedHashMap<>(10);
        this.loginConfigs = new LinkedHashMap<>(10);
        this.dataProfiles = new LinkedHashMap<>(10);
        this.webContexts = new LinkedHashMap<>(10);
        this.codeContexts = new LinkedHashMap<>(10);
    }

    /**
     * Constructs a new {@code MutableDatabaseConfig} from a {@code DatabaseConfig}.
     *
     * @param source the source {@code DatabaseConfig}
     */
    public MutableDatabaseConfig(final DatabaseConfig source) {

        this();

        for (final ServerConfig server : source.getServers()) {
            final MutableServerConfig mutableServer = new MutableServerConfig(server);
            final String serverId = mutableServer.getId();
            this.serverConfigs.put(serverId, mutableServer);
        }

        for (final LoginConfig login : source.getLogins()) {
            final MutableLoginConfig mutableLogin = new MutableLoginConfig(login);
            final String loginId = mutableLogin.getId();
            this.loginConfigs.put(loginId, mutableLogin);
        }

        for (final DataProfile profile : source.getDataProfiles()) {
            final MutableDataProfile mutableProfile = new MutableDataProfile(profile, this.loginConfigs);
            final String profileId = mutableProfile.getId();
            this.dataProfiles.put(profileId, mutableProfile);
        }

        for (final WebContext web : source.getWebContexts()) {
            final MutableWebContext mutableWeb = new MutableWebContext(web, this);
            final String host = mutableWeb.getHost();
            this.webContexts.put(host, mutableWeb);
        }

        for (final String id : source.getCodeContextIds()) {
            final DataProfile profile = source.getCodeDataProfile(id);
            this.codeContexts.put(id, profile.id);
        }
    }

    //

    /**
     * Gets the list of server configuration IDs.
     *
     * @return a copy of the list of server configuration IDs
     */
    public List<String> getServerConfigIds() {

        final Set<String> keys = this.serverConfigs.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the specified server configuration.
     *
     * @param serverConfigId the server configuration ID
     * @return the server configuration object; {@code null} if the specified server configuration is not defined
     */
    public MutableServerConfig getServerConfig(final String serverConfigId) {

        return this.serverConfigs.get(serverConfigId);
    }

    /**
     * Attempts to delete the server configuration with a specified ID.
     *
     * @param serverConfigId the server configuration ID
     * @return the server configuration that was removed; {@code null} if the specified server configuration did not
     * exist, and no action was taken
     * @throws IllegalArgumentException if the server configuration being removed is referenced by a login
     */
    public MutableServerConfig deleteServerConfig(final String serverConfigId) {

        if (this.serverConfigs.containsKey(serverConfigId)) {
            for (final MutableLoginConfig login : this.loginConfigs.values()) {
                if (login.getServer().equals(serverConfigId)) {
                    throw new IllegalArgumentException("Server is referenced by a login");
                }
            }
        }

        return this.serverConfigs.remove(serverConfigId);
    }

    /**
     * Gets the list of login IDs of defined logins.
     *
     * @return a copy of the list of login IDs
     */
    public List<String> getLoginIds() {

        final Set<String> keys = this.loginConfigs.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the specified login.
     *
     * @param loginId the login ID
     * @return the login object; {@code null} if the specified login is not defined
     */
    public MutableLoginConfig getLogin(final String loginId) {

        return this.loginConfigs.get(loginId);
    }

    /**
     * Attempts to delete the login with a specified ID.
     *
     * @param loginId the login ID
     * @return the login that was removed; {@code null} if the specified login did not exist, and no action was taken
     * @throws IllegalArgumentException if the login being removed is referenced by a data profile
     */
    MutableLoginConfig deleteLogin(final String loginId) {

        if (this.loginConfigs.containsKey(loginId)) {
            for (final MutableDataProfile profile : this.dataProfiles.values()) {
                for (final ESchemaType sch : ESchemaType.values()) {
                    final MutableLoginConfig referencedLogin = profile.getSchemaLogin(sch);
                    if (Objects.equals(referencedLogin, loginId)) {
                        throw new IllegalArgumentException("Login is referenced by a data profile");
                    }
                }
            }
        }

        return this.loginConfigs.remove(loginId);
    }

    //

    /**
     * Gets the list of data profile IDs.
     *
     * @return a copy of the list of data profile IDs
     */
    public List<String> getDataProfileIds() {

        final Set<String> keys = this.dataProfiles.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the specified data profile.
     *
     * @param dataProfileId the data profile ID
     * @return the data profile object; {@code null} if the specified data profile is not defined
     */
    MutableDataProfile getDataProfile(final String dataProfileId) {

        return this.dataProfiles.get(dataProfileId);
    }

    /**
     * Attempts to delete the data profile with a specified ID.
     *
     * @param dataProfileId the data profile ID
     * @return the data profile that was removed; {@code null} if the specified data profile did not exist, and no
     * action was taken
     * @throws IllegalArgumentException if the data profile being removed is referenced by a code or website context
     */
    public MutableDataProfile deleteDataProfile(final String dataProfileId) {

        if (this.codeContexts.containsKey(dataProfileId)){
            throw new IllegalArgumentException("Data profile is referenced by an active code context");
        }
        for (final MutableWebContext webContext : this.webContexts.values()) {
            if (webContext.isDataProfileIdReferenced(dataProfileId)) {
                throw new IllegalArgumentException("Data profile is referenced by an active website context");
            }
        }

        return this.dataProfiles.remove(dataProfileId);
    }

    //

    /**
     * Gets the list of hostnames for which web contexts are defined.
     *
     * @return a copy of the list of hostnames
     */
    public List<String> getWebHosts() {

        final Set<String> keys = this.webContexts.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the web context associated with a specified host name.
     *
     * @param host the host name
     * @return the associated web context; {@code null} if the specified host is not defined
     */
    public MutableWebContext getWebContext(final String host) {

        return this.webContexts.get(host);
    }

    /**
     * Deletes the web context for a specified host name.
     *
     * @param host the host name
     * @return the web context that was removed; {@code null} if the specified web context did not exist, and no action
     * was taken
     */
    public MutableWebContext deleteWebContext(final String host) {

        return this.webContexts.remove(host);
    }

    //

    /**
     * Gets the list of code context IDs.
     *
     * @return a copy of the list of code context IDs
     */
    public List<String> getCodeContextIds() {

        final Set<String> keys = this.codeContexts.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the data profile ID associated with a specified code context ID.
     *
     * @param codeContextId the code context ID
     * @return the data profile ID; {@code null} if the specified code context is not defined
     */
    public String getCodeContextProfileId(final String codeContextId) {

        return this.codeContexts.get(codeContextId);
    }

    /**
     * Deletes the code context with a specified ID.
     *
     * @param codeContextId the code context ID
     * @return the data profile ID of the code context that was removed; {@code null} if the specified code context
     * did not exist, and no action was taken
     */
    public String deleteCodeContext(final String codeContextId) {

        return this.codeContexts.remove(codeContextId);
    }

    /**
     * Attempts to set the data profile ID associated with a code context ID.  This can either create a new code
     * context, of the code context ID was not previously defined, or can update the data profile ID on an existing
     * code context.
     *
     * @param codeContextId the code context ID
     * @param dataProfileId the data profile ID
     * @throws IllegalArgumentException if either argument is {@code null} or blank, or if {@code dataProfileId} is not
     * a valid data profile ID
     */
    public void setCodeContextProfileId(final String codeContextId, final String dataProfileId) {

        if (codeContextId == null || codeContextId.isBlank()) {
            throw new IllegalArgumentException("Code context ID may not be null or blank");
        }
        if (dataProfileId == null || dataProfileId.isBlank()) {
            throw new IllegalArgumentException("Data profile ID may not be null or blank");
        }
        if (this.dataProfiles.containsKey(dataProfileId)) {
            this.codeContexts.put(codeContextId, dataProfileId);
        } else {
            throw new IllegalArgumentException("Undefined data profile ID");
        }
    }
}
