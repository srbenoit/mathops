package dev.mathops.session.sitelogic.data;

import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
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

    /**
     * Pre-loads all student term records so the registration data module can see if changes need to be made to pace,
     * pace track, or first course.
     * <p>
     * This method should be called before registration data loading, but the main {@code loadData} method of this class
     * should be called after registration data is loaded and any changes to pace/pace track have been made.
     *
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean preload(final StudentData studentData) throws SQLException {

        // Clear, since we will call this again if the student term data was changed so what we end up with is current
        this.studentTerms.clear();

        final List<RawStterm> list = studentData.getStudentTerms();
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
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean loadData(final StudentData studentData) throws SQLException {

        return loadActiveTermMilestones(studentData)
                && loadIncTermMilestones(studentData);
    }

    /**
     * Loads the milestone and student milestone records for the active term.
     *
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadActiveTermMilestones(final StudentData studentData) throws SQLException {

        final SystemData sysData = studentData.getSystemData();

        final TermRec active = sysData.getActiveTerm();
        final String key = active.term.shortString;

        boolean success = loadStudentTerm(studentData, active);

        if (success) {
            final RawStterm stTerm = this.studentTerms.get(key);

            if (!(stTerm == null || stTerm.paceTrack == null || stTerm.pace == null)) {
                success = loadMilestones(studentData, stTerm, active);
            }
        }

        return success;
    }

    /**
     * Loads the milestone and student milestone records for all terms in which the student earned an incomplete that is
     * still active, but which are not counted in pace.
     *
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadIncTermMilestones(final StudentData studentData) throws SQLException {

        final SystemData sysData = studentData.getSystemData();
        final String studentId = studentData.getStudentId();

        boolean success = true;

        final List<RawStcourse> regs = this.owner.siteRegistrationData.getRegistrations();
        final int numRegs = regs.size();

        for (int i = 0; success && i < numRegs; ++i) {
            final RawStcourse reg = regs.get(i);
            if ("Y".equals(reg.iInProgress) && "N".equals(reg.iCounted) && reg.iTermKey != null) {

                final TermRec incTerm = sysData.getTerm(reg.iTermKey);

                if (loadStudentTerm(studentData, incTerm)) {
                    final String key = incTerm.term.shortString;
                    final RawStterm stTerm = this.studentTerms.get(key);

                    if (stTerm == null || stTerm.paceTrack == null || stTerm.pace == null) {
                        this.owner.setError("No historic term data for  " + incTerm.term.longString);
                        success = false;
                    } else {
                        success = loadMilestones(studentData, stTerm, incTerm);
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
     * @param studentData the student data object
     * @param term        the term
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadStudentTerm(final StudentData studentData, final TermRec term) throws SQLException {

        final RawStterm rec = studentData.getStudentTerm(term.term);

        if (rec != null) {
            this.studentTerms.put(term.term.shortString, rec);
        }

        return true;
    }

    /**
     * Loads the milestone and student milestone records for the active term.
     *
     * @param studentData the student data object
     * @param stTerm      the student term
     * @param term        the term
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadMilestones(final StudentData studentData, final RawStterm stTerm, final TermRec term)
            throws SQLException {

        final SystemData systemData = studentData.getSystemData();

        final String track = stTerm.paceTrack.substring(0, 1);

        final List<RawMilestone> allMilestones = systemData.getMilestones(term.term, stTerm.pace, track);
        this.milestones.put(term.term, allMilestones);

        final List<RawStmilestone> stuMilestones = studentData.getStudentMilestones(term.term, track);
        this.studentMilestones.put(term.term, stuMilestones);

        return true;
    }
}
