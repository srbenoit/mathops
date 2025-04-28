package dev.mathops.dbjobs.io;

import dev.mathops.db.Cache;
import dev.mathops.db.course.CourseFileStructure;

import java.io.File;

/**
 * An installer that can load a course into the database (or update an existing course in the database) based on a
 * directory tree structure.
 */
public class CourseInstaller {

    /**
     * The directory relative to which paths are specified (typically a directory that contains top-level folders for
     * module categories like Algebra, Trigonometry, etc.)
     */
    private final File sourceDir;

    /** The JSON file that defines the course to be installed. */
    private final File courseFile;

    /** The course structure loaded from the specified source directory and course file. */
    private final CourseFileStructure courseFileStructure;

    /**
     * Constructs a new {@code CourseInstaller}.
     *
     * @param theSourceDir  the directory relative to which paths are specified (typically a directory that contains
     *                      top-level folders for module categories like Algebra, Trigonometry, etc.)
     * @param theCourseFile the JSON file that defines the course to be installed
     */
    public CourseInstaller(final File theSourceDir, final File theCourseFile) {

        // NOTE: the constructor for {@code CourseFileStructure} will throw an illegal argument exception if either
        //       argument is NULL, so we do not repeat that test here.

        this.sourceDir = theSourceDir;
        this.courseFile = theCourseFile;
        this.courseFileStructure = new CourseFileStructure(theSourceDir, theCourseFile);
    }

    /**
     * Verifies that the course is installed and matches the directory structure.  If the course is not installed, that
     * fact will be reported, and a subsequent call to {@code installOrUpdate} will install the course.  Otherwise, all
     * differences are reported, and a subsequent call to {@code installOrUpdate} will update course data in the
     * database to match what is found in the file structure.
     *
     * @param cache the data cache
     */
    public void verify(final Cache cache) {

    }

    /**
     * Installs or updates the course.
     *
     * @param cache the data cache
     */
    public void installOrUpdate(final Cache cache) {

    }

    /**
     * Removes the course.  This method checks for active student registrations in the course, and if there are any, no
     * action is taken.
     *
     * @param cache the data cache
     */
    public void remove(final Cache cache) {

    }
}
