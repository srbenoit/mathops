package dev.mathops.db.old.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawrecord.RawSpecialStus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A container for special categories to which a student belongs.
 */
final class SpecialCategoriesStatus {

    /** The list of special student categories; empty if none. */
    private final List<RawSpecialStus> specials;

    /**
     * Constructs a new {@code SpecialCategoriesStatus}.
     *
     * @param theSpecials a list of special student categories
     */
    private SpecialCategoriesStatus(final List<RawSpecialStus> theSpecials) {

        super();

        this.specials = Collections.unmodifiableList(theSpecials);
    }

    /**
     * Constructs a new {@code SpecialCategoriesStatus} for a student.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return the generated status object
     * @throws SQLException if there is an error accessing the database
     */
    static SpecialCategoriesStatus of(final Cache cache, final String studentId) throws SQLException {

        return new SpecialCategoriesStatus(RawSpecialStusLogic.queryByStudent(cache, studentId));
    }

//    /**
//     * Gets all special categories (active and inactive)
//     *
//     * @return the list of categories
//     */
//    public List<RawSpecialStus> getAllCategories() {
//
//        return this.specials;
//    }

    /**
     * Gets all active special categories.
     *
     * @param today the current date
     * @return the list of categories
     */
    List<RawSpecialStus> getActive(final ChronoLocalDate today) {

        final List<RawSpecialStus> result = new ArrayList<>(this.specials.size());

        for (final RawSpecialStus test : this.specials) {
            final LocalDate start = test.startDt;

            if (start == null || !start.isAfter(today)) {
                final LocalDate end = test.endDt;

                if (end == null || !end.isBefore(today)) {
                    result.add(test);
                }
            }
        }

        return result;
    }
}
