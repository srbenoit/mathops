package dev.mathops.app.deploy.sourcebuild;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;

import java.io.File;
import java.time.LocalDate;

/**
 * Builds the server from source packages (intended to run from the command line as the user who owns the /opt/build
 * directory tree, referred to as $USER below.
 *
 * <p>
 * Typical usage - execute on the server with root permissions:
 *
 * <pre>
 * java -jar sourcebuild.jar
 * </pre>
 * <p>
 * The program acts on files in the /opt/build/ directory, which must exist and should be owned by $USER with mode 700.
 * <p>
 * On entry, the program expects there to be an /opt/build/src directory, which must exist and should be owned by $USER
 * with mode 700. It expects that directory to contain the files listed below (where '{ver}' represents a valid version
 * number): The executable JAR for this class should be stored in this directory as well, since it MUST be protected
 * against modification or replacement!
 *
 * <ul>
 * <li>apache-tomcat-{ver}.tar.gz
 * <li>apr-{ver}.tar.gz
 * <li>apr-util-{ver}1.6.1.tar.gz
 * <li>curl-{ver}.tar.gz
 * <li>httpd-{ver}.tar.gz
 * <li>log4shib-{ver}.tar.gz
 * <li>openjdk-{ver}_linux-x64_bin.tar.gz
 * <li>opensaml-{ver}.tar.gz
 * <li>openssl-{ver}.tar.gz
 * <li>pcre-{ver}.tar.gz
 * <li>postgresql-{ver}.tar.gz
 * <li>shibboleth-sp-{ver}.tar.gz
 * <li>xerces-c-{ver}.tar.gz
 * <li>xml-security-c-{ver}.tar.gz
 * <li>xmltooling-{ver}.tar.gz
 * </ul>
 *
 *
 * <ul>
 * <li>ifxjdbc.jar
 * <li>ifxjdbcx.jar
 * <li>ifxlang.jar
 * <li>ifxlsupp.jar
 * <li>ifxsqlj.jar
 * <li>ifxtools.jar
 * <li>ojdbc7.jar
 * <li>postgresql-{ver}.jar
 * </ul>
 *
 * <ul>
 * <li>attribute-map.xml
 * <li>shibboleth2.xml
 * <li>csufederation-metadata-cert.pem
 * <li>csufederationtest-metadata-cert.pem
 * <li>csufederation-metadata.xml
 * <li>csufederationtest-metadata.xml
 * </ul>
 * <p>
 * The directory may also contain Shibboleth SP key files:
 * <ul>
 * <li>sp-cert.pem [must be owned by $USER, mode=400]
 * <li>sp-key.pem [must be owned by $USER, mode=400]
 * </ul>
 * <p>
 * If these files are present, they will overwrite those created as part of the Shibboleth SP
 * installation.
 *
 * <p>
 * * The program performs the following steps:
 *
 * <ol>
 * <li>Validates the directory structure and checks that all needed files are present and readable
 * <li>Cleans the build area of previous build files
 * <li>Uncompresses all source archives to /opt/build and sets ownership to $USER and top-level
 * directory permissions to 755
 * <li>Moves Java to /opt and links as /opt/jdk if needed
 * <li>Builds OpenSSL for both ShibbolethSP and ApacheHTTPD
 * <li>Builds PCRE for ApacheHTTPD
 * <li>Builds ApacheHTTPD
 * <li>Builds curl for ShibbolethSP
 * <li>Builds log4shib for ShibbolethSP
 * <li>Builds xerces for ShibbolethSP
 * <li>Builds XML security for ShibbolethSP
 * <li>Builds XML tooling for ShibbolethSP
 * <li>Builds open-SAML for ShibbolethSP
 * <li>Builds ShibbolethSP
 * <li>Moves Tomcat to /opt if needed
 * <li>Builds PostgreSQL if needed
 * <li>Configures ApacheHTTPD
 * <li>Configures Tomcat if needed
 * <li>Configures ShibbolethSP
 * <li>Configures PostgreSQL if needed
 * <li>Tests whether startup scripts are configured properly
 * <li>If everything looks correct, deploys new builds with the option to revert before exiting
 * </ol>
 * <p>
 * Optionally, it tests for toe following files in /opt/build/src/cfg
 *
 * <ul>
 * <li>sp-cert.pem [must be owned by root, mode=400]
 * <li>sp-key.pem [must be owned by root, mode=400]
 * </ul>
 */
enum ServerSourceBuild {
    ;

    /** Flag to cause verbose debugging output. */
    static final boolean DEBUG = true;

    /** Characters to add to the end of a date string to ensure uniqueness. */
    private static final String UNIQUENESS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Builds the WAR file.
     */
    private static void build() {

        final BuildState state = new BuildState(createDateString());

        Log.fine(CoreConstants.EMPTY);
        Log.fine("\u001B[94mServer Source Build v0.1 (pre-alpha)\u001B[0m");
        Log.fine("\u001B[94m------------------------------------\u001B[0m");
        Log.fine(CoreConstants.EMPTY);

        if (GatherParameters.areParametersValid(state)
                && VerifySourceTree.checkSourceTree(state)
                && CleanBuildArea.checkBuild(state)
                && DecompressSourceArchives.checkDecompress(state)
                && InstallJava.checkInstall(state)
                && BuildOpenSSL.checkBuild(state)
                && BuildCurl.checkBuild(state)
                && BuildPcre.checkBuild(state)
                && BuildApacheHttpd.checkBuild(state)
                && BuildLog4Shib.checkBuild(state)
                && BuildXerces.checkBuild(state)
                && BuildXmlSecurity.checkBuild(state)
                && BuildXmlTooling.checkBuild(state)
                && BuildOpenSaml.checkBuild(state)
                && BuildShibbolethSp.checkBuild(state)
                && BuildPostgres.checkBuild(state)
                && InstallTomcat.checkInstall(state)) {
            Log.fine();
            Log.fine("\u001B[94mBuild Succeeded.\u001B[0m");
            Log.fine();
        }
    }

    /**
     * Builds a date string and adds an optional letter to ensure uniqueness.
     *
     * @return the generated date string
     */
    private static String createDateString() {

        final LocalDate today = LocalDate.now();
        final StringBuilder suffix = new StringBuilder(10);
        suffix.append('-');
        suffix.append(today.getYear());
        if (today.getMonthValue() < 10) {
            suffix.append('0');
        }
        suffix.append(today.getMonthValue());
        if (today.getDayOfMonth() < 10) {
            suffix.append('0');
        }
        suffix.append(today.getDayOfMonth());

        String str = suffix.toString();

        if (testForCollision(str)) {
            final int len = UNIQUENESS.length();
            for (int i = 0; i < len; ++i) {
                final String test = str + UNIQUENESS.charAt(i);
                if (!testForCollision(test)) {
                    str = test;
                    break;
                }
            }
        }

        return str;
    }

    /**
     * Tests whether there exists a directory in /opt that ends with a specified suffix.
     *
     * @param suffix the suffix
     * @return {@code true} if a file exists with the suffix
     */
    private static boolean testForCollision(final String suffix) {

        boolean collision = false;

        final File[] files = StepBase.OPT.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory() && file.getName().endsWith(suffix)) {
                    collision = true;
                    break;
                }
            }
        }

        return collision;
    }

    /**
     * Main method to execute the builder.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        build();
    }
}
