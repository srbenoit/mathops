package dev.mathops.db.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A "database config" container to store all information read from the database configuration file.
 */
public class DatabaseConfig {

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
     * Adds a server definition.
     *
     * @param server the server definition
     */
    void addServer(final Server server) {

        this.servers.add(server);
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
     * Gets the {@code Login} with a specified ID.
     *
     * @param loginId the login ID
     * @return the {@code Login} object; null if none found
     */
    Login getLogin(final String loginId) {

        return this.logins.get(loginId);
    }

    /**
     * Gets the {@code Data} with a specified ID.
     *
     * @param dataId the data ID
     * @return the {@code Data} object; null if none found
     */
    Data getData(final String dataId) {

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
     * Gets the {@code Profile} with a specified ID.
     *
     * @param profileId the profile ID
     * @return the {@code Profile} object; null if none found
     */
    Profile getProfile(final String profileId) {

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
     * Adds a web context definition.
     *
     * @param codeContext the web context definition
     */
    void addCodeContext(final CodeContext codeContext) {

        this.codeContexts.put(codeContext.id, codeContext);
    }
}
