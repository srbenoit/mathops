package dev.mathops.web.site.canvas.courses;

import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.site.canvas.CanvasPageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for metadata relating to a standard.
 */
final class MetadataStandard {

    /** Suffixes for objective paths. */
    private static final String SUFFIXES = "-----------ABCDEFGHIJKLMNOPQRS";

    /** The standard directory. */
    final File standardDir;

    /** The title from the metadata file. */
    final String title;

    /** The description from the metadata file. */
    final String description;

    /** The list of objectives. */
    final List<MetadataObjective> objectives;

    /**
     * Constructs a new {@code MetadataStandard} from a JSON Object.
     *
     * @param theStandardDir the standard directory
     */
    MetadataStandard(final File theStandardDir) {

        this.standardDir = theStandardDir;
        this.objectives = new ArrayList<>(10);

        final JSONObject loadedJson = CanvasPageUtils.loadMetadata(theStandardDir);
        if (loadedJson == null) {
            this.title = null;
            this.description = null;
        } else {
            this.title = loadedJson.getStringProperty("title");
            this.description = loadedJson.getStringProperty("description");
        }

        for (int i = 11; i < 30; ++i) {
            final String objectiveDirName = i + "_objective_" + SUFFIXES.substring(i, i + 1);
            final File objectiveDir = new File(theStandardDir, objectiveDirName);

            if (objectiveDir.exists() && objectiveDir.isDirectory()) {
                final MetadataObjective objectiveMeta = new MetadataObjective(objectiveDir);
                this.objectives.add(objectiveMeta);
            }
        }
    }
}
