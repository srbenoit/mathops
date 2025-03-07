package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.site.canvas.CanvasPageUtils;

import java.io.File;

/**
 * A container for metadata relating to a topic within a course.
 *
 * <p>
 * The format of a topic object in the metadata.json file in the root directory is as follows:
 *
 * <pre>
 * {
 *   "id":        "T1",
 *   "heading":   "Topic 1",
 *   "directory": "05_trig/01_angles"
 * }
 * </pre>
 * <p>
 * Additional data needed to present the topic is taken from the topic directory.
 */
final class MetadataCourseModule {

    /** The topic id. */
    public final String id;

    /** The topic heading. */
    final String heading;

    /** The (relative) topic directory path, like "05_trig/01_angles". */
    final String directory;

    /** The topic module directory. */
    final File topicModuleDir;

    /** Metadata loaded from the topic directory. */
    final MetadataTopic topicMetadata;

    /**
     * Constructs a new {@code MetadataModuleTopic} from a JSON Object.
     *
     * @param json    the JSON object from which to extract data
     * @param rootDir the directory relative to which topic directories are specified
     */
    MetadataCourseModule(final JSONObject json, final File rootDir) {

        this.id = json.getStringProperty("id");
        if (this.id == null) {
            Log.warning("'topic' object in 'metadata.json' missing 'id' field.");
        }

        this.heading = json.getStringProperty("heading");

        MetadataTopic loadedTopicMeta = null;

        this.directory = json.getStringProperty("directory");
        if (this.directory == null) {
            Log.warning("'topic' object in 'metadata.json' missing 'directory' field.");
            this.topicModuleDir = null;
        } else {
            this.topicModuleDir = new File(rootDir, this.directory);
            final JSONObject loadedJson = CanvasPageUtils.loadMetadata(this.topicModuleDir);
            if (loadedJson != null) {
                loadedTopicMeta = new MetadataTopic(loadedJson, this.topicModuleDir);
            }
        }

        this.topicMetadata = loadedTopicMeta;
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
