package dev.mathops.app.ops.snapin.messaging.epf;

import dev.mathops.commons.IProgressListener;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStmsgLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.EPF;
import dev.mathops.app.ops.snapin.messaging.EffectiveMilestones;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class that scans the database each evening and determines any students to whom a personalized email should
 * be sent based on their current course status.
 */
final class EPFStudents {

    /** The minimum urgency to be included in the EPF report. */
    private static final int MIN_URGENCY_FOR_EPF = 15;

    /** The data cache. */
    private final Cache cache;

    /** An optional progress listener. */
    private final IProgressListener listener;

    /** Flag that can cancel an in-progress scan. */
    private final AtomicBoolean canceled;

    /**
     * Constructs a new {@code EmailsNeeded}.
     *
     * @param theCache    the data cache
     * @param theListener an optional progress listener
     */
    EPFStudents(final Cache theCache, final IProgressListener theListener) {

        this.cache = theCache;
        this.listener = theListener;
        this.canceled = new AtomicBoolean(false);
    }

    /**
     * Cancels the operations.
     */
    void cancel() {

        this.canceled.set(true);
    }

    /**
     * Executes the job.
     *
     * @param incCourseSections map from course ID to a list of section numbers to include in the scan
     * @param epf               map from student ID to report row for that EPF student
     */
    void calculate(final Map<String, ? extends List<String>> incCourseSections,
                   final Map<String, ? super MessageToSend> epf) {

        // In terms of progress, the first 1% will be loading data, then next 99 will be scanning students.

        this.canceled.set(false);
        try {
            fireProgress("Querying instructors", 0, 200);
            final Map<Integer, Map<String, String>> instructors = getInstructors();
            final TermRec act = this.cache.getSystemData().getActiveTerm();

            if (act == null) {
                Log.warning("ERROR: Cannot query active term");
            } else {
                fireProgress("Querying registrations", 1, 300);
                // Map from student ID to map from course ID to registration
                final Map<String, List<RawStcourse>> stuRegs =
                        EPF.gatherMatchingRegistrations(this.cache, incCourseSections);

                fireProgress("Gathering milestones", 2, 300);

                // Map from pace to map from track to list of milestones.
                final Map<Integer, Map<String, List<RawMilestone>>> msMap =
                        EPF.gatherMilestones(this.cache, act.term);

                final LocalDate today = LocalDate.now();
                final int numStudents = stuRegs.size();
                final int totalSteps = numStudents * 101 / 100;
                int onStudent = 0;
                int completed = totalSteps - numStudents;

                for (final Map.Entry<String, List<RawStcourse>> e : stuRegs.entrySet()) {

                    if (this.canceled.get()) {
                        Log.info("Scan canceled");
                        break;
                    }

                    ++onStudent;
                    final String descr = "Processing student " + onStudent + " out of " + numStudents;
                    fireProgress(descr, completed, totalSteps);

                    final String studentId = e.getKey();
                    final List<RawStcourse> regs = e.getValue();

                    processStudent(studentId, regs, today, msMap, act, epf, instructors);
                    ++completed;
                }

                fireProgress("Finished", totalSteps, totalSteps);

            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Fires a progress notification if there is a listener installed.
     *
     * @param description a description of the current step
     * @param done        the number of steps done
     * @param total       the number of total steps
     */
    private void fireProgress(final String description, final int done, final int total) {

        // Log.info(description);
        if (this.listener != null) {
            this.listener.progress(description, done, total);
        }
    }

    /**
     * Generates a map from pace to a map from track to instructor name.
     *
     * @return the map
     */
    private static Map<Integer, Map<String, String>> getInstructors() {

        final Map<Integer, Map<String, String>> instructors = new HashMap<>(5);

        final Map<String, String> pace1 = new HashMap<>(2);
        final Map<String, String> pace2 = new HashMap<>(2);
        final Map<String, String> pace3 = new HashMap<>(2);
        final Map<String, String> pace4 = new HashMap<>(2);
        final Map<String, String> pace5 = new HashMap<>(2);

        instructors.put(Integer.valueOf(1), pace1);
        instructors.put(Integer.valueOf(2), pace2);
        instructors.put(Integer.valueOf(3), pace3);
        instructors.put(Integer.valueOf(4), pace4);
        instructors.put(Integer.valueOf(5), pace5);

        pace1.put("A", "Anita Pattison");
        pace1.put("B", "Anita Pattison");
        pace1.put("D", "Will Bromley");

        pace2.put("A", "Anita Pattison");
        pace2.put("B", "Anita Pattison");
        pace2.put("D", "Will Bromley");

        pace3.put("A", "Anita Pattison");

        pace4.put("A", "Anita Pattison");

        pace5.put("A", "Anita Pattison");

        return instructors;
    }

    /**
     * Organizes the report data.
     *
     * @param records the collection of report records, one per student
     * @return a map from Instructor to a map from Course to a map from Section number to a map from message code to the
     *         list of students who should receive that message
     */
    public static Map<String, Map<String, Map<String, Map<EMsg, List<MessageToSend>>>>>
    organizeReport(final Iterable<MessageToSend> records) {

        final Map<String, Map<String, Map<String, Map<EMsg, List<MessageToSend>>>>> organized = new TreeMap<>();

        for (final MessageToSend row : records) {

            final String instrName = row.status.instructorName;
            final Map<String, Map<String, Map<EMsg, List<MessageToSend>>>> byInstr =
                    organized.computeIfAbsent(instrName, s -> new TreeMap<>());

            final String course = row.status.reg.course;
            final Map<String, Map<EMsg, List<MessageToSend>>> byCourse = byInstr.computeIfAbsent(course,
                    s -> new TreeMap<>());

            String sect = row.status.reg.sect;

            // Merge 401, 801, and 809, which are cross-listed
            if ("801".equals(sect) || "809".equals(sect)) {
                sect = "401";
            }

            final Map<EMsg, List<MessageToSend>> bySect = byCourse.computeIfAbsent(sect, s -> new TreeMap<>());

            final EMsg msg = row.msgCode;
            final List<MessageToSend> list = bySect.computeIfAbsent(msg, eMsg -> new ArrayList<>(10));
            list.add(row);
        }

        return organized;
    }

    /**
     * Processes a single student's registrations.
     *
     * @param stuId       the student ID
     * @param regs        the student's registrations (sorted map from course ID to registration)
     * @param today       the current date
     * @param msMap       map from pace to a map from track to list of milestones
     * @param active      the active term
     * @param epfReport   a map from student ID to report row for the EPF message report
     * @param instructors a map from pace to map from track to instructor
     * @throws SQLException if there is an error accessing the database
     */
    private void processStudent(final String stuId, final List<RawStcourse> regs, final LocalDate today,
                                final Map<Integer, ? extends Map<String, List<RawMilestone>>> msMap,
                                final TermRec active, final Map<? super String, ? super MessageToSend> epfReport,
                                final Map<Integer, ? extends Map<String, String>> instructors) throws SQLException {

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
            sc1 = nulls.removeFirst();
        }
        if (sc2 == null && !nulls.isEmpty()) {
            sc2 = nulls.removeFirst();
        }
        if (sc3 == null && !nulls.isEmpty()) {
            sc3 = nulls.removeFirst();
        }
        if (sc4 == null && !nulls.isEmpty()) {
            sc4 = nulls.removeFirst();
        }
        if (sc5 == null && !nulls.isEmpty()) {
            sc5 = nulls.removeFirst();
        }

        if (sc1 == null) {
            Log.warning("NO FIRST COURSE FOR ", stuId);
        } else {
            try {
                final int pace = PaceTrackLogic.determinePace(regs);
                final String track = PaceTrackLogic.determinePaceTrack(regs, pace);

                // NOTE: don't message the face-to-face sections (alter this for late-start EPF run)
//                if ("A".equals(track) || "B".equals(track) || "C".equals(track)) {
                if ("A".equals(track) || "B".equals(track)) {

                    final Integer paceInt = Integer.valueOf(pace);
                    final List<RawMilestone> milestones = msMap.get(paceInt).get(track);
                    final List<RawStmilestone> stmilestones = RawStmilestoneLogic
                            .getStudentMilestones(this.cache, active.term, track, stuId);
                    stmilestones.sort(null);

                    final RawStudent stu = RawStudentLogic.query(this.cache, stuId, false);
                    final RawStterm stterm = RawSttermLogic.query(this.cache, active.term, stuId);

                    if (stu == null) {
                        Log.warning("ERROR: No student record for ", stuId);
                    } else {
                        final List<RawStexam> exams = RawStexamLogic.queryByStudent(this.cache, stuId, false);

                        final List<RawSthomework> homeworks = RawSthomeworkLogic.queryByStudent(this.cache, stuId,
                                false);

                        final List<RawStmsg> messages = RawStmsgLogic.queryByStudent(this.cache, stuId);
                        final List<RawSpecialStus> specials = RawSpecialStusLogic.queryByStudent(this.cache, stuId);
                        final LocalDate lastClassDay = this.cache.getSystemData().getLastClassDay();
                        final PrerequisiteLogic prereq = new PrerequisiteLogic(this.cache, stuId);

                        final String instrName = instructors.get(paceInt).get(track);

                        final MessagingContext context = new MessagingContext(active, stu, pace, track, stterm, regs,
                                today, milestones, stmilestones, exams, homeworks, messages, specials, lastClassDay,
                                prereq);

                        switch (pace) {
                            case 1:
                                processPace1Student(context, instrName, epfReport);
                                break;

                            case 2:
                                processPace2Student(context, instrName, epfReport);
                                break;

                            case 3:
                                processPace3Student(context, instrName, epfReport);
                                break;

                            case 4:
                                processPace4Student(context, instrName, epfReport);
                                break;

                            case 5:
                                processPace5Student(context, instrName, epfReport);
                                break;

                            default:
                                Log.warning("Unexpected pace: ", Integer.toString(pace));
                                break;
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                throw ex;
            }
        }
    }

    /**
     * Processes a student in a 1-course pace.
     *
     * @param context   the messaging context
     * @param instrName the name of the instructor assigned to the student's pace/track
     * @param epfReport a map from student ID to report row for the EPF message report
     */
    private void processPace1Student(final MessagingContext context, final String instrName,
                                     final Map<? super String, ? super MessageToSend> epfReport) {

        final RawStcourse reg1 = context.sortedRegs.getFirst();
        final EffectiveMilestones ms1 = new EffectiveMilestones(this.cache, 1, 1, context);
        final MessagingCourseStatus current =
                new MessagingCourseStatus(context, reg1, ms1, instrName);

        Log.info("Processing 1-course student - urgency=" + current.urgency);

        if (current.urgency >= MIN_URGENCY_FOR_EPF) {
            final MessageToSend row = new MessageToSend(context, current, Integer.valueOf(0),
                    current.currentMilestone, EMsg.EPF, null, null);
            epfReport.put(context.student.stuId, row);
        }
    }

    /**
     * Processes a student in a 2-course pace.
     *
     * @param context   the messaging context
     * @param instrName the name of the instructor assigned to the student's pace/track
     * @param epfReport a map from student ID to report row for the EPF message report
     */
    private void processPace2Student(final MessagingContext context, final String instrName,
                                     final Map<? super String, ? super MessageToSend> epfReport) {

        // Identify the current course
        final MessagingCourseStatus current;
        final RawStcourse reg1 = context.sortedRegs.get(0);
        if ("Y".equals(reg1.completed)) {
            // Course 2 is current
            final RawStcourse reg2 = context.sortedRegs.get(1);
            final EffectiveMilestones ms2 = new EffectiveMilestones(this.cache, 2, 2, context);
            current = new MessagingCourseStatus(context, reg2, ms2, instrName);
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(this.cache, 2, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        Log.info("Processing 2-course student - urgency=" + current.urgency);

        if (current.urgency >= MIN_URGENCY_FOR_EPF) {
            final MessageToSend row = new MessageToSend(context, current, Integer.valueOf(0),
                    current.currentMilestone, EMsg.EPF, null, null);
            epfReport.put(context.student.stuId, row);
        }
    }

    /**
     * Processes a student in a 3-course pace.
     *
     * @param context   the messaging context
     * @param instrName the name of the instructor assigned to the student's pace/track
     * @param epfReport a map from student ID to report row for the EPF message report
     */
    private void processPace3Student(final MessagingContext context, final String instrName,
                                     final Map<? super String, ? super MessageToSend> epfReport) {

        // Identify the current course
        final MessagingCourseStatus current;
        final RawStcourse reg1 = context.sortedRegs.get(0);
        if ("Y".equals(reg1.completed)) {

            final RawStcourse reg2 = context.sortedRegs.get(1);
            if ("Y".equals(reg2.completed)) {
                // Course 3 is current
                final RawStcourse reg3 = context.sortedRegs.get(2);
                final EffectiveMilestones ms3 = new EffectiveMilestones(this.cache, 3, 3, context);
                current = new MessagingCourseStatus(context, reg3, ms3, instrName);
            } else {
                // Course 2 is current
                final EffectiveMilestones ms2 = new EffectiveMilestones(this.cache, 3, 2, context);
                current = new MessagingCourseStatus(context, reg2, ms2, instrName);
            }
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(this.cache, 3, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        Log.info("Processing 3-course student - urgency=" + current.urgency);

        if (current.urgency >= MIN_URGENCY_FOR_EPF) {
            final MessageToSend row = new MessageToSend(context, current, Integer.valueOf(0),
                    current.currentMilestone, EMsg.EPF, null, null);
            epfReport.put(context.student.stuId, row);
        }
    }

    /**
     * Processes a student in a 4-course pace.
     *
     * @param context   the messaging context
     * @param instrName the name of the instructor assigned to the student's pace/track
     * @param epfReport a map from student ID to report row for the EPF message report
     */
    private void processPace4Student(final MessagingContext context, final String instrName,
                                     final Map<? super String, ? super MessageToSend> epfReport) {

        // Identify the current course
        final MessagingCourseStatus current;

        final RawStcourse reg1 = context.sortedRegs.get(0);
        if ("Y".equals(reg1.completed)) {

            final RawStcourse reg2 = context.sortedRegs.get(1);
            if ("Y".equals(reg2.completed)) {

                final RawStcourse reg3 = context.sortedRegs.get(2);
                if ("Y".equals(reg3.completed)) {
                    // Course 4 is current
                    final RawStcourse reg4 = context.sortedRegs.get(3);
                    final EffectiveMilestones ms4 =
                            new EffectiveMilestones(this.cache, 4, 4, context);
                    current = new MessagingCourseStatus(context, reg4, ms4, instrName);
                } else {
                    // Course 3 is current
                    final EffectiveMilestones ms3 =
                            new EffectiveMilestones(this.cache, 4, 3, context);
                    current = new MessagingCourseStatus(context, reg3, ms3, instrName);
                }

            } else {
                // Course 2 is current
                final EffectiveMilestones ms2 = new EffectiveMilestones(this.cache, 4, 2, context);
                current = new MessagingCourseStatus(context, reg2, ms2, instrName);
            }
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(this.cache, 4, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        Log.info("Processing 4-course student - urgency=" + current.urgency);

        if (current.urgency >= MIN_URGENCY_FOR_EPF) {
            final MessageToSend row = new MessageToSend(context, current, Integer.valueOf(0),
                    current.currentMilestone, EMsg.EPF, null, null);
            epfReport.put(context.student.stuId, row);
        }
    }

    /**
     * Processes a student in a 5-course pace.
     *
     * @param context   the messaging context
     * @param instrName the name of the instructor assigned to the student's pace/track
     * @param epfReport a map from student ID to report row for the EPF message report
     */
    private void processPace5Student(final MessagingContext context, final String instrName,
                                     final Map<? super String, ? super MessageToSend> epfReport) {

        // Identify the current course
        final MessagingCourseStatus current;

        final RawStcourse reg1 = context.sortedRegs.get(0);
        if ("Y".equals(reg1.completed)) {

            final RawStcourse reg2 = context.sortedRegs.get(1);
            if ("Y".equals(reg2.completed)) {

                final RawStcourse reg3 = context.sortedRegs.get(2);
                if ("Y".equals(reg3.completed)) {

                    final RawStcourse reg4 = context.sortedRegs.get(2);
                    if ("Y".equals(reg4.completed)) {
                        // Course 5 is current
                        final RawStcourse reg5 = context.sortedRegs.get(3);
                        final EffectiveMilestones ms5 =
                                new EffectiveMilestones(this.cache, 5, 5, context);
                        current = new MessagingCourseStatus(context, reg5, ms5, instrName);
                    } else {
                        // Course 4 is current
                        final EffectiveMilestones ms4 =
                                new EffectiveMilestones(this.cache, 5, 4, context);
                        current = new MessagingCourseStatus(context, reg4, ms4, instrName);
                    }
                } else {
                    // Course 3 is current
                    final EffectiveMilestones ms3 =
                            new EffectiveMilestones(this.cache, 5, 3, context);
                    current = new MessagingCourseStatus(context, reg3, ms3, instrName);
                }

            } else {
                // Course 2 is current
                final EffectiveMilestones ms2 = new EffectiveMilestones(this.cache, 5, 2, context);
                current = new MessagingCourseStatus(context, reg2, ms2, instrName);
            }
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(this.cache, 5, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        Log.info("Processing 5-course student - urgency=" + current.urgency);

        if (current.urgency >= MIN_URGENCY_FOR_EPF) {
            final MessageToSend row = new MessageToSend(context, current, Integer.valueOf(0),
                    current.currentMilestone, EMsg.EPF, null, null);
            epfReport.put(context.student.stuId, row);
        }
    }
}
