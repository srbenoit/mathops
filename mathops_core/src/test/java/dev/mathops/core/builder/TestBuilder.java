package dev.mathops.core.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code Builder} class.
 */
final class TestBuilder {

    /**
     * A test case.
     */
    @Test
    @DisplayName("Empty builder length")
    void test001() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        assertEquals(0, htm.length(), "Expected length to be zero");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Builder length")
    void test002() {

        final HtmlBuilder htm = new HtmlBuilder(100);
        htm.appendChar('x');

        assertEquals(1, htm.length(), "Expected length to be 1");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Builder charAt(0)")
    void test003() {

        final HtmlBuilder htm = new HtmlBuilder(100);
        htm.appendChar('x');

        assertEquals('x', htm.charAt(0), "Expected charAt(0) to be 'x'");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Builder reset length")
    void test004() {

        final HtmlBuilder htm = new HtmlBuilder(100);
        htm.appendChar('x');
        htm.reset();

        assertEquals(0, htm.length(), "Expected reset length to be zero");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Builder truncated length")
    void test005() {

        final HtmlBuilder htm = new HtmlBuilder(100);
        htm.appendChar('x');
        htm.appendChar('y');
        htm.appendChar('z');
        htm.truncate(2);

        assertEquals(2, htm.length(), "Expected truncated length to be 2");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Builder truncated charAt")
    void test006() {

        final HtmlBuilder htm = new HtmlBuilder(100);
        htm.appendChar('x');
        htm.appendChar('y');
        htm.appendChar('z');
        htm.truncate(1);

        assertEquals('x', htm.charAt(0), "Expected truncated charAt(0) to be 'x'");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Builder toString")
    void test007() {

        final HtmlBuilder htm = new HtmlBuilder(100);
        htm.appendChar('x');
        htm.appendChar('y');
        htm.appendChar('z');

        assertEquals("xyz", htm.toString(), "Expected toString() to be 'xyz'");
    }
}
