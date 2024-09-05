package dev.mathops.app.sim.courses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A course offered during a semester. Student taking a course (say, LIFE 102) may be required to add more than one
 * section (say, a class section and a lab section, or a class and a recitation section).  To model this, each course
 * has one or more lists of sections where the student needs to choose one section from each list.
 */
public final class OfferedCourse {

    /** The course. */
    private final Course course;

    /** A list of lists of sections - students must choose one section from each list. */
    private final List<List<OfferedSection>> sectionLists;

    /**
     * Constructs a new {@code OfferedCourse}.
     *
     * @param theCourse the course
     */
    public OfferedCourse(final Course theCourse) {

        if (theCourse == null) {
            throw new IllegalArgumentException("Course may not be null");
        }

        this.course = theCourse;
        this.sectionLists = new ArrayList<>(2);
    }

    /**
     * Gets the course.
     *
     * @return the course
     */
    public Course getCourse() {

        return this.course;
    }

    /**
     * Adds a list of offered sections.  Every student enrolling in the course will need to select one section from this
     * list.  Multiple lists can be added through multiple calls to this method, and students will be required to choose
     * one section from each list added.
     *
     * @param sections the list of sections
     */
    public void addSectionsList(final OfferedSection... sections) {

        if (sections == null || sections.length == 0) {
            throw new IllegalArgumentException("Section list may not be null or empty");
        }

        final List<OfferedSection> list = Arrays.asList(sections);
        this.sectionLists.add(list);
    }

    /**
     * Adds a list of offered sections.  Every student enrolling in the course will need to select one section from this
     * list.  Multiple lists can be added through multiple calls to this method, and students will be required to choose
     * one section from each list added.
     *
     * @param sections the list of sections
     */
    public void addSectionsList(final Collection<OfferedSection> sections) {

        if (sections == null || sections.isEmpty()) {
            throw new IllegalArgumentException("Section list may not be null or empty");
        }

        final List<OfferedSection> list = Collections.unmodifiableList(new ArrayList<>(sections));
        this.sectionLists.add(list);
    }

    /**
     * Gets a copy of the list of section lists.
     *
     * @return the section lists (each entry in the returned list is unmodifiable, and changes to the returned list do
     *         not alter this object's contents)
     */
    public List<List<OfferedSection>> getSectionsLists() {

        return new ArrayList<>(this.sectionLists);
    }
}