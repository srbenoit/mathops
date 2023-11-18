package dev.mathops.core.installation;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.SimpleErrorWarningLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Properties;

/**
 * An installation, with a specified base directory and configuration file name.
 */
public final class Installation extends SimpleErrorWarningLog {

    /** A tag that will be replaced by the base directory in path properties. */
    private static final String BASE_DIR_TAG = "$BASE_DIR";

    /** The base directory for the installation. */
    public final File baseDir;

    /** The name of the configuration file to read. */
    public final String cfgFile;

    /** Properties loaded from the configuration file. */
    public final Properties properties;

    /** Flag indicating configuration was loaded successfully. */
    private boolean loaded;

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param theBaseDir the base directory specified in command-line arguments
     * @param theCfgFile the name of the configuration file ({@code null} to use the default file name)
     * @throws InvalidPathException if the base directory does not represent a legitimate path
     */
    /* default */ Installation(final File theBaseDir, final String theCfgFile)
            throws InvalidPathException {

        super();

        // Validate the path
        theBaseDir.toPath();

        this.baseDir = theBaseDir;
        this.cfgFile = theCfgFile;
        this.properties = new Properties();

        loadConfiguration();
    }

    /**
     * Tests whether the configuration was loaded successfully and contained all required parameters.
     *
     * @return {@code true} if configuration was loaded
     */
    public boolean isLoaded() {

        return this.loaded;
    }

    /**
     * Gets the absolute path of the base directory.
     *
     * @return the absolute path of the base directory
     */
    public String getBaseDirPath() {

        return this.baseDir.getAbsolutePath();
    }

    /**
     * Loads the bare configuration settings from the file.
     */
    private void loadConfiguration() {

        // Unload first
        this.properties.clear();
        this.loaded = false;

        final File cfg = new File(this.baseDir, this.cfgFile);

        if (cfg.exists()) {
            try (final FileInputStream reader = new FileInputStream(cfg)) {
                final String base = this.baseDir.getAbsolutePath();
                this.properties.load(reader);
                for (final String propName : this.properties.stringPropertyNames()) {
                    final String value = this.properties.getProperty(propName);
                    if (value.contains(BASE_DIR_TAG)) {
                        this.properties.setProperty(propName, value.replace(BASE_DIR_TAG, base));
                    }
                }
                this.loaded = true;
            } catch (final IOException ex) {
                final String absPath = cfg.getAbsolutePath();
                final String msg = Res.fmt(Res.CANT_READ_CFG_FILE, absPath);
                indicateException(msg, ex);
            }
        } else {
            final String absPath = cfg.getAbsolutePath();
            final String msg = Res.fmt(Res.CFG_FILE_NONEXIST, absPath);
            addError(msg);
        }
    }

    /**
     * Queries a property based on its name, and attempts to convert it to a file. The property value contains either an
     * absolute path, or a string that begins with "$BASE_DIR", in which case that string is replaced by the base
     * directory absolute path to form the absolute path of the file.
     *
     * @param propertyName the property name
     * @param def          the default file to return if the property is not found
     * @return the extracted {@code File}, {@code def} if the property was not specified, or {@code null} if the
     *         property value could not be interpreted as a directory, or the directory could not be created
     */
    public File extractFileProperty(final String propertyName, final File def) {

        File result = null;

        String path = this.properties.getProperty(propertyName);

        if (path == null) {
            result = def;
        } else {
            if (path.startsWith(BASE_DIR_TAG)) {
                final int len = BASE_DIR_TAG.length();
                path = this.baseDir.getAbsolutePath() + path.substring(len);
            }

            final File file = new File(path);
            if (file.exists() || file.mkdirs()) {
                if (file.isDirectory()) {
                    result = file;
                } else {
                    final String msg = Res.fmt(Res.PATH_IS_NOT_DIR, path);
                    addWarning(msg);
                }
            } else {
                final String msg = Res.fmt(Res.CANT_CREATE_DIR, path);
                addWarning(msg);
            }
        }

        return result;
    }

    /**
     * Generates the string representation of the installation.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.baseDir.getAbsolutePath() + CoreConstants.COLON + this.cfgFile;
    }
}
