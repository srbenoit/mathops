package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

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
     * @param dir the directory in which to find the root metadata
     */
    public Metadata(final File dir) {

        this.courses = new HashMap<>(10);

        final File rootMetadata = new File(dir, "metadata.json");
        final String fileData = FileLoader.loadFileAsString(rootMetadata, true);
        try {
            final Object parsedObj = JSONParser.parseJSON(fileData);

            if (parsedObj instanceof final JSONObject parsedJson) {
                final Object coursesField = parsedJson.getProperty("courses");

                if (coursesField != null) {
                    if (coursesField instanceof final Object[] coursesArray) {
                        for (final Object o : coursesArray) {
                            if (o instanceof final JSONObject jsonCourse) {
                                final MetadataCourse course = new MetadataCourse(jsonCourse);
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
            } else {
                Log.warning("Top-level object in parsed 'metadata.json' is not JSON Object.");
            }
        } catch (final ParsingException ex) {
            Log.warning("Failed to parse 'metadata.json' file data.", ex);
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
