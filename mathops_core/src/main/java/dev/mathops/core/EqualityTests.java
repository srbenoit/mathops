package dev.mathops.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Equality tests that supplement those provided by Java.
 */
public enum EqualityTests {
    ;

    /**
     * Tests whether a string is either {@code null} or empty.
     *
     * @param sequence the string to test
     * @return {@code true} if {@code str} is either {@code null} or empty
     */
    public static boolean isNullOrEmpty(final CharSequence sequence) {

        return sequence == null || sequence.isEmpty();
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
        final Set<Map.Entry<K, V>> entries = map.entrySet();
        final List<Map.Entry<K, V>> list = new ArrayList<>(entries);

        final Comparator<Map.Entry<K, V>> comparator = Map.Entry.comparingByValue();
        list.sort(comparator);

        if (descending) {
            Collections.reverse(list);
        }

        final int count = list.size();
        final Map<K, V> result = new LinkedHashMap<>(count);
        for (final Map.Entry<K, V> entry : list) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            result.put(key, value);
        }

        return result;
    }
}
