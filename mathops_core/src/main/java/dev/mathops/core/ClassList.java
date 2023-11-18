package dev.mathops.core;

import dev.mathops.core.log.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * A class with a static method that can locate the set of classes installed under a given package. To use, pass the
 * package name to {@code scanClasses}, which returns a list of the matching classes.
 */
public enum ClassList {
    ;

    /** Estimated number of classes for initial list allocation. */
    private static final int EST_NUM_CLASSES = 100;

    /** Buffer size. */
    private static final int BUF_SIZE = 1024;

    /** File extension for class files. */
    private static final String CLASS_FILE_EXT = ".class";

    /**
     * Scans for all classes installed under a particular path.
     *
     * @param root the root path in which to search for classes
     * @return the list of classes found, or {@code null} on any error
     */
    public static List<Class<?>> scanClasses(final String root) {

        final List<Class<?>> list = new ArrayList<>(EST_NUM_CLASSES);
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (loader instanceof java.net.URLClassLoader) {

            final URL[] urls = ((java.net.URLClassLoader) loader).getURLs();

            for (final URL url : urls) {
                // Log.info(" Trying URL " + url);

                if (url.toString().startsWith("file:")) {
                    scanFilename(url.getFile(), root, list, loader);
                } else {
                    scanUrl(url, root, list, loader);
                }
            }
        } else {
            final String classpath = System.getProperty("java.class.path", CoreConstants.EMPTY);

            final String pathSep = Character.toString(File.pathSeparatorChar);
            final String[] paths = classpath.split(pathSep);

            for (final String path : paths) {
                scanFilename(path, root, list, loader);
            }
        }

        return list;
    }

    /**
     * Scans for all classes installed under a particular URL.
     *
     * @param url    the URL to scan
     * @param path   the path in which to search for classes
     * @param list   the list to which to accumulate results
     * @param loader the class loader to use to locate resources
     */
    private static void scanUrl(final URL url, final String path, final Collection<? super Class<?>> list,
                                final ClassLoader loader) {

        final byte[] buf = new byte[BUF_SIZE];

        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Download the JAR file
            try (final InputStream input = url.openStream()) {
                int size = input.read(buf);

                while (size != -1) {
                    out.write(buf, 0, size);
                    size = input.read(buf);
                }
            }

            try (final JarInputStream jis = new JarInputStream(new ByteArrayInputStream(out.toByteArray()))) {

                JarEntry entry = jis.getNextJarEntry();

                while (entry != null) {
                    if (entry.getName().endsWith(CLASS_FILE_EXT)) {
                        processClass(entry.getName(), path, loader, list);
                    }
                    entry = jis.getNextJarEntry();
                }
            }
        } catch (final IOException ex) {
            Log.warning(Res.fmt(Res.CANT_DOWNLOAD_JAR, url), ex);
        }
    }

    /**
     * Scans for all classes installed under a particular path in a single JAR file.
     *
     * @param filename the filename to scan
     * @param path     the path in which to search for classes
     * @param list     the list to which to accumulate results
     * @param loader   the class loader to use to locate resources
     */
    private static void scanFilename(final String filename, final String path, final List<? super Class<?>> list,
                                     final ClassLoader loader) {

        if (filename.endsWith(".jar")) {
            scanJarFile(filename, path, loader, list);
        } else {
            final File file = new File(filename);

            if (file.isDirectory()) {
                recurseDirectory(file.getAbsolutePath(), file, path, loader, list);
            }
        }
    }

    /**
     * Recursively scans a directory searching for files whose names end in CLASS_FILE_EXTENSION.
     *
     * @param root   the root of the directory being searched
     * @param dir    the directory to search
     * @param path   the path being searched for
     * @param loader the ClassLoader to use
     * @param list   the list to which to add matches
     */
    private static void recurseDirectory(final String root, final File dir, final String path,
                                         final ClassLoader loader, final List<? super Class<?>> list) {

        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    recurseDirectory(root, file, path, loader, list);
                } else {
                    String absPath = file.getAbsolutePath();

                    if (absPath.startsWith(root)) {
                        absPath = absPath.substring(root.length() + 1);
                    }

                    final String entry = absPath.replace(File.separatorChar, '.');
                    if (entry.endsWith(CLASS_FILE_EXT)) {
                        processClass(entry, path, loader, list);
                    }
                }
            }
        }
    }

    /**
     * Scans a single JAR file for class entries matching the search path.
     *
     * @param filename the name of the Jar file to search
     * @param path     the path being searched for
     * @param loader   the ClassLoader to use
     * @param list     the list to which to add matches
     */
    private static void scanJarFile(final String filename, final String path, final ClassLoader loader,
                                    final Collection<? super Class<?>> list) {

        final String cleaned = filename.replace("%20", CoreConstants.SPC);
        final File file = new File(cleaned);

        if (file.exists()) {
            try (final JarFile jar = new JarFile(file)) {

                final Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    final String entry = entries.nextElement().getName();

                    if (entry.endsWith(CLASS_FILE_EXT)) {
                        processClass(entry, path, loader, list);
                    }
                }
            } catch (final IOException ex) {
                Log.warning(Res.fmt(Res.CANT_INSTANTIATE_JAR, cleaned), ex);
            }
        }
    }

    /**
     * Given the filename of a single class, tries to load that class and checks it against the search path.
     *
     * @param filename the class filename
     * @param path     the path being searched for
     * @param loader   the ClassLoader to use
     * @param list     the list to which to add matches
     */
    private static void processClass(final String filename, final String path, final ClassLoader loader,
                                     final Collection<? super Class<?>> list) {

        final String cleaned = filename.substring(0, filename.length() - CLASS_FILE_EXT.length()).replace('/', '.');

        // Classes not in the search path are ignored.
        if (cleaned.startsWith(path)) {
            try {
                list.add(Class.forName(cleaned, false, loader));
            } catch (final ClassNotFoundException nfe) {
                Log.warning(Res.fmt(Res.CLASS_NOT_FOUND, cleaned));
            } catch (final NoClassDefFoundError ex) {
                Log.warning(Res.fmt(Res.CLASS_NOT_DEF, cleaned));
            }
        }
    }
}
