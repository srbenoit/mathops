package dev.mathops.db.course;

import dev.mathops.text.parser.json.JSONObject;

import java.io.File;

/**
 * A container for metadata relating to an objective, which could exist in a Skills Review or a Standard.
 */
public final class MetadataObjective {

    /** The objective directory. */
    public final File objectiveDir;

    /** The title from the metadata file. */
    public final String title;

    /** The description from the metadata file. */
    public final String description;

    /**
     * Constructs a new {@code MetadataObjective} from a JSON Object.
     *
     * @param theStandardDir the objective directory
     */
    public MetadataObjective(final File theStandardDir) {

        this.objectiveDir = theStandardDir;

        final JSONObject loadedJson = JSONUtils.loadJsonFile(theStandardDir, "metadata.json");
        if (loadedJson == null) {
            this.title = null;
            this.description = null;
        } else {
            this.title = loadedJson.getStringProperty("title");
            this.description = loadedJson.getStringProperty("description");
        }
    }
}
