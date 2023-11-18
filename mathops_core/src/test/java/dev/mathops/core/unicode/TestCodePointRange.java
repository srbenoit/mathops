package dev.mathops.core.unicode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the CodePointRange class.
 */
final class TestCodePointRange {

    /** Test case. */
    @Test
    @DisplayName("Constructor min value")
    void testConstructor1() {

        final CodePointRange range = new CodePointRange(100, 200);

        assertEquals(range.min, 100, "Constructor 1");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor max value")
    void testConstructor2() {

        final CodePointRange range = new CodePointRange(100, 200);

        assertEquals(range.max, 200, "Constructor 2");
    }

    /** Test case. */
    @Test
    @DisplayName("IsInRange below lower bound")
    void testIsInRange1() {

        final CodePointRange range = new CodePointRange(100, 200);

        assertFalse(range.isInRange(99), "IsInRange 1");
    }

    /** Test case. */
    @Test
    @DisplayName("IsInRange equals lower bound")
    void testIsInRange2() {

        final CodePointRange range = new CodePointRange(100, 200);

        assertTrue(range.isInRange(100), "IsInRange 2");
    }

    /** Test case. */
    @Test
    @DisplayName("IsInRange equals upper bound")
    void testIsInRange3() {

        final CodePointRange range = new CodePointRange(100, 200);

        assertTrue(range.isInRange(200), "IsInRange 3");
    }

    /** Test case. */
    @Test
    @DisplayName("IsInRange above upper bound")
    void testIsInRange4() {

        final CodePointRange range = new CodePointRange(100, 200);

        assertFalse(range.isInRange(201), "IsInRange 4");
    }
}
