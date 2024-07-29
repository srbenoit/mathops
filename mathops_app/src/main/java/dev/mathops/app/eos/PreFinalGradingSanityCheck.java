package dev.mathops.app.eos;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A batch that scans all active registrations, computing the course score in each from scratch, and testing the
 * "completed" status of each to ensure all courses with passing scores have been marked as complete, and none with
 * non-passing scores are marked complete.
 */
final class PreFinalGradingSanityCheck {

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The database context. */
    private final DbContext ctx;

    /**
     * Constructs a new {@code PreFinalGradingSanityCheck}.
     */
    private PreFinalGradingSanityCheck() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        if (this.dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else {
            try {
                final DbConnection conn = this.ctx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    final TermRec active = cache.getSystemData().getActiveTerm();

                    final List<RawStcourse> allRegs = RawStcourseLogic.queryActiveForActiveTerm(cache);

                    Log.info("Found ", Integer.toString(allRegs.size()), " active registrations");

                    // Sort into lists of courses by student
                    final Map<String, List<RawStcourse>> map = new HashMap<>(allRegs.size());

                    for (final RawStcourse reg : allRegs) {
                        final List<RawStcourse> list = map.computeIfAbsent(reg.stuId, s -> new ArrayList<>(5));
                        list.add(reg);
                    }

                    int nbrBad = 0;
                    for (final Map.Entry<String, List<RawStcourse>> entry : map.entrySet()) {
                        nbrBad += processStudent(cache, active, entry.getKey(), entry.getValue());
                    }

                    Log.info("Found ", Integer.toString(nbrBad), " bad registration records");
                } finally {
                    this.ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Checks a single registration.
     *
     * @param cache     the data cache
     * @param active    the active term
     * @param studentId the student ID
     * @param regs      the list of registrations
     * @return the number of bad registrations found
     * @throws SQLException if there is an error accessing the database
     */
    private static int processStudent(final Cache cache, final TermRec active, final String studentId,
                                      final List<RawStcourse> regs) throws SQLException {

        int errors = 0;

        // Determine pace and pace track
        final int pace = PaceTrackLogic.determinePace(regs);
        final String track = PaceTrackLogic.determinePaceTrack(regs, pace);

        final SystemData systemData = cache.getSystemData();
        final Integer paceObj = Integer.valueOf(pace);

        // Determine the milestones for the pace/track combination
        final List<RawMilestone> milestones = systemData.getMilestones(active.term, paceObj, track);

        // Get student milestone overrides
        final List<RawStmilestone> stmilestones =
                RawStmilestoneLogic.getStudentMilestones(cache, active.term, track, studentId);
        stmilestones.sort(null);

        final List<RawMilestone> effectiveMilestones = stmilestones.isEmpty() ? milestones
                : computeEffectiveMilestones(milestones, stmilestones);

        // Sort registrations on pace order
        final int numRegs = regs.size();
        final Collection<RawStcourse> sorted = new ArrayList<>(numRegs);
        for (int i = 0; i < 5; ++i) {
            final Iterator<RawStcourse> iter = regs.iterator();
            while (iter.hasNext()) {
                final RawStcourse test = iter.next();
                if (Integer.valueOf(i).equals(test.paceOrder)) {
                    sorted.add(test);
                    iter.remove();
                    break;
                }
            }
        }
        if (!regs.isEmpty()) {
            // What remains are courses with no pace order - they should not be open, but sort by natural order and
            // add them anyway
            Collections.sort(regs);
            sorted.addAll(regs);
        }

        // Now process each course
        int paceIndex = 1;
        for (final RawStcourse reg : sorted) {
            if ("Y".equals(reg.openStatus)) {
                // Log.info("Processing " + reg.getStudentId(), " ", reg.getCourseId(), " ", reg.getOpenStatus());
                errors += processReg(cache, reg, paceIndex, effectiveMilestones);
            }
            ++paceIndex;
        }

        return errors;
    }

    /**
     * Processes a single registration
     *
     * @param cache               the data cache
     * @param reg                 the registration
     * @param paceIndex           the pace index
     * @param effectiveMilestones the effective milestones
     * @return the number (0 or 1 ) of error registrations
     * @throws SQLException if there is an error accessing the database
     */
    private static int processReg(final Cache cache, final RawStcourse reg, final int paceIndex,
                                  final Iterable<RawMilestone> effectiveMilestones) throws SQLException {

        final List<RawStexam> exams = RawStexamLogic.getExams(cache, reg.stuId, reg.course, true, "R", "U", "F");

        int score = 0;

        // Unit 1 review
        LocalDate deadlineRE1 = null;
        for (final RawMilestone ms : effectiveMilestones) {

            final int msIndex = ms.getIndex();
            final int msUnit = ms.getUnit();

            if (msIndex == paceIndex && msUnit == 1
                    && RawMilestone.UNIT_REVIEW_EXAM.equals(ms.msType)) {
                deadlineRE1 = ms.msDate;
            }
        }
        if (deadlineRE1 != null) {
            LocalDate firstPassedRE1 = null;
            for (final RawStexam test : exams) {
                if ((Integer.valueOf(1).equals(test.unit) && "R".equals(test.examType))
                        && (firstPassedRE1 == null || firstPassedRE1.isAfter(test.examDt))) {
                    firstPassedRE1 = test.examDt;
                }
            }
            if (firstPassedRE1 != null && !firstPassedRE1.isAfter(deadlineRE1)) {
                score += 3;
            }
        }

        // Unit 1 exam
        int highestUE1 = 0;
        for (final RawStexam test : exams) {
            if (Integer.valueOf(1).equals(test.unit) && "U".equals(test.examType) && test.examScore != null) {
                highestUE1 = Math.max(highestUE1, test.examScore.intValue());
            }
        }
        score += highestUE1;

        // Unit 2 review
        LocalDate deadlineRE2 = null;
        for (final RawMilestone ms : effectiveMilestones) {

            // nsNbr is [pace] [index] [unit]
            final int msNbr = ms.msNbr.intValue();
            final int msIndex = (msNbr / 10) % 10;
            final int msUnit = msNbr % 10;

            if (msIndex == paceIndex && msUnit == 2 && RawMilestone.UNIT_REVIEW_EXAM.equals(ms.msType)) {
                deadlineRE2 = ms.msDate;
            }
        }
        if (deadlineRE2 != null) {
            LocalDate firstPassedRE2 = null;
            for (final RawStexam test : exams) {
                if ((Integer.valueOf(2).equals(test.unit) && "R".equals(test.examType))
                        && (firstPassedRE2 == null || firstPassedRE2.isAfter(test.examDt))) {
                    firstPassedRE2 = test.examDt;
                }
            }
            if (firstPassedRE2 != null && !firstPassedRE2.isAfter(deadlineRE2)) {
                score += 3;
            }
        }

        // Unit 2 exam
        int highestUE2 = 0;
        for (final RawStexam test : exams) {
            if (Integer.valueOf(2).equals(test.unit) && "U".equals(test.examType) && test.examScore != null) {
                highestUE2 = Math.max(highestUE2, test.examScore.intValue());
            }
        }
        score += highestUE2;

        // Unit 3 review
        LocalDate deadlineRE3 = null;
        for (final RawMilestone ms : effectiveMilestones) {

            // nsNbr is [pace] [index] [unit]
            final int msNbr = ms.msNbr.intValue();
            final int msIndex = (msNbr / 10) % 10;
            final int msUnit = msNbr % 10;

            if (msIndex == paceIndex && msUnit == 3 && RawMilestone.UNIT_REVIEW_EXAM.equals(ms.msType)) {
                deadlineRE3 = ms.msDate;
            }
        }
        if (deadlineRE3 != null) {
            LocalDate firstPassedRE3 = null;
            for (final RawStexam test : exams) {
                if ((Integer.valueOf(3).equals(test.unit) && "R".equals(test.examType))
                        && (firstPassedRE3 == null || firstPassedRE3.isAfter(test.examDt))) {
                    firstPassedRE3 = test.examDt;
                }
            }
            if (firstPassedRE3 != null && !firstPassedRE3.isAfter(deadlineRE3)) {
                score += 3;
            }
        }

        // Unit 3 exam
        int highestUE3 = 0;
        for (final RawStexam test : exams) {
            if (Integer.valueOf(3).equals(test.unit) && "U".equals(test.examType) && test.examScore != null) {
                highestUE3 = Math.max(highestUE3, test.examScore.intValue());
            }
        }
        score += highestUE3;

        // Unit 4 review
        LocalDate deadlineRE4 = null;
        for (final RawMilestone ms : effectiveMilestones) {

            // nsNbr is [pace] [index] [unit]
            final int msNbr = ms.msNbr.intValue();
            final int msIndex = (msNbr / 10) % 10;
            final int msUnit = msNbr % 10;

            if (msIndex == paceIndex && msUnit == 4 && RawMilestone.UNIT_REVIEW_EXAM.equals(ms.msType)) {
                deadlineRE4 = ms.msDate;
            }
        }
        if (deadlineRE4 != null) {
            LocalDate firstPassedRE4 = null;
            for (final RawStexam test : exams) {
                if ((Integer.valueOf(4).equals(test.unit) && "R".equals(test.examType))
                        && (firstPassedRE4 == null || firstPassedRE4.isAfter(test.examDt))) {
                    firstPassedRE4 = test.examDt;
                }
            }
            if (firstPassedRE4 != null && !firstPassedRE4.isAfter(deadlineRE4)) {
                score += 3;
            }
        }

        // Unit 4 exam
        int highestUE4 = 0;
        for (final RawStexam test : exams) {
            if (Integer.valueOf(4).equals(test.unit) && "U".equals(test.examType) && test.examScore != null) {
                highestUE4 = Math.max(highestUE4, test.examScore.intValue());
            }
        }
        score += highestUE4;

        // Final exam
        int highestFIN = 0;
        for (final RawStexam test : exams) {
            if (Integer.valueOf(5).equals(test.unit) && "F".equals(test.examType) && test.examScore != null) {
                highestFIN = Math.max(highestFIN, test.examScore.intValue());
            }
        }
        score += highestFIN;

        int error = 0;

        if (score >= 54) {
            if (!"Y".equals(reg.completed)) {
                Log.warning("Student ", reg.stuId, " - ", reg.course, " score was " + score
                        + " but COMPLETED was not set");

                // final StudentCourseCache scc = StudentCourseCache.get(this.dbProfile);
                // reg.setIsCompleted(Boolean.TRUE);
                // reg.setCourseScore(Integer.valueOf(score));
                // scc.updateCompletedScore(ERole.ADMINISTRATOR, reg);

                error = 1;
            }
        } else if ("Y".equals(reg.completed)) {
            Log.warning("Student ", reg.stuId, " - ", reg.course, " score was " + score + " but COMPLETED was set");
            error = 1;

            if (!Integer.valueOf(score).equals(reg.score)) {
                Log.info("Student ", reg.stuId, " - ", reg.course, " score was ", Integer.toString(score),
                        " but STCOURSE row says ", reg.score);
            }
        }

        return error;
    }

    /**
     * Takes a list of milestones and replaces any that have student milestone records with an updated milestone.
     *
     * @param milestones   the original milestones
     * @param stmilestones the list of student milestone overrides (not empty)
     * @return the updated list of effective milestones
     */
    private static List<RawMilestone> computeEffectiveMilestones(
            final List<RawMilestone> milestones, final Iterable<RawStmilestone> stmilestones) {

        final int count = milestones.size();
        for (int i = 0; i < count; ++i) {
            final RawMilestone ms = milestones.get(i);

            for (final RawStmilestone test : stmilestones) {

                if (test.paceTrack.equals(ms.paceTrack) && test.msNbr.equals(ms.msNbr)
                        && test.msType.equals(ms.msType)) {

                    final RawMilestone override = new RawMilestone();

                    override.termKey = ms.termKey;
                    override.pace = ms.pace;
                    override.paceTrack = ms.paceTrack;
                    override.msNbr = ms.msNbr;
                    override.msType = ms.msType;
                    override.msDate = test.msDate;
                    override.nbrAtmptsAllow = test.nbrAtmptsAllow;

                    milestones.set(i, override);
                    // Don't break - student milestones are sorted by deadline date, and if there are multiple, we want
                    // the later date
                }
            }
        }

        return milestones;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final PreFinalGradingSanityCheck job = new PreFinalGradingSanityCheck();

        job.execute();
    }
}
