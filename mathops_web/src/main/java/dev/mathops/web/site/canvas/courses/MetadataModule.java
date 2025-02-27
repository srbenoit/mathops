package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for metadata relating to a single module within a course.
 *
 * <p>
 * The format of a module object in the metadata.json file in the root directory is as follows:
 *
 * <pre>
 * {
 *   "title":   "Module 1: Angles and Triangles",
 *   "topics":
 *   [
 *     ( see {@code MetadataTopic} for the format of each topic object )
 *   ]
 * }
 * </pre>
 */
class MetadataModule {

    /** The module title. */
    public final String title;

    /** A list of topics in the module. */
    private final List<MetadataTopic> topics;

    /**
     * Constructs a new {@code MetadataModule} from a JSON Object.
     *
     * @param json the JSON object from which to extract data
     */
    MetadataModule(final JSONObject json) {

        this.title = json.getStringProperty("title");
        if (this.title == null) {
            Log.warning("'module' object in 'metadata.json' missing 'title' field.");
        }

        this.topics = new ArrayList<>(4);

        final Object topicsField = json.getProperty("topics");

        if (topicsField != null) {
            if (topicsField instanceof final Object[] topicsArray) {
                for (final Object o : topicsArray) {
                    if (o instanceof final JSONObject jsonCourse) {
                        final MetadataTopic topic = new MetadataTopic(jsonCourse);
                        if (topic.isValid()) {
                            this.topics.add(topic);
                        }
                    } else {
                        Log.warning("Entry in 'topics' array in 'metadata.json' is not JSON object.");
                    }
                }
            } else {
                Log.warning("'topics' field in 'metadata.json' top-level object is not an array.");
            }
        }
    }
}
