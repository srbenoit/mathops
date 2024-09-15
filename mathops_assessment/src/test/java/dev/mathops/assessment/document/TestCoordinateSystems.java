package dev.mathops.assessment.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the {@code CoordinateSystems} class.
 */
final class TestCoordinateSystems {

    /** A small number used when comparing double values for equality. */
    private static final double EPSILON = 0.0000001;

    /**
     * Constructs a new {@code TestCoordinateSystems}
     */
    TestCoordinateSystems() {

        //  No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor and Accessors - graph coordinates and border")
    void test0101() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final Double graphLeftX = Double.valueOf(-2.0 * Math.PI);
        final Double graphRightX = Double.valueOf(Math.PI * 0.5);
        final Double graphBottomY = Double.valueOf(-1.25);
        final Double graphTopY = Double.valueOf(1.75);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth,
                graphBounds);

        final Number objPixelSpaceWidth = systems.getPixelSpaceWidth();
        final Number objPixelSpaceHeight = systems.getPixelSpaceHeight();
        final Number objBorderWidth = systems.getBorderWidth();
        final NumberBounds objGraphSpaceBounds = systems.getGraphSpaceBounds();

        assertEquals(pixelSpaceWidth, objPixelSpaceWidth, "Object has incorrect pixel-space width");
        assertEquals(pixelSpaceHeight, objPixelSpaceHeight, "Object has incorrect pixel-space height");
        assertEquals(borderWidth, objBorderWidth, "Object has incorrect border width");
        assertEquals(graphBounds, objGraphSpaceBounds, "Object has incorrect graph-space bounds");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor and Accessors - graph coordinates and no border")
    void test0102() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);

        final Double graphLeftX = Double.valueOf(-2.0 * Math.PI);
        final Double graphRightX = Double.valueOf(Math.PI * 0.5);
        final Double graphBottomY = Double.valueOf(-1.25);
        final Double graphTopY = Double.valueOf(1.75);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, null, graphBounds);

        final Number objPixelSpaceWidth = systems.getPixelSpaceWidth();
        final Number objPixelSpaceHeight = systems.getPixelSpaceHeight();
        final Number objBorderWidth = systems.getBorderWidth();
        final NumberBounds objGraphSpaceBounds = systems.getGraphSpaceBounds();

        assertEquals(pixelSpaceWidth, objPixelSpaceWidth, "Object has incorrect pixel-space width");
        assertEquals(pixelSpaceHeight, objPixelSpaceHeight, "Object has incorrect pixel-space height");
        assertNull(objBorderWidth, "Object has non-null border width");
        assertEquals(graphBounds, objGraphSpaceBounds, "Object has incorrect graph-space bounds");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor and Accessors - border, but no graph coordinates")
    void test0103() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth);

        final Number objPixelSpaceWidth = systems.getPixelSpaceWidth();
        final Number objPixelSpaceHeight = systems.getPixelSpaceHeight();
        final Number objBorderWidth = systems.getBorderWidth();
        final NumberBounds objGraphSpaceBounds = systems.getGraphSpaceBounds();

        final Double graphLeftXTopY = Double.valueOf(11.0);
        final Double graphRightX = Double.valueOf(1000.0 - 11.0);
        final Double graphBottomY = Double.valueOf(700.0 - 11.0);
        final NumberBounds expectBounds = new NumberBounds(graphLeftXTopY, graphRightX, graphBottomY, graphLeftXTopY);

        assertEquals(pixelSpaceWidth, objPixelSpaceWidth, "Object has incorrect pixel-space width");
        assertEquals(pixelSpaceHeight, objPixelSpaceHeight, "Object has incorrect pixel-space height");
        assertEquals(borderWidth, objBorderWidth, "Object has incorrect border width");
        assertEquals(expectBounds, objGraphSpaceBounds, "Object has incorrect graph-space bounds");
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor and Accessors - no border or graph coordinates")
    void test0104() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, null);

        final Number objPixelSpaceWidth = systems.getPixelSpaceWidth();
        final Number objPixelSpaceHeight = systems.getPixelSpaceHeight();
        final Number objBorderWidth = systems.getBorderWidth();
        final NumberBounds objGraphSpaceBounds = systems.getGraphSpaceBounds();

        final Double graphLeftXTopY = Double.valueOf(0.0);
        final Double graphRightX = Double.valueOf(1000.0);
        final Double graphBottomY = Double.valueOf(700.0);
        final NumberBounds expectBounds = new NumberBounds(graphLeftXTopY, graphRightX, graphBottomY, graphLeftXTopY);

        assertEquals(pixelSpaceWidth, objPixelSpaceWidth, "Object has incorrect pixel-space width");
        assertEquals(pixelSpaceHeight, objPixelSpaceHeight, "Object has incorrect pixel-space height");
        assertNull(objBorderWidth, "Object has non-null border width");
        assertEquals(expectBounds, objGraphSpaceBounds, "Object has incorrect graph-space bounds");
    }

    /** Test case. */
    @Test
    @DisplayName("Converting graph X coordinates to pixel X coordinates")
    void test0201() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final double minX = -2.0 * Math.PI;
        final double maxX = Math.PI * 0.5;

        final Double graphLeftX = Double.valueOf(minX);
        final Double graphRightX = Double.valueOf(maxX);
        final Double graphBottomY = Double.valueOf(-1.25);
        final Double graphTopY = Double.valueOf(1.75);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth,
                graphBounds);

        final double pixel1 = systems.graphXToPixelX(minX);
        final double pixel2 = systems.graphXToPixelX(maxX);
        final double pixel3 = systems.graphXToPixelX(minX * 0.75 + maxX * 0.25);
        final double pixel4 = systems.graphXToPixelX(minX * 0.5 + maxX * 0.5);
        final double pixel5 = systems.graphXToPixelX(minX * 0.25 + maxX * 0.75);

        final double pix1 = 11.0;
        final double pix2 = 1000.0 - 11.0;
        final double pix3 = pix1 * 0.75 + pix2 * 0.25;
        final double pix4 = pix1 * 0.5 + pix2 * 0.5;
        final double pix5 = pix1 * 0.25 + pix2 * 0.75;

        assertEquals(pix1, pixel1, EPSILON, "Graph coordinate at left edge converted incorrectly to pixel X");
        assertEquals(pix2, pixel2, EPSILON, "Graph coordinate at right edge converted incorrectly to pixel X");
        assertEquals(pix3, pixel3, EPSILON, "Graph coordinate at 25% converted incorrectly to pixel X");
        assertEquals(pix4, pixel4, EPSILON, "Graph coordinate at 50% converted incorrectly to pixel X");
        assertEquals(pix5, pixel5, EPSILON, "Graph coordinate at 75% converted incorrectly to pixel X");
    }

    /** Test case. */
    @Test
    @DisplayName("Converting graph Y coordinates to pixel Y coordinates")
    void test0202() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final double minY = -1.25;
        final double maxY = 1.75;

        final Double graphLeftX = Double.valueOf(-2.0 * Math.PI);
        final Double graphRightX = Double.valueOf(Math.PI * 0.5);
        final Double graphBottomY = Double.valueOf(minY);
        final Double graphTopY = Double.valueOf(maxY);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth,
                graphBounds);

        final double pixel1 = systems.graphYToPixelY(minY);
        final double pixel2 = systems.graphYToPixelY(maxY);
        final double pixel3 = systems.graphYToPixelY(minY * 0.75 + maxY * 0.25);
        final double pixel4 = systems.graphYToPixelY(minY * 0.5 + maxY * 0.5);
        final double pixel5 = systems.graphYToPixelY(minY * 0.25 + maxY * 0.75);

        final double pix1 = 700.0 - 11.0;
        final double pix2 = 11.0;
        final double pix3 = pix1 * 0.75 + pix2 * 0.25;
        final double pix4 = pix1 * 0.5 + pix2 * 0.5;
        final double pix5 = pix1 * 0.25 + pix2 * 0.75;

        assertEquals(pix1, pixel1, EPSILON, "Graph coordinate at bottom edge converted incorrectly to pixel Y");
        assertEquals(pix2, pixel2, EPSILON, "Graph coordinate at top edge converted incorrectly to pixel Y");
        assertEquals(pix3, pixel3, EPSILON, "Graph coordinate at 25% converted incorrectly to pixel Y");
        assertEquals(pix4, pixel4, EPSILON, "Graph coordinate at 50% converted incorrectly to pixel Y");
        assertEquals(pix5, pixel5, EPSILON, "Graph coordinate at 75% converted incorrectly to pixel Y");
    }

    /** Test case. */
    @Test
    @DisplayName("Converting graph width to pixel width")
    void test0203() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final double minX = -2.0 * Math.PI;
        final double maxX = Math.PI * 0.5;

        final Double graphLeftX = Double.valueOf(minX);
        final Double graphRightX = Double.valueOf(maxX);
        final Double graphBottomY = Double.valueOf(-1.25);
        final Double graphTopY = Double.valueOf(1.75);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth,
                graphBounds);

        final double minPix = 11.0;
        final double maxPix = 1000.0 - 11.0;

        final double graphWidth1 = maxX - minX;
        final double converted1 = systems.graphWidthToPixelWidth(graphWidth1);
        final double pixWidth1 = maxPix - minPix;

        final double converted2 = systems.graphWidthToPixelWidth(graphWidth1 * 0.75);
        final double pixWidth2 = pixWidth1 * 0.75;

        final double converted3 = systems.graphWidthToPixelWidth(graphWidth1 * 0.5);
        final double pixWidth3 = pixWidth1 * 0.5;

        final double converted4 = systems.graphWidthToPixelWidth(graphWidth1 * 0.25);
        final double pixWidth4 = pixWidth1 * 0.25;

        final double converted5 = systems.graphWidthToPixelWidth(0.0);

        assertEquals(pixWidth1, converted1, EPSILON, "Graph 100% width converted incorrectly to pixel width");
        assertEquals(pixWidth2, converted2, EPSILON, "Graph 75% width converted incorrectly to pixel width");
        assertEquals(pixWidth3, converted3, EPSILON, "Graph 50% width converted incorrectly to pixel width");
        assertEquals(pixWidth4, converted4, EPSILON, "Graph 25% width converted incorrectly to pixel width");
        assertEquals(0.0, converted5, EPSILON, "Graph 0% width converted incorrectly to pixel width");
    }

    /** Test case. */
    @Test
    @DisplayName("Converting graph height to pixel height")
    void test0204() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final double minY = -1.25;
        final double maxY = 1.75;

        final Double graphLeftX = Double.valueOf(-2.0 * Math.PI);
        final Double graphRightX = Double.valueOf(Math.PI * 0.5);
        final Double graphBottomY = Double.valueOf(minY);
        final Double graphTopY = Double.valueOf(maxY);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth,
                graphBounds);

        final double minPix = 11.0;
        final double maxPix = 700.0 - 11.0;

        final double graphHeight1 = maxY - minY;
        final double converted1 = systems.graphHeightToPixelHeight(graphHeight1);
        final double pixHeight1 = maxPix - minPix;

        final double converted2 = systems.graphHeightToPixelHeight(graphHeight1 * 0.75);
        final double pixWidth2 = pixHeight1 * 0.75;

        final double converted3 = systems.graphHeightToPixelHeight(graphHeight1 * 0.5);
        final double pixWidth3 = pixHeight1 * 0.5;

        final double converted4 = systems.graphHeightToPixelHeight(graphHeight1 * 0.25);
        final double pixWidth4 = pixHeight1 * 0.25;

        final double converted5 = systems.graphHeightToPixelHeight(0.0);

        assertEquals(pixHeight1, converted1, EPSILON, "Graph 100% height converted incorrectly to pixel height");
        assertEquals(pixWidth2, converted2, EPSILON, "Graph 75% height converted incorrectly to pixel height");
        assertEquals(pixWidth3, converted3, EPSILON, "Graph 50% height converted incorrectly to pixel height");
        assertEquals(pixWidth4, converted4, EPSILON, "Graph 25% height converted incorrectly to pixel height");
        assertEquals(0.0, converted5, EPSILON, "Graph 0% height converted incorrectly to pixel height");
    }

    /** Test case. */
    @Test
    @DisplayName("Converting pixel X coordinates to graph X coordinates")
    void test0301() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final double minX = -2.0 * Math.PI;
        final double maxX = Math.PI * 0.5;

        final Double graphLeftX = Double.valueOf(minX);
        final Double graphRightX = Double.valueOf(maxX);
        final Double graphBottomY = Double.valueOf(-1.25);
        final Double graphTopY = Double.valueOf(1.75);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth,
                graphBounds);

        final double pix1 = 11.0;
        final double pix2 = 1000.0 - 11.0;

        final double graph1 = systems.pixelXToGraphX(pix1);
        final double graph2 = systems.pixelXToGraphX(pix2);
        final double graph3 = systems.pixelXToGraphX(pix1 * 0.75 + pix2 * 0.25);
        final double graph4 = systems.pixelXToGraphX(pix1 * 0.5 + pix2 * 0.5);
        final double graph5 = systems.pixelXToGraphX(pix1 * 0.25 + pix2 * 0.75);

        final double expect3 = minX * 0.75 + maxX * 0.25;
        final double expect4 = minX * 0.5 + maxX * 0.5;
        final double expect5 = minX * 0.25 + maxX * 0.75;

        assertEquals(minX, graph1, EPSILON, "Pixel coordinate at left edge converted incorrectly to graph X");
        assertEquals(maxX, graph2, EPSILON, "Pixel coordinate at right edge converted incorrectly to graph X");
        assertEquals(expect3, graph3, EPSILON, "Pixel coordinate at 25% converted incorrectly to graph X");
        assertEquals(expect4, graph4, EPSILON, "Pixel coordinate at 50% converted incorrectly to graph X");
        assertEquals(expect5, graph5, EPSILON, "Pixel coordinate at 75% converted incorrectly to graph X");
    }

    /** Test case. */
    @Test
    @DisplayName("Converting pixel Y coordinates to graph Y coordinates")
    void test0302() {

        final Integer pixelSpaceWidth = Integer.valueOf(1000);
        final Integer pixelSpaceHeight = Integer.valueOf(700);
        final Integer borderWidth = Integer.valueOf(11);

        final double minY = -1.25;
        final double maxY = 1.75;

        final Double graphLeftX = Double.valueOf(-2.0 * Math.PI);
        final Double graphRightX = Double.valueOf(Math.PI * 0.5);
        final Double graphBottomY = Double.valueOf(minY);
        final Double graphTopY = Double.valueOf(maxY);
        final NumberBounds graphBounds = new NumberBounds(graphLeftX, graphRightX, graphBottomY, graphTopY);

        final CoordinateSystems systems = new CoordinateSystems(pixelSpaceWidth, pixelSpaceHeight, borderWidth,
                graphBounds);

        final double pix1 = 11.0;
        final double pix2 = 700.0 - 11.0;

        final double graph1 = systems.pixelYToGraphY(pix1);
        final double graph2 = systems.pixelYToGraphY(pix2);
        final double graph3 = systems.pixelYToGraphY(pix1 * 0.75 + pix2 * 0.25);
        final double graph4 = systems.pixelYToGraphY(pix1 * 0.5 + pix2 * 0.5);
        final double graph5 = systems.pixelYToGraphY(pix1 * 0.25 + pix2 * 0.75);

        final double expect1 = maxY;
        final double expect2 = minY;
        final double expect3 = maxY * 0.75 + minY * 0.25;
        final double expect4 = maxY * 0.5 + minY * 0.5;
        final double expect5 = maxY * 0.25 + minY * 0.75;

        assertEquals(expect1, graph1, EPSILON, "Pixel coordinate at left edge converted incorrectly to graph X");
        assertEquals(expect2, graph2, EPSILON, "Pixel coordinate at right edge converted incorrectly to graph X");
        assertEquals(expect3, graph3, EPSILON, "Pixel coordinate at 25% converted incorrectly to graph X");
        assertEquals(expect4, graph4, EPSILON, "Pixel coordinate at 50% converted incorrectly to graph X");
        assertEquals(expect5, graph5, EPSILON, "Pixel coordinate at 75% converted incorrectly to graph X");
    }
}
