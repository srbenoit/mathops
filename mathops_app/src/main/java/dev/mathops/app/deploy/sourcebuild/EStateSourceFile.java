package dev.mathops.app.deploy.sourcebuild;

/**
 * The set of source files needed for a full build from source.
 */
enum EStateSourceFile {

    /** The 'apache-tomcat-*' found. */
    APACHE_TOMCAT("apache-tomcat-{ver}.tar.gz", "Apache Tomcat source archive"),

    /** The 'apr-*' found. */
    APR("apr-{ver}.tar.gz", "Apache APR source archive"),

    /** The 'apr-util-*' found. */
    APR_UTIL("apr-util-{ver}.tar.gz", "Apache APR-Util source archive"),

    /** The 'curl-*' found. */
    CURL("curl-{ver}.tar.gz", "Curl source archive"),

    /** The 'httpd-*' found. */
    HTTPD("httpd-{ver}.tar.gz", "Apache HTTPD source archive"),

    /** The 'log4shib-*' found. */
    LOG4SHIB("log4shib-{ver}.tar.gz", "Log4shib source archive"),

    /** The 'openjdk-*' found. */
    OPENJDK("openjdk-{ver}_linux-x64_bin.tar.gz", "OpenJDK source archive"),

    /** The 'opensaml-*' found. */
    OPENSAML("opensaml-{ver}.tar.gz", "OpenSAML source archive"),

    /** The 'openssl-*' found. */
    OPENSSL("openssl-{ver}.tar.gz", "OpenSSL source archive"),

    /** The 'pcre-*' found. */
    PCRE("pcre-{ver}.tar.gz", "PCRE source archive"),

    /** The 'postgresql-*' found. */
    POSTGRESQL("postgresql-{ver}.tar.gz", "PostgreSQL source archive"),

    /** The 'shibboleth-sp-*' found. */
    SHIBBOLETH_SP("shibboleth-sp-{ver}.tar.gz", "Shibboleth-SP source archive"),

    /** The 'xerces-*' found. */
    XERCES("xerces-c-{ver}.tar.gz", "Xerces source archive"),

    /** The 'xml-security-*' found. */
    XML_SECURITY("xml-security-c-{ver}.tar.gz", "XML Security source archive"),

    /** The 'xmltooling-*' found. */
    XMLTOOLING("xmltooling-{ver}.tar.gz", "XML Tooling source archive"),

    /** The 'ifxjdbc.jar' found. */
    IFXJDBC_JAR("ifxjdbc.jar", "ifxjdbc.jar"),

    /** The 'ifxjdbcx.jar' found. */
    IFXJDBCX_JAR("ifxjdbcx.jar", "ifxjdbcx.jar"),

    /** The 'ifxlang.jar' found. */
    IFXLANG_JAR("ifxlang.jar", "ifxlang.jar"),

    /** The 'ifxlsupp.jar' found. */
    IFXSUPP_JAR("ifxlsupp.jar", "ifxlsupp.jar"),

    /** The 'ifxsqlj.jar' found. */
    IFXSQLJ_JAR("ifxsqlj.jar", "ifxsqlj.jar"),

    /** The 'ifxtools.jar' found. */
    IFXTOOLS_JAR("ifxtools.jar", "ifxtools.jar"),

    /** The 'ojdbc7.jar' found. */
    OJDBC7_JAR("ojdbc7.jar", "ojdbc7.jar"),

    /** The 'postgresql-*.jar' found. */
    POSTGRESQL_JAR("postgresql-{ver}.jar", "postgresql-*.jar"),

    /** The 'attribute-map.xml' found. */
    ATTRIBUTE_MAP_XML("attribute-map.xml", "attribute-map.xml"),

    /** The 'shibboleth2.xml' found. */
    SHIBBOLETH2_XML("shibboleth2.xml", "shibboleth2.xml"),

    /** The 'csufederation-metadata-cert.pem' found. */
    CSUFEDERATION_METADATA_CERT_PEM("csufederation-metadata-cert.pem", "csufederation-metadata-cert.pem"),

    /** The 'csufederationtest-metadata-cert.pem' found. */
    CSUFEDERATIONTEST_METADATA_CERT_PEM("csufederationtest-metadata-cert.pem", "csufederationtest-metadata-cert.pem"),

    /** The 'csufederation-metadata.xml' found. */
    CSUFEDERATION_METADATA_XML("csufederation-metadata.xml", "csufederation-metadata.xml"),

    /** The 'csufederationtest-metadata.xml' found. */
    CSUFEDERATIONTEST_METADATA_XML("csufederationtest-metadata.xml", "csufederationtest-metadata.xml");

    /** The filename. */
    public final String filename;

    /** The label. */
    public final String label;

    /**
     * Constructs a new {@code EStateSourceFile}.
     *
     * @param theFilename the filename
     * @param theLabel    the label
     */
    EStateSourceFile(final String theFilename, final String theLabel) {

        this.filename = theFilename;
        this.label = theLabel;
    }
}
