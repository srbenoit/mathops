package dev.mathops.db.old.logic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.type.TermKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@code PlacementLogic} and {@code PlacementStatus} classes.
 */
final class TestPlacementLogic {

    /** The current date/time. */
    private static ZonedDateTime now = null;

    /** A test status. */
    private static PlacementStatus statusCIMP000 = null;

    /** A test status. */
    private static PlacementStatus statusCIMP001 = null;

    /** Initializes test class. */
    @BeforeAll
    static void initAll() {

        final DatabaseConfig databaseConfig = DatabaseConfig.getDefault();
        final Profile profile = databaseConfig.getCodeProfile(Contexts.CHECKIN_PATH);

        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.PLC_ERR_NO_CHECKIN_PROFILE));
        }

        now = ZonedDateTime.now();

        PlacementStatus logic000 = null;
        PlacementStatus logic001 = null;

        final Cache cache = new Cache(profile);

        try {
            // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored
            logic000 = new PlacementLogic(cache, "99CIMP000", new TermKey(ETermName.FALL, 2021), now).status;

            // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term
            logic001 = new PlacementLogic(cache, "99CIMP001", new TermKey(ETermName.FALL, 2021), now).status;
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        statusCIMP000 = logic000;
        statusCIMP001 = logic001;
    }

    /** Test case. */
    @Test
    @DisplayName("Correct available local proctored IDs")
    void test0101() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        final Set<String> availLocal = statusCIMP000.availableLocalProctoredIds;

        assertEquals(2, availLocal.size(), "Expected 2 available local proctored ID");
        assertTrue(availLocal.contains("MPTTC"), "Expected 'MPTTC'");
        assertTrue(availLocal.contains("MPTUT"), "Expected 'MPTUT'");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct available online proctored IDs")
    void test0102() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        final Set<String> availOnline = statusCIMP000.availableOnlineProctoredIds;

        assertEquals(1, availOnline.size(), "Expected 1 available online proctored ID");
        assertTrue(availOnline.contains("MPTPU"), "Expected 'MPTPU'");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct available unproctored IDs")
    void test0103() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        final Set<String> availUnproctored = statusCIMP000.availableUnproctoredIds;

        assertTrue(availUnproctored.isEmpty(), "Expected 0 available unproctored ID");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct allowed to use unproctored")
    void test0104() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertFalse(statusCIMP000.allowedToUseUnproctored, "Expected not allowed to use unproctored");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why unproctored allowed")
    void test0105() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertNull(statusCIMP000.whyUnproctoredAllowed, "Expected null why unproctored allowed");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why unproctored unavailable")
    void test0106() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertNull(statusCIMP000.whyUnproctoredUnavailable, "Expected null why unproctored unavailable");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct placement attempted")
    void test0107() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertFalse(statusCIMP000.placementAttempted, "Expected false placement attempted");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct attempts used")
    void test0108() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertEquals(0, statusCIMP000.attemptsUsed, "Expected 0 atempts used");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct attempts remaining")
    void test0109() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertEquals(2, statusCIMP000.attemptsRemaining, "Expected 2 atempts remaining");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why local proctored unavailable")
    void test0110() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertNull(statusCIMP000.whyProctoredUnavailable, "Expected null why local proctored unavailable");
        assertNull(statusCIMP000.shortWhyProctoredUnavailable, "Expected null why local proctored short unavailable");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why unproctored unavailable")
    void test0111() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertNull(statusCIMP000.whyUnproctoredUnavailable, "Expected null why unproctored unavailable");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct when unproctored last available")
    void test0112() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertNull(statusCIMP000.unproctoredDateRanges.getMostRecentPast(),
                "Expected null when unproctored last available");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct when unproctored currently available")
    void test0113() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertNull(statusCIMP000.unproctoredDateRanges.current, "Expected null when unproctored currently available");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct empty unproctored future availability")
    void test0114() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertTrue(statusCIMP000.unproctoredDateRanges.future.isEmpty(),
                "Expected empty when unproctored future availability");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct empty placed out of")
    void test0115() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertTrue(statusCIMP000.placedOutOf.isEmpty(), "Expected empty placed out of");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct cleared for")
    void test0116() {

        // 99CIMP000: Has not taken MPT/MPE, not eligible for unproctored

        assertEquals(5, statusCIMP000.clearedFor.size(), "Expected 5 cleared for");
        assertTrue(statusCIMP000.clearedFor.contains("MATH 101"), "Expected cleared for MATH 101");
        assertTrue(statusCIMP000.clearedFor.contains("MATH 105"), "Expected cleared for MATH 105");
        assertTrue(statusCIMP000.clearedFor.contains("STAT 100"), "Expected cleared for STAT 100");
        assertTrue(statusCIMP000.clearedFor.contains("STAT 201"), "Expected cleared for STAT 201");
        assertTrue(statusCIMP000.clearedFor.contains("STAT 204"), "Expected cleared for STAT 204");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct available local proctored IDs")
    void test0201() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        final Set<String> availLocal = statusCIMP001.availableLocalProctoredIds;

        assertEquals(2, availLocal.size(), "Expected 2 available local proctored ID");
        assertTrue(availLocal.contains("MPTTC"), "Expected 'MPTTC'");
        assertTrue(availLocal.contains("MPTUT"), "Expected 'MPTUT'");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct available online proctored IDs")
    void test0202() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        final Set<String> availOnline = statusCIMP001.availableOnlineProctoredIds;

        assertEquals(1, availOnline.size(), "Expected 1 available online proctored ID");
        assertTrue(availOnline.contains("MPTPU"), "Expected 'MPTPU'");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct available unproctored IDs")
    void test0203() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        final Set<String> availUnproctored = statusCIMP001.availableUnproctoredIds;

        assertEquals(1, availUnproctored.size(), "Expected 1 available unproctored ID");
        assertTrue(availUnproctored.contains("MPTUN"), "Expected 'MPTUN'");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct allowed to use unproctored")
    void test0204() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertTrue(statusCIMP001.allowedToUseUnproctored, "Expected allowed to use unproctored");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why unproctored allowed")
    void test0205() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertEquals("you are an incoming first-year student", statusCIMP001.whyUnproctoredAllowed, //
                "Expected null why unproctored allowed");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why unproctored unavailable")
    void test0206() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertNull(statusCIMP001.whyUnproctoredUnavailable, "Expected null why unproctored unavailable");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct placement attempted")
    void test0207() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertFalse(statusCIMP001.placementAttempted, "Expected false placement attempted");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct attempts used")
    void test0208() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertEquals(0, statusCIMP001.attemptsUsed, "Expected 0 atempts used");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct attempts remaining")
    void test0209() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertEquals(2, statusCIMP001.attemptsRemaining, "Expected 2 atempts remaining");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why local proctored unavailable")
    void test0210() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertNull(statusCIMP001.whyProctoredUnavailable, "Expected null why local proctored unavailable");
        assertNull(statusCIMP001.shortWhyProctoredUnavailable,
                "Expected null why local proctored short unavailable");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct why unproctored unavailable")
    void test0211() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertNull(statusCIMP001.whyUnproctoredUnavailable, "Expected null why unproctored unavailable");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct when unproctored last available")
    void test0212() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertNull(statusCIMP001.unproctoredDateRanges.getMostRecentPast(),
                "Expected null when unproctored last available");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct when unproctored currently available")
    void test0213() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertTrue(statusCIMP001.unproctoredDateRanges.current != null
                   && statusCIMP001.unproctoredDateRanges.current.contains(now.toLocalDate()),
                "Expected null when unproctored currently available");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct unproctored future availability")
    void test0214() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertFalse(statusCIMP001.unproctoredDateRanges.future.isEmpty(),
                "Expected when unproctored future availability");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct empty placed out of")
    void test0215() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertTrue(statusCIMP001.placedOutOf.isEmpty(), "Expected empty placed out of");
    }

    /** Test case. */
    @Test
    @DisplayName("Correct cleared for")
    void test0216() {

        // 99CIMP001: Has not taken MPT/MPE, eligible for unproctored by Application Term

        assertEquals(5, statusCIMP001.clearedFor.size(), "Expected 5 cleared for");
        assertTrue(statusCIMP000.clearedFor.contains("MATH 101"), "Expected cleared for MATH 101");
        assertTrue(statusCIMP000.clearedFor.contains("MATH 105"), "Expected cleared for MATH 105");
        assertTrue(statusCIMP000.clearedFor.contains("STAT 100"), "Expected cleared for STAT 100");
        assertTrue(statusCIMP000.clearedFor.contains("STAT 201"), "Expected cleared for STAT 201");
        assertTrue(statusCIMP000.clearedFor.contains("STAT 204"), "Expected cleared for STAT 204");
    }
}
