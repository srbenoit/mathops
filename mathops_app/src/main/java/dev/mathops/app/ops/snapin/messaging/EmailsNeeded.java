package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.app.ops.snapin.messaging.factory.BlockedMessageFactory;
import dev.mathops.app.ops.snapin.messaging.factory.LateMessageFactory;
import dev.mathops.app.ops.snapin.messaging.factory.OnTimeMessageFactory;
import dev.mathops.app.ops.snapin.messaging.factory.WelcomeMessageFactory;
import dev.mathops.app.ops.snapin.messaging.factory1of1.Factory1of1;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;
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

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class that scans the database each evening and determines any students to whom a personalized email should
 * be sent based on their current course status.
 */
public class EmailsNeeded {

    /** The maximum urgency to merit an email every 7 days. */
    private static final int MAX_URGENCY_EVERY_7_DAYS = 2;

    /** The maximum urgency to merit an email every 5 days. */
    private static final int MAX_URGENCY_EVERY_5_DAYS = 4;

    /** The maximum urgency to merit an email every 3 days. */
    public static final int MAX_URGENCY_EVERY_3_DAYS = 10;

    /** Flag that can cancel an in-progress scan. */
    private final AtomicBoolean canceled;

    /**
     * Constructs a new {@code EmailsNeeded}.
     */
    public EmailsNeeded() {

        this.canceled = new AtomicBoolean(false);
    }

    /**
     * Cancels the operations.
     */
    public void cancel() {

        this.canceled.set(true);
    }

    /**
     * Generates a map from pace to a map from track to instructor name.
     *
     * @return the map
     */
    public static Map<Integer, Map<String, String>> getInstructors() {

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
     * Processes a single student's registrations.
     *
     * @param cache       the data cache
     * @param stuId       the student ID
     * @param regs        the student's registrations (sorted map from course ID to registration)
     * @param today       the current date
     * @param msMap       map from pace to a map from track to list of milestones
     * @param active      the active term
     * @param messagesDue a map from student ID to message to be sent
     * @param instructors a map from pace to map from track to instructor
     * @throws SQLException if there is an error accessing the database
     */
    public static void processStudent(final Cache cache, final String stuId, final List<RawStcourse> regs,
                                      final LocalDate today,
                                      final Map<Integer, ? extends Map<String, List<RawMilestone>>> msMap,
                                      final TermRec active,
                                      final Map<? super String, ? super MessageToSend> messagesDue,
                                      final Map<Integer, ? extends Map<String, String>> instructors)
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

                // NOTE: don't message the face-to-face sections (tracks D and E)

                if (track == null) {
                    Log.warning("Pace track is null for " + stuId);
                } else if (!"D".equals(track) && !"E".equals(track)) {
                    final Integer paceInt = Integer.valueOf(pace);

                    final Map<String, List<RawMilestone>> paceMap = msMap.get(paceInt);

                    if (paceMap == null) {
                        Log.warning("No milestones for pace ", paceInt);
                    } else {
                        final List<RawMilestone> milestones = paceMap.get(track);

                        if (milestones == null) {
                            Log.warning("No milestones for pace ", paceInt,
                                    " track ", track);
                        } else {
                            final List<RawStmilestone> stmilestones = RawStmilestoneLogic
                                    .getStudentMilestones(cache, active.term, track, stuId);
                            stmilestones.sort(null);

                            final RawStudent stu = RawStudentLogic.query(cache, stuId, false);
                            final RawStterm stterm =
                                    RawSttermLogic.query(cache, active.term, stuId);

                            if (stu == null) {
                                Log.warning("ERROR: No student record for ", stuId);
                            } else if (stterm == null) {
                                Log.warning("ERROR: No stterm record for ", stuId);
                            } else if ("Y".equals(stterm.doNotDisturb)) {
                                Log.info("Skipping student ", stuId,
                                        " marked 'do-not-disturb'");
                            } else {
                                final List<RawStexam> exams =
                                        RawStexamLogic.queryByStudent(cache, stuId, false);

                                final List<RawSthomework> homeworks =
                                        RawSthomeworkLogic.queryByStudent(cache, stuId, false);

                                final List<RawStmsg> messages =
                                        RawStmsgLogic.queryByStudent(cache, stuId);
                                final List<RawSpecialStus> specials =
                                        RawSpecialStusLogic.queryByStudent(cache, stuId);

                                final LocalDate lastClassDay = cache.getSystemData().getLastClassDay();

                                final PrerequisiteLogic prereq =
                                        new PrerequisiteLogic(cache, stuId);

                                final String instrName = instructors.get(paceInt).get(track);

                                final MessagingContext context = new MessagingContext(active, stu,
                                        pace, track, stterm, regs, today, milestones, stmilestones,
                                        exams, homeworks, messages, specials, lastClassDay, prereq);

                                switch (pace) {
                                    case 1:
                                        Factory1of1.processPace1Student(cache, context, instrName, messagesDue);
                                        break;

                                    case 2:
                                        processPace2Student(cache, context, instrName, messagesDue);
                                        break;

                                    case 3:
                                        processPace3Student(cache, context, instrName, messagesDue);
                                        break;

                                    case 4:
                                        processPace4Student(cache, context, instrName, messagesDue);
                                        break;

                                    case 5:
                                        processPace5Student(cache, context, instrName, messagesDue);
                                        break;

                                    default:
                                        Log.warning("Unexpected pace: ", Integer.toString(pace));
                                        break;
                                }
                            }
                        }
                    }
                }
            } catch (final Exception ex) {
                Log.warning(ex);
                throw ex;
            }
        }
    }

    /**
     * Processes a student in a 2-course pace.
     *
     * @param cache       the data cache
     * @param context     the messaging context
     * @param instrName   the name of the instructor assigned to the student's pace/track
     * @param messagesDue a map from student ID to message to be sent
     */
    private static void processPace2Student(final Cache cache, final MessagingContext context, final String instrName,
                                            final Map<? super String, ? super MessageToSend> messagesDue) {

        // Log.info("Processing 2-course student");

        // Identify the current course
        final MessagingCourseStatus current;
        final RawStcourse reg1 = context.sortedRegs.get(0);
        if ("Y".equals(reg1.completed)) {
            // Course 2 is current
            final RawStcourse reg2 = context.sortedRegs.get(1);
            final EffectiveMilestones ms2 = new EffectiveMilestones(cache, 2, 2, context);
            current = new MessagingCourseStatus(context, reg2, ms2, instrName);
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(cache, 2, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        MessageToSend msg = null;

        if (MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.WELCOME)) {
            msg = WelcomeMessageFactory.generate(context, current);
        } else if (current.blocked) {
            msg = BlockedMessageFactory.generate(context, current);
        } else {
            final int urgency = current.urgency;

            // final HtmlBuilder regsList = new HtmlBuilder(40);
            // for (int i = 0; i < context.sortedRegs.size(); ++i) {
            // regsList.add(context.sortedRegs.get(i).course).add(CoreConstants.SPC);
            // }
            // Log.info("Student ", context.student.stuId, ": [2] ", regsList.toString(),
            // ", urgency ", Integer.toString(urgency));

            try {
                RawSttermLogic.updateUrgency(cache, context.student.stuId,
                        context.studentTerm.termKey, Integer.valueOf(urgency));
            } catch (final SQLException ex) {
                Log.warning("Failed to update urgency", ex);
            }

            final int daysAgo = latestMessageWeekdaysAgo(context);

            // TODO: Adjust delays for "late" messages near end of term.

            if (urgency < 1 && daysAgo > 4) {
                msg = OnTimeMessageFactory.generate(context, current);
            } else if (urgency <= MAX_URGENCY_EVERY_7_DAYS) {
                if (daysAgo > 7) {
                    msg = LateMessageFactory.generate(cache, context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_5_DAYS) {
                if (daysAgo > 5) {
                    msg = LateMessageFactory.generate(cache, context, current);
                }
            } else if (daysAgo > 3) {
                msg = LateMessageFactory.generate(cache, context, current);
            }
        }

        if (msg != null) {
            messagesDue.put(context.student.stuId, msg);
        }
    }

    /**
     * Processes a student in a 3-course pace.
     *
     * @param cache       the data cache
     * @param context     the messaging context
     * @param instrName   the name of the instructor assigned to the student's pace/track
     * @param messagesDue a map from student ID to message to be sent
     */
    private static void processPace3Student(final Cache cache, final MessagingContext context, final String instrName,
                                            final Map<? super String, ? super MessageToSend> messagesDue) {

        // Log.info("Processing 3-course student");

        // Identify the current course
        final MessagingCourseStatus current;
        final RawStcourse reg1 = context.sortedRegs.get(0);
        if ("Y".equals(reg1.completed)) {

            final RawStcourse reg2 = context.sortedRegs.get(1);
            if ("Y".equals(reg2.completed)) {
                // Course 3 is current
                final RawStcourse reg3 = context.sortedRegs.get(2);
                final EffectiveMilestones ms3 = new EffectiveMilestones(cache, 3, 3, context);
                current = new MessagingCourseStatus(context, reg3, ms3, instrName);
            } else {
                // Course 2 is current
                final EffectiveMilestones ms2 = new EffectiveMilestones(cache, 3, 2, context);
                current = new MessagingCourseStatus(context, reg2, ms2, instrName);
            }
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(cache, 3, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        MessageToSend row = null;

        if (MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.WELCOME)) {
            row = WelcomeMessageFactory.generate(context, current);
        } else if (current.blocked) {
            row = BlockedMessageFactory.generate(context, current);
        } else {
            final int urgency = current.urgency;

            // final HtmlBuilder regsList = new HtmlBuilder(40);
            // for (int i = 0; i < context.sortedRegs.size(); ++i) {
            // regsList.add(context.sortedRegs.get(i).course).add(CoreConstants.SPC);
            // }
            // Log.info("Student ", context.student.stuId, ": [3] ", regsList.toString(),
            // ", urgency ", Integer.toString(urgency));

            try {
                RawSttermLogic.updateUrgency(cache, context.student.stuId,
                        context.studentTerm.termKey, Integer.valueOf(urgency));
            } catch (final SQLException ex) {
                Log.warning("Failed to update urgency", ex);
            }

            final int daysAgo = latestMessageWeekdaysAgo(context);

            // TODO: Adjust delays for "late" messages near end of term.

            if (urgency < 1 && daysAgo > 4) {
                row = OnTimeMessageFactory.generate(context, current);
            } else if (urgency <= MAX_URGENCY_EVERY_7_DAYS) {
                if (daysAgo > 7) {
                    row = LateMessageFactory.generate(cache, context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_5_DAYS) {
                if (daysAgo > 5) {
                    row = LateMessageFactory.generate(cache, context, current);
                }
            } else if (daysAgo > 3) {
                row = LateMessageFactory.generate(cache, context, current);
            }
        }

        if (row != null) {
            messagesDue.put(context.student.stuId, row);
        }
    }

    /**
     * Processes a student in a 4-course pace.
     *
     * @param cache       the data cache
     * @param context     the messaging context
     * @param instrName   the name of the instructor assigned to the student's pace/track
     * @param messagesDue a map from student ID to message to be sent
     */
    private static void processPace4Student(final Cache cache, final MessagingContext context, final String instrName,
                                            final Map<? super String, ? super MessageToSend> messagesDue) {

        // Log.info("Processing 4-course student");

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
                    final EffectiveMilestones ms4 = new EffectiveMilestones(cache, 4, 4, context);
                    current = new MessagingCourseStatus(context, reg4, ms4, instrName);
                } else {
                    // Course 3 is current
                    final EffectiveMilestones ms3 = new EffectiveMilestones(cache, 4, 3, context);
                    current = new MessagingCourseStatus(context, reg3, ms3, instrName);
                }

            } else {
                // Course 2 is current
                final EffectiveMilestones ms2 = new EffectiveMilestones(cache, 4, 2, context);
                current = new MessagingCourseStatus(context, reg2, ms2, instrName);
            }
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(cache, 4, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        MessageToSend row = null;

        if (MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.WELCOME)) {
            row = WelcomeMessageFactory.generate(context, current);
        } else if (current.blocked) {
            row = BlockedMessageFactory.generate(context, current);
        } else {
            final int urgency = current.urgency;

            // final HtmlBuilder regsList = new HtmlBuilder(40);
            // for (int i = 0; i < context.sortedRegs.size(); ++i) {
            // regsList.add(context.sortedRegs.get(i).course).add(CoreConstants.SPC);
            // }
            // Log.info("Student ", context.student.stuId, ": [4] ", regsList.toString(),
            // ", urgency ", Integer.toString(urgency));

            try {
                RawSttermLogic.updateUrgency(cache, context.student.stuId,
                        context.studentTerm.termKey, Integer.valueOf(urgency));
            } catch (final SQLException ex) {
                Log.warning("Failed to update urgency", ex);
            }

            final int daysAgo = latestMessageWeekdaysAgo(context);

            // TODO: Adjust delays for "late" messages near end of term.

            if (urgency < 1 && daysAgo > 4) {
                row = OnTimeMessageFactory.generate(context, current);
            } else if (urgency <= MAX_URGENCY_EVERY_7_DAYS) {
                if (daysAgo > 7) {
                    row = LateMessageFactory.generate(cache, context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_5_DAYS) {
                if (daysAgo > 5) {
                    row = LateMessageFactory.generate(cache, context, current);
                }
            } else if (daysAgo > 3) {
                row = LateMessageFactory.generate(cache, context, current);
            }
        }

        if (row != null) {
            messagesDue.put(context.student.stuId, row);
        }
    }

    /**
     * Processes a student in a 5-course pace.
     *
     * @param cache       the data cache
     * @param context     the messaging context
     * @param instrName   the name of the instructor assigned to the student's pace/track
     * @param messagesDue a map from student ID to message to be sent
     */
    private static void processPace5Student(final Cache cache, final MessagingContext context, final String instrName,
                                            final Map<? super String, ? super MessageToSend> messagesDue) {

        // Log.info("Processing 5-course student");

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
                                new EffectiveMilestones(cache, 5, 5, context);
                        current = new MessagingCourseStatus(context, reg5, ms5, instrName);
                    } else {
                        // Course 4 is current
                        final EffectiveMilestones ms4 =
                                new EffectiveMilestones(cache, 5, 4, context);
                        current = new MessagingCourseStatus(context, reg4, ms4, instrName);
                    }
                } else {
                    // Course 3 is current
                    final EffectiveMilestones ms3 = new EffectiveMilestones(cache, 5, 3, context);
                    current = new MessagingCourseStatus(context, reg3, ms3, instrName);
                }

            } else {
                // Course 2 is current
                final EffectiveMilestones ms2 = new EffectiveMilestones(cache, 5, 2, context);
                current = new MessagingCourseStatus(context, reg2, ms2, instrName);
            }
        } else {
            // Course 1 is current
            final EffectiveMilestones ms1 = new EffectiveMilestones(cache, 5, 1, context);
            current = new MessagingCourseStatus(context, reg1, ms1, instrName);
        }

        MessageToSend row = null;

        if (MsgUtils.hasNoMsgForMilestone(context.messages, EMilestone.WELCOME)) {
            row = WelcomeMessageFactory.generate(context, current);
        } else if (current.blocked) {
            row = BlockedMessageFactory.generate(context, current);
        } else {
            final int urgency = current.urgency;

            // final HtmlBuilder regsList = new HtmlBuilder(40);
            // for (int i = 0; i < context.sortedRegs.size(); ++i) {
            // regsList.add(context.sortedRegs.get(i).course).add(CoreConstants.SPC);
            // }
            // Log.info("Student ", context.student.stuId, ": [5] ", regsList.toString(), ",
            // urgency ", Integer.toString(urgency));

            try {
                RawSttermLogic.updateUrgency(cache, context.student.stuId,
                        context.studentTerm.termKey, Integer.valueOf(urgency));
            } catch (final SQLException ex) {
                Log.warning("Failed to update urgency", ex);
            }

            final int daysAgo = latestMessageWeekdaysAgo(context);

            // TODO: Adjust delays for "late" messages near end of term.

            if (urgency < 1) {
                if (daysAgo > 4) {
                    row = OnTimeMessageFactory.generate(context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_7_DAYS) {
                if (daysAgo > 7) {
                    row = LateMessageFactory.generate(cache, context, current);
                }
            } else if (urgency <= MAX_URGENCY_EVERY_5_DAYS) {
                if (daysAgo > 5) {
                    row = LateMessageFactory.generate(cache, context, current);
                }
            } else if (daysAgo > 3) {
                row = LateMessageFactory.generate(cache, context, current);
            }
        }

        if (row != null) {
            messagesDue.put(context.student.stuId, row);
        }
    }

    /**
     * Given a list of two registration records, generates a list with the same records, sorted by pace order.
     *
     * @param regsList the registration list
     * @return the sorted list
     */
    public static List<RawStcourse> sort2ByPaceOrder(final List<RawStcourse> regsList) {

        // Sort by course ID
        Collections.sort(regsList);

        RawStcourse reg1 = null;
        RawStcourse reg2 = null;

        for (final RawStcourse reg : regsList) {
            if (reg.paceOrder != null) {
                if (reg.paceOrder.intValue() == 1) {
                    reg1 = reg;
                } else if (reg.paceOrder.intValue() == 2) {
                    reg2 = reg;
                }
            }
        }

        if (reg2 == null) {
            if (reg1 == null) {
                reg1 = regsList.get(0);
                reg2 = regsList.get(1);
            } else {
                regsList.remove(reg1);
                reg2 = regsList.getFirst();
            }
        } else {
            regsList.remove(reg2);

            if (reg1 == null) {
                reg1 = regsList.getFirst();
            } else {
                regsList.remove(reg1);
            }
        }

        return Arrays.asList(reg1, reg2);
    }

    /**
     * Given a list of three registration records, generates a list with the same records, sorted by pace order.
     *
     * @param regsList the registration list
     * @return the sorted list
     */
    public static List<RawStcourse> sort3ByPaceOrder(final List<RawStcourse> regsList) {

        // Sort by course ID
        Collections.sort(regsList);

        RawStcourse reg1 = null;
        RawStcourse reg2 = null;
        RawStcourse reg3 = null;

        for (final RawStcourse reg : regsList) {
            if (reg.paceOrder != null) {
                if (reg.paceOrder.intValue() == 1) {
                    reg1 = reg;
                } else if (reg.paceOrder.intValue() == 2) {
                    reg2 = reg;
                } else if (reg.paceOrder.intValue() == 3) {
                    reg3 = reg;
                }
            }
        }

        if (reg3 == null) {
            if (reg2 == null) {
                if (reg1 == null) {
                    reg1 = regsList.get(0);
                    reg2 = regsList.get(1);
                    reg3 = regsList.get(2);
                } else {
                    regsList.remove(reg1);
                    reg2 = regsList.get(0);
                    reg3 = regsList.get(1);
                }
            } else {
                regsList.remove(reg2);

                if (reg1 == null) {
                    reg1 = regsList.get(0);
                    reg3 = regsList.get(1);
                } else {
                    regsList.remove(reg1);
                    reg3 = regsList.getFirst();
                }
            }
        } else {
            regsList.remove(reg3);

            if (reg2 == null) {
                if (reg1 == null) {
                    reg1 = regsList.get(0);
                    reg2 = regsList.get(1);
                } else {
                    regsList.remove(reg1);
                    reg2 = regsList.getFirst();
                }
            } else {
                regsList.remove(reg2);

                if (reg1 == null) {
                    reg1 = regsList.getFirst();
                } else {
                    regsList.remove(reg1);
                }
            }
        }

        return Arrays.asList(reg1, reg2, reg3);
    }

    /**
     * Given a list of four registration records, generates a list with the same records, sorted by pace order.
     *
     * @param regsList the registration list
     * @return the sorted list
     */
    public static List<RawStcourse> sort4ByPaceOrder(final List<RawStcourse> regsList) {

        // Sort by course ID
        Collections.sort(regsList);

        RawStcourse reg1 = null;
        RawStcourse reg2 = null;
        RawStcourse reg3 = null;
        RawStcourse reg4 = null;

        for (final RawStcourse reg : regsList) {
            if (reg.paceOrder != null) {
                if (reg.paceOrder.intValue() == 1) {
                    reg1 = reg;
                } else if (reg.paceOrder.intValue() == 2) {
                    reg2 = reg;
                } else if (reg.paceOrder.intValue() == 3) {
                    reg3 = reg;
                } else if (reg.paceOrder.intValue() == 4) {
                    reg4 = reg;
                }
            }
        }

        if (reg4 == null) {
            if (reg3 == null) {
                if (reg2 == null) {
                    if (reg1 == null) {
                        reg1 = regsList.get(0);
                        reg2 = regsList.get(1);
                        reg3 = regsList.get(2);
                        reg4 = regsList.get(3);
                    } else {
                        regsList.remove(reg1);
                        reg2 = regsList.get(0);
                        reg3 = regsList.get(1);
                        reg4 = regsList.get(2);
                    }
                } else {
                    regsList.remove(reg2);

                    if (reg1 == null) {
                        reg1 = regsList.get(0);
                        reg3 = regsList.get(1);
                        reg4 = regsList.get(2);
                    } else {
                        regsList.remove(reg1);
                        reg3 = regsList.get(0);
                        reg4 = regsList.get(1);
                    }
                }
            } else {
                regsList.remove(reg3);

                if (reg2 == null) {
                    if (reg1 == null) {
                        reg1 = regsList.get(0);
                        reg2 = regsList.get(1);
                        reg4 = regsList.get(2);
                    } else {
                        regsList.remove(reg1);
                        reg2 = regsList.get(0);
                        reg4 = regsList.get(1);
                    }
                } else {
                    regsList.remove(reg2);

                    if (reg1 == null) {
                        reg1 = regsList.get(0);
                        reg4 = regsList.get(1);
                    } else {
                        regsList.remove(reg1);
                        reg4 = regsList.getFirst();
                    }
                }
            }
        } else {
            regsList.remove(reg4);

            if (reg3 == null) {
                if (reg2 == null) {
                    if (reg1 == null) {
                        reg1 = regsList.get(0);
                        reg2 = regsList.get(1);
                        reg3 = regsList.get(2);
                    } else {
                        regsList.remove(reg1);
                        reg2 = regsList.get(0);
                        reg3 = regsList.get(1);
                    }
                } else {
                    regsList.remove(reg2);

                    if (reg1 == null) {
                        reg1 = regsList.get(0);
                        reg3 = regsList.get(1);
                    } else {
                        regsList.remove(reg1);
                        reg3 = regsList.getFirst();
                    }
                }
            } else {
                regsList.remove(reg3);

                if (reg2 == null) {
                    if (reg1 == null) {
                        reg1 = regsList.get(0);
                        reg2 = regsList.get(1);
                    } else {
                        regsList.remove(reg1);
                        reg2 = regsList.getFirst();
                    }
                } else {
                    regsList.remove(reg2);

                    if (reg1 == null) {
                        reg1 = regsList.getFirst();
                    } else {
                        regsList.remove(reg1);
                    }
                }
            }
        }

        return Arrays.asList(reg1, reg2, reg3, reg4);
    }

    /**
     * Given a list of five registration records, generates a list with the same records, sorted by pace order.
     *
     * @param regsList the registration list
     * @return the sorted list
     */
    public static List<RawStcourse> sort5ByPaceOrder(final List<RawStcourse> regsList) {

        // Sort by course ID
        Collections.sort(regsList);

        RawStcourse reg1 = null;
        RawStcourse reg2 = null;
        RawStcourse reg3 = null;
        RawStcourse reg4 = null;
        RawStcourse reg5 = null;

        for (final RawStcourse reg : regsList) {
            if (reg.paceOrder != null) {
                if (reg.paceOrder.intValue() == 1) {
                    reg1 = reg;
                } else if (reg.paceOrder.intValue() == 2) {
                    reg2 = reg;
                } else if (reg.paceOrder.intValue() == 3) {
                    reg3 = reg;
                } else if (reg.paceOrder.intValue() == 4) {
                    reg4 = reg;
                } else if (reg.paceOrder.intValue() == 5) {
                    reg5 = reg;
                }
            }
        }

        if (reg5 == null) {
            if (reg4 == null) {
                if (reg3 == null) {
                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                            reg3 = regsList.get(2);
                            reg4 = regsList.get(3);
                            reg5 = regsList.get(4);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.get(0);
                            reg3 = regsList.get(1);
                            reg4 = regsList.get(2);
                            reg5 = regsList.get(3);
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg3 = regsList.get(1);
                            reg4 = regsList.get(2);
                            reg5 = regsList.get(3);
                        } else {
                            regsList.remove(reg1);
                            reg3 = regsList.get(0);
                            reg4 = regsList.get(1);
                            reg5 = regsList.get(2);
                        }
                    }
                } else {
                    regsList.remove(reg3);

                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                            reg4 = regsList.get(2);
                            reg5 = regsList.get(3);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.get(0);
                            reg4 = regsList.get(1);
                            reg5 = regsList.get(2);
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg4 = regsList.get(1);
                            reg5 = regsList.get(2);
                        } else {
                            regsList.remove(reg1);
                            reg4 = regsList.get(0);
                            reg5 = regsList.get(1);
                        }
                    }
                }
            } else {
                regsList.remove(reg4);

                if (reg3 == null) {
                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                            reg3 = regsList.get(2);
                            reg5 = regsList.get(3);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.get(0);
                            reg3 = regsList.get(1);
                            reg5 = regsList.get(2);
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg3 = regsList.get(1);
                            reg5 = regsList.get(2);
                        } else {
                            regsList.remove(reg1);
                            reg3 = regsList.get(0);
                            reg5 = regsList.get(1);
                        }
                    }
                } else {
                    regsList.remove(reg3);

                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                            reg5 = regsList.get(2);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.get(0);
                            reg5 = regsList.get(1);
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg5 = regsList.get(1);
                        } else {
                            regsList.remove(reg1);
                            reg5 = regsList.getFirst();
                        }
                    }
                }
            }

        } else {
            regsList.remove(reg5);

            if (reg4 == null) {
                if (reg3 == null) {
                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                            reg3 = regsList.get(2);
                            reg4 = regsList.get(3);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.get(0);
                            reg3 = regsList.get(1);
                            reg4 = regsList.get(2);
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg3 = regsList.get(1);
                            reg4 = regsList.get(2);
                        } else {
                            regsList.remove(reg1);
                            reg3 = regsList.get(0);
                            reg4 = regsList.get(1);
                        }
                    }
                } else {
                    regsList.remove(reg3);

                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                            reg4 = regsList.get(2);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.get(0);
                            reg4 = regsList.get(1);
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg4 = regsList.get(1);
                        } else {
                            regsList.remove(reg1);
                            reg4 = regsList.getFirst();
                        }
                    }
                }
            } else {
                regsList.remove(reg4);

                if (reg3 == null) {
                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                            reg3 = regsList.get(2);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.get(0);
                            reg3 = regsList.get(1);
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg3 = regsList.get(1);
                        } else {
                            regsList.remove(reg1);
                            reg3 = regsList.getFirst();
                        }
                    }
                } else {
                    regsList.remove(reg3);

                    if (reg2 == null) {
                        if (reg1 == null) {
                            reg1 = regsList.get(0);
                            reg2 = regsList.get(1);
                        } else {
                            regsList.remove(reg1);
                            reg2 = regsList.getFirst();
                        }
                    } else {
                        regsList.remove(reg2);

                        if (reg1 == null) {
                            reg1 = regsList.getFirst();
                        }
                    }
                }
            }
        }

        return Arrays.asList(reg1, reg2, reg3, reg4, reg5);
    }

    /**
     * Finds the date of the most recent message sent to a student, and calculates the number of weekdays that have
     * elapsed since then.
     *
     * @param context the messaging context
     * @return the date of the most recent message
     */
    private static int latestMessageWeekdaysAgo(final MessagingContext context) {

        LocalDate latest = null;

        for (final RawStmsg msg : context.messages) {
            if (latest == null || latest.isBefore(msg.msgDt)) {
                latest = msg.msgDt;
            }
        }

        int daysAgo;

        if (latest == null) {
            daysAgo = 100;
        } else {
            final LocalDate today = context.today;

            daysAgo = 0;
            while (latest.isBefore(today)) {

                if (latest.getDayOfWeek() != DayOfWeek.SATURDAY
                        && latest.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    ++daysAgo;
                }

                latest = latest.plusDays(1L);
            }

        }

        return daysAgo;
    }
}
