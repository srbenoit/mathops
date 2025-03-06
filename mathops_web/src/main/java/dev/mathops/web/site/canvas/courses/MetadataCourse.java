package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for metadata relating to a single course.
 *
 * <p>
 * The format of a course object in the metadata.json file in the root directory is as follows:
 *
 * <pre>
 * {
 *   "id":      "MATH 125",
 *   "title":   "Numerical Trigonometry",
 *   "modules":
 *   [
 *     ( see {@code MetadataModule} for the format of each module object )
 *   ]
 * }
 * </pre>
 */
public class MetadataCourse {

    /** The course ID. */
    public final String id;

    /** The course title. */
    public final String title;

    /** A map from course ID to course modules object. */
    final List<MetadataCourseModule> modules;

    /**
     * Constructs a new {@code MetadataCourse} from a JSON Object.
     *
     * @param json    the JSON object from which to extract data
     * @param rootDir the directory relative to which topic directories are specified
     */
    MetadataCourse(final JSONObject json, final File rootDir) {

        this.id = json.getStringProperty("id");
        if (this.id == null) {
            Log.warning("'course' object in 'metadata.json' missing 'id' field.");
        }

        this.title = json.getStringProperty("title");
        if (this.title == null) {
            Log.warning("'course' object in 'metadata.json' missing 'title' field.");
        }

        this.modules = new ArrayList<>(10);

        final Object modulesField = json.getProperty("modules");

        if (modulesField == null) {
            Log.warning("Missing required 'modules' field in course object in 'metadata.json'.");
        } else if (modulesField instanceof final Object[] topicsArray) {
            for (final Object o : topicsArray) {
                if (o instanceof final JSONObject jsonTopic) {
                    final MetadataCourseModule module = new MetadataCourseModule(jsonTopic, rootDir);

                    if (module.id != null && module.directory != null) {
                        this.modules.add(module);
                    }
                } else {
                    Log.warning("Entry in 'modules' array in 'metadata.json' is not JSON object.");
                }
            }
        } else {
            Log.warning("'modules' field in 'metadata.json' top-level object is not an array.");
        }
    }
}
