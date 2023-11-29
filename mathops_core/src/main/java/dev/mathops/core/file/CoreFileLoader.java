package dev.mathops.core.file;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility class to load files. This class should be able to load from a local file or a file in a JAR in the class
 * path, using a given class a the base of the package in which to search for the resource.
 */
public enum CoreFileLoader {
    ;

    /** The file extension for properties files. */
    private static final String PROPS_EXT = ".properties";

    /** Size of buffer to allocate for reading. */
    private static final int BUF_SIZE = 8192;

    /** Estimated number of lines in typical file for initial list allocation. */
    private static final int EST_NUM_LINES = 100;

    /** Underscore character. */
    private static final char UNDERSCORE = '_';

    /** An empty string array. */
    private static final String[] EMPTY_STR_ARRAY = new String[0];

    /**
     * Loads a text file, storing the file contents in a {@code String}. Lines in the returned file are separated by
     * single '\r\n' characters regardless of the line terminator in the source file. The last line will end with a
     * '\r\n' character, whether there was one in the input file or not.
     *
     * @param file    the file to read
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded file contents, or {@code null} if unable to load
     */
    public static String loadFileAsString(final File file, final boolean logFail) {

        String result = null;

        final int len = (int) file.length();
        final byte[] buffer = new byte[len];

        try (final InputStream input = new FileInputStream(file)) {
            final int count = input.read(buffer);
            if (count == len) {
                result = new String(buffer, StandardCharsets.UTF_8);
            } else {
                if (logFail) {
                    final String filename = file.getName();
                    final String msg = Res.fmt(Res.FILE_LOAD_FAIL, filename);
                    Log.warning(msg);
                }
            }
        } catch (final IOException ex) {
            if (logFail) {
                final String filename = file.getName();
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, filename);
                Log.warning(msg, ex);
            }
        }

        return result;
    }

    /**
     * Loads a text file, storing the file contents in a {@code String}. Lines in the returned file are separated by
     * single '\r\n' characters regardless of the line terminator in the source file. The last line will end with a
     * '\r\n' character, whether there was one in the input file or not.
     *
     * @param caller  the class of the object making the call, so that relative resource paths are based on the caller's
     *                position in the source tree
     * @param name    the name of the file to read
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded file contents, or {@code null} if unable to load
     */
    public static String loadFileAsString(final Class<?> caller, final String name, final boolean logFail) {

        String result = null;

        try (final InputStream input = openInputStream(caller, name, logFail)) {
            final HtmlBuilder htm = new HtmlBuilder(BUF_SIZE);
            readStreamAsString(input, htm);
            result = htm.toString();
        } catch (final IOException ex) {
            if (logFail) {
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, name);
                Log.warning(msg, ex);
            }
        }

        return result;
    }

    /**
     * Reads all lines of text from an input stream and appends them to a {@code HtmlBuilder} .
     *
     * @param stream the stream to read
     * @param str    a {@code HtmlBuilder} to which to add the loaded text from the stream
     * @throws IOException if an error occurred reading from the stream
     */
    private static void readStreamAsString(final InputStream stream, final HtmlBuilder str)
            throws IOException {

        try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
                str.add(line, CoreConstants.CRLF);
            }
        }
    }

    /**
     * Loads a text file, storing the resulting lines of text in a {@code String} array.
     *
     * @param file    the file to read
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded file contents, or {@code null} if unable to load
     */
    public static String[] loadFileAsLines(final File file, final boolean logFail) {

        String[] result = null;

        try (final InputStream stream = new FileInputStream(file)) {
            result = readStreamAsLines(stream);
        } catch (final IOException ex) {
            if (logFail) {
                final String filename = file.getName();
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, filename);
                Log.warning(msg, ex);
            }
        }

        return result;
    }

    /**
     * Loads a text file, storing the resulting lines of text in a {@code String} array.
     *
     * @param caller  the class of the object making the call, so that relative resource paths are based on the caller's
     *                position in the source tree
     * @param name    the name of the file to read
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded file contents, or {@code null} if unable to load
     */
    public static String[] loadFileAsLines(final Class<?> caller, final String name, final boolean logFail) {

        String[] result = null;

        try (final InputStream input = openInputStream(caller, name, logFail)) {
            result = readStreamAsLines(input);
        } catch (final IOException ex) {
            if (logFail) {
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, name);
                Log.warning(msg, ex);
            }
        }

        return result;
    }

    /**
     * Reads all lines of text from an input stream and returns them as a {@code String} array.
     *
     * @param stream the stream to read
     * @return the loaded contents, or {@code null} if unable to load
     * @throws IOException if an error occurred reading from the stream
     */
    private static String[] readStreamAsLines(final InputStream stream) throws IOException {

        final String[] result;

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            final List<String> lines = new ArrayList<>(EST_NUM_LINES);

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }

            result = lines.toArray(EMPTY_STR_ARRAY);
        }

        return result;
    }

    /**
     * Loads a binary file, storing the resulting data in a {@code byte} array.
     *
     * @param file    the file to read
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded file contents, or {@code null} if unable to load
     */
    public static byte[] loadFileAsBytes(final File file, final boolean logFail) {

        byte[] result = null;

        try (final InputStream input = new FileInputStream(file)) {
            result = readStreamAsBytes(input);
        } catch (final FileNotFoundException ex) {
            if (logFail) {
                final String filename = file.getName();
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, filename);
                Log.warning(msg);
            }
        } catch (final IOException ex) {
            if (logFail) {
                final String filename = file.getName();
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, filename);
                Log.warning(msg, ex);
            }
        }

        return result;
    }

    /**
     * Loads a binary file, storing the resulting data in a {@code byte} array.
     *
     * @param caller  the class of the object making the call, so that relative resource paths are based on the caller's
     *                position in the source tree
     * @param name    the name of the file to read
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded file contents, or {@code null} if unable to load
     */
    public static byte[] loadFileAsBytes(final Class<?> caller, final String name, final boolean logFail) {

        byte[] result = null;

        try (final InputStream input = openInputStream(caller, name, logFail)) {
            result = readStreamAsBytes(input);
        } catch (final IOException ex) {
            if (logFail) {
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, name);
                Log.warning(msg, ex);
            }
        }

        return result;
    }

    /**
     * Loads a binary file, storing the resulting data in a {@code byte} array.
     *
     * @param file  the file to read
     * @param start the index of the first byte to read
     * @param end   the index after the last byte to read
     * @return the loaded file contents, or {@code null} if unable to load
     */
    public static byte[] loadFileAsBytes(final File file, final long start, final long end) {

        byte[] result = null;

        try (final InputStream input = new FileInputStream(file)) {
            result = readStreamAsBytes(input, start, end);
        } catch (final IOException ex) {
            final String filename = file.getName();
            final String msg = Res.fmt(Res.FILE_LOAD_FAIL, filename);
            Log.warning(msg, ex);
        }

        return result;
    }

    /**
     * Reads all bytes of data from an input stream and returns them as a byte array.
     *
     * @param stream the stream to read
     * @return the data read from the stream
     * @throws IOException if an error occurred reading from the stream
     */
    public static byte[] readStreamAsBytes(final InputStream stream) throws IOException {

        final byte[] buffer = new byte[BUF_SIZE];
        final byte[] result;

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(BUF_SIZE)) {
            int count = stream.read(buffer);
            while (count != -1) {
                baos.write(buffer, 0, count);
                count = stream.read(buffer);
            }
            result = baos.toByteArray();
        }

        return result;
    }

    /**
     * Reads all bytes of data from an input stream and returns them as a byte array.
     *
     * @param stream the stream to read
     * @param start  the index of the first byte to read
     * @param end    the index after the last byte to read
     * @return the data read from the stream
     * @throws IOException if an error occurred reading from the stream
     */
    private static byte[] readStreamAsBytes(final InputStream stream, final long start, final long end)
            throws IOException {

        final byte[] buffer = new byte[BUF_SIZE];
        final byte[] result;

        long remainToRead = end;
        if (start > 0L) {
            remainToRead -= stream.skip(start);
        }

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(BUF_SIZE)) {
            long min = Math.min(remainToRead, buffer.length);
            int count = stream.read(buffer, 0, (int) min);
            while (count != -1 && remainToRead > 0L) {
                out.write(buffer, 0, count);
                remainToRead = remainToRead - (long) count;
                min = Math.min(remainToRead, buffer.length);
                count = stream.read(buffer, 0, (int) min);
            }
            result = out.toByteArray();
        }

        return result;
    }

    /**
     * Reads a single image file into a buffered image.
     *
     * @param file    the file to read
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded buffered image
     */
    public static BufferedImage loadFileAsImage(final File file, final boolean logFail) {

        BufferedImage img = null;

        try (final InputStream input = new FileInputStream(file)) {
            img = ImageIO.read(input);
        } catch (final IOException ex) {
            if (logFail) {
                final String filename = file.getName();
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, filename);
                Log.warning(msg, ex);
            }
        }

        return img;
    }

    /**
     * Reads a single image file into a buffered image.
     *
     * @param caller  the class of the object making the call, so that relative resource paths are based on the caller's
     *                position in the source tree
     * @param path    the resource path of the image file
     * @param logFail {@code true} to log a warning on failure
     * @return the loaded buffered image
     */
    public static BufferedImage loadFileAsImage(final Class<?> caller, final String path, final boolean logFail) {

        BufferedImage img = null;

        try (final InputStream input = openInputStream(caller, path, logFail)) {
            img = ImageIO.read(input);
        } catch (final IOException ex) {
            if (logFail) {
                final String msg = Res.fmt(Res.FILE_LOAD_FAIL, path);
                Log.warning(msg, ex);
            }
        }

        return img;
    }

    /**
     * Finds and opens the appropriate resource bundle for a given locale. This locale may change if the user selects
     * different languages from the interface, in which case the GUI will be rebuilt with the new settings.
     *
     * <p>
     * If the resource bundle cannot be loaded for any reason, a set of default settings will be generated and
     * returned.
     *
     * @param dir     the directory in which to find the properties file
     * @param base    the base name (without language extension) of the properties file
     * @param logFail {@code true} to log a warning on failure
     * @return the opened resources, as a {@code Properties} object
     */
    public static Properties loadFileAsProperties(final File dir, final String base, final boolean logFail) {

        // look for a file qualified with the locale name, then for a file with no qualifications
        final String path1 = base + UNDERSCORE + Locale.getDefault().getLanguage() + PROPS_EXT;
        final File file = new File(dir, path1);
        final File file2 = file.exists() ? file : new File(dir, base + PROPS_EXT);

        Properties res = null;

        if (file2.exists()) {
            try (final InputStream input = new FileInputStream(file2);
                 final InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                res = new Properties();
                res.load(isr);
            } catch (final IOException ex) {
                if (logFail) {
                    final String path = file2.getAbsolutePath();
                    final String msg = Res.fmt(Res.FILE_LOAD_FAIL, path);
                    Log.warning(msg, ex);
                }
            }
        }

        return res;
    }

    /**
     * Finds and opens the appropriate resource bundle for a given locale. This locale may change if the user selects
     * different languages from the interface, in which case the GUI will be rebuilt with the new settings.
     *
     * <p>
     * If the resource bundle cannot be loaded for any reason, a set of default settings will be generated and
     * returned.
     *
     * @param caller  the class of the object making the call, so that relative resource paths are based on the caller's
     *                position in the source tree
     * @param base    the base name (without language extension) of the resource bundle
     * @param def     a set of defaults in case the resources could not be found
     * @param logFail {@code true} to log a warning on failure
     * @return the opened resources, as a {@code Properties} object
     */
    public static Properties loadFileAsProperties(final Class<?> caller, final String base,
                                                  final Properties def, final boolean logFail) {

        Properties res = def;

        // look for a file qualified with the locale name, then for a file with no qualifications
        final Locale defaultLocale = Locale.getDefault();
        final String defaultLang = defaultLocale.getLanguage();

        try (final InputStream input = openInputStream(caller, base + UNDERSCORE + defaultLang + PROPS_EXT, logFail);
             final InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            res = new Properties(def);
            res.load(isr);
        } catch (final IOException ex) {
            // Fully qualified resource could not be opened, try default
            try (final InputStream input = openInputStream(caller, base + PROPS_EXT, logFail);
                 final InputStreamReader isr = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                res = new Properties(def);
                res.load(isr);
            } catch (final IOException ex2) {
                if (logFail) {
                    final String msg = Res.fmt(Res.FILE_LOAD_FAIL, base + PROPS_EXT);
                    Log.warning(msg, ex2);
                }
            }
        }

        return res;
    }

    /**
     * Obtains an input stream for a particular resource. Several methods are attempted to locate and open the stream
     *
     * @param caller  the class of the object making the call, so that relative resource paths are based on the caller's
     *                position in the source tree
     * @param name    the name of the resource to read
     * @param logFail {@code true} to log a warning on failure
     * @return the input stream
     * @throws IOException if the stream could not be opened
     */
    public static InputStream openInputStream(final Class<?> caller, final String name, final boolean logFail)
            throws IOException {

        final String classname = caller.getName();
        String path;
        final int lastDot = classname.lastIndexOf('.');
        if (lastDot == -1) {
            path = name;
        } else {
            final String packagename = classname.substring(0, lastDot);
            path = "/" + packagename.replace('.', '/') + "/" + name;
        }

        // Let the calling class try to locate the resource
        InputStream input = caller.getResourceAsStream(name);
        if (input == null) {
            Log.warning("  *** ", caller.getName(), ".getResourceAsStream(", name, ") failed");
            final ClassLoader loader = caller.getClassLoader();
            input = loader.getResourceAsStream(name);
            if (input == null) {
                Log.warning("  *** ", caller.getName(), ".getClassLoader().getResourceAsStream(", name, ") failed");
                input = caller.getResourceAsStream(path);

                if (input == null) {
                    Log.warning("  *** ", caller.getName(), ".getResourceAsStream(", path, ") failed");
                    input = loader.getResourceAsStream(path);
                    if (input == null) {
                        Log.warning("  *** ", caller.getName(), ".getClassLoader().getResourceAsStream(", path, ") failed");
                    }
                }
            }
        }

        if (input == null) {
            // Could be in a foreign module - let the module classloader try
            final Module module = caller.getModule();
            input = module.getResourceAsStream(name);
            if (input == null) {
                Log.warning("  *** ", module.getName(), ".getResourceAsStream(", name, ") failed");
                final ClassLoader loader = module.getClassLoader();
                input = loader.getResourceAsStream(name);
                if (input == null) {
                    Log.warning("  *** ", module.getName(), ".getClassLoader().getResourceAsStream(", name, ") failed");
                    input = caller.getResourceAsStream(path);

                    if (input == null) {
                        Log.warning("  *** ", module.getName(), ".getResourceAsStream(", path, ") failed");
                        input = loader.getResourceAsStream(path);
                        if (input == null) {
                            Log.warning("  *** ", module.getName(), ".getClassLoader().getResourceAsStream(", path, ") failed");
                        }
                    }
                }
            }
        }

        if (input == null) {
            // Next, let the thread context class loader try
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            input = loader.getResourceAsStream(name);
            if (input == null) {
                Log.warning("  *** Thread.currentThread().getContextClassLoader().getResourceAsStream(", name, ") failed");
                input = loader.getResourceAsStream(path);
                if (input == null) {
                    Log.warning("  *** Thread.currentThread().getContextClassLoader().getResourceAsStream(", path, ") failed");
                }
            }
        }

        if (input == null) {
            // Try the system class loader
            input = ClassLoader.getSystemResourceAsStream(name);
            if (input == null) {
                Log.warning("  *** ClassLoader.getSystemResourceAsStream(", name, ") failed");
                input = ClassLoader.getSystemResourceAsStream(path);
                if (input == null) {
                    Log.warning("  *** ClassLoader.getSystemResourceAsStream(", path, ") failed");
                }
            }
        }

        if (input == null) {
            // Last chance - look in the working directory
            final File file = new File(System.getProperty("user.dir"));

            try {
                input = new FileInputStream(new File(file, path));
            } catch (final FileNotFoundException ex) {
                if (logFail) {
                    final String callerName = caller.getName();
                    final String msg = Res.fmt(Res.FILE_NOT_FOUND, callerName, name);
                    Log.fine(msg);
                }
            }
        }

        if (input == null) {
            final String callerName = caller.getName();
            final String msg = Res.fmt(Res.FILE_NOT_FOUND, callerName, name);
            throw new IOException(msg);
        }

        return input;
    }
}
