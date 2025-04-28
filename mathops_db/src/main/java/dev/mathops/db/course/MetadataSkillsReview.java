package dev.mathops.db.course;

import dev.mathops.text.parser.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for metadata relating to a Skills Review for a topic.
 */
public final class MetadataSkillsReview {

    /** Suffixes for objective paths. */
    public static final String SUFFIXES = "-----------ABCDEFGHIJKLMNOPQRS";

    /** The Skills Review directory. */
    public final File skillsReviewDir;

    /** The description from the metadata file. */
    public final String description;

    /** The list of objectives. */
    public final List<MetadataObjective> objectives;

    /**
     * Constructs a new {@code MetadataSkillsReview} from a JSON Object.
     *
     * @param theSkillsReviewDir the Skills Review directory
     */
    public MetadataSkillsReview(final File theSkillsReviewDir) {

        this.skillsReviewDir = theSkillsReviewDir;
        this.objectives = new ArrayList<>(10);

        final JSONObject loadedJson = JSONUtils.loadJsonFile(theSkillsReviewDir, "metadata.json");
        if (loadedJson == null) {
            this.description = null;
        } else {
            this.description = loadedJson.getStringProperty("description");
        }

        for (int i = 11; i < 30; ++i) {
            final String objectiveDirName = i + "_objective_" + SUFFIXES.substring(i, i + 1);
            final File objectiveDir = new File(theSkillsReviewDir, objectiveDirName);

            if (objectiveDir.exists() && objectiveDir.isDirectory()) {
                final MetadataObjective objectiveMeta = new MetadataObjective(objectiveDir);
                this.objectives.add(objectiveMeta);
            }
        }
    }
}
