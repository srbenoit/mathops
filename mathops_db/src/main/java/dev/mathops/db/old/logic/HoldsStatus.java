package dev.mathops.db.old.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * A container for holds for a particular student.
 */
public final class HoldsStatus {

    /** The list of holds; empty if none. */
    public final List<RawAdminHold> holds;

    /**
     * Constructs a new {@code RemoteTestingStatus}.
     *
     * @param theHolds a list of holds (this list is copied int this object and exposed as an unmodifiable list)
     */
    private HoldsStatus(final List<RawAdminHold> theHolds) {

        this.holds = Collections.unmodifiableList(theHolds);
    }

    /**
     * Constructs a new {@code HoldsStatus} for a student.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return the generated status object
     * @throws SQLException if there is an error accessing the database
     */
    public static HoldsStatus of(final Cache cache, final String studentId) throws SQLException {

        return new HoldsStatus(RawAdminHoldLogic.queryByStudent(cache, studentId));
    }
}
