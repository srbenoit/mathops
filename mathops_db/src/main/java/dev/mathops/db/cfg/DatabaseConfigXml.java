package dev.mathops.db.cfg;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.PathList;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A codec that can read (or write) the XML form of the database configuration file.
 */
public enum DatabaseConfigXml {
    ;

    /** Name of file where context map data is stored. */
    public static final String FILENAME = "database-config.xml";

    /** The top-level XML tag for the database configuration. */
    private static final String DATABASE_CONFIG_TAG = "database-config";

    /** The XML tag for a server. */
    private static final String SERVER_TAG = "server";

    /** The XML tag for a database. */
    private static final String DATABASE_TAG = "database";

    /** The XML tag for a login. */
    private static final String LOGIN_TAG = "login";

    /** The XML tag for a database schema. */
    private static final String DATA_TAG = "data";

    /** The XML tag for a database. */
    private static final String PROFILE_TAG = "profile";

    /** The XML tag for a database. */
    private static final String FACET_TAG = "facet";

    /** The XML tag for a web context. */
    private static final String WEB_CONTEXT_TAG = "web-context";

    /** The XML tag for a site. */
    private static final String SITE_TAG = "site";

    /** The XML tag for a code context. */
    private static final String CODE_CONTEXT_TAG = "code-context";

    /** An XML attribute. */
    private static final String ID_ATTR = "id";

    /** An XML attribute. */
    private static final String TYPE_ATTR = "type";

    /** An XML attribute. */
    private static final String HOST_ATTR = "host";

    /** An XML attribute. */
    private static final String PORT_ATTR = "port";

    /** An XML attribute. */
    private static final String INSTANCE_ATTR = "instance";

    /** An XML attribute. */
    private static final String DBA_ATTR = "dba";

    /** An XML attribute. */
    private static final String USER_ATTR = "user";

    /** An XML attribute. */
    private static final String PASSWORD_ATTR = "password";

    /** An XML attribute. */
    private static final String SCHEMA_ATTR = "schema";

    /** An XML attribute. */
    private static final String USE_ATTR = "use";

    /** An XML attribute. */
    private static final String PREFIX_ATTR = "prefix";

    /** An XML attribute. */
    private static final String DATA_ATTR = "data";

    /** An XML attribute. */
    private static final String LOGIN_ATTR = "login";

    /** An XML attribute. */
    private static final String PATH_ATTR = "path";

    /** An XML attribute. */
    private static final String PROFILE_ATTR = "profile";

    /**
     * Gets the default file location for the database configuration.
     *
     * @return the file location
     */
    public static File getDefaultFile() {

        final PathList pathList = PathList.getInstance();
        final File baseDir = pathList.getBaseDir();
        final File dbDir = new File(baseDir, "db");
        File result = null;

        if (dbDir.exists() && dbDir.isDirectory()) {
            result = new File(dbDir, FILENAME);
        }

        return result;
    }

    /**
     * Attempts to load a source file, parse it as XML, then extract a database configuration from its structure.
     *
     * @param source the source file
     * @return the successfully parsed {@code DatabaseConfig}
     * @throws IOException      if the file could not be read
     * @throws ParsingException if the XML could not be parsed as a {@code DatabaseConfig}
     */
    public static DatabaseConfig load(final File source) throws IOException, ParsingException {

        final String fileData = FileLoader.loadFileAsString(source, false);

        if (fileData == null) {
            final String sourcePath = source.getAbsolutePath();
            final String msg = Res.fmt(Res.XML_CANT_READ_FILE, sourcePath);
            throw new IOException(msg);
        }

        final XmlContent content = new XmlContent(fileData, true, false);
        final IElement topLevel = content.getTopLevel();
        if (topLevel == null) {
            final String sourcePath = source.getAbsolutePath();
            final String msg = Res.fmt(Res.XML_CANT_FIND_TOP_ELEM, sourcePath);
            throw new IOException(msg);
        }

        if (topLevel instanceof final NonemptyElement nonempty) {
            return parse(nonempty);
        } else {
            final String sourcePath = source.getAbsolutePath();
            final String msg = Res.fmt(Res.XML_TOP_ELEM_IS_EMPTY, sourcePath);
            throw new IOException(msg);
        }
    }

    /**
     * Attempts to extract a database configuration from XML file data.
     *
     * @param elem the XML element from which to extract the database configuration
     * @return the parsed {@code DatabaseConfig}
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static DatabaseConfig parse(final NonemptyElement elem) throws ParsingException {

        final String tagName = elem.getTagName();

        if (DATABASE_CONFIG_TAG.equals(tagName)) {
            final DatabaseConfig config = new DatabaseConfig();

            for (final IElement childElem : elem.getElementChildrenAsList()) {
                final String childTag = childElem.getTagName();

                switch (childTag) {
                    case SERVER_TAG -> parseServer(childElem, config);
                    case PROFILE_TAG -> parseProfile(childElem, config);
                    case WEB_CONTEXT_TAG -> parseWebContext(childElem, config);
                    case CODE_CONTEXT_TAG -> parseCodeContext(childElem, config);
                    case null, default -> {
                        final String msg = Res.fmt(Res.XML_UNEXPECTED_ELEM, childTag, DATABASE_CONFIG_TAG);
                        throw new ParsingException(elem, msg);
                    }
                }
            }

            return config;
        } else {
            final String msg = Res.fmt(Res.XML_TOP_INCORRECT, DATABASE_CONFIG_TAG);
            throw new ParsingException(elem, msg);
        }
    }

    /**
     * Attempts to extract a server from XML file data.
     *
     * @param elem   the XML element from which to extract the server (the tag name of this element is verified)
     * @param config the database configuration to which to add the parsed server
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseServer(final IElement elem, final DatabaseConfig config) throws ParsingException {

        final String typeStr = elem.getRequiredStringAttr(TYPE_ATTR);
        final String hostStr = elem.getRequiredStringAttr(HOST_ATTR);
        final String portStr = elem.getRequiredStringAttr(PORT_ATTR);

        final EDbProduct type = EDbProduct.forName(typeStr);
        if (type == null) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, TYPE_ATTR, typeStr, SERVER_TAG);
            throw new ParsingException(elem, msg);
        }

        try {
            final int port = Integer.parseInt(portStr);

            final Server server = new Server(type, hostStr, port);
            config.addServer(server);

            if (elem instanceof final NonemptyElement nonemptyElement) {
                for (final IElement childElem : nonemptyElement.getElementChildrenAsList()) {
                    final String childTag = childElem.getTagName();

                    switch (childTag) {
                        case DATABASE_TAG -> parseDatabase(config, childElem, server);
                        case null, default -> {
                            final String msg = Res.fmt(Res.XML_UNEXPECTED_ELEM, childTag, SERVER_TAG);
                            throw new ParsingException(elem, msg);
                        }
                    }
                }
            }
        } catch (final NumberFormatException ex) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, PORT_ATTR, portStr, SERVER_TAG);
            throw new ParsingException(elem, msg, ex);
        }
    }

    /**
     * Attempts to extract a database from XML file data.
     *
     * @param config the database configuration
     * @param elem   the XML element from which to extract the database (the tag name of this element is verified)
     * @param server the server to which to add the parsed database
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseDatabase(final DatabaseConfig config, final IElement elem, final Server server)
            throws ParsingException {

        final String idStr = elem.getRequiredStringAttr(ID_ATTR);
        final String instanceStr = elem.getStringAttr(INSTANCE_ATTR);
        final String dbaStr = elem.getStringAttr(DBA_ATTR);

        final Database database = new Database(server, idStr, instanceStr, dbaStr);
        final List<Database> serverDatabases = server.getDatabases();
        serverDatabases.add(database);

        if (elem instanceof final NonemptyElement nonemptyElement) {
            for (final IElement childElem : nonemptyElement.getElementChildrenAsList()) {
                final String childTag = childElem.getTagName();

                switch (childTag) {
                    case LOGIN_TAG -> parseLogin(config, childElem, database);
                    case DATA_TAG -> parseData(config, childElem, database);
                    case null, default -> {
                        final String msg = Res.fmt(Res.XML_UNEXPECTED_ELEM, childTag, DATABASE_TAG);
                        throw new ParsingException(elem, msg);
                    }
                }
            }
        }
    }

    /**
     * Attempts to extract a login from XML file data.
     *
     * @param config   the database configuration
     * @param elem     the XML element from which to extract the login (the tag name of this element is verified)
     * @param database the database to which to add the parsed login
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseLogin(final DatabaseConfig config, final IElement elem, final Database database)
            throws ParsingException {

        final String idStr = elem.getRequiredStringAttr(ID_ATTR);
        final String userStr = elem.getRequiredStringAttr(USER_ATTR);
        final String passwordStr = elem.getStringAttr(PASSWORD_ATTR);

        final Login login = new Login(database, idStr, userStr, passwordStr);
        final List<Login> databaseLogins = database.getLogins();
        databaseLogins.add(login);
        config.addLogin(login);
    }

    /**
     * Attempts to extract a data from XML file data.
     *
     * @param config   the database configuration
     * @param elem     the XML element from which to extract the data (the tag name of this element is verified)
     * @param database the database to which to add the parsed data
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseData(final DatabaseConfig config, final IElement elem, final Database database)
            throws ParsingException {

        final String idStr = elem.getRequiredStringAttr(ID_ATTR);
        final String schemaStr = elem.getRequiredStringAttr(SCHEMA_ATTR);
        final String useStr = elem.getRequiredStringAttr(USE_ATTR);
        final String prefixStr = elem.getStringAttr(PREFIX_ATTR);

        final ESchema schema = ESchema.forName(schemaStr);
        if (schema == null) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, SCHEMA_ATTR, schemaStr, DATA_TAG);
            throw new ParsingException(elem, msg);
        }

        final EDbUse use = EDbUse.forName(useStr);
        if (use == null) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, USE_ATTR, useStr, DATA_TAG);
            throw new ParsingException(elem, msg);
        }

        final Data data = new Data(database, idStr, schema, use, prefixStr);
        final List<Data> databaseData = database.getData();
        databaseData.add(data);
        config.addData(data);
    }

    /**
     * Attempts to extract a profile from XML file data.
     *
     * @param elem   the XML element from which to extract the profile (the tag name of this element is verified)
     * @param config the database configuration to which to add the parsed profile
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseProfile(final IElement elem, final DatabaseConfig config) throws ParsingException {

        final String idStr = elem.getRequiredStringAttr(ID_ATTR);

        final Profile profile = new Profile(idStr);

        if (elem instanceof final NonemptyElement nonemptyElement) {
            for (final IElement childElem : nonemptyElement.getElementChildrenAsList()) {
                final String childTag = childElem.getTagName();

                switch (childTag) {
                    case FACET_TAG -> parseSchema(config, childElem, profile);
                    case null, default -> {
                        final String msg = Res.fmt(Res.XML_UNEXPECTED_ELEM, childTag, PROFILE_TAG);
                        throw new ParsingException(elem, msg);
                    }
                }
            }
        }

        final String error = profile.validate();
        if (error != null) {
            throw new ParsingException(elem, error);
        }

        config.addProfile(profile);
    }

    /**
     * Attempts to extract a schema from XML file data.
     *
     * @param config  the database configuration
     * @param elem    the XML element from which to extract the schema (the tag name of this element is verified)
     * @param profile the profile to which to add the parsed schema
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseSchema(final DatabaseConfig config, final IElement elem, final Profile profile)
            throws ParsingException {

        final String dataStr = elem.getRequiredStringAttr(DATA_ATTR);
        final String loginStr = elem.getRequiredStringAttr(LOGIN_ATTR);

        final Data data = config.getData(dataStr);
        if (data == null) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, DATA_ATTR, dataStr, FACET_TAG);
            throw new ParsingException(elem, msg);
        }

        final Login login = config.getLogin(loginStr);
        if (login == null) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, LOGIN_ATTR, loginStr, FACET_TAG);
            throw new ParsingException(elem, msg);
        }

        final Facet facet = new Facet(data, login);
        profile.addFacet(facet);
    }

    /**
     * Attempts to extract a web context from XML file data.
     *
     * @param elem   the XML element from which to extract the web context (the tag name of this element is verified)
     * @param config the database configuration to which to add the parsed web context
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseWebContext(final IElement elem, final DatabaseConfig config) throws ParsingException {

        final String hostStr = elem.getRequiredStringAttr(HOST_ATTR);

        final WebContext webContext = new WebContext(hostStr);

        if (elem instanceof final NonemptyElement nonemptyElement) {
            for (final IElement childElem : nonemptyElement.getElementChildrenAsList()) {
                final String childTag = childElem.getTagName();

                switch (childTag) {
                    case SITE_TAG -> parseSite(config, childElem, webContext);
                    case null, default -> {
                        final String msg = Res.fmt(Res.XML_UNEXPECTED_ELEM, childTag, WEB_CONTEXT_TAG);
                        throw new ParsingException(elem, msg);
                    }
                }
            }
        }

        config.addWebContext(webContext);
    }

    /**
     * Attempts to extract a site from XML file data.
     *
     * @param config     the database configuration
     * @param elem       the XML element from which to extract the site (the tag name of this element is verified)
     * @param webContext the web context to which to add the parsed site
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseSite(final DatabaseConfig config, final IElement elem, final WebContext webContext)
            throws ParsingException {

        final String pathStr = elem.getRequiredStringAttr(PATH_ATTR);
        final String profileStr = elem.getRequiredStringAttr(PROFILE_ATTR);

        final Profile profile = config.getProfile(profileStr);
        if (profile == null) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, PROFILE_ATTR, profileStr, SITE_TAG);
            throw new ParsingException(elem, msg);
        }

        final Site site = new Site(webContext, pathStr, profile);
        webContext.addSite(site);
    }

    /**
     * Attempts to extract a code context from XML file data.
     *
     * @param elem   the XML element from which to extract the code context (the tag name of this element is verified)
     * @param config the database configuration to which to add the parsed code context
     * @throws ParsingException if the data could not be parsed from the XML
     */
    private static void parseCodeContext(final IElement elem, final DatabaseConfig config) throws ParsingException {

        final String idStr = elem.getRequiredStringAttr(ID_ATTR);
        final String profileStr = elem.getRequiredStringAttr(PROFILE_ATTR);

        final Profile profile = config.getProfile(profileStr);
        if (profile == null) {
            final String msg = Res.fmt(Res.XML_BAD_ATTR, PROFILE_ATTR, profileStr, CODE_CONTEXT_TAG);
            throw new ParsingException(elem, msg);
        }

        final CodeContext codeContext = new CodeContext(idStr, profile);
        config.addCodeContext(codeContext);
    }
}
