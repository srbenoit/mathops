package dev.mathops.assessment.document;

/**
 * Attribute names.
 */
public enum EAttribute {

    /**
     * The x coordinate of the first corner of a rectangle region, or the x coordinate of the anchor point of a
     * positioned primitive, in pixel space.
     */
    X("x"),

    /** The x coordinate of the first corner of a rectangle region or the start of a line, in pixel space. */
    X1("x1"),

    /** The x coordinate of the second corner of a rectangle region or the end of a line, in pixel space. */
    X2("x2"),

    /** The x coordinate of the center of a rectangle region, in pixel space. */
    CX("cx"),

    /**
     * The y coordinate of the first corner of a rectangle region, or the y coordinate of the anchor point of a
     * positioned primitive, in pixel space.
     */
    Y("y"),

    /** The y coordinate of the first corner of a rectangle region or the start of a line, in pixel space. */
    Y1("y1"),

    /** The y coordinate of the second corner of a rectangle region or the end of a line, in pixel space. */
    Y2("y2"),

    /** The y coordinate of the center of a rectangle region, in pixel space. */
    CY("cy"),

    /**
     * The x coordinate of the first corner of a rectangle region, or the x coordinate of the anchor point of a
     * positioned primitive, in graph space.
     */
    GX("gx"),

    /** The x coordinate of the first corner of a rectangle region or the start of a line, in graph space. */
    GX1("gx1"),

    /** The x coordinate of the second corner of a rectangle region or the end of a line, in graph space. */
    GX2("gx2"),

    /** The x coordinate of the center of a rectangle region, in graph space. */
    GCX("gcx"),

    /**
     * The y coordinate of the first corner of a rectangle region, or the y coordinate of the anchor point of a
     * positioned primitive, in graph space.
     */
    GY("gy"),

    /** The y coordinate of the first corner of a rectangle region or the start of a line, in graph space. */
    GY1("gy1"),

    /** The y coordinate of the second corner of a rectangle region or the end of a line, in graph space. */
    GY2("gy2"),

    /** The y coordinate of the center of a rectangle region, in graph space. */
    GCY("gcy"),

    /** The width of a rectangle region, in pixel space. */
    WIDTH("width"),

    /** The height of a rectangle region, in pixel space. */
    HEIGHT("height"),

    /** The width of a rectangle region, in graph space. */
    GWIDTH("gwidth"),

    /** The height of a rectangle region, in graph space. */
    GHEIGHT("gheight"),

    /** A circle radius, in pixel space. */
    R("r"),

    /** The x-axis radius of an axis-aligned ellipse, in pixel space. */
    RX("rx"),

    /** The y-axis radius of an axis-aligned ellipse, in pixel space. */
    RY("ry"),

    /** A circle radius, in graph space. */
    GR("gr"),

    /** The x-axis radius of an axis-aligned ellipse, in graph space. */
    GRX("grx"),

    /** The y-axis radius of an axis-aligned ellipse, in graph space. */
    GRY("gry");

    /** The attribute label. */
    public final String label;

    /**
     * Constructs a new {@code EAttribute}.
     *
     * @param theLabel the attribute label
     */
    EAttribute(final String theLabel) {

        this.label = theLabel;
    }
}
