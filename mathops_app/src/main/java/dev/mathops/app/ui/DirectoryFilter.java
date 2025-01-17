package dev.mathops.app.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A file filter that allows only directories.
 */
public final class DirectoryFilter extends FileFilter {

    /**
     * Constructs a new {@code DirectoryFilter}.
     */
    public DirectoryFilter() {

        super();
    }

    /**
     * Gets the description of the filter.
     *
     * @return the description of the filter
     */
    @Override
    public String getDescription() {

        return "Directories";
    }

    /**
     * The filter function, which accepts only directories.
     *
     * @param f the file being tested
     * @return {@code true} if the file is a directory; {@code false} otherwise
     */
    @Override
    public boolean accept(final File f) {

        return f.isDirectory();
    }
}
