package dev.mathops.assessment;

// MOVE TO Mathemetric "math" library

/**
 * A utility class that can parse a number from a string, and that supports the {@code Irrational} numeric type.
 *
 * <p>
 * Supported formats include:
 *
 * <pre>
 * 12345        Integers - parsed using "Long.valueOf()"
 * 123.456      Doubles  - parsed using "Double.valueOf()"
 * 3PI/4        Any string with "PI" is treated as a fraction with PI in the numerator.  There can
 *              be an integer coefficient before PI, and optionally a denominator made up of a
 *              slash character followed by an integer.  No characters between the PI and slash are
 *              allowed.
 * 3E/4         Same format as above, but with the constant E rather than PI.
 * 3R2/2        Any string with "R" is treated as a rational multiple of the square root of an
 *              integer.  The format has an optional integer coefficient before the R, a required
 *              integer after the R (whose square root is to be taken), and an optional denominator
 *              consisting of a slash character then an integer.
 * </pre>
 */
public enum NumberParser {
    ;

    /**
     * Attempts to parse a number from a string.
     *
     * @param str the string
     * @return the number (a Long, Double, or Irrational)
     * @throws NumberFormatException if the string is not in a valid format
     */
    public static Number parse(final String str) throws NumberFormatException {

        Number result;

        final int slash = str.indexOf('/');
        if (slash == -1) {
            try {
                result = Long.valueOf(str);
            } catch (final NumberFormatException ex1) {
                try {
                    result = Double.valueOf(str);
                } catch (final NumberFormatException ex2) {
                    final String toParse = str.replace("\u03C0", "PI");
                    result = Irrational.valueOf(toParse);
                }
            }
        } else {
            final String left = str.substring(0, slash).trim();
            final String right = str.substring(slash + 1).trim();

            boolean isRational;
            long numer = 0L;
            long denom = 0L;
            try {
                numer = Long.parseLong(left);
                denom = Long.parseLong(right);
                isRational = true;
            } catch (final NumberFormatException ex2) {
                isRational = false;
            }

            if (isRational) {
                if (denom == 0L) {
                    throw new NumberFormatException("Division by zero");
                }
                if (denom == 1L) {
                    result = Long.valueOf(numer);
                } else {
                    result = Double.valueOf((double) numer / (double) denom);
                }
            } else {
                final String toParse = str.replace("\u03C0", "PI");
                result = Irrational.valueOf(toParse);
            }
        }

        return result;
    }
}
