package dev.mathops.core.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code SimpleBuilder} class.
 */
final class TestSimpleBuilder {

    /**
     * A test case.
     */
    @Test
    @DisplayName("String value after concat()")
    void test001() {

        final String str = SimpleBuilder.concat("A", Integer.valueOf(1), Character.valueOf('B'), Double.valueOf(2.0),
                Boolean.TRUE, Boolean.FALSE, null, "foo".toCharArray());

        assertEquals("A1B2.0truefalsefoo", str, "Expected concat() to be 'A1B2.0truefalsefoo' (" + str + ")");
    }
}
