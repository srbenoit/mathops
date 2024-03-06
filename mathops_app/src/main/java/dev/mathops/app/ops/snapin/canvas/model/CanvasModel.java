package dev.mathops.app.ops.snapin.canvas.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A container class for the Canvas data model.
 */
public final class CanvasModel {

    /** All course terms (map from course term ID to course term object). */
    public final Map<String, CourseTerm> courseTerms;

    /**
     * Constructs a new {@code CanvasModel}
     */
    public CanvasModel() {

        this.courseTerms = new HashMap<>(20);
    }
}
