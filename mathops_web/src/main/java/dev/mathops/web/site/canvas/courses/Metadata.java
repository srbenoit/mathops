package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.site.canvas.CanvasPageUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A container for data loaded from JSON metadata files.
 *
 * <p>
 * The format of the metadata.json file in the root directory is as follows:
 *
 * <pre>
 * {
 *   "courses":
 *   [
 *     ( see {@code MetadataCourse} for the format of each course object )
 *   ]
 * }
 *  ...
 * </pre>
 */
public class Metadata {

    /** A map from course ID to course metadata object. */
    private final Map<String, MetadataCourse> courses;

    /**
     * Constructs metadata object for a directory by loading a "metadata.json" file in that directory and using its
     * contents to find and load related metadata files.
     *
     * @param rootDir the directory in which to find the root metadata
     */
    public Metadata(final File rootDir) {

        this.courses = new HashMap<>(10);

        final JSONObject loadedJson = CanvasPageUtils.loadMetadata(rootDir);

        if (loadedJson != null) {
            final Object coursesField = loadedJson.getProperty("courses");

            if (coursesField != null) {
                if (coursesField instanceof final Object[] coursesArray) {
                    for (final Object o : coursesArray) {
                        if (o instanceof final JSONObject jsonCourse) {
                            final MetadataCourse course = new MetadataCourse(jsonCourse, rootDir);
                            if (course.id != null) {
                                // If ID is null, a warning will have already been logged
                                this.courses.put(course.id, course);
                            }
                        } else {
                            Log.warning("Entry in 'courses' array in 'metadata.json' is not JSON object.");
                        }
                    }
                } else {
                    Log.warning("'courses' field in 'metadata.json' top-level object is not an array.");
                }
            }
        }
    }

    /**
     * Tests whether there is a course metadata object defined for a specified course ID.
     *
     * @param id the course ID
     * @return true if there is a metadata object defined for the course ID; false if not
     */
    public boolean hasCourse(final String id) {

        return this.courses.containsKey(id);
    }

    /**
     * Retrieves the course metadata object for a specified  course.
     *
     * @param id the course ID
     * @return the metadata object; null if none is defined with the specified course ID
     */
    public MetadataCourse getCourse(final String id) {

        return this.courses.get(id);
    }
}
