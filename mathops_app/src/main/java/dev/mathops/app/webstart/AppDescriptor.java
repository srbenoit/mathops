package dev.mathops.app.webstart;

import dev.mathops.app.AppFileLoader;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.HexEncoder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.IElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.core.parser.xml.XmlContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A data class that can be populated from an XML file, with information on a version of an installed application.
 *
 * <p>
 * The XML file should be in the following format:
 *
 * <pre>
 * &lt;app name='...' version='...' releaseDate='...' mainClass='...'&gt;
 *   &lt;file name='...' size='...' sha256='...'/&gt;
 *   &lt;file name='...' size='...' sha256='...'/&gt;
 *   ...
 * &lt;/app&gt;
 * </pre>
 */
final class AppDescriptor {

    /** The application name. */
    public final String name;

    /** The application version. */
    public final String version;

    /** The application release date. */
    public final ZonedDateTime releaseDate;

    /** The main class name. */
    public final String mainClass;

    /** Descriptors of the files that make up the application. */
    private final List<FileDescriptor> files;

    /**
     * Private constructor to prevent instantiation.
     *
     * @param theName        the application name
     * @param theVersion     the application version
     * @param theReleaseDate the release date
     * @param theMainClass   the main class
     */
    private AppDescriptor(final String theName, final String theVersion,
                          final ZonedDateTime theReleaseDate, final String theMainClass) {

        this.name = theName;
        this.version = theVersion;
        this.releaseDate = theReleaseDate;
        this.mainClass = theMainClass;
        this.files = new ArrayList<>(10);
    }

    /**
     * Attempts to load and parse an application descriptor from an XML file.
     *
     * @param xmlFile the XML file
     * @return the parsed {@code AppDescriptor}; {@code null} if file could not be loaded and parsed
     */
    public static AppDescriptor parse(final File xmlFile) {

        AppDescriptor result;

        final String raw = AppFileLoader.loadFileAsString(xmlFile, false);
        if (raw == null) {
            Log.warning("Unable to load ", xmlFile.getAbsolutePath());
            result = null;
        } else {
            try {
                final XmlContent content = new XmlContent(raw, false, false);
                final IElement top = content.getToplevel();

                if ("app".equals(top.getTagName())) {
                    result = extractDescriptors(top);
                } else {
                    Log.warning("Missing 'app' top-level element in ",
                            xmlFile.getAbsolutePath());
                    result = null;
                }
            } catch (final ParsingException ex) {
                Log.warning("Unable to parse ", xmlFile.getAbsolutePath(), ex);
                result = null;
            }
        }

        return result;
    }

    /**
     * Attempts to load and parse an application descriptor from an input stream.
     *
     * @param in the input stream
     * @return the parsed {@code AppDescriptor}; {@code null} if stream could not be read and parsed
     */
    public static AppDescriptor parse(final InputStream in) {

        AppDescriptor result = null;

        try {
            final byte[] preBytes = AppFileLoader.readStreamAsBytes(in);

            if (preBytes == null) {
                Log.warning("Unable to read from input stream");
            } else {
                final String preStr = new String(preBytes, StandardCharsets.UTF_8);

                try {
                    final XmlContent content = new XmlContent(preStr, false, false);
                    final IElement top = content.getToplevel();

                    if ("app".equals(top.getTagName())) {
                        result = extractDescriptors(top);
                    } else {
                        Log.warning(//
                                "Missing 'app' top-level element in input stream");
                    }
                } catch (final ParsingException ex2) {
                    Log.warning("Unable to parse input stream", ex2);
                }
            }
        } catch (final IOException ex1) {
            Log.warning("Unable to read from input stream", ex1);
        }

        return result;
    }

    /**
     * Attempts to extract attributes and child elements to construct application and file descriptors.
     *
     * @param top the top-level 'app' element
     * @return the parsed {@code AppDescriptor}; {@code null} if data could not be parsed
     */
    private static AppDescriptor extractDescriptors(final IElement top) {

        AppDescriptor result;

        final String name = top.getStringAttr("name");
        final String ver = top.getStringAttr("version");
        final String date = top.getStringAttr("releaseDate");
        final String main = top.getStringAttr("mainClass");

        if (name == null) {
            Log.warning("Missing 'name' attribute on <app> element");
            result = null;
        } else if (ver == null) {
            Log.warning("Missing 'version' attribute on <app> element");
            result = null;
        } else if (date == null) {
            Log.warning("Missing 'releaseDate' attribute on <app> element");
            result = null;
        } else if (main == null) {
            Log.warning("Missing 'mainClass' attribute on <app> element");
            result = null;
        } else {
            try {
                final ZonedDateTime parsedDate = ZonedDateTime.parse(date);

                result = new AppDescriptor(name, ver, parsedDate, main);

                if (top instanceof final NonemptyElement non) {
                    for (final INode child : non.getChildrenAsList()) {
                        if (child instanceof final IElement childElem) {

                            if ("file".equals(childElem.getTagName())) {
                                final FileDescriptor fd = FileDescriptor.extract(childElem);
                                if (fd != null) {
                                    result.addFile(fd);
                                }
                            } else {
                                Log.warning("Unexpected child element of top-level <app> element: ",
                                        childElem.getTagName());
                            }
                        }
                    }

                    if (result.getFiles().isEmpty()) {
                        Log.warning("No <file> children of top-level <app> element");
                    }
                } else {
                    Log.warning("Top-level <app> element is ", top.getClass().getName(),
                            ", not the expected NonemptyElement");
                }
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid 'releaseDate' attribute on <app> element: ", date, ex);
                result = null;
            }
        }

        return result;
    }

    /**
     * Adds a file descriptor.
     *
     * @param fd the descriptor to add
     */
    private void addFile(final FileDescriptor fd) {

        this.files.add(fd);
    }

    /**
     * Gets the list of descriptors for all files in the application.
     *
     * @return the list of files (an unmodifiable view)
     */
    public List<FileDescriptor> getFiles() {

        return Collections.unmodifiableList(this.files);
    }

    /**
     * Generates a hash code for the descriptor.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return EqualityTests.objectHashCode(this.name) + EqualityTests.objectHashCode(this.version)
                + EqualityTests.objectHashCode(this.releaseDate)
                + EqualityTests.objectHashCode(this.mainClass)
                + EqualityTests.objectHashCode(this.files);
    }

    /**
     * Tests whether this object is equal to another.
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final AppDescriptor ad) {
            equal = ad.name.equals(this.name) && ad.version.equals(this.version)
                    && ad.releaseDate.equals(this.releaseDate) && ad.files.equals(this.files);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates the XML representing the descriptor.
     *
     * @return the XML
     */
    public String toXml() {

        final HtmlBuilder xml = new HtmlBuilder(500);

        xml.addln("<app name='", this.name, "' version='", this.version, "' releaseDate='", this.releaseDate,
                "' mainClass='", this.mainClass, "'>");

        for (final FileDescriptor fd : this.files) {
            xml.addln("  <file name='", fd.name, "' size='", Long.toString(fd.size), "' sha256='",
                    HexEncoder.encodeUppercase(fd.getSHA256()), "'/>");
        }

        xml.addln("</app>");

        return xml.toString();
    }
}
