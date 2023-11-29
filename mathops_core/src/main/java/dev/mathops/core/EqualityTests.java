package dev.mathops.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Sorts a map by value.
     *
     * @param map the map to sort
     * @param descending true to sort descending
     * @return the sorted map
     * @param <K> the key type
     * @param <V> the value type
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map,
                                                                             final boolean descending) {
        final List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        if (descending) {
            Collections.reverse(list);
        }

        final Map<K, V> result = new LinkedHashMap<>(list.size());
        for (final Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
