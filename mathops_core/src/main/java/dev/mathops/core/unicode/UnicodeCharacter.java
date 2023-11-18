package dev.mathops.core.unicode;

import dev.mathops.core.CoreConstants;

/**
 * A unicode character encoding, consisting of a code point, character name, general category, canonical combining
 * classes, bidirectional category, character decomposition mapping, decimal digit value, digit value, numeric value,
 * mirrored, Unicode 1.0 name, 10646 comment field, uppercase mapping, lowercase mapping, and titlecase mapping.
 */
public final class UnicodeCharacter {

    /** Radix for parsing hexadecimal. */
    private static final int HEX_RADIX = 16;

    /** String used to indicate true. */
    private static final String TRUE_STR = "Y";

    /** A field index into the string array from UnicodeData.txt. */
    private static final int CODE_POINT_IDX = 0;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int NAME_IDX = 1;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int CATEGORY_IDX = 2;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int COMBINING_IDX = 3;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int BIDI_CATEGORY_IDX = 4;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int DECOMP_MAPPING_IDX = 5;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int DECIMAL_IDX = 6;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int DIGIT_IDX = 7;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int NUMERIC_IDX = 8;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int MIRRORED_IDX = 9;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int OLD_NAME_IDX = 10;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int COMMENT_IDX = 11;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int UPPERCASE_IDX = 12;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int LOWERCASE_IDX = 13;

    /** A field index into the string array from UnicodeData.txt. */
    private static final int TITLECASE_IDX = 14;

    /** The Unicode code point. */
    public final int codePoint;

    /** The name published in Chapter 14 of the Unicode Standard, Version 3.0. */
    public final String name;

    /**
     * A breakdown into various "character types" which can be used as a default categorization in implementations.
     */
    public final String category;

    /** The class used for the Canonical Ordering Algorithm in the Unicode Standard. */
    public final Integer combining;

    /** Category required by the Bidirectional Behavior Algorithm in the Unicode Standard. */
    public final String bidiCategory;

    /**
     * The tags supplied with certain decomposition mappings generally indicate formatting information. Where no such
     * tag is given, the mapping is designated as canonical. Conversely, the presence of a formatting tag also indicates
     * that the mapping is a compatibility mapping and not a canonical mapping. In the absence of other formatting
     * information in a compatibility mapping, the tag is used to distinguish it from canonical mappings.
     */
    public final EDecompMapping decompMapping;

    /**
     * If the character has the decimal digit property, the value of that digit is represented with an integer value in
     * this field.
     */
    public final Integer decimal;

    /**
     * If the character represents a digit, not necessarily a decimal digit, the value is here. This covers digits which
     * do not form decimal radix forms, such as the compatibility superscript digits.
     */
    public final Integer digit;

    /**
     * If the character has the numeric property, the value of that character is represented with a rational number in
     * this field. This includes fractions as, e.g., "1/5" for U+2155 VULGAR FRACTION ONE FIFTH Also included are
     * numerical values for compatibility characters such as circled numbers.
     */
    public final Double numeric;

    /**
     * True if the character has been identified as a "mirrored" character in bidirectional text.
     */
    public final Boolean mirrored;

    /**
     * The old name as published in Unicode 1.0. This name is only provided when it is significantly different from the
     * Unicode 3.0 name for the character.
     */
    public final String oldName;

    /** The ISO 10646 comment field. It is in parentheses in the 10646 names list. */
    public final String comment;

    /**
     * Upper case equivalent mapping. If a character is part of an alphabet with case distinctions, and has an upper
     * case equivalent, then the upper case equivalent is in this field.
     */
    public final Integer uppercase;

    /**
     * Lower case equivalent mapping. If a character is part of an alphabet with case distinctions, and has a lower case
     * equivalent, then the lower case equivalent is in this field.
     */
    public final Integer lowercase;

    /**
     * Title case equivalent mapping. If a character is part of an alphabet with case distinctions, and has a title case
     * equivalent, then the title case equivalent is in this field.
     */
    public final Integer titlecase;

    /** The code points associated with a decomposition mapping. */
    private final Integer[] decompMappingCodePoints;

    /**
     * Constructs a new {@code UnicodeCharacter}.
     *
     * @param fields the list of non-{@code null} string fields extracted from {@code UnicodeData.txt}
     * @throws NumberFormatException if a number field cannot be parsed
     */
    public UnicodeCharacter(final String... fields) throws NumberFormatException {

        this.codePoint = Integer.parseInt(fields[CODE_POINT_IDX], HEX_RADIX);
        this.name = fields[NAME_IDX];
        this.category = fields[CATEGORY_IDX];

        this.combining =
                fields[COMBINING_IDX].isEmpty() ? null : Integer.valueOf(fields[COMBINING_IDX]);

        this.bidiCategory = fields[BIDI_CATEGORY_IDX];

        if (fields[DECOMP_MAPPING_IDX].isEmpty()) {
            this.decompMapping = null;
            this.decompMappingCodePoints = null;
        } else {
            final String[] split = fields[DECOMP_MAPPING_IDX].split(CoreConstants.SPC);
            final int splitLen = split.length;
            this.decompMapping = EDecompMapping.forTag(split[0]);
            final int start = this.decompMapping == null ? 0 : 1;
            this.decompMappingCodePoints = new Integer[splitLen - start];
            for (int i = start; i < splitLen; ++i) {
                this.decompMappingCodePoints[i - start] = Integer.valueOf(split[i], HEX_RADIX);
            }
        }

        this.decimal = fields[DECIMAL_IDX].isEmpty() ? null : Integer.valueOf(fields[DECIMAL_IDX]);

        this.digit = fields[DIGIT_IDX].isEmpty() ? null : Integer.valueOf(fields[DIGIT_IDX]);

        this.numeric = parseDouble(fields[NUMERIC_IDX]);
        this.mirrored = Boolean.valueOf(TRUE_STR.equals(fields[MIRRORED_IDX]));
        this.oldName = fields[OLD_NAME_IDX];
        this.comment = fields[COMMENT_IDX];
        this.uppercase = parseInteger(fields[UPPERCASE_IDX]);
        this.lowercase = parseInteger(fields[LOWERCASE_IDX]);
        this.titlecase = parseInteger(fields[TITLECASE_IDX]);
    }

    /**
     * Parses a double from a string.
     *
     * @param str the string to parse (could be an integer, or may include a '/' between an integer numerator and
     *            positive integer denominator)
     * @return the parsed {@code RationalNumber}, null if the input string is empty
     * @throws NumberFormatException if parsing fails
     */
    private static Double parseDouble(final String str) {

        Double result = null;

        if (!str.isEmpty()) {
            final int slash = str.indexOf('/');
            final double numer;
            final double denom;
            if (slash == -1) {
                numer = Double.parseDouble(str);
                denom = 1.0;
            } else {
                denom = Double.parseDouble(str.substring(slash + 1));
                numer = Double.parseDouble(str.substring(0, slash));
            }

            result = Double.valueOf(numer / denom);
        }

        return result;
    }

    /**
     * Parses an integer from a string with its hexadecimal representation.
     *
     * @param str the string to parse
     * @return the parsed {@code Integer}, null if the input string is empty
     * @throws NumberFormatException if parsing fails
     */
    private static Integer parseInteger(final String str) {

        return str.isEmpty() ? null : Integer.valueOf(str, HEX_RADIX);
    }

    /**
     * Gets the decomposition mapping code point.
     *
     * @return the code point
     */
    public Integer[] getDecompMappingCodePoints() {

        return this.decompMappingCodePoints == null ? null : this.decompMappingCodePoints.clone();
    }

    /**
     * Gets the character as a Java string.
     *
     * @return the Java string
     */
    public String stringValue() {

        return new String(new int[]{this.codePoint}, 0, 1);
    }
}
