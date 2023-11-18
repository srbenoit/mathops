package dev.mathops.core;

/**
 * Equality tests that supplement those provided by Java.
 */
public enum EqualityTests {
    ;

    /**
     * Tests whether a string is either {@code null} or empty.
     *
     * @param str the string to test
     * @return {@code true} if {@code str} is either {@code null} or empty
     */
    public static boolean isNullOrEmpty(final CharSequence str) {

        return str == null || str.isEmpty();
    }

    /**
     * Computes a hash code for an object that could be {@code null}.
     *
     * @param obj the object
     * @return the hash code of the object, or 0 if the object is {@code null}
     */
    public static int objectHashCode(final Object obj) {

        return obj == null ? 0 : obj.hashCode();
    }
}
