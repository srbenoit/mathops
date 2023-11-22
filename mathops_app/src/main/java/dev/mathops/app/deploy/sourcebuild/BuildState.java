package dev.mathops.app.deploy.sourcebuild;

import java.io.File;
import java.util.EnumMap;

/**
 * All state variables for a build. An instance of this object is created at the start of the build process and
 * populated with state as the build progresses.
 */
final class BuildState {

    /** The installation date suffix. */
    final String dateSuffix;

    /** The server domain. */
    private String domain;

    /** The server type (PROD | DEV | TEST). */
    private String type;

    /** The /opt/build file (set once directory verified as present). */
    private File optBuild;

    /** The /opt/build/src file (set once directory verified as present). */
    private File optBuildSrc;

    /** All source files. */
    private final EnumMap<EStateSourceFile, File> sourceFiles;

    /** All uncompressed source archive directories. */
    private final EnumMap<EStateSourceFile, File> uncompressedDirectories;

    /** The target directory for Apache HTTPD installation. */
    private File httpdTargetDir;

    /** The target directory for Shibboleth SP installation. */
    private File shibbolethTargetDir;

    /** The target directory for PostgreSQL installation. */
    private File postgresqlTargetDir;

    /** The target directory for Apache Tomcat installation. */
    private File tomcatTargetDir;

    /**
     * Constructs a new {@code BuildState}.
     *
     * @param theDateSuffix the date suffix
     */
    BuildState(final String theDateSuffix) {

        this.dateSuffix = theDateSuffix;

        this.sourceFiles = new EnumMap<>(EStateSourceFile.class);
        this.uncompressedDirectories = new EnumMap<>(EStateSourceFile.class);
    }

    /**
     * Sets the server domain.
     *
     * @param theDomain the domain
     */
    public void setDomain(final String theDomain) {

        this.domain = theDomain;
    }

    /**
     * Gets the server domain.
     *
     * @return the domain
     */
    public String getDomain() {

        return this.domain;
    }

    /**
     * Sets the server type.
     *
     * @param theType the type
     */
    public void setType(final String theType) {

        this.type = theType;
    }

    /**
     * Gets the server type.
     *
     * @return the type
     */
    public String getType() {

        return this.type;
    }

    /**
     * Sets the /opt/build file.
     *
     * @param theOptBuild the file
     */
    void setOptBuild(final File theOptBuild) {

        this.optBuild = theOptBuild;
    }

    /**
     * Gets the /opt/build file.
     *
     * @return the file
     */
    File getOptBuild() {

        return this.optBuild;
    }

    /**
     * Sets the /opt/build/src file.
     *
     * @param theOptBuildSrc the file
     */
    void setOptBuildSrc(final File theOptBuildSrc) {

        this.optBuildSrc = theOptBuildSrc;
    }

    /**
     * Gets the /opt/build/src file.
     *
     * @return the file
     */
    File getOptBuildSrc() {

        return this.optBuildSrc;
    }

//    /**
//     * Sets the HTTP target directory.
//     *
//     * @param theHttpTargetDir the target directory
//     */
//    private void setHttpTargetDir(final File theHttpTargetDir) {
//
//        this.httpdTargetDir = theHttpTargetDir;
//    }

    /**
     * Gets the ShibbolethSP target directory.
     *
     * @return the target directory
     */
    File getHttpTargetDir() {

        return this.httpdTargetDir;
    }

//    /**
//     * Sets the ShibbolethSP target directory.
//     *
//     * @param theShibbolethTargetDir the target directory
//     */
//    private void setShibbolethTargetDir(final File theShibbolethTargetDir) {
//
//        this.shibbolethTargetDir = theShibbolethTargetDir;
//    }

    /**
     * Gets the HTTP target directory.
     *
     * @return the target directory
     */
    File getShibbolethTargetDir() {

        return this.shibbolethTargetDir;
    }

//    /**
//     * Sets the PostgeSQL target directory.
//     *
//     * @param thePostgresqlTargetDirectory the target directory
//     */
//    private void setPostgresqlTargetDir(final File thePostgresqlTargetDirectory) {
//
//        this.postgresqlTargetDir = thePostgresqlTargetDirectory;
//    }

    /**
     * Gets the PostgeSQL target directory.
     *
     * @return the target directory
     */
    File getPostgresqlTargetDir() {

        return this.postgresqlTargetDir;
    }

//    /**
//     * Sets the Tomcat target directory.
//     *
//     * @param theTomcatTargetDirectory the target directory
//     */
//    private void setTomcatTargetDir(final File theTomcatTargetDirectory) {
//
//        this.tomcatTargetDir = theTomcatTargetDirectory;
//    }

//    /**
//     * Gets the Tomcat target directory.
//     *
//     * @return the target directory
//     */
//    public File getTomcatTargetDir() {
//
//        return this.tomcatTargetDir;
//    }

    /**
     * Sets a source file.
     *
     * @param key  the key
     * @param file the source file
     */
    void setSourceFile(final EStateSourceFile key, final File file) {

        this.sourceFiles.put(key, file);
    }

    /**
     * Tests whether a source file is associated with a specified key.
     *
     * @param key the key
     * @return {@code true} if a file has been associated with the key
     */
    boolean hasSourceFile(final EStateSourceFile key) {

        return this.sourceFiles.containsKey(key);
    }

    /**
     * Gets a source file.
     *
     * @param key the key
     * @return the source file
     */
    File getSourceFile(final EStateSourceFile key) {

        return this.sourceFiles.get(key);
    }

    /**
     * Sets an uncompressed archive directory.
     *
     * @param key  the key
     * @param file the source directory
     */
    void setUncompressedDir(final EStateSourceFile key, final File file) {

        this.uncompressedDirectories.put(key, file);

        if (key == EStateSourceFile.HTTPD) {
            this.httpdTargetDir = new File(StepBase.OPT, file.getName() + this.dateSuffix);
        } else if (key == EStateSourceFile.SHIBBOLETH_SP) {
            this.shibbolethTargetDir = new File(StepBase.OPT, file.getName() + this.dateSuffix);
        } else if (key == EStateSourceFile.POSTGRESQL) {
            this.postgresqlTargetDir = new File(StepBase.OPT, file.getName() + this.dateSuffix);
        } else if (key == EStateSourceFile.APACHE_TOMCAT) {
            this.tomcatTargetDir = new File(StepBase.OPT, file.getName() + this.dateSuffix);
        }
    }

    /**
     * Gets an uncompressed archive directory.
     *
     * @param key the key
     * @return the source directory
     */
    File getUncompressedDir(final EStateSourceFile key) {

        return this.uncompressedDirectories.get(key);
    }
}
