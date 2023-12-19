package dev.mathops.db.config;

import dev.mathops.core.PathList;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.core.parser.xml.XmlContent;
import dev.mathops.db.DbFileLoader;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable database configuration object.  This object provides a map from named web context (host and path) or
 * named code context to the data profile that will be used to connect to the database for that context.
 * This can be used to select different profiles for various named contexts.
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
 * This context map is loaded from an XML file stored in a given directory.
 */
public final class DatabaseConfig {

    /** Name of file where context map data is stored. */
    private static final String FILENAME = "db_config.xml";

    /** The XML tag for the context map. */
    private static final String XML_TAG = "database-config";

    /** The XML tag for a web context. */
    private static final String WEB_TAG = "web";

    /** The XML tag for a site context. */
    private static final String SITE_TAG = "site";

    /** The XML tag for a code context. */
    private static final String CODE_TAG = "code";

    /** The context attribute name. */
    private static final String CONTEXT_ATTR = "context";

    /** The profile attribute name. */
    private static final String PROFILE_ATTR = "profile";

    /** The host attribute name. */
    private static final String HOST_ATTR = "host";

    /** The path attribute name. */
    private static final String PATH_ATTR = "path";

    /** An empty array used when converting strings to arrays. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** An empty array used when converting collections of server configurations to arrays. */
    private static final ServerConfig[] EMPTY_SERVER_CFG_ARRAY = new ServerConfig[0];

    /** An empty array used when converting collections of login configurations to arrays. */
    private static final LoginConfig[] EMPTY_LOGIN_CFG_ARRAY = new LoginConfig[0];

    /** An empty array used when converting collections of data profiles to arrays. */
    private static final DataProfile[] EMPTY_DATA_PROFILE_ARRAY = new DataProfile[0];

    /** An empty array used when converting collections of web contexts to arrays. */
    private static final WebContext[] EMPTY_WEB_CONTEXT_ARRAY = new WebContext[0];

    //

    /** Map from server ID to server configuration object. */
    private final Map<String, ServerConfig> servers;

    /** Map from login configuration ID to the login configuration object. */
    private final Map<String, LoginConfig> logins;

    /** Map from data profile configuration ID to the configuration object. */
    private final Map<String, DataProfile> dataProfiles;

    /** Map from hostname to website context containers. */
    private final Map<String, WebContext> webContexts;

    /** Map from context ID to profile for code contexts. */
    private final Map<String, DataProfile> codeContexts;

    /**
     * A private constructor that creates an empty {@code DatabaseConfig} in the event the context map could not be
     * loaded.
     */
    private DatabaseConfig() {

        this.servers = new LinkedHashMap<>(10);
        this.logins = new HashMap<>(10);
        this.dataProfiles = new LinkedHashMap<>(10);
        this.webContexts = new LinkedHashMap<>(10);
        this.codeContexts = new LinkedHashMap<>(10);
    }

    /**
     * A private constructor to ensure {@code getInstance} is used and a single instance exists for a given
     * configuration directory.
     *
     * @param elem the XML element from which to extract the {@code DatabaseConfig}
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private DatabaseConfig(final NonemptyElement elem) throws ParsingException {

        this();

        final int numChildren = elem.getNumChildren();

        for (int i = 0; i < numChildren; ++i) {
            final INode child = elem.getChild(i);

            if (child instanceof final NonemptyElement innerElem) {
                final String innerTag = innerElem.getTagName();
                switch (innerTag) {
                    case DataProfile.ELEM_TAG -> processDataProfileNode(innerElem);
                    case WEB_TAG -> processWebNode(innerElem);
                    case null, default -> {
                        final String msg = Res.fmt(Res.DB_CFG_UNEXPEC_CHILD, innerTag);
                        Log.warning(msg);
                    }
                }
            } else if (child instanceof final EmptyElement innerElem) {
                final String innerTag = innerElem.getTagName();
                switch (innerTag) {
                    case ServerConfig.ELEM_TAG -> processServerNode(innerElem);
                    case LoginConfig.ELEM_TAG -> processLoginNode(innerElem);
                    case CODE_TAG -> processCodeNode(innerElem);
                    case null, default -> {
                        final String msg = Res.fmt(Res.DB_CFG_UNEXPEC_CHILD, innerTag);
                        Log.warning(msg);
                    }
                }
            }
        }
    }

    /**
     * Processes a "server" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processServerNode(final EmptyElement elem) throws ParsingException {

        final ServerConfig server = new ServerConfig(elem);

        if (this.servers.containsKey(server.id)) {
            final String msg = SimpleBuilder.concat("Multiple servers with the id '", server.id, "'");
            throw new ParsingException(elem, msg);
        }

        this.servers.put(server.id, server);
    }

    /**
     * Processes a "login" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processLoginNode(final EmptyElement elem) throws ParsingException {

        final LoginConfig login = new LoginConfig(elem, this.servers);

        if (this.logins.containsKey(login.id)) {
            final String msg = SimpleBuilder.concat("Multiple logins with the id '", login.id, "'");
            throw new ParsingException(elem, msg);
        }

        this.logins.put(login.id, login);
    }

    /**
     * Processes a "data-profile" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processDataProfileNode(final NonemptyElement elem) throws ParsingException {

        final DataProfile dataProfile = new DataProfile(this.logins, elem);

        if (this.dataProfiles.containsKey(dataProfile.id)) {
            final String msg = SimpleBuilder.concat("Multiple data profiles with the id '",
                    dataProfile.id, "'");
            throw new ParsingException(elem, msg);
        }

        this.dataProfiles.put(dataProfile.id, dataProfile);
    }

    /**
     * Processes a "web" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processWebNode(final NonemptyElement elem) throws ParsingException {

        final String host = elem.getRequiredStringAttr(HOST_ATTR);
        if (this.webContexts.containsKey(host)) {
            final String msg = Res.fmt(Res.DB_CFG_DUP_HOST, host);
            throw new ParsingException(elem, msg);
        }

        final int count = elem.getNumChildren();

        final Map<String, DataProfile> pathProfiles = new HashMap<>(10);

        for (int i = 0; i < count; ++i) {
            final INode child = elem.getChild(i);
            if (child instanceof final EmptyElement innerElem) {
                processWebChildNode(innerElem, pathProfiles);
            } else {
                Log.warning("Unexpected child element of 'web'.");
            }
        }

        this.webContexts.put(host, new WebContext(host, pathProfiles));
    }

    /**
     * Processes a child element of a "web" element.
     *
     * @param elem         the child element
     * @param pathProfiles the map from path to profile
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processWebChildNode(final EmptyElement elem,
                                     final Map<? super String, ? super DataProfile> pathProfiles)
            throws ParsingException {

        final String tag = elem.getTagName();
        if (SITE_TAG.equals(tag)) {
            final String path = elem.getRequiredStringAttr(PATH_ATTR);
            final String profile = elem.getRequiredStringAttr(PROFILE_ATTR);

            final DataProfile cfg = this.dataProfiles.get(profile);
            if (cfg == null) {
                final String msg = Res.fmt(Res.DB_CFG_BAD_SITE_PROFILE, profile);
                throw new ParsingException(elem, msg);
            }

            if (pathProfiles.containsKey(path)) {
                final String msg = Res.fmt(Res.DB_CFG_DUP_PATH, path);
                throw new ParsingException(elem, msg);
            }

            pathProfiles.put(path, cfg);
        } else {
            final String msg = Res.fmt(Res.DB_CFG_BAD_SITE_TAG, tag);
            throw new ParsingException(elem, msg);
        }
    }

    /**
     * Processes a "code" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processCodeNode(final EmptyElement elem) throws ParsingException {

        final String profile = elem.getRequiredStringAttr(PROFILE_ATTR);

        final DataProfile cfg = this.dataProfiles.get(profile);
        if (cfg == null) {
            final String msg = Res.fmt(Res.DB_CFG_BAD_CODE_PROFILE, profile);
            throw new ParsingException(elem, msg);
        }

        final String context = elem.getRequiredStringAttr(CONTEXT_ATTR);

        if (this.codeContexts.containsKey(context)) {
            final String msg = Res.fmt(Res.DB_CFG_DUP_CODE, context);
            throw new ParsingException(elem, msg);
        }

        this.codeContexts.put(context, cfg);
    }

    /**
     * Gets the default instance which reads data from a "db" subdirectory under the base directory configured in
     * {@code PathList}.
     *
     * @return the instance
     */
    public static DatabaseConfig getDefaultInstance() {

        final PathList pathList = PathList.getInstance();
        final File dbDir = new File(pathList.baseDir, "db");
        final String absPath = dbDir.getAbsolutePath();

        final String loadingMsg = Res.fmt(Res.DB_CFG_LOADING, absPath);
        Log.info(loadingMsg);

        DatabaseConfig theMap = null;

        if (dbDir.exists() && dbDir.isDirectory()) {
            try {
                theMap = load(dbDir);
            } catch (final ParsingException ex) {
                final String msg = Res.get(Res.DB_CFG_CANT_LOAD);
                Log.warning(msg, ex);
                theMap = new DatabaseConfig();
            }
        } else {
            final String msg = Res.fmt(Res.DB_CFG_DIR_NONEXIST, absPath);
            Log.warning(msg);
            theMap = new DatabaseConfig();
        }

        return theMap;
    }

    /**
     * Loads an XML representation of a {@code DatabaseConfig}, which includes the set of {@code DriverConfig} objects
     * as well as their mapping.
     *
     * @param dir the directory in which to locate the source file to load (which is in XML format, as generated by
     *            {@code toXml})
     * @return the loaded {@code DatabaseConfig}
     * @throws ParsingException if there is an error loading or parsing the XML
     */
    public static DatabaseConfig load(final File dir) throws ParsingException {

        final File xmlFile = new File(dir, FILENAME);

        if (!xmlFile.exists()) {
            final String xmlPath = xmlFile.getAbsolutePath();
            final String msg = Res.fmt(Res.DB_CFG_FILE_NONEXIST, xmlPath);
            throw new ParsingException(-1, -1, msg);
        }

        final String xml = DbFileLoader.loadFileAsString(xmlFile, true);

        if (xml == null) {
            final String xmlPath = xmlFile.getAbsolutePath();
            final String msg = Res.fmt(Res.DB_CFG_CANT_OPEN_SRC, xmlPath);
            throw new ParsingException(-1, -1, msg);
        }

        final XmlContent content = new XmlContent(xml, true, false);
        final List<INode> nodes = content.getNodes();

        if (nodes != null && nodes.size() == 1) {
            final INode firstNode = nodes.getFirst();
            if (firstNode instanceof final NonemptyElement elem) {
                final String tagName = elem.getTagName();
                if (XML_TAG.equals(tagName)) {
                    return new DatabaseConfig(elem);
                }

                final String msg = Res.get(Res.DB_CFG_NO_TOPLEVEL);
                throw new ParsingException(0, 0, msg);
            }
        }

        final String msg = Res.get(Res.DB_CFG_BAD_TOPLEVEL);
        throw new ParsingException(-1, -1, msg);
    }

    /**
     * Gets the list of servers in the context map.
     *
     * @return a copy of the list of servers
     */
    public ServerConfig[] getServers() {

        return this.servers.values().toArray(EMPTY_SERVER_CFG_ARRAY);
    }

    /**
     * Gets the server configuration with a particular ID from the server map.
     *
     * @param id the ID of the server configuration to retrieve
     * @return the server configuration, or {@code null} if there was no server configuration with the given ID
     */
    public ServerConfig getServer(final String id) {

        return this.servers.get(id);
    }

    /**
     * Gets the list of login configurations in the context map.
     *
     * @return the array of login configuration names
     */
    public LoginConfig[] getLogins() {

        return this.logins.values().toArray(EMPTY_LOGIN_CFG_ARRAY);
    }

    /**
     * Gets the login configuration with a particular ID from the login map.
     *
     * @param id the ID of the login configuration to retrieve
     * @return the login configuration, or {@code null} if there was no login configuration with the given ID
     */
    public LoginConfig getLogin(final String id) {

        return this.logins.get(id);
    }

    /**
     * Gets the list of data profiles in the context map.
     *
     * @return a copy of the list of data profiles
     */
    public DataProfile[] getDataProfiles() {

        return this.dataProfiles.values().toArray(EMPTY_DATA_PROFILE_ARRAY);
    }

    /**
     * Gets the list of data profile IDs in the context map.
     *
     * @return the array of data profile IDs
     */
    public String[] getDataProfileIDs() {

        return this.dataProfiles.keySet().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the data profile with a particular ID from the context map.
     *
     * @param id the ID of the data profile to retrieve
     * @return the data profile, or {@code null} if there was no data profile with the given ID
     */
    public DataProfile getDataProfile(final String id) {

        return this.dataProfiles.get(id);
    }

    /**
     * Gets the list of web contexts in the context map.
     *
     * @return a copy of the list of web contexts
     */
    public WebContext[] getWebContexts() {

        return this.webContexts.values().toArray(EMPTY_WEB_CONTEXT_ARRAY);
    }

    /**
     * Gets the list of host names for the web contexts present.
     *
     * @return the array of host names
     */
    public String[] getWebHosts() {

        return this.webContexts.keySet().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the list of paths with web contexts under a particular host name.
     *
     * @param hostname the host name
     * @return the list of site paths (null if the host name is not found)
     */
    public List<String> getWebPaths(final String hostname) {

        final WebContext webContext = this.webContexts.get(hostname);
        return webContext == null ? null : webContext.getPaths();
    }

    /**
     * Gets the profile configuration for the web context for a specified host name and path.
     *
     * @param hostname the hostname
     * @param path     the path
     * @return the profile configuration (null if the hostname is not found)
     */
    public DataProfile getWebPathDataProfile(final String hostname, final String path) {

        final WebContext webContext = this.webContexts.get(hostname);
        return webContext == null ? null : webContext.getProfile(path);
    }

    /**
     * Gets the list of code context names present.
     *
     * @return the array of code context names
     */
    public String[] getCodeContextIds() {

            return this.codeContexts.keySet().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Gets the profile configuration for the code context.
     *
     * @param id the code context ID
     * @return the profile configuration (null if not found)
     */
    public DataProfile getCodeDataProfile(final String id) {

        return this.codeContexts.get(id);
    }
}
