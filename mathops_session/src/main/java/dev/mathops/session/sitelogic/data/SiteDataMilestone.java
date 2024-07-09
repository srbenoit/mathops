package dev.mathops.session.sitelogic.data;

import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A container for the milestone-oriented data relating to a {@code SiteData} object.
 */
public final class SiteDataMilestone {

    /** The data object that owns this object. */
    private final SiteData owner;

    /** The student term records. */
    private final Map<String, RawStterm> studentTerms;

    /** The milestone records. */
    private final Map<TermKey, List<RawMilestone>> milestones;

    /** The student milestone records. */
    private final Map<TermKey, List<RawStmilestone>> studentMilestones;

    /**
     * Constructs a new {@code SiteDataMilestone}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataMilestone(final SiteData theOwner) {

        this.owner = theOwner;

        this.studentTerms = new TreeMap<>();
        this.milestones = new TreeMap<>();
        this.studentMilestones = new TreeMap<>();
    }

    /**
     * Gets the student term record for a particular term.
     *
     * @param termStr the term string
     * @return the student term records (CStudentTerm)
     */
    public RawStterm getStudentTerm(final String termStr) {

        return this.studentTerms.get(termStr);
    }

    /**
     * Gets the milestone records.
     *
     * @param termKey the term key
     * @return the milestone records (RawMilestone)
     */
    public List<RawMilestone> getMilestones(final TermKey termKey) {

        final List<RawMilestone> list = this.milestones.get(termKey);

        return list == null ? new ArrayList<>(0) : new ArrayList<>(list);
    }

    /**
     * Gets the student milestone records.
     *
     * @param termKey the term key
     * @return the student milestone records (RawStmilestone)
     */
    public List<RawStmilestone> getStudentMilestones(final TermKey termKey) {

        final List<RawStmilestone> list = this.studentMilestones.get(termKey);

        return list == null ? new ArrayList<>(0) : new ArrayList<>(list);
    }

//    /**
//     * Given a list of milestones and a list of student milestone overrides, attempts to resolve the date associated
//     * with a milestone.
//     *
//     * @param milestones   the list of milestones
//     * @param stmilestones the list of student milestones
//     * @param pace         the pace (1 to 5)
//     * @param paceTrack    the pace track (such as "A")
//     * @param msNbr        the milestone number (pace, order, unit)
//     * @param msType       the milestone type
//     * @return the resolved date; null if not found
//     */
//    public static ResolvedMS resolveMilestone(final Iterable<? extends RawMilestone> milestones,
//                                              final Iterable<? extends RawStmilestone> stmilestones, final Integer
//                                              pace,
//                                              final String paceTrack, final Integer msNbr, final String msType) {
//
//        ResolvedMS result = null;
//
//        RawMilestone ms = null;
//        for (final RawMilestone test : milestones) {
//            if (test.pace.equals(pace) && test.paceTrack.equals(paceTrack) && test.msNbr.equals(msNbr)
//                    && test.msType.equals(msType)) {
//                ms = test;
//                break;
//            }
//        }
//
//        if (ms != null) {
//
//            RawStmilestone stms = null;
//            for (final RawStmilestone test : stmilestones) {
//                if (test.paceTrack.equals(paceTrack) && test.msNbr.equals(msNbr) && test.msType.equals(msType)) {
//                    stms = test;
//                    break;
//                }
//            }
//
//            if (stms == null) {
//                result = new ResolvedMS(ms.msDate, ms.nbrAtmptsAllow);
//            } else {
//                result = new ResolvedMS(stms.msDate, stms.nbrAtmptsAllow);
//            }
//        }
//
//        return result;
//    }

    /**
     * Pre-loads all student term records so the registration data module can see if changes need to be made to pace,
     * pace track, or first course.
     * <p>
     * This method should be called before registration data loading, but the main {@code loadData} method of this class
     * should be called after registration data is loaded and any changes to pace/pace track have been made.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean preload(final Cache cache) throws SQLException {

        final String studentId = this.owner.studentData.getStudent().stuId;

        // Clear, since we will call this again if the student term data was changed so what we
        // end up with is current
        this.studentTerms.clear();

        final List<RawStterm> list = RawSttermLogic.queryByStudent(cache, studentId);
        for (final RawStterm test : list) {
            this.studentTerms.put(test.termKey.shortString, test);
        }

        return true;
    }

    /**
     * Queries all database data relevant to a session's effective user ID within the session's context.
     * <p>
     * At the time this method is called; the {@code SiteData} object will have loaded the active term, all calendar
     * records, all pace track rules, the {@code SiteDataContext} object, the {@code SiteDataStudent} object, and the
     * {@code SuteDataProfile} object.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean loadData(final Cache cache) throws SQLException {

        final String studentId = this.owner.studentData.getStudent().stuId;

        return loadActiveTermMilestones(cache, studentId) && loadIncTermMilestones(cache, studentId);
    }

    /**
     * Loads the milestone and student milestone records for the active term.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadActiveTermMilestones(final Cache cache, final String studentId) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        final String key = active.term.shortString;

        boolean success = loadStudentTerm(cache, studentId, active);

        if (success) {
            final RawStterm stTerm = this.studentTerms.get(key);

            if (!(stTerm == null || stTerm.paceTrack == null || stTerm.pace == null)) {
                success = loadMilestones(cache, studentId, stTerm, active);
            }
        }

        return success;
    }

    /**
     * Loads the milestone and student milestone records for all terms in which the student earned an incomplete that is
     * still active, but which are not counted in pace.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadIncTermMilestones(final Cache cache, final String studentId) throws SQLException {

        boolean success = true;

        final List<RawStcourse> regs = this.owner.registrationData.getRegistrations();
        final int numRegs = regs.size();

        for (int i = 0; success && i < numRegs; ++i) {
            final RawStcourse reg = regs.get(i);
            if ("Y".equals(reg.iInProgress) && "N".equals(reg.iCounted) && reg.iTermKey != null) {

                final TermRec incTerm = cache.getSystemData().getTerm(reg.iTermKey);

                if (loadStudentTerm(cache, studentId, incTerm)) {
                    final String key = incTerm.term.shortString;
                    final RawStterm stTerm = this.studentTerms.get(key);

                    if (stTerm == null || stTerm.paceTrack == null || stTerm.pace == null) {
                        this.owner.setError("No historic term data for  " + incTerm.term.longString);
                        success = false;
                    } else {
                        success = loadMilestones(cache, studentId, stTerm, incTerm);
                    }
                } else {
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * Loads the student term record for a specified term.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param term      the term
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadStudentTerm(final Cache cache, final String studentId, final TermRec term) throws SQLException {

        final RawStterm rec = RawSttermLogic.query(cache, term.term, studentId);

        if (rec != null) {
            this.studentTerms.put(term.term.shortString, rec);
        }

        return true;
    }

    /**
     * Loads the milestone and student milestone records for the active term.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param stTerm    the student term
     * @param term      the term
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadMilestones(final Cache cache, final String studentId, final RawStterm stTerm,
                                   final TermRec term) throws SQLException {

        final String track = stTerm.paceTrack.substring(0, 1);

        final List<RawMilestone> allMilestones = RawMilestoneLogic.getAllMilestones(cache, term.term,
                stTerm.pace.intValue(), track);
        Collections.sort(allMilestones);
        this.milestones.put(term.term, allMilestones);

        final List<RawStmilestone> stuMilestones = RawStmilestoneLogic.getStudentMilestones(cache, term.term, track,
                studentId);
        stuMilestones.sort(null);

        this.studentMilestones.put(term.term, stuMilestones);

        return true;
    }

//    /** A resolved milestone. */
//    public static class ResolvedMS {
//
//        /** The milestone date. */
//        public final LocalDate date;
//
//        /** The number of attempts allowed. */
//        public final Integer attempts;
//
//        /**
//         * Constructs a new {@code ResolvedMS}.
//         *
//         * @param theDate     the milestone date
//         * @param theAttempts the number of attempts allowed
//         */
//        ResolvedMS(final LocalDate theDate, final Integer theAttempts) {
//
//            this.date = theDate;
//            this.attempts = theAttempts;
//        }
//    }
}
