package dev.mathops.db.course;

import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.text.parser.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A container for metadata relating to a single course.
 *
 * <p>
 * The format of a course object in the metadata.json file in the root directory is as follows:
 *
 * <pre>
 * {
 *   "course_id":    "MATH 125",
 *   "course_title": "Numerical Trigonometry",
 *   "nbr_credits":  1,
 *   "allow_lend":   ["textbook", "calculator", "laptop", "headphones"],
 *   "modules":
 *   [
 *     {
 *       "module_nbr": 1,
 *       "directory":  "05_trig/01_angles"
 *     },
 *     ... additional module specifications ...
 *   ]
 * }
 * </pre>
 */
public class MetadataCourse {

    /** The directory relative to which topic directories are specified. */
    public final File rootDir;

    /** The course ID. */
    public final String courseId;

    /** The course title. */
    public final String courseTitle;

    /** The number of credits. */
    public final int nbrCredits;

    /** The bitwise OR of flags for all resource types that can be lent to students in the course. */
    public final int allowLend;

    /** A map from module number to the module directory. */
    final Map<Integer, File> moduleDirectories;

    /**
     * Constructs a new {@code MetadataCourse} from a JSON Object.
     *
     * @param json       the JSON object from which to extract data
     * @param theRootDir the directory relative to which topic directories are specified
     * @throws IllegalArgumentException if a required field is missing or invalid
     */
    MetadataCourse(final JSONObject json, final File theRootDir) {

        this.rootDir = theRootDir;

        this.courseId = json.getStringProperty("course_id");
        if (this.courseId == null) {
            throw new IllegalArgumentException("Course JSON file missing 'course_id' field.");
        }

        this.courseTitle = json.getStringProperty("course_title");
        if (this.courseTitle == null) {
            throw new IllegalArgumentException("Course JSON file missing 'course_title' field.");
        }

        final Double nbrCreditsDbl = json.getNumberProperty("nbr_credits");
        if (nbrCreditsDbl == null) {
            throw new IllegalArgumentException("Course JSON file missing 'nbr_credits' field.");
        }
        this.nbrCredits = nbrCreditsDbl.intValue();
        if (this.nbrCredits < 1 || this.nbrCredits > 9) {
            throw new IllegalArgumentException("Course JSON file has invalid 'nbr_credits' field.");
        }

        final Object lendObj = json.getProperty("allow_lend");
        if (lendObj == null) {
            this.allowLend = 0;
        } else if (lendObj instanceof final Object[] lendArray) {
            int accum = 0;

            for (final Object entry : lendArray) {
                if (entry instanceof final String entryStr) {
                    final String lowercase = entryStr.toLowerCase(Locale.ROOT);
                    if ("textbook".equals(entryStr)) {
                        accum |= StandardsCourseRec.TEXTBOOK;
                    } else if ("calculator".equals(entryStr)) {
                        accum |= StandardsCourseRec.CALCULATOR;
                    } else if ("laptop".equals(entryStr)) {
                        accum |= StandardsCourseRec.LAPTOP;
                    } else if ("headphones".equals(entryStr)) {
                        accum |= StandardsCourseRec.HEADPHONES;
                    } else {
                        throw new IllegalArgumentException("Course JSON file has invalid entry in 'allow_lend' array.");
                    }
                } else {
                    throw new IllegalArgumentException("Course JSON file has invalid 'allow_lend' field.");
                }
            }

            this.allowLend = accum;
        } else {
            throw new IllegalArgumentException("Course JSON file has invalid 'allow_lend' field.");
        }

        final Object modulesObj = json.getProperty("modules");
        if (modulesObj == null) {
            throw new IllegalArgumentException("Course JSON file missing 'modules' field.");
        }
        if (modulesObj instanceof final Object[] modulesArray) {
            final int len = modulesArray.length;
            this.moduleDirectories = new HashMap<>(len);

            for (final Object entry : modulesArray) {
                if (entry instanceof final JSONObject entryJson) {
                    final Double moduleNbrDbl = entryJson.getNumberProperty("module_nbr");
                    final String moduleDir = entryJson.getStringProperty("directory");

                    if (moduleNbrDbl == null || moduleDir == null) {
                        throw new IllegalArgumentException("Course JSON file has invalid entry in 'modules' array.");
                    } else {
                        final int moduleNbrValue = moduleNbrDbl.intValue();
                        final Integer moduleNbr = Integer.valueOf(moduleNbrValue);
                        final File dir = new File(theRootDir, moduleDir);

                        if (dir.exists() && dir.isDirectory()) {
                            this.moduleDirectories.put(moduleNbr, dir);
                        } else {
                            throw new IllegalArgumentException("Course JSON file has invalid path in 'modules' array.");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Course JSON file has invalid entry in 'modules' array.");
                }
            }
        } else {
            throw new IllegalArgumentException("Course JSON file has invalid 'modules' field.");
        }
    }
}
