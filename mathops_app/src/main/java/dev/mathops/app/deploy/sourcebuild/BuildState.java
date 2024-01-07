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
    private String domain = null;

    /** The server type (PROD | DEV | TEST). */
    private String type = null;

    /** The /opt/build file (set once directory verified as present). */
    private File optBuild = null;

    /** The /opt/build/src file (set once directory verified as present). */
    private File optBuildSrc = null;

    /** All source files. */
    private final EnumMap<EStateSourceFile, File> sourceFiles;

    /** All uncompressed source archive directories. */
    private final EnumMap<EStateSourceFile, File> uncompressedDirectories;

    /** The target directory for Apache HTTPD installation. */
    private File httpdTargetDir = null;

    /** The target directory for Shibboleth SP installation. */
    private File shibbolethTargetDir = null;

    /** The target directory for PostgreSQL installation. */
    private File postgresqlTargetDir = null;

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
    void setDomain(final String theDomain) {

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

    /**
     * Gets the ShibbolethSP target directory.
     *
     * @return the target directory
     */
    File getHttpTargetDir() {

        return this.httpdTargetDir;
    }

    /**
     * Gets the HTTP target directory.
     *
     * @return the target directory
     */
    File getShibbolethTargetDir() {

        return this.shibbolethTargetDir;
    }

    /**
     * Gets the PostgeSQL target directory.
     *
     * @return the target directory
     */
    File getPostgresqlTargetDir() {

        return this.postgresqlTargetDir;
    }

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
//            File tomcatTargetDir = new File(StepBase.OPT, file.getName() + this.dateSuffix);
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
