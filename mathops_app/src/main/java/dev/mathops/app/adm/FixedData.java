package dev.mathops.app.adm;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawHoldTypeLogic;
import dev.mathops.db.old.rawlogic.RawSemesterCalendarLogic;
import dev.mathops.db.old.rawlogic.RawUserClearanceLogic;
import dev.mathops.db.old.rawrecord.RawHoldType;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.db.old.rawrecord.RawUserClearance;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * A container for all fixed data needed by the admin app to populate displays. This is queried once when the
 * application is launched and never refreshed.
 */
public class FixedData {

    /** The logged in user. */
    /* default */ final String username;

    /** The active term. */
    public final TermRec activeTerm;

    /** All term weeks in the active term. */
    public final List<RawSemesterCalendar> termWeeks;

    /** The defined hold types. */
    public final List<RawHoldType> holdTypes;

    /** The permissions for each login user. */
    public final List<RawUserClearance> userPermissions;

    /**
     * Constructs a new {@code FixedData} object, populating all fields.
     * <p>
     * User permissions include the following functions:
     * <ul>
     * <li>ADHOLD (holds: 1, 2 = full control; 3, 4, 5 = read-only)
     * <li>APPEAL (deadline appeals)
     * <li>COURSE (registrations and registration history)
     * <li>FORMS (1, 2 = forms available; 3, 4, 5 = forms not available)
     * <li>LOAN (resource)
     * <li>LOCK (lock the window)
     * <li>PACING (testing and deadline history)
     * <li>PENDEX (add/modify/delete exams, issue exams/calculators, check answers)
     * <li>PLACE (placement, tutorial, transfer/AP credit)
     * <li>ACTIVE (?)
     * <li>COUPON (?)
     * <li>CAIC (?)
     *
     * <li>TSTCTR (testing center management)
     *
     * <li>RES_MENU (resource menu - absent to hide "Resources" menu from admin app)
     * <li>RES_LOAN (absent to hide "Lend Item" from "Resources" menu in admin app)
     * <li>RES_RETRN (absent to hide "Return Item" from "Resources" menu in admin app)
     * <li>RES_STU (absent to hide "Check Student's Loans" from "Resources" menu in admin app)
     * <li>RES_OUTST (absent to hide "View Outstanding Items" from "Resources" menu in admin app)
     * <li>RES_TODAY (absent to hide "Today's Activity" from "Resources" menu in admin app)
     * <li>RES_IVENT (absent to hide "Inventory" from "Resources" menu in admin app)
     *
     * <li>STU_MENU (student menu - absent to hide "Students" menu from admin app)
     * <li>STU_ACTIV
     * <li>STU_DISCP (disciplinary history - absent to hide "Discipline" Tab in student panel)
     * <li>STU_DLINE (deadlines - absent to hide "Deadlines" Tab in student panel, 1 or 2 to allow
     * deadline changes)
     * <li>STU_EXAMS
     * <li>STU_HOLDS
     * <li>STU_INFO
     * <li>STU_PLCMT
     * <li>STU_REGS
     *
     * <li>EXM_CHANS (exam change answers - absent to disallow changing student exam answers)
     *
     * <li>TST_MENU (testing menu - absent to hide "Testing Center" menu from admin app)
     * <li>TST_MAP (absent to hide "Map" from "Resources" menu in admin app)
     * <li>TST_MANAG (absent to hide "Manage" from "Resources" menu in admin app)
     * <li>TST_ISSUE (absent to hide "Issue Exam", "Cancel Exam" from "Resources" menu in admin app)
     *
     * <li>MGT_MENU (management menu - absent to hide "Management" menu from admin app)
     *
     * <li>FRM_MENU (forms menu - absent to hide "Forms" menu from admin app)
     * </ul>
     *
     * @param cache       the data cache
     * @param theUsername the username of the user whose permissions to load
     * @throws SQLException if there is an error accessing the database
     */
    /* default */ FixedData(final Cache cache, final String theUsername) throws SQLException {

        this.username = theUsername;

        this.activeTerm = TermLogic.get(cache).queryActive(cache);

        this.termWeeks = RawSemesterCalendarLogic.INSTANCE.queryAll(cache);
        Collections.sort(this.termWeeks);

        this.holdTypes = RawHoldTypeLogic.INSTANCE.queryAll(cache);

        this.userPermissions = RawUserClearanceLogic.queryAllForLogin(cache, theUsername);
    }

    /**
     * Tests whether the logged in user has a specified permission, and if so, returns the clearance level.
     *
     * @param permission the permission for which to test
     * @return {@code null} if the user does not have the permission, a clearance level from 1 (highest) to 5 (lowest)
     *         otherwise
     */
    public Integer getClearanceLevel(final String permission) {

        Integer clearance = null;

        for (final RawUserClearance p : this.userPermissions) {
            if (permission.equals(p.clearFunction)) {
                clearance = p.clearType;
                break;
            }
        }

        return clearance;
    }
}
