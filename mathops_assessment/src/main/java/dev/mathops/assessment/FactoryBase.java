package dev.mathops.assessment;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.parser.CharSpan;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.XmlContent;

import java.io.File;

/**
 * The base class for factory methods that load objects from XML sources.
 */
public enum FactoryBase {
    ;

    /**
     * Gets XML content from a {@code File}.
     *
     * @param file the {@code File} from which to load the XML source
     * @return an {@code XmlContent} object that contains the parsed XML
     * @throws ParsingException of the source file cannot be parsed as XML
     */
    public static XmlContent getSourceContent(final File file) throws ParsingException {

        final String xml = FileLoader.loadFileAsString(file, true);

        if (xml == null) {
            throw new ParsingException(new CharSpan(0, 0, 1, 1), "Failed to load file");
        }

        return new XmlContent(xml, false, false);
    }

    /**
     * Convert any escape sequences found in a string into their corresponding special characters.
     *
     * @param value The string to convert.
     * @return The converted string, or null on any error.
     */
    public static String unescape(final String value) {

        // Convert any &...; values into their equivalents
        String converted = value.replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("&quot;", CoreConstants.QUOTE)
                .replace("&apos;", "'")
                .replace("&amp;", "&");

        // Convert any \\u#### codes to Unicode Characters
        int escape = converted.indexOf("\\u");

        while (escape != -1) {

            if (escape >= converted.length() - 5) {
                return null;
            }

            final char ch;
            try {
                ch = (char) Integer.parseInt(converted.substring(escape + 2, escape + 6), 16);
            } catch (final NumberFormatException e) {
                return null;
            }

            converted = converted.substring(0, escape) + Character.valueOf(ch) + converted.substring(escape + 6);

            escape = converted.indexOf("\\u");
        }

        return converted;
    }

    /**
     * Generates a {@code File} that points to the referenced object. It is possible this file will not exist, in which
     * case a URL should be generated and tried instead.
     *
     * @param baseDir the directory under which to look for the source file
     * @param ref     the ref
     * @return the {@code File} that corresponds to the referenced object
     */
    static File getRefSourceFile(final File baseDir, final String ref) {

        // Convert into local pathname
        File file = new File(baseDir, "instruction");

        // If we were provided the instruction directory already, use it.
        if (!file.exists() && (baseDir.getAbsolutePath().endsWith("/instruction")
                || baseDir.getAbsolutePath().endsWith("\\instruction"))) {
            file = baseDir;
        }

        final String[] tokens = ref.split("\\.");

        final int len = tokens.length;

        for (int i = 0; i < len; ++i) {
            if (i < len - 1) {
                file = new File(file, tokens[i]);
            } else {
                file = new File(file, tokens[i] + ".xml");
            }
        }

        return file;
    }
}
