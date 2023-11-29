package dev.mathops.core.unicode;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.file.CoreFileLoader;
import dev.mathops.core.log.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads the Unicode character database and generates a map from code point to a {@code UnicodeCharacter}.
 */
public final class UnicodeCharacterSet {

    /** The name of the file to read. */
    private static final String FILENAME = "UnicodeData.txt";

    /** The number of fields per line in the UnicodeData.txt file. */
    private static final int LINE_FIELDS = 15;

    /** The singleton instance. */
    private static UnicodeCharacterSet instance;

    /** The unicode characters by code point. */
    private final Map<Integer, UnicodeCharacter> chars;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private UnicodeCharacterSet() {

        this.chars = new TreeMap<>();

        loadCharsFile();
    }

    /**
     * Attempts to load and populate the blocks map by reading the Blocks.txt file, which should be from the latest
     * Unicode character database.
     */
    private void loadCharsFile() {

        final String[] lines = CoreFileLoader.loadFileAsLines(getClass(), FILENAME, true);
        try {
            for (String line : lines) {
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    processLine(line);
                }
            }
        } catch (NumberFormatException ex) {
            Log.warning("Invalid character specification in ", FILENAME, ex);
            this.chars.clear();
        }
    }

    /**
     * Processes a single line of the input blocks file, after it has been discovered that the line is not a comment or
     * empty.
     *
     * @param line the line to process
     */
    private void processLine(final String line) {

        final String[] split = line.split(";");
        final String[] padded;

        if (split.length >= LINE_FIELDS) {
            padded = split;
        } else {
            padded = new String[LINE_FIELDS];
            Arrays.fill(padded, split.length, LINE_FIELDS, CoreConstants.EMPTY);
            System.arraycopy(split, 0, padded, 0, split.length);
        }

        final UnicodeCharacter chr = new UnicodeCharacter(padded);
        this.chars.put(Integer.valueOf(chr.codePoint), chr);
    }

    /**
     * Gets the singleton instance, loading the unicode database data file if not already loaded.
     *
     * @return the singleton instance
     */
    public static UnicodeCharacterSet getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {

            if (instance == null) {
                instance = new UnicodeCharacterSet();
            }

            return instance;
        }
    }

    /**
     * Tests whether an integer is a valid Unicode code point.
     *
     * @param codePoint the integer to test
     * @return {@code true} if the integer is a valid Unicode code point
     */
    public boolean isValid(final int codePoint) {

        return this.chars.containsKey(Integer.valueOf(codePoint));
    }

    /**
     * Gets an iterator over the {@code UnicodeCharacter}s in the set.
     *
     * @return the iterator
     */
    public Iterator<UnicodeCharacter> iterator() {

        return this.chars.values().iterator();
    }

    /**
     * Gets the {@code UnicodeCharacter} corresponding to a code point.
     *
     * @param codePoint the code point to test
     * @return the corresponding {@code UnicodeCharacter}, null if no character is mapped to the specified code point
     */
    public UnicodeCharacter getCharacter(final int codePoint) {

        return codePoint < 0 ? null : this.chars.get(Integer.valueOf(codePoint));
    }

    /**
     * Given a Unicode code point, returns code point of the uppercase equivalent. If the code point does not have an
     * uppercase mapping or is not defined, the original code point is returned.
     *
     * @param codePoint the code point
     * @return the uppercase equivalent
     */
    public int toUppercase(final int codePoint) {

        final int result;

        final UnicodeCharacter chr = getCharacter(codePoint);

        if (chr == null || chr.uppercase == null) {
            result = codePoint;
        } else {
            result = chr.uppercase.intValue();
        }

        return result;
    }

    /**
     * Given a Unicode code point, returns code point of the lowercase equivalent. If the code point does not have a
     * lowercase mapping or is not defined, the original code point is returned.
     *
     * @param codePoint the code point
     * @return the lowercase equivalent
     */
    public int toLowercase(final int codePoint) {

        final int result;

        final UnicodeCharacter chr = getCharacter(codePoint);

        if (chr == null || chr.lowercase == null) {
            result = codePoint;
        } else {
            result = chr.lowercase.intValue();
        }

        return result;
    }

    /**
     * Given a Unicode code point, returns code point of the titlecase equivalent. If the code point does not have a
     * titlecase mapping or is not defined, the original code point is returned.
     *
     * @param codePoint the code point
     * @return the lowercase equivalent
     */
    public int toTitlecase(final int codePoint) {

        final int result;

        final UnicodeCharacter chr = getCharacter(codePoint);

        if (chr == null || chr.titlecase == null) {
            result = codePoint;
        } else {
            result = chr.titlecase.intValue();
        }

        return result;
    }
}
