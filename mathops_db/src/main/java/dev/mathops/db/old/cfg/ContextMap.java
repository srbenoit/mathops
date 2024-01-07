package dev.mathops.db.old.cfg;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.PathList;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.core.parser.xml.XmlContent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A map from named web context (host and path) or code context to the database profile that will be used to connect to
 * the data store for that context. This can be used to select different profiles for various named contexts.
 *
 * <p>
 * Typically, each website or logical grouping of code will use a distinct named context. That way, the entire site or
 * application will use a common database (with consistent data), but can be changed as a unit to another database for
 * testing or to provide alternate data.
 *
 * <p>
 * This context map is loaded from an XML file stored in a given directory. This class stores a static map from
 * directory to {@code ContextMap}, allowing the possibility of multiple maps being loaded at the same time. The
 * advantage of this is that this class can be instantiated once, then multiple clients can request maps based on their
 * base directories, but all queries for the same directory will return the same map.
 */
public final class ContextMap {

    /** Name of file where context map data is stored. */
    private static final String FILENAME = "db-config.xml";

    /** The XML tag for the context map. */
    private static final String XML_TAG = "context-map";

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

    /** The singleton instance. */
    private static final Map<File, ContextMap> INSTANCES;

    /** An empty array used when converting strings to arrays. */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** An empty array used when converting strings to arrays. */
    private static final ServerConfig[] EMPTY_SERVER_CFG_ARRAY = new ServerConfig[0];

    /** Object on which to synchronize member access. */
    private final Object synch;

    /** Map from schema ID to the schema configuration object. */
    private final Map<String, SchemaConfig> schemata;

    /** Map from login configuration ID to the configuration object. */
    private final Map<String, LoginConfig> logins;

    /** List of servers. */
    private final List<ServerConfig> servers;

    /** Map from profile configuration ID to the configuration object. */
    private final Map<String, DbProfile> profiles;

    /** Map from hostname to a map from path to website profile for web contexts. */
    private final Map<String, Map<String, WebSiteProfile>> webContexts;

    /** Map from context ID to profile for code contexts. */
    private final Map<String, DbProfile> codeContexts;

    static {
        INSTANCES = new TreeMap<>();
    }

    /**
     * A private constructor that creates an empty {@code ContextMap} in the event the context map could not be loaded.
     */
    private ContextMap() {

        super();

        this.synch = new Object();
        this.servers = new ArrayList<>(10);
        this.logins = new HashMap<>(10);
        this.profiles = new HashMap<>(10);
        this.schemata = new HashMap<>(10);
        this.webContexts = new HashMap<>(10);
        this.codeContexts = new HashMap<>(10);
    }

    /**
     * A private constructor to ensure {@code getInstance} is used and a single instance exists for a given
     * configuration directory.
     *
     * @param elem the XML element from which to extract the {@code ContextMap}
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private ContextMap(final NonemptyElement elem) throws ParsingException {

        this();

        final int numChildren = elem.getNumChildren();

        for (int i = 0; i < numChildren; ++i) {
            final INode child = elem.getChild(i);

            if (child instanceof EmptyElement) {
                final EmptyElement innerNode = (EmptyElement) child;

                if (SchemaConfig.ELEM_TAG.equals(innerNode.getTagName())) {
                    final SchemaConfig schema = new SchemaConfig(innerNode);
                    this.schemata.put(schema.id, schema);
                } else if (CODE_TAG.equals(innerNode.getTagName())) {
                    processCodeNode(innerNode);
                } else {
                    Log.warning("Unexpected tag: " + innerNode.getTagName());
                }
            } else if (child instanceof NonemptyElement) {
                final NonemptyElement innerNode = (NonemptyElement) child;

                if (ServerConfig.ELEM_TAG.equals(innerNode.getTagName())) {
                    final ServerConfig server = new ServerConfig(this.schemata, this.logins, innerNode);
                    this.servers.add(server);
                } else if (DbProfile.ELEM_TAG.equals(innerNode.getTagName())) {
                    final DbProfile profile = new DbProfile(this.schemata, this.logins, innerNode);
                    this.profiles.put(profile.id, profile);
                } else if (WEB_TAG.equals(innerNode.getTagName())) {
                    processWebNode(innerNode);
                } else {
                    Log.warning("Unexpected tag: " + innerNode.getTagName());
                }
            }
        }
    }

    /**
     * Processes a "web" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processWebNode(final NonemptyElement elem) throws ParsingException {

        // Called only from the constructor, so no synch needed

        final String host = elem.getRequiredStringAttr(HOST_ATTR);
        if (this.webContexts.containsKey(host)) {
            throw new ParsingException(elem.getStart(), elem.getEnd(), Res.fmt(Res.CTX_MAP_DUP_HOST, host));
        }

        final int count = elem.getNumChildren();

        final Map<String, WebSiteProfile> map = new HashMap<>(count);
        this.webContexts.put(host, map);

        for (int i = 0; i < count; ++i) {
            final INode child = elem.getChild(i);
            if (child instanceof EmptyElement) {
                processWebChildNode(host, (EmptyElement) child, map);
            } else {
                Log.warning("Unexpected child element of 'server'.");
            }
        }
    }

    /**
     * Processes a child element of a "web" element.
     *
     * @param host the host name
     * @param elem the child element
     * @param map  the map from path to profile
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processWebChildNode(final String host, final EmptyElement elem,
                                     final Map<? super String, ? super WebSiteProfile> map) throws ParsingException {

        // Called only from the constructor, so no synch needed

        if (SITE_TAG.equals(elem.getTagName())) {

            final String profile = elem.getRequiredStringAttr(PROFILE_ATTR);

            final DbProfile cfg = this.profiles.get(profile);
            if (cfg == null) {
                throw new ParsingException(elem.getStart(), elem.getEnd(),
                        Res.fmt(Res.CTX_MAP_BAD_SITE_PROFILE, profile));
            }

            final String path = elem.getRequiredStringAttr(PATH_ATTR);

            if (map.containsKey(path)) {
                throw new ParsingException(elem.getStart(), elem.getEnd(), Res.fmt(Res.CTX_MAP_DUP_PATH, path));
            }

            map.put(path, new WebSiteProfile(host, path, cfg));
        } else {
            throw new ParsingException(elem.getStart(), elem.getEnd(),
                    Res.fmt(Res.CTX_MAP_BAD_SITE_TAG, elem.getTagName()));
        }
    }

    /**
     * Processes a "code" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processCodeNode(final EmptyElement elem) throws ParsingException {

        // Called only from the constructor, so no synch needed

        final String profile = elem.getRequiredStringAttr(PROFILE_ATTR);

        final DbProfile cfg = this.profiles.get(profile);
        if (cfg == null) {
            throw new ParsingException(elem.getStart(), elem.getEnd(), Res.fmt(Res.CTX_MAP_BAD_CODE_PROFILE, profile));
        }

        final String context = elem.getRequiredStringAttr(CONTEXT_ATTR);

        if (this.codeContexts.containsKey(context)) {
            throw new ParsingException(elem.getStart(), elem.getEnd(), Res.fmt(Res.CTX_MAP_DUP_CODE, context));
        }

        this.codeContexts.put(context, cfg);
    }

    /**
     * Gets the default instance which reads data from a "db" subdirectory under the base directory configured in
     * {@code PathList}.
     *
     * @return the instance
     */
    public static ContextMap getDefaultInstance() {

        final File dbDir = new File(PathList.getInstance().baseDir, "db");

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            ContextMap theMap = INSTANCES.get(dbDir);

            if (theMap == null) {
                Log.info(Res.fmt(Res.CTX_MAP_LOADING, dbDir.getAbsolutePath()));

                if (dbDir.exists() && dbDir.isDirectory()) {
                    try {
                        theMap = load(dbDir);
                    } catch (final ParsingException ex) {
                        Log.warning(Res.get(Res.CTX_MAP_CANT_LOAD), ex);
                        theMap = new ContextMap();
                    }
                } else {
                    Log.warning(Res.fmt(Res.CTX_MAP_DIR_NONEXIST, dbDir.getAbsolutePath()));
                    theMap = new ContextMap();
                }

                INSTANCES.put(dbDir, theMap);
            }

            return theMap;
        }
    }

    /**
     * Loads an XML representation of a {@code ContextMap}, which includes the set of {@code DriverConfig} objects as
     * well as their mapping.
     *
     * @param dir the directory in which to locate the source file to load (which is in XML format, as generated by
     *            {@code toXml})
     * @return the loaded {@code ContextMap}
     * @throws ParsingException if there is an error loading or parsing the XML
     */
    public static ContextMap load(final File dir) throws ParsingException {

        final File xmlFile = new File(dir, FILENAME);

        if (!xmlFile.exists()) {
            throw new ParsingException(-1, -1, Res.fmt(Res.CTX_MAP_FILE_NONEXIST, xmlFile.getAbsolutePath()));
        }

        final String xml = FileLoader.loadFileAsString(xmlFile, true);

        if (xml == null) {
            throw new ParsingException(-1, -1, Res.fmt(Res.CTX_MAP_CANT_OPEN_SRC, xmlFile.getAbsolutePath()));
        }

        final XmlContent content = new XmlContent(xml, true, false);
        final List<INode> nodes = content.getNodes();

        if (nodes != null) {
            if (nodes.size() == 1 && nodes.get(0) instanceof final NonemptyElement elem) {
                if (XML_TAG.equals(elem.getTagName())) {
                    return new ContextMap(elem);
                }

                throw new ParsingException(0, 0, Res.get(Res.CTX_MAP_NO_TOPLEVEL));
            }
        }

        throw new ParsingException(-1, -1, Res.get(Res.CTX_MAP_BAD_TOPLEVEL));
    }

//    /**
//     * Gets the list of schema IDs in the context map.
//     *
//     * @return the array of schema IDs
//     */
//    public String[] getSchemaIDs() {
//
//        synchronized (this.synch) {
//            return this.schemata.keySet().toArray(EMPTY_STRING_ARRAY);
//        }
//    }

//    /**
//     * Gets the schema with a particular ID from the context map.
//     *
//     * @param id the ID of the schema to retrieve
//     * @return the schema, or {@code null} if there was no schema with the given ID
//     */
//    private SchemaConfig getSchema(final String id) {
//
//        synchronized (this.synch) {
//            return this.schemata.get(id);
//        }
//    }

    /**
     * Gets the list of login configuration IDS in the context map.
     *
     * @return the array of driver configuration names
     */
    public String[] getLoginIDs() {

        synchronized (this.synch) {
            return this.logins.keySet().toArray(EMPTY_STRING_ARRAY);
        }
    }

    /**
     * Gets the login configuration with a particular ID from the login map.
     *
     * @param id the ID of the login configuration to retrieve
     * @return the login configuration, or {@code null} if there was no login configuration with the given ID
     */
    public LoginConfig getLogin(final String id) {

        synchronized (this.synch) {
            return this.logins.get(id);
        }
    }

    /**
     * Gets the list of servers in the context map.
     *
     * @return a copy of the list of servers
     */
    public ServerConfig[] getServers() {

        synchronized (this.synch) {
            return this.servers.toArray(EMPTY_SERVER_CFG_ARRAY);
        }
    }

//    /**
//     * Gets the list of profile IDs in the context map.
//     *
//     * @return the array of profile IDs
//     */
//    public String[] getProfileIDs() {
//
//        synchronized (this.synch) {
//            return this.profiles.keySet().toArray(EMPTY_STRING_ARRAY);
//        }
//    }

//    /**
//     * Gets the profile with a particular ID from the context map.
//     *
//     * @param id the ID of the profile to retrieve
//     * @return the profile, or {@code null} if there was no profile with the given ID
//     */
//    private DbProfile getProfile(final String id) {
//
//        synchronized (this.synch) {
//            return this.profiles.get(id);
//        }
//    }

    /**
     * Gets the list of host names for the web contexts present.
     *
     * @return the array of host names
     */
    public String[] getWebHosts() {

        synchronized (this.synch) {
            return this.webContexts.keySet().toArray(EMPTY_STRING_ARRAY);
        }
    }

    /**
     * Gets the list of sites with web contexts under a particular host name.
     *
     * @param hostname the host name
     * @return the array of site paths (null if the host name is not found)
     */
    public String[] getWebSites(final String hostname) {

        synchronized (this.synch) {
            final Map<String, WebSiteProfile> map = this.webContexts.get(hostname);
            return map == null ? null : map.keySet().toArray(EMPTY_STRING_ARRAY);
        }
    }

    /**
     * Gets the profile configuration for the web context for a specified host name and path.
     *
     * @param hostname the hostname
     * @param path     the path
     * @return the profile configuration (null if the hostname is not found)
     */
    public WebSiteProfile getWebSiteProfile(final String hostname, final String path) {

        synchronized (this.synch) {
            final Map<String, WebSiteProfile> map = this.webContexts.get(hostname);
            return map == null ? null : map.get(path);
        }
    }

//    /**
//     * Gets the list of code context IDs.
//     *
//     * @return the array of context IDs
//     */
//    public String[] getCodeContexts() {
//
//        synchronized (this.synch) {
//            return this.codeContexts.keySet().toArray(EMPTY_STRING_ARRAY);
//        }
//    }

    /**
     * Gets the profile configuration for the code context.
     *
     * @param id the code context ID
     * @return the profile configuration (null if not found)
     */
    public DbProfile getCodeProfile(final String id) {

        synchronized (this.synch) {
            return this.codeContexts.get(id);
        }
    }

//    /**
//     * Prints the default context map.
//     *
//     * @param args command-line arguments
//     */
//    public static void main(final String... args) {
//
//        final HtmlBuilder builder = new HtmlBuilder(1000);
//
//        final ContextMap map = ContextMap.getDefaultInstance();
//
//        builder.addln("SCHEMATA:");
//        for (final String id : map.getSchemaIDs()) {
//            builder.addln("  ", map.getSchema(id).toString());
//        }
//        builder.addln();
//
//        builder.addln("SERVERS:");
//        for (final ServerConfig server : map.getServers()) {
//            builder.addln("  ", server.toString());
//            builder.addln("    DATABASES:");
//            for (final DbConfig db : server.getDatabases()) {
//                builder.addln("      ", db.toString());
//                builder.addln("        LOGINS:");
//                for (final LoginConfig login : db.getLogins()) {
//                    builder.addln("          ", login.toString());
//                }
//            }
//        }
//        builder.addln();
//
//        builder.addln("PROFILES:");
//        for (final String id : map.getProfileIDs()) {
//            builder.addln("  ", map.getProfile(id).toString());
//        }
//        builder.addln();
//
//        builder.addln("WEB HOSTS:");
//        for (final String host : map.getWebHosts()) {
//            builder.addln("  ", host);
//            builder.addln("    PATHS:");
//            final String[] paths = map.getWebSites(host);
//            if (paths != null) {
//                for (final String path : paths) {
//                    final WebSiteProfile profile = map.getWebSiteProfile(host, path);
//                    if (profile != null) {
//                        builder.addln("      ", path, " using profile ", profile.dbProfile.id);
//                    }
//                }
//            }
//        }
//        builder.addln();
//
//        builder.addln("CODE CONTEXTS:");
//        for (final String id : map.getCodeContexts()) {
//            builder.addln("  ", id, " using context ", map.getCodeProfile(id).id);
//        }
//        builder.addln();
//
//        Log.fine(builder.toString());
//    }
}
