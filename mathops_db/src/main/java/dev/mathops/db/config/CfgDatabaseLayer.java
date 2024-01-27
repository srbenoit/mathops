package dev.mathops.db.config;

import dev.mathops.commons.PathList;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable representation of the configuration of the database layer.  This object provides a map from named web
 * context (host and path) or named code context to the data profile that will be used to connect to the database for
 * that context. This can be used to select different profiles for various named contexts.
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
public final class CfgDatabaseLayer {

    /** Name of file where context map data is stored. */
    private static final String FILENAME = "cfg_database_layer.xml";

    /** The XML tag for the context map. */
    private static final String XML_TAG = "database-layer";

    /** An empty array used when converting collections of server configurations to arrays. */
    private static final CfgInstance[] EMPTY_SERVER_CFG_ARRAY = new CfgInstance[0];

    /** An empty array used when converting collections of login configurations to arrays. */
    private static final CfgDatabase[] EMPTY_DATABASE_CFG_ARRAY = new CfgDatabase[0];

    /** An empty array used when converting collections of login configurations to arrays. */
    private static final CfgLogin[] EMPTY_LOGIN_CFG_ARRAY = new CfgLogin[0];

    /** An empty array used when converting collections of data profiles to arrays. */
    private static final CfgDataProfile[] EMPTY_DATA_PROFILE_ARRAY = new CfgDataProfile[0];

    /** An empty array used when converting collections of web contexts to arrays. */
    private static final CfgWebContext[] EMPTY_WEB_CONTEXT_ARRAY = new CfgWebContext[0];

    /** An empty array used when converting collections of web contexts to arrays. */
    private static final CfgCodeContext[] EMPTY_CODE_CONTEXT_ARRAY = new CfgCodeContext[0];

    //

    /** Map from instance ID to instance configuration. */
    private final Map<String, CfgInstance> instances;

    /** Map from database ID to database configuration. */
    private final Map<String, CfgDatabase> databases;

    /** Map from login ID to the login configuration. */
    private final Map<String, CfgLogin> logins;

    /** Map from data profile ID to data profile configuration. */
    private final Map<String, CfgDataProfile> dataProfiles;

    /** Map from hostname to web context configuration. */
    private final Map<String, CfgWebContext> webContexts;

    /** Map from code contextID to code context configuration. */
    private final Map<String, CfgCodeContext> codeContexts;

    /**
     * A private constructor that creates an empty {@code CfgDatabaseLayer} in the event the context map could not be
     * loaded.
     *
     * @param theInstances    the collection of instances
     * @param theDataProfiles the collection of data profiles
     * @param theWebContexts  the collection of web contexts
     * @param theCodeContexts the collection of code contexts
     */
    public CfgDatabaseLayer(final Collection<CfgInstance> theInstances,
                             final Collection<CfgDataProfile> theDataProfiles,
                             final Collection<CfgWebContext> theWebContexts,
                             final Collection<CfgCodeContext> theCodeContexts) {

        if (theInstances == null || theInstances.isEmpty()) {
            this.instances = new LinkedHashMap<>(0);
            this.databases = new HashMap<>(0);
            this.logins = new HashMap<>(0);
        } else {
            final int numInstances = theInstances.size();
            this.instances = new LinkedHashMap<>(numInstances);
            this.databases = new HashMap<>(numInstances * 5);
            this.logins = new HashMap<>(numInstances * 3);

            for (final CfgInstance instance : theInstances) {
                if (instance != null) {
                    if (this.instances.containsKey(instance.id)) {
                        throw new IllegalArgumentException("Instance ID '" + instance.id + "' is duplicated.");
                    }
                    this.instances.put(instance.id, instance);

                    for (final CfgDatabase database : instance.getDatabases()) {
                        if (database != null) {
                            if (this.databases.containsKey(database.id)) {
                                throw new IllegalArgumentException("Database ID '" + database.id + "' is duplicated.");
                            }
                            this.databases.put(database.id, database);
                        }
                    }

                    for (final CfgLogin login : instance.getLogins()) {
                        if (login != null) {
                            if (this.logins.containsKey(login.id)) {
                                throw new IllegalArgumentException("Login ID '" + login.id + "' is duplicated.");
                            }
                            this.logins.put(login.id, login);
                        }
                    }
                }
            }
        }

        if (theDataProfiles == null || theDataProfiles.isEmpty()) {
            this.dataProfiles = new LinkedHashMap<>(0);
        } else {
            final int numDataProfiles = theDataProfiles.size();
            this.dataProfiles = new LinkedHashMap<>(numDataProfiles);

            for (final CfgDataProfile dataProfile : theDataProfiles) {
                if (dataProfile != null) {
                    if (this.dataProfiles.containsKey(dataProfile.id)) {
                        throw new IllegalArgumentException("Data profile ID '" + dataProfile.id + "' is duplicated.");
                    }
                    this.dataProfiles.put(dataProfile.id, dataProfile);
                }
            }
        }

        if (theWebContexts == null || theWebContexts.isEmpty()) {
            this.webContexts = new LinkedHashMap<>(0);
        } else {
            final int numWebContexts = theWebContexts.size();
            this.webContexts = new LinkedHashMap<>(numWebContexts);

            for (final CfgWebContext webContext : theWebContexts) {
                if (webContext != null) {
                    if (this.webContexts.containsKey(webContext.host)) {
                        throw new IllegalArgumentException("Web context host '" + webContext.host + "' is duplicated.");
                    }
                    this.webContexts.put(webContext.host, webContext);
                }
            }
        }

        if (theCodeContexts == null || theCodeContexts.isEmpty()) {
            this.codeContexts = new LinkedHashMap<>(0);
        } else {
            final int numCodeContexts = theCodeContexts.size();
            this.codeContexts = new LinkedHashMap<>(numCodeContexts);

            for (final CfgCodeContext codeContext : theCodeContexts) {
                if (codeContext != null) {
                    if (this.codeContexts.containsKey(codeContext.id)) {
                        throw new IllegalArgumentException("Code context ID '" + codeContext.id + "' is duplicated.");
                    }
                    this.codeContexts.put(codeContext.id, codeContext);
                }
            }
        }
    }

    /**
     * A private constructor to ensure {@code getInstance} is used and a single instance exists.
     *
     * @param elem the XML element from which to extract the {@code DatabaseConfig}
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private CfgDatabaseLayer(final NonemptyElement elem) throws ParsingException {

        this.instances = new LinkedHashMap<>(10);
        this.databases = new LinkedHashMap<>(10);
        this.logins = new LinkedHashMap<>(10);
        this.dataProfiles = new LinkedHashMap<>(10);
        this.webContexts = new LinkedHashMap<>(10);
        this.codeContexts = new LinkedHashMap<>(10);

        for (final IElement child : elem.getElementChildrenAsList()) {

            if (child instanceof final NonemptyElement innerElem) {
                final String innerTag = innerElem.getTagName();
                switch (innerTag) {
                    case CfgInstance.ELEM_TAG -> processInstance(innerElem);
                    case CfgDataProfile.ELEM_TAG -> processDataProfile(innerElem);
                    case CfgWebContext.ELEM_TAG -> processWebContext(innerElem);
                    case null, default -> {
                        final String msg = Res.fmt(Res.DB_CFG_UNEXPEC_CHILD, innerTag);
                        Log.warning(msg);
                    }
                }
            } else if (child instanceof final EmptyElement innerElem) {
                final String innerTag = innerElem.getTagName();
                switch (innerTag) {
                    case CfgCodeContext.ELEM_TAG -> processCodeContext(innerElem);
                    case null, default -> {
                        final String msg = Res.fmt(Res.DB_CFG_UNEXPEC_CHILD, innerTag);
                        Log.warning(msg);
                    }
                }
            }
        }
    }

    /**
     * Processes an "instance" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processInstance(final NonemptyElement elem) throws ParsingException {

        final CfgInstance instance = new CfgInstance(elem, this.databases, this.logins);

        if (this.instances.containsKey(instance.id)) {
            final String msg = SimpleBuilder.concat("Multiple instances with the id '", instance.id, "'");
            throw new ParsingException(elem, msg);
        }

        this.instances.put(instance.id, instance);
    }

    /**
     * Processes a "data-profile" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processDataProfile(final NonemptyElement elem) throws ParsingException {

        final CfgDataProfile dataProfile = new CfgDataProfile(this.databases, this.logins, elem);

        if (this.dataProfiles.containsKey(dataProfile.id)) {
            final String msg = SimpleBuilder.concat("Multiple data profiles with the id '", dataProfile.id, "'");
            throw new ParsingException(elem, msg);
        }

        this.dataProfiles.put(dataProfile.id, dataProfile);
    }

    /**
     * Processes a "web-context" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processWebContext(final NonemptyElement elem) throws ParsingException {

        final CfgWebContext webContext = new CfgWebContext(this.dataProfiles, elem);

        if (this.webContexts.containsKey(webContext.host)) {
            final String msg = SimpleBuilder.concat("Multiple web contexts with host '", webContext.host, "'");
            throw new ParsingException(elem, msg);
        }

        this.webContexts.put(webContext.host, webContext);
    }

    /**
     * Processes a "code" child element.
     *
     * @param elem the child element
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private void processCodeContext(final EmptyElement elem) throws ParsingException {

        final CfgCodeContext codeContext = new CfgCodeContext(this.dataProfiles, elem);

        if (this.codeContexts.containsKey(codeContext.id)) {
            final String msg = SimpleBuilder.concat("Multiple code contexts with ID '", codeContext.id, "'");
            throw new ParsingException(elem, msg);
        }

        this.codeContexts.put(codeContext.id, codeContext);
    }

    /**
     * Gets the default instance which reads data from a "db" subdirectory under the base directory configured in
     * {@code PathList}.
     *
     * @return the instance
     */
    public static CfgDatabaseLayer getDefaultInstance() {

        final PathList pathList = PathList.getInstance();
        final File dbDir = new File(pathList.baseDir, "db");
        final String absPath = dbDir.getAbsolutePath();

        final String loadingMsg = Res.fmt(Res.DB_CFG_LOADING, absPath);
        Log.info(loadingMsg);

        CfgDatabaseLayer theMap = null;

        if (dbDir.exists() && dbDir.isDirectory()) {
            try {
                theMap = load(dbDir);
            } catch (final ParsingException ex) {
                final String msg = Res.get(Res.DB_CFG_CANT_LOAD);
                Log.warning(msg, ex);
                theMap = new CfgDatabaseLayer(null, null, null, null);
            }
        } else {
            final String msg = Res.fmt(Res.DB_CFG_DIR_NONEXIST, absPath);
            Log.warning(msg);
            theMap = new CfgDatabaseLayer(null, null, null, null);
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
    public static CfgDatabaseLayer load(final File dir) throws ParsingException {

        final File xmlFile = new File(dir, FILENAME);

        if (!xmlFile.exists()) {
            final String xmlPath = xmlFile.getAbsolutePath();
            final String msg = Res.fmt(Res.DB_CFG_FILE_NONEXIST, xmlPath);
            throw new ParsingException(-1, -1, msg);
        }

        final String xml = FileLoader.loadFileAsString(xmlFile, true);

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
                    return new CfgDatabaseLayer(elem);
                }

                final String msg = Res.get(Res.DB_CFG_NO_TOPLEVEL);
                throw new ParsingException(0, 0, msg);
            }
        }

        final String msg = Res.get(Res.DB_CFG_BAD_TOPLEVEL);
        throw new ParsingException(-1, -1, msg);
    }

    /**
     * Gets the list of instances in the context map.
     *
     * @return a copy of the list of instances
     */
    public CfgInstance[] getInstances() {

        return this.instances.values().toArray(EMPTY_SERVER_CFG_ARRAY);
    }

    /**
     * Gets the instance configuration with a particular ID from the instance map.
     *
     * @param id the ID of the instance configuration to retrieve
     * @return the instance configuration, or {@code null} if there was no instance configuration with the given ID
     */
    public CfgInstance getInstance(final String id) {

        return this.instances.get(id);
    }

    /**
     * Gets the database configurations in the context map.
     *
     * @return the array of database configurations
     */
    public CfgDatabase[] getDatabases() {

        return this.databases.values().toArray(EMPTY_DATABASE_CFG_ARRAY);
    }

    /**
     * Gets the database configuration with a particular ID from the database map.
     *
     * @param id the ID of the database configuration to retrieve
     * @return the database configuration, or {@code null} if there was no database configuration with the given ID
     */
    public CfgDatabase getDatabase(final String id) {

        return this.databases.get(id);
    }

    /**
     * Gets the login configurations in the context map.
     *
     * @return the array of login configurations
     */
    public CfgLogin[] getLogins() {

        return this.logins.values().toArray(EMPTY_LOGIN_CFG_ARRAY);
    }

    /**
     * Gets the login configuration with a particular ID from the login map.
     *
     * @param id the ID of the login configuration to retrieve
     * @return the login configuration, or {@code null} if there was no login configuration with the given ID
     */
    public CfgLogin getLogin(final String id) {

        return this.logins.get(id);
    }

    /**
     * Gets the data profiles in the data profile map.
     *
     * @return the array of data profile configurations
     */
    public CfgDataProfile[] getDataProfiles() {

        return this.dataProfiles.values().toArray(EMPTY_DATA_PROFILE_ARRAY);
    }

    /**
     * Gets the data profile with a particular ID from the data profile map.
     *
     * @param id the ID of the data profile to retrieve
     * @return the data profile, or {@code null} if there was no data profile with the given ID
     */
    public CfgDataProfile getDataProfile(final String id) {

        return this.dataProfiles.get(id);
    }

    /**
     * Gets of web contexts in the web context map.
     *
     * @return the array of web context configurations
     */
    public CfgWebContext[] getWebContexts() {

        return this.webContexts.values().toArray(EMPTY_WEB_CONTEXT_ARRAY);
    }

    /**
     * Gets the web context with a particular host from the web context map.
     *
     * @param host the host of the web context to retrieve
     * @return the web context, or {@code null} if there was no web context with the given ID
     */
    public CfgWebContext getWebContext(final String host) {

        return this.webContexts.get(host);
    }

    /**
     * Gets of web contexts in the code context map.
     *
     * @return the array of code context configurations
     */
    public CfgCodeContext[] getCodeContexts() {

        return this.codeContexts.values().toArray(EMPTY_CODE_CONTEXT_ARRAY);
    }

    /**
     * Gets the web context with a particular ID from the code context map.
     *
     * @param host the ID of the code context to retrieve
     * @return the code context, or {@code null} if there was no code context with the given ID
     */
    public CfgCodeContext getCodeContext(final String host) {

        return this.codeContexts.get(host);
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgDatabaseLayer{instances=", this.instances, ",databases=", this.databases,
                ",logins=", this.logins, ",dataProfiles=", this.dataProfiles, ",webContexts=", this.webContexts,
                ",codeContexts=", this.codeContexts);
    }

//    public static void main(final String... args) {
//
//        Log.fine(getDefaultInstance());
//    }
}
