package dev.mathops.core.unicode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the EDecompMapping class.
 */
final class TestDecompMapping {

    /** Test case. */
    @Test
    @DisplayName("font tag")
    void testGetTag() {

        assertEquals("<font>", EDecompMapping.FONT.tag, "getTag");
    }

    /** Test case. */
    @Test
    @DisplayName("font description")
    void testGetDescription() {

        assertEquals("A font variant (e.g. a blackletter form).", EDecompMapping.FONT.description, "getDescription");
    }
}
