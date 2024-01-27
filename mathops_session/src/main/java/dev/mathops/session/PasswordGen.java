package dev.mathops.session;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

/**
 * Generates random passwords using characters and digits (no special characters). It attempts to choose consonant-vowel
 * combinations that will be relatively easy to remember. We do not use lower case L, uppercase I,the number 1,
 * uppercase O or the number 0 to avoid confusion.
 * <p>
 * 8-character passwords are out of 6,543,192,100 possibilities, 12-character passwords are out of 5.29E14
 * possibilities.
 */
public enum PasswordGen {
    ;

    /** Uppercase consonants. */
    private static final String UPPER_CONS = "BCFGHJKLMNPQRSTVWXZ";

    /** Lowercase consonants. */
    private static final String LOWER_CONS = "bcdfghjkmnpqrstvwxz";

    /** Uppercase vowels. */
    private static final String UPPER_VOWEL = "AEU";

    /** Lowercase vowels. */
    private static final String LOWER_VOWEL = "aeiou";

    /** Digits. */
    private static final String DIGITS = "23456789";

    /** Possible password patterns (81890 possible 4-char groups). */
    private static final String[] HALF_PATTERNS = {//
            "Cvc#", "Vcv#",
            "Cvcv", "Vcvc",
            "Vccv", "cvc#",
            "vcv#", "cvcv",
            "vcvc", "vccv",};

    /**
     * Generates some number of passwords.
     *
     * @param count the number to generate
     * @return the array of passwords
     */
    private static String[] genPasswords(final int count) {

        Random rnd;

        try {
            rnd = SecureRandom.getInstance("SHA1PRNG");
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
            rnd = new Random(System.currentTimeMillis());
        }

        final String[] result = new String[count];
        final HtmlBuilder builder = new HtmlBuilder(10);

        for (int i = 0; i < count; ++i) {
            appendFour(HALF_PATTERNS[rnd.nextInt(HALF_PATTERNS.length)], builder, rnd);
            appendFour(HALF_PATTERNS[rnd.nextInt(HALF_PATTERNS.length)], builder, rnd);
            if (rnd.nextBoolean()) {
                appendFour(HALF_PATTERNS[rnd.nextInt(HALF_PATTERNS.length)], builder, rnd);
            }
            result[i] = builder.toString();
            builder.reset();
        }

        return result;
    }

    /**
     * Appends four characters based on a pattern.
     *
     * @param pattern the pattern
     * @param builder     the {@code HtmlBuilder} to which to append
     * @param rnd     the random number generator
     */
    private static void appendFour(final String pattern, final HtmlBuilder builder, final RandomGenerator rnd) {

        final char[] chars = pattern.toCharArray();

        for (final char ch : chars) {
            if (ch == 'C') {
                builder.add(UPPER_CONS.charAt(rnd.nextInt(UPPER_CONS.length())));
            } else if (ch == 'c') {
                builder.add(LOWER_CONS.charAt(rnd.nextInt(LOWER_CONS.length())));
            } else if (ch == 'V') {
                builder.add(UPPER_VOWEL.charAt(rnd.nextInt(UPPER_VOWEL.length())));
            } else if (ch == 'v') {
                builder.add(LOWER_VOWEL.charAt(rnd.nextInt(LOWER_VOWEL.length())));
            } else if (ch == '#') {
                builder.add(DIGITS.charAt(rnd.nextInt(DIGITS.length())));
            }
        }
    }

    /**
     * Main method that generates several passwords.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        Log.info("*** WARNING: screen for embedded inappropriate words before use ***");
        final String[] pwds = genPasswords(5);
        for (final String pwd : pwds) {
            Log.info(pwd);
        }
    }
}
