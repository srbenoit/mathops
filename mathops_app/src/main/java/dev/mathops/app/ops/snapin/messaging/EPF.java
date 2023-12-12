package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A utility class that scans the database and determines any students who are far enough behind that they should be
 * given Early Performance Feedback (EPF).
 */
public enum EPF {
    ;

    /**
     * Executes the job.
     *
     * <p>
     * This process groups all registered students into buckets, where each bucket is some number of days behind
     * schedule, and there is a single bucket for all students who are on time or ahead of schedule, and separate
     * buckets for students who have not started their course or not passed the user's exam.
     *
     * @param cache             the data cache
     * @param incCourseSections map from course ID to a list of section numbers to include in the scan
     * @return a map from the number of days behind to the list of CSU IDs of students in that state, where -2 means
     *         "not started", "-1" means "no user's exam", and "0" means on-time or ahead
     */
    private static Map<Integer, List<String>> calculate(final Cache cache,
                                                        final Map<String, ? extends List<String>> incCourseSections) {

        final Map<Integer, List<String>> result = new HashMap<>(30);

        try {
            exec(cache, incCourseSections, result);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        return result;
    }

    /**
     * Given the result from the {@code calculate} method, gathers a list of CSU IDs of students who are some number of
     * days (or more) late.
     *
     * @param calculateResult the result from {@code calculate}
     * @param numDays         the number of days
     * @return the list of all students who have either not started (with key -2), not passed the User's exam (with key
     *         -1), or are at least {@code numDays} late
     */
    private static List<String> studentsNDaysLate(final Map<Integer, List<String>> calculateResult,
                                                  final int numDays) {

        final List<String> result = new ArrayList<>(100);

        for (final Map.Entry<Integer, List<String>> entry : calculateResult.entrySet()) {
            final int key = entry.getKey().intValue();

            if (key < 0 || key >= numDays) {
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    /**
     * Executes the job.
     *
     * @param cache             the data cache
     * @param incCourseSections map from course ID to a list of section numbers to include in the scan
     * @param result            a map to be populated by this method - keys are the number of days behind, where -2
     *                          means "not started", "-1" means "no user's exam", and "0" means on-time or ahead,
     *                          and values are the list of CSU IDs of students in that condition
     * @throws SQLException if there is an error accessing the database
     */
    private static void exec(final Cache cache, final Map<String, ? extends List<String>> incCourseSections,
                             final Map<? super Integer, List<String>> result) throws SQLException {

        // Map from student ID to map from course ID to registration
        final Map<String, List<RawStcourse>> stuRegs = gatherMatchingRegistrations(cache, incCourseSections);

        final TermRec act = TermLogic.get(cache).queryActive(cache);
        if (act == null) {
            Log.warning("Unable to query the active term.");
        } else {
            // Map from pace to map from pace track to milestones for that pace/track
            final Map<Integer, Map<String, List<RawMilestone>>> msMap = gatherMilestones(cache, act.term);

            final LocalDate today = LocalDate.now();

            for (final Map.Entry<String, List<RawStcourse>> e : stuRegs.entrySet()) {
                final String stuId = e.getKey();
                processStudent(cache, stuId, e.getValue(), today, msMap, act, result);
            }
        }
    }

    /**
     * Queries all active registrations and filters that result to the requested "include" list (we also remove
     * incompletes here, since they don't need EPF). Then we organize into a map whose key is student ID, then key is
     * course ID.
     *
     * @param cache             the data cache
     * @param incCourseSections map from course ID to a list of section numbers to include in the scan
     * @return a map from student ID to the list of that student's registrations
     * @throws SQLException if there is an error accessing the database
     */
    public static Map<String, List<RawStcourse>> gatherMatchingRegistrations(
            final Cache cache, final Map<String, ? extends List<String>> incCourseSections) throws SQLException {

        final List<RawStcourse> allRegs = RawStcourseLogic.queryActiveForActiveTerm(cache);
        final Map<String, List<RawStcourse>> stuRegs = new HashMap<>(3000);

        final Iterator<RawStcourse> iter = allRegs.iterator();
        while (iter.hasNext()) {
            final RawStcourse reg = iter.next();

            if ("Y".equals(reg.iInProgress)) {
                iter.remove();
            } else {
                final List<String> includeSections = incCourseSections.get(reg.course);

                if (includeSections == null || !includeSections.contains(reg.sect)) {
                    iter.remove();
                } else {
                    final String stuId = reg.stuId;
                    final List<RawStcourse> map = stuRegs.computeIfAbsent(stuId, s -> new ArrayList<>(5));
                    map.add(reg);
                }
            }
        }

        return stuRegs;
    }

    /**
     * Gathers all milestones configured for the active term and organizes them by pace and pace track.
     *
     * @param cache     the data cache
     * @param activeKey the active term key
     * @return a map from pace to a map from pace track to a list of milestones
     * @throws SQLException if there is an error accessing the database
     */
    public static Map<Integer, Map<String, List<RawMilestone>>> gatherMilestones(
            final Cache cache, final TermKey activeKey) throws SQLException {

        // Next, we get all milestones and organize into a map from pace to map from track to list of milestones.
        final Map<Integer, Map<String, List<RawMilestone>>> msMap = new HashMap<>(5);

        final List<RawMilestone> allMilestones = RawMilestoneLogic.getAllMilestones(cache, activeKey);

        for (final RawMilestone ms : allMilestones) {
            final Integer paceKey = ms.pace;

            final Map<String, List<RawMilestone>> paceMap = msMap.computeIfAbsent(paceKey, k -> new HashMap<>(5));
            final String track = ms.paceTrack;
            final List<RawMilestone> msList = paceMap.computeIfAbsent(track, s -> new ArrayList<>(8));
            msList.add(ms);
        }

        return msMap;
    }

    /**
     * Processes a single student's registrations.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param regs   the student's registrations (sorted map from course ID to registration)
     * @param today  the current date
     * @param msMap  map from pace to a map from track to list of milestones
     * @param active the active term
     * @param result a map to be populated by this method - keys are the number of days behind, where -2 means "not
     *               started", "-1" means "no user's exam", and "0" means on-time or ahead, and values are the list of
     *               CSU IDs of students in that condition
     * @throws SQLException if there is an error accessing the database
     */
    private static void processStudent(final Cache cache, final String stuId, final List<RawStcourse> regs,
                                       final ChronoLocalDate today,
                                       final Map<Integer, ? extends Map<String, List<RawMilestone>>> msMap,
                                       final TermRec active, final Map<? super Integer, List<String>> result)
            throws SQLException {

        RawStcourse sc1 = null;
        RawStcourse sc2 = null;
        RawStcourse sc3 = null;
        RawStcourse sc4 = null;
        RawStcourse sc5 = null;

        final List<RawStcourse> nulls = new ArrayList<>(10);
        for (final RawStcourse reg : regs) {
            if (reg.paceOrder == null) {
                nulls.add(reg);
            } else {
                final int order = reg.paceOrder.intValue();
                if (order == 1) {
                    sc1 = reg;
                } else if (order == 2) {
                    sc2 = reg;
                } else if (order == 3) {
                    sc3 = reg;
                } else if (order == 4) {
                    sc4 = reg;
                } else if (order == 5) {
                    sc5 = reg;
                }
            }
        }

        if (sc1 == null && !nulls.isEmpty()) {
            sc1 = nulls.remove(0);
        }
        if (sc2 == null && !nulls.isEmpty()) {
            sc2 = nulls.remove(0);
        }
        if (sc3 == null && !nulls.isEmpty()) {
            sc3 = nulls.remove(0);
        }
        if (sc4 == null && !nulls.isEmpty()) {
            sc4 = nulls.remove(0);
        }
        if (sc5 == null && !nulls.isEmpty()) {
            sc5 = nulls.remove(0);
        }

        if (sc1 == null) {
            Log.warning("NO FIRST COURSE FOR ", stuId, ", SKIPPING");
        } else {
            final int pace = PaceTrackLogic.determinePace(regs);
            final String track = PaceTrackLogic.determinePaceTrack(regs, pace);

            // Skip "face to face" students until we know better how to assess them.
            if (!"D".equals(track)) {

                final Integer paceInt = Integer.valueOf(pace);
                final List<RawMilestone> milestones = msMap.get(paceInt).get(track);
                final List<RawStmilestone> stmilestones =
                        RawStmilestoneLogic.getStudentMilestones(cache, active.term, track, stuId);

                final RawStudent stu = RawStudentLogic.query(cache, stuId, false);
                if (stu == null) {
                    Log.warning("ERROR: No student record for ", stuId);
                } else {
                    final List<RawStexam> exams = RawStexamLogic.queryByStudent(cache, stuId, false);

                    switch (pace) {
                        case 1:
                            processPace1Student(stu, regs, today, milestones, stmilestones, exams, result);
                            break;
                        case 2:
                            processPace2Student(stu, regs, today, milestones, stmilestones, exams, result);
                            break;
                        case 3:
                            processPace3Student(stu, regs, today, milestones, stmilestones, exams, result);
                            break;
                        case 4:
                            processPace4Student(stu, regs, today, milestones, stmilestones, exams, result);
                            break;
                        case 5:
                            processPace5Student(stu, regs, today, milestones, stmilestones, exams, result);
                            break;
                        default:
                            Log.warning("Unexpected pace: ", Integer.toString(pace));
                            break;
                    }
                }
            }
        }
    }

    /**
     * Processes a student in a 1-course pace.
     *
     * @param stu    the student
     * @param regs   the student's registrations (sorted map from course ID to registration)
     * @param today  the current date
     * @param ms     the milestones for the course
     * @param stuMs  the student milestone overrides
     * @param exams  the student's exams
     * @param result a map to be populated by this method - keys are the number of days behind, where -2 means "not
     *               started", "-1" means "no user's exam", and "0" means on-time or ahead, and values are the list of
     *               CSU IDs of students in that condition
     */
    private static void processPace1Student(final RawStudent stu, final List<RawStcourse> regs,
                                            final ChronoLocalDate today, final Iterable<RawMilestone> ms,
                                            final Iterable<RawStmilestone> stuMs, final Iterable<RawStexam> exams,
                                            final Map<? super Integer, List<String>> result) {

        // Log.info("Processing 1-course student");

        boolean needsToStart = true;
        for (final RawStcourse reg : regs) {
            final String open = reg.openStatus;
            if ("Y".equals(open) || "N".equals(open)) {
                needsToStart = false;
                break;
            }
        }

        int daysBehind = 0;

        if (needsToStart) {
            daysBehind = -2;
        } else if ("Y".equals(stu.licensed)) {

            final List<RawStcourse> regsList = new ArrayList<>(regs);
            final RawStcourse reg1 = regsList.get(0);
            final String course1 = reg1.course;

            // Find the earliest milestone date of an activity that has not been completed

            LocalDate earliestNotComplete = null;
            if (isUnitReviewNotComplete(course1, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 111);
            } else if (isUnitReviewNotComplete(course1, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 112);
            } else if (isUnitReviewNotComplete(course1, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 113);
            } else if (isUnitReviewNotComplete(course1, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 114);
            } else if (isFinalNotComplete(course1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 115);
            }

            if (earliestNotComplete != null) {
                while (earliestNotComplete.isBefore(today)) {
                    ++daysBehind;
                    earliestNotComplete = earliestNotComplete.plusDays(1L);
                }
            }
        } else {
            daysBehind = -1;
        }

        final Integer key = Integer.valueOf(daysBehind);
        final List<String> list = result.computeIfAbsent(key, object -> new ArrayList<>(100));
        list.add(stu.stuId);
    }

    /**
     * Processes a student in a 2-course pace.
     *
     * @param stu    the student
     * @param regs   the student's registrations (sorted map from course ID to registration)
     * @param today  the current date
     * @param ms     the milestones for the course
     * @param stuMs  the student milestone overrides
     * @param exams  the student's exams
     * @param result a map to be populated by this method - keys are the number of days behind, where -2 means "not
     *               started", "-1" means "no user's exam", and "0" means on-time or ahead, and values are the list of
     *               CSU IDs of students in that condition
     */
    private static void processPace2Student(final RawStudent stu, final List<RawStcourse> regs,
                                            final ChronoLocalDate today, final Iterable<RawMilestone> ms,
                                            final Iterable<RawStmilestone> stuMs, final Iterable<RawStexam> exams,
                                            final Map<? super Integer, List<String>> result) {

        boolean needsToStart = true;
        for (final RawStcourse reg : regs) {
            final String open = reg.openStatus;
            if ("Y".equals(open) || "N".equals(open)) {
                needsToStart = false;
                break;
            }
        }

        int daysBehind = 0;

        if (needsToStart) {
            daysBehind = -2;
        } else if ("Y".equals(stu.licensed)) {

            final List<RawStcourse> regsList = new ArrayList<>(regs);
            final List<RawStcourse> sorted = EmailsNeeded.sort2ByPaceOrder(regsList);
            final RawStcourse reg1 = sorted.get(0);
            final RawStcourse reg2 = sorted.get(1);
            final String course1 = reg1.course;
            final String course2 = reg2.course;

            // Find the earliest milestone date of an activity that has not been completed

            LocalDate earliestNotComplete = null;
            if (isUnitReviewNotComplete(course1, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 211);
            } else if (isUnitReviewNotComplete(course1, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 212);
            } else if (isUnitReviewNotComplete(course1, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 213);
            } else if (isUnitReviewNotComplete(course1, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 214);
            } else if (isFinalNotComplete(course1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 215);
            } else if (isUnitReviewNotComplete(course2, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 221);
            } else if (isUnitReviewNotComplete(course2, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 222);
            } else if (isUnitReviewNotComplete(course2, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 223);
            } else if (isUnitReviewNotComplete(course2, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 224);
            } else if (isFinalNotComplete(course2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 225);
            }

            if (earliestNotComplete != null) {
                while (earliestNotComplete.isBefore(today)) {
                    ++daysBehind;
                    earliestNotComplete = earliestNotComplete.plusDays(1L);
                }
            }
        } else {
            daysBehind = -1;
        }

        final Integer key = Integer.valueOf(daysBehind);
        final List<String> list = result.computeIfAbsent(key, object -> new ArrayList<>(100));
        list.add(stu.stuId);
    }

    /**
     * Processes a student in a 3-course pace.
     *
     * @param stu    the student
     * @param regs   the student's registrations (sorted map from course ID to registration)
     * @param today  the current date
     * @param ms     the milestones for the course
     * @param stuMs  the student milestone overrides
     * @param exams  the student's exams
     * @param result a map to be populated by this method - keys are the number of days behind, where -2 means "not
     *               started", "-1" means "no user's exam", and "0" means on-time or ahead, and values are the list of
     *               CSU IDs of students in that condition
     */
    private static void processPace3Student(final RawStudent stu, final List<RawStcourse> regs,
                                            final ChronoLocalDate today, final Iterable<RawMilestone> ms,
                                            final Iterable<RawStmilestone> stuMs, final Iterable<RawStexam> exams,
                                            final Map<? super Integer, List<String>> result) {

        boolean needsToStart = true;
        for (final RawStcourse reg : regs) {
            final String open = reg.openStatus;
            if ("Y".equals(open) || "N".equals(open)) {
                needsToStart = false;
                break;
            }
        }

        int daysBehind = 0;

        if (needsToStart) {
            daysBehind = -2;
        } else if ("Y".equals(stu.licensed)) {

            final List<RawStcourse> regsList = new ArrayList<>(regs);
            final List<RawStcourse> sorted = EmailsNeeded.sort3ByPaceOrder(regsList);
            final RawStcourse reg1 = sorted.get(0);
            final RawStcourse reg2 = sorted.get(1);
            final RawStcourse reg3 = sorted.get(2);
            final String course1 = reg1.course;
            final String course2 = reg2.course;
            final String course3 = reg3.course;

            // Find the earliest milestone date of an activity that has not been completed

            LocalDate earliestNotComplete = null;
            if (isUnitReviewNotComplete(course1, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 311);
            } else if (isUnitReviewNotComplete(course1, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 312);
            } else if (isUnitReviewNotComplete(course1, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 313);
            } else if (isUnitReviewNotComplete(course1, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 314);
            } else if (isFinalNotComplete(course1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 315);
            } else if (isUnitReviewNotComplete(course2, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 321);
            } else if (isUnitReviewNotComplete(course2, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 322);
            } else if (isUnitReviewNotComplete(course2, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 323);
            } else if (isUnitReviewNotComplete(course2, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 324);
            } else if (isFinalNotComplete(course2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 325);
            } else if (isUnitReviewNotComplete(course3, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 331);
            } else if (isUnitReviewNotComplete(course3, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 332);
            } else if (isUnitReviewNotComplete(course3, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 333);
            } else if (isUnitReviewNotComplete(course3, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 334);
            } else if (isFinalNotComplete(course3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 335);
            }

            if (earliestNotComplete != null) {
                while (earliestNotComplete.isBefore(today)) {
                    ++daysBehind;
                    earliestNotComplete = earliestNotComplete.plusDays(1L);
                }
            }
        } else {
            daysBehind = -1;
        }

        final Integer key = Integer.valueOf(daysBehind);
        final List<String> list = result.computeIfAbsent(key, object -> new ArrayList<>(100));
        list.add(stu.stuId);
    }

    /**
     * Processes a student in a 4-course pace.
     *
     * @param stu    the student
     * @param regs   the student's registrations (sorted map from course ID to registration)
     * @param today  the current date
     * @param ms     the milestones for the course
     * @param stuMs  the student milestone overrides
     * @param exams  the student's exams
     * @param result a map to be populated by this method - keys are the number of days behind, where -2 means "not
     *               started", "-1" means "no user's exam", and "0" means on-time or ahead, and values are the list of
     *               CSU IDs of students in that condition
     */
    private static void processPace4Student(final RawStudent stu, final List<RawStcourse> regs,
                                            final ChronoLocalDate today, final Iterable<RawMilestone> ms,
                                            final Iterable<RawStmilestone> stuMs, final Iterable<RawStexam> exams,
                                            final Map<? super Integer, List<String>> result) {

        boolean needsToStart = true;
        for (final RawStcourse reg : regs) {
            final String open = reg.openStatus;
            if ("Y".equals(open) || "N".equals(open)) {
                needsToStart = false;
                break;
            }
        }

        int daysBehind = 0;

        if (needsToStart) {
            daysBehind = -2;
        } else if ("Y".equals(stu.licensed)) {

            final List<RawStcourse> regsList = new ArrayList<>(regs);
            final List<RawStcourse> sorted = EmailsNeeded.sort4ByPaceOrder(regsList);
            final RawStcourse reg1 = sorted.get(0);
            final RawStcourse reg2 = sorted.get(1);
            final RawStcourse reg3 = sorted.get(2);
            final RawStcourse reg4 = sorted.get(3);
            final String course1 = reg1.course;
            final String course2 = reg2.course;
            final String course3 = reg3.course;
            final String course4 = reg4.course;

            // Find the earliest milestone date of an activity that has not been completed

            LocalDate earliestNotComplete = null;
            if (isUnitReviewNotComplete(course1, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 411);
            } else if (isUnitReviewNotComplete(course1, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 412);
            } else if (isUnitReviewNotComplete(course1, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 413);
            } else if (isUnitReviewNotComplete(course1, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 414);
            } else if (isFinalNotComplete(course1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 415);
            } else if (isUnitReviewNotComplete(course2, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 421);
            } else if (isUnitReviewNotComplete(course2, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 422);
            } else if (isUnitReviewNotComplete(course2, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 423);
            } else if (isUnitReviewNotComplete(course2, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 424);
            } else if (isFinalNotComplete(course2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 425);
            } else if (isUnitReviewNotComplete(course3, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 431);
            } else if (isUnitReviewNotComplete(course3, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 432);
            } else if (isUnitReviewNotComplete(course3, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 433);
            } else if (isUnitReviewNotComplete(course3, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 434);
            } else if (isFinalNotComplete(course3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 435);
            } else if (isUnitReviewNotComplete(course4, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 441);
            } else if (isUnitReviewNotComplete(course4, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 442);
            } else if (isUnitReviewNotComplete(course4, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 443);
            } else if (isUnitReviewNotComplete(course4, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 444);
            } else if (isFinalNotComplete(course4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 445);
            }

            if (earliestNotComplete != null) {
                while (earliestNotComplete.isBefore(today)) {
                    ++daysBehind;
                    earliestNotComplete = earliestNotComplete.plusDays(1L);
                }
            }
        } else {
            daysBehind = -1;
        }

        final Integer key = Integer.valueOf(daysBehind);
        final List<String> list = result.computeIfAbsent(key, object -> new ArrayList<>(100));
        list.add(stu.stuId);
    }

    /**
     * Processes a student in a 5-course pace.
     *
     * @param stu    the student
     * @param regs   the student's registrations (sorted map from course ID to registration)
     * @param today  the current date
     * @param ms     the milestones for the course
     * @param stuMs  the student milestone overrides
     * @param exams  the student's exams
     * @param result a map to be populated by this method - keys are the number of days behind, where -2 means "not
     *               started", "-1" means "no user's exam", and "0" means on-time or ahead, and values are the list of
     *               CSU IDs of students in that condition
     */
    private static void processPace5Student(final RawStudent stu, final List<RawStcourse> regs,
                                            final ChronoLocalDate today, final Iterable<RawMilestone> ms,
                                            final Iterable<RawStmilestone> stuMs, final Iterable<RawStexam> exams,
                                            final Map<? super Integer, List<String>> result) {

        boolean needsToStart = true;
        for (final RawStcourse reg : regs) {
            final String open = reg.openStatus;
            if ("Y".equals(open) || "N".equals(open)) {
                needsToStart = false;
                break;
            }
        }

        int daysBehind = 0;

        if (needsToStart) {
            daysBehind = -2;
        } else if ("Y".equals(stu.licensed)) {

            final List<RawStcourse> regsList = new ArrayList<>(regs);
            final List<RawStcourse> sorted = EmailsNeeded.sort5ByPaceOrder(regsList);
            final RawStcourse reg1 = sorted.get(0);
            final RawStcourse reg2 = sorted.get(1);
            final RawStcourse reg3 = sorted.get(2);
            final RawStcourse reg4 = sorted.get(3);
            final RawStcourse reg5 = sorted.get(4);
            final String course1 = reg1.course;
            final String course2 = reg2.course;
            final String course3 = reg3.course;
            final String course4 = reg4.course;
            final String course5 = reg5.course;

            // Find the earliest milestone date of an activity that has not been completed

            LocalDate earliestNotComplete = null;
            if (isUnitReviewNotComplete(course1, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 511);
            } else if (isUnitReviewNotComplete(course1, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 512);
            } else if (isUnitReviewNotComplete(course1, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 513);
            } else if (isUnitReviewNotComplete(course1, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 514);
            } else if (isFinalNotComplete(course1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 515);
            } else if (isUnitReviewNotComplete(course2, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 521);
            } else if (isUnitReviewNotComplete(course2, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 522);
            } else if (isUnitReviewNotComplete(course2, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 523);
            } else if (isUnitReviewNotComplete(course2, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 524);
            } else if (isFinalNotComplete(course2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 525);
            } else if (isUnitReviewNotComplete(course3, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 531);
            } else if (isUnitReviewNotComplete(course3, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 532);
            } else if (isUnitReviewNotComplete(course3, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 533);
            } else if (isUnitReviewNotComplete(course3, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 534);
            } else if (isFinalNotComplete(course3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 535);
            } else if (isUnitReviewNotComplete(course4, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 541);
            } else if (isUnitReviewNotComplete(course4, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 542);
            } else if (isUnitReviewNotComplete(course4, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 543);
            } else if (isUnitReviewNotComplete(course4, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 544);
            } else if (isFinalNotComplete(course4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 545);
            } else if (isUnitReviewNotComplete(course5, 1, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 551);
            } else if (isUnitReviewNotComplete(course5, 2, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 552);
            } else if (isUnitReviewNotComplete(course5, 3, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 553);
            } else if (isUnitReviewNotComplete(course5, 4, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "RE", 554);
            } else if (isFinalNotComplete(course5, exams)) {
                earliestNotComplete = findDate(ms, stuMs, "FE", 555);
            }

            if (earliestNotComplete != null) {
                while (earliestNotComplete.isBefore(today)) {
                    ++daysBehind;
                    earliestNotComplete = earliestNotComplete.plusDays(1L);
                }
            }
        } else {
            daysBehind = -1;
        }

        final Integer key = Integer.valueOf(daysBehind);
        final List<String> list = result.computeIfAbsent(key, object -> new ArrayList<>(100));
        list.add(stu.stuId);
    }

    /**
     * Tests whether the student still needs to pass a Unit review exam.
     *
     * @param course the course
     * @param unit   the unit
     * @param exams  the list of the student's exams
     * @return true if the student still needs to pass the Unit review exam
     */
    private static boolean isUnitReviewNotComplete(final String course, final int unit,
                                                   final Iterable<RawStexam> exams) {

        boolean missing = true;

        for (final RawStexam exam : exams) {
            if (exam.unit.intValue() == unit && course.equals(exam.course) && "R".equals(exam.examType)
                    && "Y".equals(exam.passed)) {
                missing = false;
                break;
            }
        }

        return missing;
    }

    /**
     * Tests whether the student still needs to pass a Final exam.
     *
     * @param course the course
     * @param exams  the list of the student's exams
     * @return true if the student still needs to pass the Final exam
     */
    private static boolean isFinalNotComplete(final String course, final Iterable<RawStexam> exams) {

        boolean missing = true;

        for (final RawStexam exam : exams) {
            if (course.equals(exam.course) && "F".equals(exam.examType) && "Y".equals(exam.passed)) {
                missing = false;
                break;
            }
        }

        return missing;
    }

    /**
     * Uses the Milestone and StMilestone records to generate a due date.
     *
     * @param milestones   the list of all milestones for the student's pace and track
     * @param stmilestones the list of student overrides (if any)
     * @param type         the milestone type whose date to find
     * @param number       the milestone number whose date to find
     * @return the milestone date
     */
    private static LocalDate findDate(final Iterable<RawMilestone> milestones,
                                      final Iterable<RawStmilestone> stmilestones, final String type, final int number) {

        LocalDate due = null;

        for (final RawMilestone ms : milestones) {
            if (ms.msType.equals(type) && ms.msNbr.intValue() == number) {
                due = ms.msDate;
            }
        }
        if (due != null) {
            for (final RawStmilestone sms : stmilestones) {
                if (sms.msType.equals(type) && sms.msNbr.intValue() == number) {
                    due = sms.msDate;
                }
            }
        }

        return due;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();

        final DbProfile dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);

        if (dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else {
            final DbContext dbCtx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

            if (dbCtx == null) {
                Log.warning("Unable to create database context.");
            } else {
                try {
                    final DbConnection conn = dbCtx.checkOutConnection();
                    final Cache cache = new Cache(dbProfile, conn);

                    final Map<String, List<String>> incCourseSections = new HashMap<>(10);

                    final String[] sections = {"001", "401", "801", "809"};
                    final List<String> sect117 = Arrays.asList(sections);
                    final List<String> sect118 = Arrays.asList(sections);
                    final List<String> sect124 = Arrays.asList(sections);
                    final List<String> sect125 = Arrays.asList(sections);
                    final List<String> sect126 = Arrays.asList(sections);

                    incCourseSections.put(RawRecordConstants.M117, sect117);
                    incCourseSections.put(RawRecordConstants.M118, sect118);
                    incCourseSections.put(RawRecordConstants.M124, sect124);
                    incCourseSections.put(RawRecordConstants.M125, sect125);
                    incCourseSections.put(RawRecordConstants.M126, sect126);

                    final Map<Integer, List<String>> result =
                            calculate(cache, incCourseSections);

                    for (final Map.Entry<Integer, List<String>> entry : result.entrySet()) {
                        Log.info(entry.getKey(), ": ", Integer.toString(entry.getValue().size()), " students");
                    }

                    final List<String> late = studentsNDaysLate(result, 14);
                    Log.info("There are ", Integer.toString(late.size()),
                            " students who are 14 days or more late, or have not yet started");

                } catch (final SQLException ex) {
                    Log.warning(ex);
                }
            }
        }
    }
}
