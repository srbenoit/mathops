package dev.mathops.core.parser.xml;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Locale;

/**
 * A filter to limit file views to only files with ".xml" extensions (not case-sensitive).
 */
public final class XmlFileFilter extends FileFilter implements java.io.FileFilter {

    /** The suffix for files this filter accepts. */
    private static final String SUFFIX = ".xml";

    /**
     * Constructs a new {@code XmlFileFilter}.
     */
    public XmlFileFilter() {

        super();
    }

    /**
     * Tests whether the specified abstract pathname should be included in a pathname list.
     *
     * @param f The abstract pathname to be tested
     * @return {@code true} if and only if {@code pathname} should be included
     */
    @Override
    public boolean accept(final File f) {

        return f.isDirectory() || f.getName().toLowerCase(Locale.ROOT).endsWith(SUFFIX);
    }

    /**
     * Gets the description of this filter.
     *
     * @return the description
     */
    @Override
    public String getDescription() {

        return Res.get(Res.XML_FILE_FILTER_DESC);
    }
}
