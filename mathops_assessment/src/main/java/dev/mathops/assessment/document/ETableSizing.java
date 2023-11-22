package dev.mathops.assessment.document;

/**
 * Methods for sizing table rows or columns.
 */
public enum ETableSizing {

    /** All columns the same width. */
    UNIFORM,

    /** Columns sized to width of contents. */
    NONUNIFORM,

    /** Columns sized to average of width of contents and maximum column width. */
    SCALED
}
