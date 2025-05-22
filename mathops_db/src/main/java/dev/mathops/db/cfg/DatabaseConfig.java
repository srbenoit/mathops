package dev.mathops.db.cfg;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.ParsingException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A "database config" container to store all information read from the database configuration file.
 */
public class DatabaseConfig {

    /** The default database configuration. */
    private final static DatabaseConfig DEFAULT = loadDefault();

    /** A list of server definitions. */
    private final List<Server> servers;

    /** A map from login ID to login definition. */
    private final Map<String, Login> logins;

    /** A map from data ID to data definition. */
    private final Map<String, Data> datas;

    /** A map from profile ID to profile definition. */
    private final Map<String, Profile> profiles;

    /** A map from host to web context definition. */
    private final Map<String, WebContext> webContexts;

    /** A map from code contextID to code context definition. */
    private final Map<String, CodeContext> codeContexts;

    /**
     * Constructs a new {@code DatabaseConfig}
     */
    DatabaseConfig() {

        this.servers = new ArrayList<>(10);
        this.logins = new HashMap<>(10);
        this.datas = new HashMap<>(40);
        this.profiles = new HashMap<>(20);
        this.webContexts = new HashMap<>(10);
        this.codeContexts = new HashMap<>(10);
    }

    /**
     * Loads the default database configuration.
     *
     * @return the loaded configuration (empty if loading failed; never null)
     */
    private static DatabaseConfig loadDefault() {

        DatabaseConfig config;

        final File source = DatabaseConfigXml.getDefaultFile();

        if (source == null) {
            final String path = System.getProperty("user.dir");
            final File dir = new File(path);
            final File cfgFile = new File(dir, DatabaseConfigXml.FILENAME);
            try {
                config = DatabaseConfigXml.load(cfgFile);
                final String filePath = cfgFile.getAbsolutePath();
                final String msg = Res.fmt(Res.DATABASE_CFG_LOADED_FROM, filePath);
                Log.info(msg);
            } catch (final IOException | ParsingException ex) {
                Log.warning(ex);
                config = new DatabaseConfig();
            }
        } else {
            try {
                config = DatabaseConfigXml.load(source);
                final String filePath = source.getAbsolutePath();
                final String msg = Res.fmt(Res.DATABASE_CFG_LOADED_FROM, filePath);
                Log.info(msg);
            } catch (final IOException | ParsingException ex) {
                Log.warning(ex);
                config = new DatabaseConfig();
            }
        }

        return config;
    }

    /**
     * Gets the default database configuration.
     *
     * @return the default configuration (empty if loading failed; never null)
     */
    public static DatabaseConfig getDefault() {

        return DEFAULT;
    }

    /**
     * Adds a server definition.
     *
     * @param server the server definition
     */
    void addServer(final Server server) {

        this.servers.add(server);
    }

    /**
     * Gets the list of servers.
     *
     * @return the list of servers
     */
    public List<Server> getServers() {

        return this.servers;
    }

    /**
     * Adds a login definition.
     *
     * @param login the login definition
     */
    void addLogin(final Login login) {

        this.logins.put(login.id, login);
    }

    /**
     * Adds a data definition.
     *
     * @param data the data definition
     */
    public void addData(final Data data) {

        this.datas.put(data.id, data);
    }

    /**
     * Gets the list of all login IDs.
     *
     * @return the list of login IDs
     */
    public List<String> getLoginIds() {

        final Set<String> keys = this.logins.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the {@code Login} with a specified ID.
     *
     * @param loginId the login ID
     * @return the {@code Login} object; null if none found
     */
    public Login getLogin(final String loginId) {

        return this.logins.get(loginId);
    }

    /**
     * Gets the list of all data IDs.
     *
     * @return the list of data IDs
     */
    public List<String> getDataIds() {

        final Set<String> keys = this.datas.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the {@code Data} with a specified ID.
     *
     * @param dataId the data ID
     * @return the {@code Data} object; null if none found
     */
    public Data getData(final String dataId) {

        return this.datas.get(dataId);
    }

    /**
     * Adds a profile definition.
     *
     * @param profile the profile definition
     */
    void addProfile(final Profile profile) {

        this.profiles.put(profile.id, profile);
    }

    /**
     * Gets the list of profiles.
     *
     * @return the list of profiles
     */
    public List<Profile> getProfiles() {

        final Collection<Profile> values = this.profiles.values();

        return new ArrayList<>(values);
    }

    /**
     * Gets the {@code Profile} with a specified ID.
     *
     * @param profileId the profile ID
     * @return the {@code Profile} object; null if none found
     */
    public Profile getProfile(final String profileId) {

        return this.profiles.get(profileId);
    }

    /**
     * Adds a web context definition.
     *
     * @param webContext the web context definition
     */
    void addWebContext(final WebContext webContext) {

        this.webContexts.put(webContext.host, webContext);
    }


    /**
     * Gets the {@code Profile} for the site with a specified path within the web context with a specified host.
     *
     * @param host the web context host
     * @param path the site path
     * @return the {@code Profile} object; null if none found
     */
    public Site getSite(final String host, final String path) {

        final WebContext context = this.webContexts.get(host);

        return context == null ? null : context.getSite(path);
    }

    /**
     * Gets the {@code Profile} for the site with a specified path within the web context with a specified host.
     *
     * @param host the web context host
     * @param path the site path
     * @return the {@code Profile} object; null if none found
     */
    public Profile getWebProfile(final String host, final String path) {

        final WebContext context = this.webContexts.get(host);
        final Site site = context == null ? null : context.getSite(path);

        return site == null ? null : site.profile;
    }

    /**
     * Gets the list of web hosts.
     *
     * @return the list of hosts
     */
    public List<String> getWebHosts() {

        final Set<String> keys = this.webContexts.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the list of sites within a host.
     *
     * @param host the host
     * @return the list of sites
     */
    public List<String> getWebSites(final String host) {

        final WebContext context = this.webContexts.get(host);

        return context.getSites();
    }

    /**
     * Adds a web context definition.
     *
     * @param codeContext the web context definition
     */
    void addCodeContext(final CodeContext codeContext) {

        this.codeContexts.put(codeContext.id, codeContext);
    }

    /**
     * Gets a list of all code context IDs.
     *
     * @return the list of code context IDs
     */
    public List<String> getCodeContextIds() {

        final Set<String> keys = this.codeContexts.keySet();
        return new ArrayList<>(keys);
    }

    /**
     * Gets the {@code Profile} for the code context with a specified ID.
     *
     * @param codeContextId the code context ID
     * @return the {@code Profile} object; null if none found
     */
    public Profile getCodeProfile(final String codeContextId) {

        final CodeContext context = this.codeContexts.get(codeContextId);

        return context == null ? null : context.profile;
    }
}
