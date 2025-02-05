package dev.mathops.dbjobs.batch;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A utility class that generates a list of all students who have passed all course exams but do not yet have 54 points
 * needed to pass the course.
 */
final class StillNeeds54Points {

    /** The date after the drop date for section 1 (day before census). */
    private static final LocalDate DAY_AFTER_DROP_SECT_1 = LocalDate.of(2025, 2, 4);

    /** The date after the drop date for section 2 (Wednesday of week 10). */
    private static final LocalDate DAY_AFTER_DROP_SECT_2 = LocalDate.of(2025, 4, 2);

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code StillNeeds54Points}.
     */
    private StillNeeds54Points() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        if (this.profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            try {
                final Cache cache = new Cache(this.profile);
                exec(cache);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Executes the job.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void exec(final Cache cache) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, active.term, false, true);

        final Iterator<RawStcourse> iter = allRegs.iterator();
        while (iter.hasNext()) {
            final RawStcourse reg = iter.next();

            // If dropped before drop deadline disregard
            // Remove incompletes from this list
            if ("002".equals(reg.sect)) {
                if (("D".equals(reg.openStatus) && reg.lastClassRollDt.isBefore(DAY_AFTER_DROP_SECT_2))
                    || "Y".equals(reg.iInProgress)) {
                    iter.remove();
                    continue;
                }
            } else {
                if (("D".equals(reg.openStatus) && reg.lastClassRollDt.isBefore(DAY_AFTER_DROP_SECT_1))
                    || "Y".equals(reg.iInProgress)) {
                    iter.remove();
                    continue;
                }
            }

            final String course = reg.course;
            switch (course) {
                case RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124 -> {
                    final String sect = reg.sect;

                    // NOTE: For these courses, we only disregard 550

                    if ("550".equals(sect)) {
                        iter.remove();
                    }
                }
                case RawRecordConstants.M125, RawRecordConstants.M126 -> {
                    final String sect = reg.sect;

                    // NOTE: For these, we disregard, 550, 003, and 004 (face-to-face)

                    if ("550".equals(sect) || "003".equals(sect) || "004".equals(sect)) {
                        iter.remove();
                    }
                }
                case RawRecordConstants.MATH125, RawRecordConstants.MATH126 ->
                    // New courses, remove.
                        iter.remove();
                case null, default -> {
                    Log.warning("Unexpected course: ", course);
                    iter.remove();
                }
            }
        }

        // Organize registrations by student
        final Map<String, List<RawStcourse>> regsByStudent = new HashMap<>(10);
        for (final RawStcourse reg : allRegs) {
            final List<RawStcourse> list = regsByStudent.computeIfAbsent(reg.stuId, s -> new ArrayList<>(5));
            list.add(reg);
        }

        // For each student, if there is a row with open status not "D" for a course, remove any
        // that have open status "D", and if they are all "D", leave only one
        for (final List<RawStcourse> list : regsByStudent.values()) {

            int num117 = 0;
            int num118 = 0;
            int num124 = 0;
            int num125 = 0;
            int num126 = 0;
            int num117d = 0;
            int num118d = 0;
            int num124d = 0;
            int num125d = 0;
            int num126d = 0;

            for (final RawStcourse rec : list) {
                if ("D".equals(rec.openStatus)) {
                    if (RawRecordConstants.M117.equals(rec.course)) {
                        ++num117d;
                    } else if (RawRecordConstants.M118.equals(rec.course)) {
                        ++num118d;
                    } else if (RawRecordConstants.M124.equals(rec.course)) {
                        ++num124d;
                    } else if (RawRecordConstants.M125.equals(rec.course)) {
                        ++num125d;
                    } else if (RawRecordConstants.M126.equals(rec.course)) {
                        ++num126d;
                    }
                } else if (RawRecordConstants.M117.equals(rec.course)) {
                    ++num117;
                } else if (RawRecordConstants.M118.equals(rec.course)) {
                    ++num118;
                } else if (RawRecordConstants.M124.equals(rec.course)) {
                    ++num124;
                } else if (RawRecordConstants.M125.equals(rec.course)) {
                    ++num125;
                } else if (RawRecordConstants.M126.equals(rec.course)) {
                    ++num126;
                }
            }

            if (num117 > 0) {
                if (num117d > 0) {
                    removeAllDropped(list, RawRecordConstants.M117);
                }
            } else if (num117d > 1) {
                removeAllButOneDropped(list, RawRecordConstants.M117);
            }

            if (num118 > 0) {
                if (num118d > 0) {
                    removeAllDropped(list, RawRecordConstants.M118);
                }
            } else if (num118d > 1) {
                removeAllButOneDropped(list, RawRecordConstants.M118);
            }

            if (num124 > 0) {
                if (num124d > 0) {
                    removeAllDropped(list, RawRecordConstants.M124);
                }
            } else if (num124d > 1) {
                removeAllButOneDropped(list, RawRecordConstants.M124);
            }

            if (num125 > 0) {
                if (num125d > 0) {
                    removeAllDropped(list, RawRecordConstants.M125);
                }
            } else if (num125d > 1) {
                removeAllButOneDropped(list, RawRecordConstants.M125);
            }

            if (num126 > 0) {
                if (num126d > 0) {
                    removeAllDropped(list, RawRecordConstants.M126);
                }
            } else if (num126d > 1) {
                removeAllButOneDropped(list, RawRecordConstants.M126);
            }
        }

        for (final List<RawStcourse> list : regsByStudent.values()) {
            for (final RawStcourse reg : list) {
                if ("D".equals(reg.openStatus)) {
                    if (!"W".equals(reg.courseGrade)) {
                        Log.info("Marking withdrawal for ", reg.stuId, " in ", reg.course);

                        // FIXME:
//                         RawStcourseLogic.updateDroppedGrade(cache, reg.stuId, reg.course, reg.sect,
//                         reg.termKey, reg.lastClassRollDt, "W");
                    }
                } else if ("N".equals(reg.openStatus)) {

                    if (!"U".equals(reg.courseGrade)) {
                        Log.info("Marking open=N as U for ", reg.stuId, " in ", reg.course);

                        // FIXME:
//                         RawStcourseLogic.updateCompletedScoreGrade(cache, reg.stuId, reg.course,
//                         reg.sect, reg.termKey, reg.completed, null, "U");
                    }

                } else if ("G".equals(reg.openStatus)) {

                    if (!"U".equals(reg.courseGrade)) {
                        Log.info("Marking open=G as U for ", reg.stuId, " in ", reg.course);

                        // FIXME:
//                         RawStcourseLogic.updateCompletedScoreGrade(cache, reg.stuId, reg.course,
//                         reg.sect, reg.termKey, reg.completed, null, "U");
                    }

                } else if (reg.openStatus == null) {

                    if (!"U".equals(reg.courseGrade)) {
                        Log.info("Marking unopened as U for ", reg.stuId, " in ", reg.course);

                        // FIXME:
//                         RawStcourseLogic.updateCompletedScoreGrade(cache, reg.stuId, reg.course,
//                         reg.sect, reg.termKey, reg.completed, null, "U");
                    }

                } else if (reg.paceOrder == null) {
                    Log.warning("Open stcourse row with no pace order for ", reg.stuId);
                } else {
                    processOpenReg(cache, reg);
                }
            }
        }
    }

    /**
     * Removes all registration records for a course that are marked as dropped from a list.
     *
     * @param list   the list
     * @param course the course to remove
     */
    private static void removeAllDropped(final Collection<RawStcourse> list, final String course) {

        list.removeIf(reg -> "D".equals(reg.openStatus) && course.equals(reg.course));
    }

    /**
     * Removes all but one registration records for a course that are marked as dropped from a list.
     *
     * @param list   the list
     * @param course the course to remove
     */
    private static void removeAllButOneDropped(final Iterable<RawStcourse> list, final String course) {

        final Iterator<RawStcourse> iter = list.iterator();
        boolean found = false;
        while (iter.hasNext()) {
            final RawStcourse reg = iter.next();

            if ("D".equals(reg.openStatus) && course.equals(reg.course)) {
                if (found) {
                    iter.remove();
                } else {
                    found = true;
                }
            }
        }
    }

    /**
     * Checks the status of a registration.
     *
     * @param cache the data cache
     * @param reg   the registration to check
     * @throws SQLException if there is an error accessing the database
     */
    private static void processOpenReg(final Cache cache, final RawStcourse reg) throws SQLException {

        // Determine student's pace

        final SystemData systemData = cache.getSystemData();

        final TermRec active = systemData.getActiveTerm();
        final RawStterm stterm = RawSttermLogic.query(cache, active.term, reg.stuId);

        // See when student first passed each review exam, accumulate best scores on units/finals

        final List<RawStexam> passedExams = RawStexamLogic.getExams(cache, reg.stuId, reg.course, true, "U", "R", "F");

        LocalDate whenPassedRE1 = null;
        LocalDate whenPassedRE2 = null;
        LocalDate whenPassedRE3 = null;
        LocalDate whenPassedRE4 = null;
        LocalDate whenPassedFIN = null;
        int scoreUE1 = 0;
        int scoreUE2 = 0;
        int scoreUE3 = 0;
        int scoreUE4 = 0;
        int scoreFIN = 0;

        for (final RawStexam test : passedExams) {
            switch (test.examType) {
                case "R" -> {
                    final int unit = test.unit == null ? -1 : test.unit.intValue();

                    if (unit == 1) {
                        if (whenPassedRE1 == null || test.examDt.isBefore(whenPassedRE1)) {
                            whenPassedRE1 = test.examDt;
                        }
                    } else if (unit == 2) {
                        if (whenPassedRE2 == null || test.examDt.isBefore(whenPassedRE2)) {
                            whenPassedRE2 = test.examDt;
                        }
                    } else if (unit == 3) {
                        if (whenPassedRE3 == null || test.examDt.isBefore(whenPassedRE3)) {
                            whenPassedRE3 = test.examDt;
                        }
                    } else if (unit == 4 && (whenPassedRE4 == null || test.examDt.isBefore(whenPassedRE4))) {
                        whenPassedRE4 = test.examDt;
                    }
                }
                case "U" -> {
                    if (test.examScore != null) {
                        final int unit = test.unit == null ? -1 : test.unit.intValue();
                        final int score = test.examScore.intValue();

                        if (unit == 1) {
                            scoreUE1 = Math.max(scoreUE1, score);
                        } else if (unit == 2) {
                            scoreUE2 = Math.max(scoreUE2, score);
                        } else if (unit == 3) {
                            scoreUE3 = Math.max(scoreUE3, score);
                        } else if (unit == 4) {
                            scoreUE4 = Math.max(scoreUE4, score);
                        }
                    }
                }
                case "F" -> {
                    if (test.examScore != null) {
                        final int score = test.examScore.intValue();
                        scoreFIN = Math.max(scoreFIN, score);

                        if (whenPassedFIN == null || test.examDt.isBefore(whenPassedFIN)) {
                            whenPassedFIN = test.examDt;
                        }
                    }
                }
                case null, default -> Log.warning("Bad exam type: ", test.examType);
            }
        }

        if (whenPassedFIN == null) {
            final List<RawStexam> allFinals = RawStexamLogic.getExams(cache, reg.stuId, reg.course, false, "F");
            int bestFIN = 0;

            for (final RawStexam test : allFinals) {
                if (test.examScore != null) {
                    final int score = test.examScore.intValue();
                    bestFIN = Math.max(bestFIN, score);
                }
            }

            if (bestFIN >= 15) {
                Log.info(reg.stuId, " did not pass ", reg.course, " but had ", Integer.toString(bestFIN),
                        " on the final exam.");
            }
        } else {
            final List<RawMilestone> allMilestones = systemData.getMilestones(active.term, stterm.pace,
                    stterm.paceTrack);

            final List<RawStmilestone> stMilestones = RawStmilestoneLogic
                    .getStudentMilestones(cache, active.term, stterm.paceTrack, reg.stuId);
            stMilestones.sort(null);

            // Find deadline for review exams
            LocalDate deadlineRE1 = null;
            LocalDate deadlineRE2 = null;
            LocalDate deadlineRE3 = null;
            LocalDate deadlineRE4 = null;

            for (final RawMilestone ms : allMilestones) {
                if ("RE".equals(ms.msType) && ms.getIndex() == reg.paceOrder.intValue()) {

                    if (ms.getUnit() == 1) {
                        deadlineRE1 = ms.msDate;
                        for (final RawStmilestone sms : stMilestones) {
                            if ("RE".equals(sms.msType) && sms.msNbr.equals(ms.msNbr)) {
                                deadlineRE1 = sms.msDate;
                                // Don't break - student milestones are sorted by deadline date, and if there are
                                // multiple, we want the later date
                            }
                        }
                    } else if (ms.getUnit() == 2) {
                        deadlineRE2 = ms.msDate;
                        for (final RawStmilestone sms : stMilestones) {
                            if ("RE".equals(sms.msType) && sms.msNbr.equals(ms.msNbr)) {
                                deadlineRE2 = sms.msDate;
                                // Don't break - student milestones are sorted by deadline date, and if there are
                                // multiple, we want the later date
                            }
                        }
                    } else if (ms.getUnit() == 3) {
                        deadlineRE3 = ms.msDate;
                        for (final RawStmilestone sms : stMilestones) {
                            if ("RE".equals(sms.msType) && sms.msNbr.equals(ms.msNbr)) {
                                deadlineRE3 = sms.msDate;
                                // Don't break - student milestones are sorted by deadline date, and if there are
                                // multiple, we want the later date
                            }
                        }
                    } else if (ms.getUnit() == 4) {
                        deadlineRE4 = ms.msDate;
                        for (final RawStmilestone sms : stMilestones) {
                            if ("RE".equals(sms.msType) && sms.msNbr.equals(ms.msNbr)) {
                                deadlineRE4 = sms.msDate;
                                // Don't break - student milestones are sorted by deadline date, and if there are
                                // multiple, we want the later date
                            }
                        }
                    }
                }
            }

            // Compute course point total
            int total = scoreUE1 + scoreUE2 + scoreUE3 + scoreUE4 + scoreFIN;

            if (whenPassedRE1 != null && deadlineRE1 != null && !whenPassedRE1.isAfter(deadlineRE1)) {
                total += 3;
            }
            if (whenPassedRE2 != null && deadlineRE2 != null && !whenPassedRE2.isAfter(deadlineRE2)) {
                total += 3;
            }
            if (whenPassedRE3 != null && deadlineRE3 != null && !whenPassedRE3.isAfter(deadlineRE3)) {
                total += 3;
            }
            if (whenPassedRE4 != null && deadlineRE4 != null && !whenPassedRE4.isAfter(deadlineRE4)) {
                total += 3;
            }

            if (total < 54) {
                Log.info(reg.stuId, " has ", Integer.toString(total), " in ", reg.course);
            } else if ("Y".equals(reg.completed)) {
                final Integer totalInt = Integer.valueOf(total);

                final String grade;
                if (total >= 65) {
                    grade = "A";
                } else if (total >= 62) {
                    grade = "B";
                } else {
                    grade = "C";
                }

                if ((!totalInt.equals(reg.score) || !grade.equals(reg.courseGrade))) {
                    Log.info("Storing score of ", totalInt, " (", grade, ") for ", reg.stuId, " in ", reg.course);

                    // FIXME:
//                    RawStcourseLogic.updateCompletedScoreGrade(cache, reg.stuId, reg.course,
//                            reg.sect, reg.termKey, "Y", totalInt, grade);
                }
            } else {
                Log.warning(reg.course, " not marked as completed for ", reg.stuId);
            }
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        final StillNeeds54Points job = new StillNeeds54Points();

        job.execute();
    }
}
