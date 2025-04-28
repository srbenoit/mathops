package dev.mathops.db.course;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.json.JSONObject;

import java.io.File;

/**
 * A container for metadata relating to a module within a course.  A module is constructed from a module specifier in a
 * course JSON file plus the module metadata file in the directory referenced by that specifier.
 *
 * <p>
 * The format of the metadata.json file in the module directory is as shown below:
 * <pre>
 * {
 *   "title":        "... The module title ...",
 *   "description":  "... A description of the module as might appear in a syllabus ...",
 *   "authors":      "... Authors' names, like 'First Last, First Last, ...' ...",
 *   "goals":        "... A statement of the 'understanding' goals that drive learning outcomes ...",
 *   "thumb-file":   "thumb.png",
 *   "thumb-alt":    "... Alt-text for the module thumbnail image ...",
 *   "attributions":
 *   [
 *     {
 *       "resource": "... filename in the topic module directory ...",
 *       "author":   "",
 *       "actors":   "... if media has actors, their names ...",
 *       "source":   "",
 *       "license":  "... try to obtain signed releases from all actors ..."
 *     },
 *     ... attributions for thumbnail image, fonts, any other licensed resources ...
 *   ],
 *   "notes":
 *   [
 *     {
 *       "resource": "... filename in the topic module directory ...",
 *       "author":   "",
 *       "note":     ""
 *     },
 *     ... optional notes attached to any resource file (if none, "notes" can be omitted) ...
 *   ]
 * }
 * </pre>
 */
public final class MetadataCourseModule {

    /** The module id. */
    public final String moduleId;

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
    public MetadataCourseModule(final JSONObject json, final File rootDir) {

        this.moduleId = json.getStringProperty("id");
        if (this.moduleId == null) {
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
            final JSONObject loadedJson = JSONUtils.loadJsonFile(this.topicModuleDir, "metadata.json");
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
