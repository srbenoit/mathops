package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

/**
 * A container for metadata relating to a topic within a module, as configured for a course.
 *
 * <p>
 * The format of a topic object in the metadata.json file in the root directory is as follows:
 *
 * <pre>
 * {
 *   "id":       "T1",
 *   "heading":  "Topic 1",
 *   "directory": "05_trig/01_angles"
 * }
 * </pre>
 */
public final class MetadataModuleTopic {

    /** The topic id. */
    public final String id;

    /** The topic heading. */
    public final String heading;

    /** The (relative) topic directory. */
    public final String directory;

    /**
     * Constructs a new {@code MetadataModuleTopic} from a JSON Object.
     *
     * @param json the JSON object from which to extract data
     */
    MetadataModuleTopic(final JSONObject json) {

        this.id = json.getStringProperty("id");
        if (this.id == null) {
            Log.warning("'topic' object in 'metadata.json' missing 'id' field.");
        }

        this.heading = json.getStringProperty("heading");

        this.directory = json.getStringProperty("directory");
        if (this.directory == null) {
            Log.warning("'topic' object in 'metadata.json' missing 'directory' field.");
        }
    }

    /**
     * Tests whether a topic is valid.
     *
     * @return true if valid; false if not
     */
    boolean isValid() {

        return this.directory != null;
    }
}
