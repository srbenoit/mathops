package dev.mathops.core;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.installation.Installations;
import dev.mathops.core.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A class to read and store the list of path names where files are stored.
 */
public final class PathList {

    /** The filename of the paths properties file. */
    private static final String PATHS_FILE_NAME = "paths.properties";

    /** Equals character. */
    private static final String EQUALS = "=";

    /** Left brace character. */
    private static final String LBRACE = "{";

    /** Right brace character. */
    private static final String RBRACE = "}";

    /** Object on which to synchronize creation of singleton instance. */
    private static final Object SYNCH = new Object();

    /** Date formatter for path list file header. */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' HH:mm a", Locale.US);

    /** The singleton instance. */
    private static PathList instance;

    /** The base installation directory. */
    public final File baseDir;

    /** A map from a path enumeration to the corresponding path. */
    private final Map<EPath, File> paths;

    static {
        instance = null;
    }

    /**
     * Private constructor to prevent instantiation.
     *
     * @param theBaseDir the directory in which to find the "paths.properties" file
     */
    private PathList(final File theBaseDir) {

        super();

        this.baseDir = theBaseDir;
        this.paths = new TreeMap<>();

        final Properties defaults = loadDefaults();

        final File file = new File(theBaseDir, PATHS_FILE_NAME);
        final Properties props = new Properties(defaults);

        if (file.exists()) {
            try (final InputStream input = new FileInputStream(file)) {
                props.load(input);
            } catch (final IOException ex) {
                Log.warning(Res.fmt(Res.PATH_LIST_READ_ERR, file.getAbsolutePath()), ex.getMessage());
            }
        }

        for (final EPath path : EPath.values()) {
            this.paths.put(path, makeFile(props.getProperty(path.key)));
        }
    }

    /**
     * Gets the paths map.
     *
     * @return the paths map
     */
    public Map<EPath, File> getPaths() {

        return this.paths;
    }

    /**
     * Loads the default path properties from the {@code EPath} enumerated values.
     *
     * @return the loaded properties file
     */
    private static Properties loadDefaults() {

        final Properties defaults = new Properties();

        final EPath[] values = EPath.values();
        for (final EPath value : values) {
            String defPath = value.defaultPath;

            if (defPath != null) {
                // See if there is a replacement string, and apply it
                for (final EPath ePath : values) {
                    final String replacement = defaults.getProperty(ePath.key);
                    if (replacement != null) {
                        final String str = LBRACE + ePath.key + RBRACE;
                        defPath = defPath.replace(str, replacement);
                    }
                }

                defaults.setProperty(value.key, defPath);
            }
        }

        return defaults;
    }

    /**
     * Initializes the singleton instance of {@code PathList}.
     *
     * @param baseDir the directory in which to find the "paths.properties" file
     */
    public static void init(final File baseDir) {

        synchronized (SYNCH) {
            instance = new PathList(baseDir);
        }
    }

    /**
     * Gets the singleton instance of {@code PathList}.
     *
     * @return the singleton instance
     */
    public static PathList getInstance() {

        synchronized (SYNCH) {
            if (instance == null) {
                instance = new PathList(Installations.DEF_BASE_DIR);
            }
        }
        return instance;
    }

    /**
     * Generates a file based on a path.
     *
     * @param path the path
     * @return the corresponding {@code File} or {@code null} if the path is null
     */
    private File makeFile(final String path) {

        final File result;

        if (path == null) {
            result = null;
        } else {
            String str = path;
            final EPath[] values = EPath.values();
            for (final EPath value : values) {
                final File replacement = this.paths.get(value);
                if (replacement != null) {
                    final String testStr = LBRACE + value.key + RBRACE;
                    str = str.replace(testStr, normalize(replacement));
                }
            }

            result = new File(str);
        }

        return result;
    }

    /**
     * Generates the normalize path of a file, which is an absolute path using '/' as a file separator.
     *
     * @param file the file
     * @return the normalized path, of the form "/a/b/c/name"
     */
    private static String normalize(final File file) {

        String path = file.getName();
        File parent = file.getParentFile();

        if (parent != null) {
            final StringBuilder str = new StringBuilder(50);
            while (parent != null) {
                str.setLength(0);
                str.append(parent.getName()).append(CoreConstants.SLASH).append(path);
                path = str.toString();
                parent = parent.getParentFile();
            }
        }

        return path;
    }

    /**
     * Retrieves the directory configured for a particular system path.
     *
     * @param path the path whose value to retrieve
     * @return the corresponding directory
     */
    public File get(final EPath path) {

        return this.paths.get(path);
    }

    /**
     * Sets new paths, which causes the paths file to be rewritten, and cached paths to be updated so future queries
     * will retrieve the new paths.
     *
     * @param newPaths the new paths
     */
    public void setPaths(final Map<EPath, ? extends File> newPaths) {

        if (newPaths.get(EPath.BASE_DIR) == null) {
            throw new IllegalArgumentException(Res.get(Res.PATH_LIST_NO_BASE));
        }

        this.paths.clear();
        this.paths.putAll(newPaths);

        final HtmlBuilder str = new HtmlBuilder(500);

        str.addln("# Created by ", getClass().getName(), " on ", LocalDateTime.now().format(DATE_FMT));
        str.addln();

        final EPath[] values = EPath.values();
        for (final EPath value : values) {
            if (value == EPath.BASE_DIR) {
                continue;
            }
            final File theFile = newPaths.get(value);
            if (theFile != null) {
                if (theFile.getAbsolutePath().startsWith("C:\\")) {
                    str.addln(value.key, EQUALS, normalize(theFile));
                } else {
                    str.addln(value.key, EQUALS, theFile.getAbsolutePath());
                }
            }
        }

        str.addln();

        final File file = new File(newPaths.get(EPath.BASE_DIR), PATHS_FILE_NAME);

        try (final FileOutputStream output = new FileOutputStream(file)) {
            output.write(str.toString().getBytes(StandardCharsets.UTF_8));
        } catch (final IOException ex) {
            Log.warning(Res.fmt(Res.PATH_LIST_WRITE_ERR, file.getAbsolutePath()), ex.getMessage());
        }
    }
}
