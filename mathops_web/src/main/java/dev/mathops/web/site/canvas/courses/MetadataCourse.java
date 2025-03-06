package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

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

    /** A map from course ID to course topic object. */
    final List<MetadataCourseTopic> topics;

    /**
     * Constructs a new {@code MetadataCourse} from a JSON Object.
     *
     * @param json the JSON object from which to extract data
     */
    MetadataCourse(final JSONObject json) {

        this.id = json.getStringProperty("id");
        if (this.id == null) {
            Log.warning("'course' object in 'metadata.json' missing 'id' field.");
        }

        this.title = json.getStringProperty("title");
        if (this.title == null) {
            Log.warning("'course' object in 'metadata.json' missing 'title' field.");
        }

        this.topics = new ArrayList<>(10);

        final Object topicsField = json.getProperty("topics");

        if (topicsField == null) {
            Log.warning("Missing required 'topics' field in course object in 'metadata.json'.");
        } else if (topicsField instanceof final Object[] topicsArray) {
            for (final Object o : topicsArray) {
                if (o instanceof final JSONObject jsonTopic) {
                    final MetadataCourseTopic topic = new MetadataCourseTopic(jsonTopic);
                    Log.info("Found topic with ID ", topic.id, " and directory ", topic.directory);

                    if (topic.id != null && topic.directory != null) {
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
