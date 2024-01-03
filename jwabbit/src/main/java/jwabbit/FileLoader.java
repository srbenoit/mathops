package jwabbit;

import dev.mathops.core.log.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to load files. This class should be able to load from a local file or a file in a JAR in the class
 * path, using a given class a the base of the package in which to search for the resource.
 */
public enum FileLoader {
    ;

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
     * Obtains an input stream for a particular resource. Several methods are attempted to locate and open the stream
     *
     * @param caller  the class of the object making the call, so that relative resource paths are based on the caller's
     *                position in the source tree
     * @param name    the name of the resource to read
     * @param logFail {@code true} to log a warning on failure
     * @return the input stream
     * @throws IOException if the stream could not be opened
     */
    private static InputStream openInputStream(final Class<?> caller, final String name, final boolean logFail)
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
            Log.warning("  *** ", classname, ".getResourceAsStream(", name, ") failed");
            final ClassLoader loader = caller.getClassLoader();
            input = loader.getResourceAsStream(name);
            if (input == null) {
                Log.warning("  *** ", classname, ".getClassLoader().getResourceAsStream(", name, ") failed");
                input = caller.getResourceAsStream(path);

                if (input == null) {
                    Log.warning("  *** ", classname, ".getResourceAsStream(", path, ") failed");
                    input = loader.getResourceAsStream(path);
                    if (input == null) {
                        Log.warning("  *** ", classname, ".getClassLoader().getResourceAsStream(", path, ") failed");
                    }
                }
            }
        }

        if (input == null) {
            // Could be in a foreign module - let the module classloader try
            final Module module = caller.getModule();
            input = module.getResourceAsStream(name);

            if (input == null) {
                final String modName = module.getName();
                Log.warning("  *** ", modName, ".getResourceAsStream(", name, ") failed");
                final ClassLoader loader = module.getClassLoader();
                input = loader.getResourceAsStream(name);
                if (input == null) {
                    Log.warning("  *** ", modName, ".getClassLoader().getResourceAsStream(", name, ") failed");
                    input = caller.getResourceAsStream(path);

                    if (input == null) {
                        Log.warning("  *** ", modName, ".getResourceAsStream(", path, ") failed");
                        input = loader.getResourceAsStream(path);
                        if (input == null) {
                            Log.warning("  *** ", modName, ".getClassLoader().getResourceAsStream(", path, ") failed");
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
                Log.warning("  *** Thread.currentThread().getContextClassLoader().getResourceAsStream(", name,
                        ") failed");
                input = loader.getResourceAsStream(path);
                if (input == null) {
                    Log.warning("  *** Thread.currentThread().getContextClassLoader().getResourceAsStream(", path,
                            ") failed");
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
            final String userDir = System.getProperty("user.dir");
            final File file = new File(userDir);

            try {
                input = new FileInputStream(new File(file, path));
            } catch (final FileNotFoundException ex) {
                if (logFail) {
                    final String msg = Res.fmt(Res.FILE_NOT_FOUND, classname, name);
                    Log.fine(msg);
                }
            }
        }

        if (input == null) {
            final String msg = Res.fmt(Res.FILE_NOT_FOUND, classname, name);
            throw new IOException(msg);
        }

        return input;
    }
}
