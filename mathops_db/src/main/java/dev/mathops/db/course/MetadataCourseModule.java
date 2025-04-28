package dev.mathops.db.course;

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

    /** The module number. */
    public final Integer moduleNbr;

    /** The (relative) topic directory path, like "05_trig/01_angles". */
    public final String moduleRelPath;

    /** The topic module directory. */
    public final File moduleDir;

    /** The topic title. */
    public final String title;

    /** The thumbnail filename. */
    public final String thumbnailFile;

    /** The thumbnail alt-text. */
    public final String thumbnailAltText;

    /** Flag indicating metadata was loaded successfully and in valid. */
    final boolean valid;

    /**
     * Constructs a new {@code MetadataModuleTopic} from a JSON Object.
     *
     * @param mediaRoot        the root media directory relative to which the module path is specified
     * @param theModuleRelPath the relative path to the module directory, as specified in the course JSON file
     * @param theModuleNbr     the module number, as specified in the course JSON file
     */
    public MetadataCourseModule(final File mediaRoot, final String theModuleRelPath, final Integer theModuleNbr) {

        this.moduleNbr = theModuleNbr;
        this.moduleRelPath = theModuleRelPath;
        this.moduleDir = new File(mediaRoot, theModuleRelPath);

        boolean isValid = false;
        String theTitle = null;
        String theThumb = null;
        String theThumbAlt = null;

        final JSONObject loadedJson = JSONUtils.loadJsonFile(this.moduleDir, "metadata.json");
        if (loadedJson != null) {
            theTitle = loadedJson.getStringProperty("title");
            theThumb = loadedJson.getStringProperty("thumb-file");
            theThumbAlt = loadedJson.getStringProperty("thumb-alt");

            isValid = theTitle != null;
        }

        this.title = theTitle;
        this.thumbnailFile = theThumb;
        this.thumbnailAltText = theThumbAlt;
        this.valid = isValid;
    }

    /**
     * Tests whether a topic is valid.
     *
     * @return true if valid; false if not
     */
    public boolean isValid() {

        return this.valid;
    }
}
