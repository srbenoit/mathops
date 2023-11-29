package dev.mathops.core.unicode;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.CoreFileLoader;
import dev.mathops.core.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Performs lookups of Unicode block names based on the version 8.0.0 "Blocks.txt" file from the Unicode Character
 * Database.
 */
public final class UnicodeBlocks {

    /** The name of the file to read. */
    private static final String FILENAME = "Blocks.txt";

    /** The number of named Unicode blocks. */
    private static final int NUM_BLOCKS = 222;

    /** The singleton instance. */
    private static UnicodeBlocks instance;

    /** A map from normalized block name to code point range. */
    private final Map<String, CodePointRange> blocks;

    /** A map from normalized block name to non-normalized name. */
    private final Map<String, String> names;

    /** A map from partially normalized name (without spaces) to normalized name. */
    private final Map<String, String> noSpaceNames;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private UnicodeBlocks() {

        this.blocks = new HashMap<>(NUM_BLOCKS);
        this.names = new HashMap<>(NUM_BLOCKS);
        this.noSpaceNames = new HashMap<>(NUM_BLOCKS);

        loadBlocksFile();
    }

    /**
     * Attempts to load and populate the blocks map by reading the Blocks.txt file, which should be from the latest
     * Unicode character database.
     */
    private void loadBlocksFile() {

        final String[] lines = CoreFileLoader.loadFileAsLines(getClass(), FILENAME, true);
        try {
            for (String line : lines) {
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    processLine(line);
                }
            }
        } catch (NumberFormatException ex) {
            Log.warning("Invalid block specification in ", FILENAME, ex);
            this.blocks.clear();
        }
    }

    /**
     * Processes a single line of the input blocks file, after it has been discovered that the line is not a comment or
     * empty.
     *
     * @param line the line to process
     */
    private void processLine(final String line) {

        final int semi = line.indexOf(';');
        final int dots = line.indexOf("..");

        if (semi != -1 && dots != -1) {
            final int first = Integer.parseInt(line.substring(0, dots), 16);
            final int last = Integer.parseInt(line.substring(dots + 2, semi), 16);
            final String name = line.substring(semi + 2);
            final String normalized = normalizeBlockName(name);

            this.blocks.put(normalized, new CodePointRange(first, last));
            this.names.put(normalized, name);
            this.noSpaceNames.put(stripSpaces(name), normalized);
        }
    }

    /**
     * Normalizes a block name by converting to lowercase and removing all whitespace, hyphens, and underbars.
     *
     * @param name the name to normalize
     * @return the normalized name
     */
    public static String normalizeBlockName(final CharSequence name) {

        final int len = name.length();
        final HtmlBuilder str = new HtmlBuilder(len);

        for (int i = 0; i < len; ++i) {
            final char chr = name.charAt(i);

            if (chr == '-' || chr == '_' || chr == ' ' || chr == '\t') {
                continue;
            }

            str.add(Character.toLowerCase(chr));
        }

        return str.toString();
    }

    /**
     * Partially normalizes a block name by removing all whitespaces (used in XML Schema regular expression block name
     * matching). Case is not affected.
     *
     * @param name the name to normalize
     * @return the partially normalized name
     */
    public static String stripSpaces(final CharSequence name) {

        final int len = name.length();
        final HtmlBuilder str = new HtmlBuilder(len);

        for (int i = 0; i < len; ++i) {
            final char chr = name.charAt(i);

            if (chr == ' ' || chr == '\t') {
                continue;
            }

            str.add(chr);
        }

        return str.toString();
    }

    /**
     * Gets the singleton instance, loading the blocks file if not already loaded.
     *
     * @return the singleton instance
     */
    public static UnicodeBlocks getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {

            if (instance == null) {
                instance = new UnicodeBlocks();
            }

            return instance;
        }
    }

    /**
     * Tests whether a code point falls within the code point range specified for a block.
     *
     * @param codePoint the code point to test
     * @param name      the normalized or no-space block name
     * @return {@code true} if the block name was found and the character is in the block
     */
    public boolean isInBlock(final int codePoint, final String name) {

        CodePointRange range = this.blocks.get(name);

        if (range == null) {
            final String normalized = this.noSpaceNames.get(name);
            if (normalized != null) {
                range = this.blocks.get(normalized);
            }
        }

        return range != null && range.isInRange(codePoint);
    }

    /**
     * Tests whether string is a normalized block name.
     *
     * @param noSpaceName the normalized block name
     * @return {@code true} if the block name was found
     */
    public boolean isValidNoSpaceName(final String noSpaceName) {

        return this.noSpaceNames.containsKey(noSpaceName);
    }
}
