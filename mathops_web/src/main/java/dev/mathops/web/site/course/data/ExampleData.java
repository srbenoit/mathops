package dev.mathops.web.site.course.data;

/**
 * Data for a single example.
 */
public final class ExampleData {

    /** The media ID. */
    public final String mediaId;

    /** The example label. */
    public final String label;

    /**
     * Constructs a new {@code ExampleData}.
     *
     * @param theMediaId the media ID
     * @param theLabel   the label
     */
    ExampleData(final String theMediaId, final String theLabel) {

        this.mediaId = theMediaId;
        this.label = theLabel;
    }
}
