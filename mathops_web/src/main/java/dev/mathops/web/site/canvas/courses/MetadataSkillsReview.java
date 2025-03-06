package dev.mathops.web.site.canvas.courses;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for metadata relating to a Skills Review for a topic.
 */
final class MetadataSkillsReview {

    /** Suffixes for objective paths. */
    private static final String SUFFIXES = "-----------ABCDEFGHIJKLMNOPQRS";

    /** The Skills Review directory. */
    final File skillsReviewDir;

    /** The list of titles of topics. */
    final List<String> topicTitles;

    /**
     * Constructs a new {@code MetadataSkillsReview} from a JSON Object.
     *
     * @param theSkillsReviewDir the Skills Review directory
     */
    MetadataSkillsReview(final File theSkillsReviewDir) {

        this.skillsReviewDir = theSkillsReviewDir;
        this.topicTitles = new ArrayList<>(10);

        for (int i = 11; i < 30; ++i) {
            final String objectiveDirName = i + "_objective_" + SUFFIXES.substring(i, i + 1);
            final File objectiveDir = new File(theSkillsReviewDir, objectiveDirName);

            if (objectiveDir.exists() && objectiveDir.isDirectory()) {

                final File objectiveMetadataFile = new File(objectiveDir, "metadata.json");
                final String fileData = FileLoader.loadFileAsString(objectiveMetadataFile, true);

                if (fileData == null) {
                    final String metaPath = objectiveMetadataFile.getAbsolutePath();
                    Log.warning("Unable to load ", metaPath);
                } else {
                    try {
                        final Object parsedObj = JSONParser.parseJSON(fileData);

                        if (parsedObj instanceof final JSONObject parsedJson) {
                            final String title = parsedJson.getStringProperty("title");
                            if (title != null) {
                                this.topicTitles.add(title);
                            }
                        } else {
                            final String metaPath = objectiveMetadataFile.getAbsolutePath();
                            Log.warning("Top-level object in ", metaPath, " is not JSON Object.");
                        }
                    } catch (final ParsingException ex) {
                        final String metaPath = objectiveMetadataFile.getAbsolutePath();
                        Log.warning("Failed to parse " + metaPath, ex);
                    }
                }
            }
        }
    }
}
