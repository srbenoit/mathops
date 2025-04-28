package dev.mathops.db.course;

import java.io.File;

/**
 * A container for data found in a course directory tree, as specified by a course JSON file.
 */
public class CourseFileStructure {

    /**
     * Constructs a new {@code CourseInstaller}.
     *
     * @param theSourceDir  the directory relative to which paths are specified (typically a directory that contains
     *                      top-level folders for module categories like Algebra, Trigonometry, etc.)
     * @param theCourseFile the JSON file that defines the course to be installed
     */
    public CourseFileStructure(final File theSourceDir, final File theCourseFile) {

        if (theSourceDir == null) {
            throw new IllegalArgumentException("Source directory may not be null");
        }
        if (theCourseFile == null) {
            throw new IllegalArgumentException("Course file may not be null");
        }
    }
}
