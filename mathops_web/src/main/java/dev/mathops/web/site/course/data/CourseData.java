package dev.mathops.web.site.course.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a single course.
 */
public final class CourseData {

    /** The course ID, such as "M 125". */
    public final String courseId;

    /** The course number, such as "MATH 125". */
    public final String courseNumber;

    /** The course title, such as "Numerical Trigonometry". */
    public final String courseTitle;

    /** The subdirectory where course media is located, such as "m125". */
    public final String mediaDir;

    /** The list of modules. */
    public final List<ModuleData> modules;

    /**
     * Constructs a new {@code CourseData}.
     *
     * @param theCourseId     the course ID, such as "M 125"
     * @param theCourseNumber the course number, such as "MATH 125"
     * @param theCourseTitle  the course title, such as "Numerical Trigonometry"
     * @param theMediaDir     the subdirectory where course media is located, such as "m125"
     */
    CourseData(final String theCourseId, final String theCourseNumber, final String theCourseTitle,
               final String theMediaDir) {

        this.courseId = theCourseId;
        this.courseNumber = theCourseNumber;
        this.courseTitle = theCourseTitle;
        this.mediaDir = theMediaDir;

        this.modules = new ArrayList<>(10);
    }

    /**
     * Creates a {@code ModuleData} for a module and adds it to the course.
     *
     * @param theModuleNumber   the module number
     * @param theModuleTitle    the module title
     * @param theSRAssignment   the assignment ID for the Skills Review assignment
     * @param theThumbnailImage the thumbnail image filename
     * @return the generated module
     */
    ModuleData addModule(final int theModuleNumber, final String theModuleTitle, final String theSRAssignment,
                         final String theThumbnailImage) {

        final ModuleData module = new ModuleData(this, theModuleNumber, theModuleTitle, theSRAssignment,
                theThumbnailImage);

        this.modules.add(module);

        return module;
    }
}
