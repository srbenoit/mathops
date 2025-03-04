package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

/**
 * A container for metadata relating to a topic within a module.
 *
 * <p>
 * The format of a topic object in the metadata.json file in the root directory is as follows:
 *
 * <pre>
 * {
 *   "title":     "Topic 1: Angles and Angle Measure",
 *   "directory": "05_trig/01_angles"
 * }
 * </pre>
 */
public final class MetadataTopic {

    /** The topic title. */
    public final String title;

    /** The thumbnail filename. */
    public final String thumbnailFile;

    /** The thumbnail alt-text. */
    public final String thumbnailAltText;

    /**
     * Constructs a new {@code MetadataTopic} from a JSON Object.
     *
     * @param json the JSON object from which to extract data
     */
    MetadataTopic(final JSONObject json) {

        this.title = json.getStringProperty("title");
        if (this.title == null) {
            Log.warning("'topic' object in 'metadata.json' missing 'title' field.");
        }

        this.thumbnailFile = json.getStringProperty("thumb-file");
        this.thumbnailAltText = json.getStringProperty("thumb-alt");
    }

    /**
     * Tests whether a topic is valid.
     *
     * @return true if valid; false if not
     */
    boolean isValid() {

        return this.title != null;
    }
}
