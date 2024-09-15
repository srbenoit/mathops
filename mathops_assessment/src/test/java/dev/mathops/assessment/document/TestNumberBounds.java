package dev.mathops.assessment.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests for the {@code NumberBounds} class.
 */
final class TestNumberBounds {

    /**
     * Constructs a new {@code TestNumberBounds}
     */
    TestNumberBounds() {

        //  No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor and Accessors")
    void test0101() {

        final Double leftX = Double.valueOf(-2.0 * Math.PI);
        final Double rightX = Double.valueOf(Math.PI * 0.5);
        final Double bottomY = Double.valueOf(-1.25);
        final Double topY = Double.valueOf(1.75);
        final NumberBounds bounds = new NumberBounds(leftX, rightX, bottomY, topY);

        final Number objLeftX = bounds.getLeftX();
        final Number objRightX = bounds.getRightX();
        final Number objBottomY = bounds.getBottomY();
        final Number objTopY = bounds.getTopY();

        assertEquals(leftX, objLeftX, "Object has incorrect left X");
        assertEquals(rightX, objRightX, "Object has incorrect right X");
        assertEquals(bottomY, objBottomY, "Object has incorrect bottom Y");
        assertEquals(topY, objTopY, "Object has incorrect top Y");
    }

    /** Test case. */
    @Test
    @DisplayName("Hash code and Equals")
    void test0102() {

        final Double leftX1 = Double.valueOf(-2.0 * Math.PI);
        final Double rightX1 = Double.valueOf(Math.PI * 0.5);
        final Double bottomY1 = Double.valueOf(-1.25);
        final Double topY1 = Double.valueOf(1.75);
        final NumberBounds bounds1 = new NumberBounds(leftX1, rightX1, bottomY1, topY1);
        final int hash1 = bounds1.hashCode();

        final Double leftX2 = Double.valueOf(-2.0 * Math.PI);
        final Double rightX2 = Double.valueOf(Math.PI * 0.5);
        final Double bottomY2 = Double.valueOf(-1.25);
        final Double topY2 = Double.valueOf(1.75);
        final NumberBounds bounds2 = new NumberBounds(leftX2, rightX2, bottomY2, topY2);
        final int hash2 = bounds2.hashCode();

        assertEquals(bounds1, bounds2, "Object with same coordinates are not equal");
        assertEquals(hash1, hash2, "Object with same coordinates have different hash codes");

        final Double leftX3 = Double.valueOf(-3.0 * Math.PI);
        final Double rightX3 = Double.valueOf(Math.PI * 0.5);
        final Double bottomY3 = Double.valueOf(-1.25);
        final Double topY3 = Double.valueOf(1.75);
        final NumberBounds bounds3 = new NumberBounds(leftX3, rightX3, bottomY3, topY3);
        final int hash3 = bounds3.hashCode();

        assertNotEquals(bounds1, bounds3, "Object with different left X are equal");
        assertNotEquals(hash1, hash3, "Object with different left X have same hash codes");

        final Double leftX4 = Double.valueOf(-2.0 * Math.PI);
        final Double rightX4 = Double.valueOf(Math.PI * 0.6);
        final Double bottomY4 = Double.valueOf(-1.25);
        final Double topY4 = Double.valueOf(1.75);
        final NumberBounds bounds4 = new NumberBounds(leftX4, rightX4, bottomY4, topY4);
        final int hash4 = bounds4.hashCode();

        assertNotEquals(bounds1, bounds4, "Object with different right X are equal");
        assertNotEquals(hash1, hash4, "Object with different right X have same hash codes");

        final Double leftX5 = Double.valueOf(-2.0 * Math.PI);
        final Double rightX5 = Double.valueOf(Math.PI * 0.5);
        final Double bottomY5 = Double.valueOf(-1.2);
        final Double topY5 = Double.valueOf(1.75);
        final NumberBounds bounds5 = new NumberBounds(leftX5, rightX5, bottomY5, topY5);
        final int hash5 = bounds5.hashCode();

        assertNotEquals(bounds1, bounds5, "Object with different bottom Y are equal");
        assertNotEquals(hash1, hash5, "Object with different bottom Y have same hash codes");

        final Double leftX6 = Double.valueOf(-2.0 * Math.PI);
        final Double rightX6 = Double.valueOf(Math.PI * 0.5);
        final Double bottomY6 = Double.valueOf(-1.25);
        final Double topY6 = Double.valueOf(1.7);
        final NumberBounds bounds6 = new NumberBounds(leftX6, rightX6, bottomY6, topY6);
        final int hash6 = bounds6.hashCode();

        assertNotEquals(bounds1, bounds6, "Object with different top Y are equal");
        assertNotEquals(hash1, hash6, "Object with different top Y have same hash codes");
    }
}
