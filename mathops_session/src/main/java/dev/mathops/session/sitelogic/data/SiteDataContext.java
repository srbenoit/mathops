package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for the context-oriented data relating to a {@code SiteData} object.
 */
public final class SiteDataContext {

    /** The cache. */
    private final Cache cache;

    /** Map from term key to a list of cached course section records. */
    private final Map<TermKey, List<RawCsection>> courseSections;

    /**
     * Constructs a new {@code SiteDataContext}.
     *
     * @param theCache the cache
     */
    SiteDataContext(final Cache theCache) {

        this.cache = theCache;
        this.courseSections = new HashMap<>(3);
    }

    /**
     * Gets the course section record for a specified course and section in a specified term.
     *
     * @param courseId the course ID
     * @param section  the section number
     * @param term     the term
     * @return the course section record; null if none found
     */
    public RawCsection getCourseSection(final String courseId, final String section, final TermKey term) {

        final List<RawCsection> list = this.courseSections.computeIfAbsent(term, key -> new ArrayList<>(10));

        RawCsection result = null;

        for (final RawCsection row : list) {
            if (row.course.equals(courseId) && row.sect.equals(section)) {
                result = row;
                break;
            }
        }

        if (result == null) {
            try {
                result = this.cache.getSystemData().getCourseSection(courseId, section, term);
                if (result != null) {
                    list.add(result);
                }
            } catch (final SQLException ex) {
                Log.severe("Failed to query for course section", ex);
            }
        }

        return result;
    }

    /**
     * Gets all course section records for a specified course in a specified term.
     *
     * @param courseId the course ID
     * @param term     the term
     * @return the course section record; null if none found
     */
    List<RawCsection> getAllCourseSections(final String courseId, final TermKey term) {

        List<RawCsection> result = null;

        try {
            result = this.cache.getSystemData().getCourseSectionsByCourse(courseId, term);
        } catch (final SQLException ex) {
            Log.severe("Failed to query for course sections", ex);
        }

        return result;
    }
}
