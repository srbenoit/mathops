package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student messaging status in a course.
 */
public final class MessagingCourseStatus {

    /** A commonly used integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A commonly used integer. */
    private static final Integer TWO = Integer.valueOf(2);

    /** A commonly used integer. */
    private static final Integer THREE = Integer.valueOf(3);

    /** A commonly used integer. */
    private static final Integer FOUR = Integer.valueOf(4);

    /** A commonly used integer. */
    private static final Integer FIVE = Integer.valueOf(5);

    /** The student's registration. */
    public final RawStcourse reg;

    /** The student's milestones in the course. */
    public final EffectiveMilestones milestones;

    /** The student's assigned instructor name. */
    public final String instructorName;

    /**
     * The current milestone; null only if student has reached the maximum possible grade for their course, or if they
     * are blocked and can do nothing further.
     */
    public EMilestone currentMilestone;

    /**
     * The student's "urgency" rating. where 0 indicates the student is on-time or ahead of schedule, and 1 or higher
     * indicates increasing urgency of action. Numbers above 10 will trigger assignment to a one-on-one case manager.
     */
    public int urgency;

    /** The student's current situation with respect to their "current" course. */
    public ESituationCourse situation;

    /** Flag indicating student has met the course prerequisites. */
    public boolean metPrereq;

    /** Flag indicating course is started. */
    public boolean started;

    /** The number of failed attempts on the User's exam. */
    public int failedTriesOnUsers;

    /** The date of the last attempt on the user's exam. */
    public LocalDate lastUsersTry;

    /** Flag indicating student has passed user's exam. */
    public boolean passedUsers;

    /** The number of failed attempts on the SR exam. */
    public int failedTriesOnSR;

    /** The date of the last attempt on the Skills Review exam. */
    public LocalDate lastSRTry;

    /** Flag indicating student has passed the Skills Review exam. */
    public boolean passedSR;

    /** The number of failed attempts on RE1. */
    public int failedTriesOnRE1;

    /** The date of the last attempt on the Unit 1 Review exam. */
    public LocalDate lastR1Try;

    /** The date of the last attempt on the Unit 1 exam. */
    public LocalDate lastU1Try;

    /** Map from objective to number of tries on unit 1 HW. */
    public Map<Integer, Integer> triesOnUnit1HW;

    /** Map from objective to passing state on unit 1 HW. */
    private final Map<Integer, Boolean> passedUnit1HW;

    /** The date of the last attempt on Homework 1.1. */
    public LocalDate lastH11Try;

    /** The date of the last attempt on Homework 1.2. */
    public LocalDate lastH12Try;

    /** The date of the last attempt on Homework 1.3. */
    public LocalDate lastH13Try;

    /** The date of the last attempt on Homework 1.4. */
    public LocalDate lastH14Try;

    /** The date of the last attempt on Homework 1.5. */
    public LocalDate lastH15Try;

    /** Flag indicating student has passed RE1. */
    public boolean passedRE1;

    /** Flag indicating student has passed RE1 by its deadline. */
    public boolean passedRE1OnTime;

    /** The number of failed attempts on UE1. */
    public int failedTriesOnUE1;

    /** Flag indicating student has passed UE1. */
    public boolean passedUE1;

    /** The number of failed attempts on RE2. */
    public int failedTriesOnRE2;

    /** The date of the last attempt on the Unit 2 Review exam. */
    public LocalDate lastR2Try;

    /** The date of the last attempt on the Unit 2 exam. */
    public LocalDate lastU2Try;

    /** Map from objective to number of tries on unit 2 HW. */
    public Map<Integer, Integer> triesOnUnit2HW;

    /** Map from objective to passing state on unit 2 HW. */
    private final Map<Integer, Boolean> passedUnit2HW;

    /** The date of the last attempt on Homework 2.1. */
    public LocalDate lastH21Try;

    /** The date of the last attempt on Homework 2.2. */
    public LocalDate lastH22Try;

    /** The date of the last attempt on Homework 2.3. */
    public LocalDate lastH23Try;

    /** The date of the last attempt on Homework 2.4. */
    public LocalDate lastH24Try;

    /** The date of the last attempt on Homework 2.5. */
    public LocalDate lastH25Try;

    /** Flag indicating student has passed RE2. */
    public boolean passedRE2;

    /** Flag indicating student has passed RE2 by its deadline. */
    public boolean passedRE2OnTime;

    /** The number of failed attempts on UE2. */
    public int failedTriesOnUE2;

    /** Flag indicating student has passed UE2. */
    public boolean passedUE2;

    /** The number of failed attempts on RE3. */
    public int failedTriesOnRE3;

    /** The date of the last attempt on the Unit 3 Review exam. */
    public LocalDate lastR3Try;

    /** The date of the last attempt on the Unit 3 exam. */
    public LocalDate lastU3Try;

    /** Map from objective to number of tries on unit 3 HW. */
    public Map<Integer, Integer> triesOnUnit3HW;

    /** Map from objective to passing state on unit 3 HW. */
    private final Map<Integer, Boolean> passedUnit3HW;

    /** The date of the last attempt on Homework 3.1. */
    public LocalDate lastH31Try;

    /** The date of the last attempt on Homework 3.2. */
    public LocalDate lastH32Try;

    /** The date of the last attempt on Homework 3.3. */
    public LocalDate lastH33Try;

    /** The date of the last attempt on Homework 3.4. */
    public LocalDate lastH34Try;

    /** The date of the last attempt on Homework 3.5. */
    public LocalDate lastH35Try;

    /** Flag indicating student has passed RE3. */
    public boolean passedRE3;

    /** Flag indicating student has passed RE3 by its deadline. */
    public boolean passedRE3OnTime;

    /** The number of failed attempts on UE3. */
    public int failedTriesOnUE3;

    /** Flag indicating student has passed UE3. */
    public boolean passedUE3;

    /** public number of failed attempts on RE4. */
    public int failedTriesOnRE4;

    /** The date of the last attempt on the Unit 4 Review exam. */
    public LocalDate lastR4Try;

    /** The date of the last attempt on the Unit 4 exam. */
    public LocalDate lastU4Try;

    /** Map from objective to number of tries on unit 4 HW. */
    public Map<Integer, Integer> triesOnUnit4HW;

    /** Map from objective to passing state on unit 4 HW. */
    private final Map<Integer, Boolean> passedUnit4HW;

    /** The date of the last attempt on Homework 4.1. */
    public LocalDate lastH41Try;

    /** The date of the last attempt on Homework 4.2. */
    public LocalDate lastH42Try;

    /** The date of the last attempt on Homework 4.3. */
    public LocalDate lastH43Try;

    /** The date of the last attempt on Homework 4.4. */
    public LocalDate lastH44Try;

    /** The date of the last attempt on Homework 4.5. */
    public LocalDate lastH45Try;

    /** public indicating student has passed RE4. */
    public boolean passedRE4;

    /** Flag indicating student has passed RE4 by its deadline. */
    public boolean passedRE4OnTime;

    /** The number of failed attempts on UE4. */
    public int failedTriesOnUE4;

    /** Flag indicating student has passed UE4. */
    public boolean passedUE4;

    /** The number of failed attempts on FIN. */
    public int failedTriesOnFIN;

    /** Flag indicating student has passed FIN. */
    public boolean passedFIN;

    /**
     * Flag indicating student has last-try attempt available on final (this is only set if the final exam due date is
     * in the past).
     */
    public boolean lastTryAvailable;

    /** Flag indicating student is blocked. */
    public boolean blocked;

    /** On-time score on RE1. */
    private int re1Score;

    /** On-time score on RE2. */
    private int re2Score;

    /** On-time score on RE3. */
    private int re3Score;

    /** On-time score on RE4. */
    private int re4Score;

    /** Highest passing score on UE1. */
    public int ue1Score;

    /** Highest passing score on UE2. */
    public int ue2Score;

    /** Highest passing score on UE3. */
    public int ue3Score;

    /** Highest passing score on UE4. */
    public int ue4Score;

    /** Highest passing score on FIN. */
    private int finScore;

    /** Total score so far. */
    public int totalScore;

    /** Maximum possible score. */
    public int maxPossibleScore;

    /** Special student categories to which the student belongs. */
    private final List<RawSpecialStus> specials;

    /** The number of days since the student's last activity. */
    public int daysSinceLastActivity;

    /** The number of days since the last message to student. */
    public int daysSinceLastMessage;

    /**
     * Constructs a new {@code MessagingCourseStatus}, computing all data.
     *
     * @param context           the messaging context
     * @param theReg            the registration record
     * @param theMilestones     the milestones for the student's track, including overrides, if any
     * @param theInstructorName the instructor name
     * @throws IllegalArgumentException if {@code reg} is null
     */
    public MessagingCourseStatus(final MessagingContext context, final RawStcourse theReg,
                                 final EffectiveMilestones theMilestones, final String theInstructorName) {

        if (context == null) {
            throw new IllegalArgumentException("Context may not be null");
        }
        if (theReg == null) {
            throw new IllegalArgumentException("Registration for " + context.student.stuId + " may not be null");
        }

        this.reg = theReg;
        this.milestones = theMilestones;
        this.instructorName = theInstructorName;
        this.specials = context.specials;

        this.metPrereq = context.prereqLogic != null && context.prereqLogic.hasSatisfiedPrereqsFor(theReg.course);

        this.started = "Y".equals(theReg.openStatus) || "N".equals(theReg.openStatus);

        this.triesOnUnit1HW = new HashMap<>(5);
        this.triesOnUnit2HW = new HashMap<>(5);
        this.triesOnUnit3HW = new HashMap<>(5);
        this.triesOnUnit4HW = new HashMap<>(5);

        this.passedUnit1HW = new HashMap<>(5);
        this.passedUnit2HW = new HashMap<>(5);
        this.passedUnit3HW = new HashMap<>(5);
        this.passedUnit4HW = new HashMap<>(5);

        for (int i = 1; i <= 5; ++i) {
            final Integer key = Integer.valueOf(i);
            this.triesOnUnit1HW.put(key, ZERO);
            this.triesOnUnit2HW.put(key, ZERO);
            this.triesOnUnit3HW.put(key, ZERO);
            this.triesOnUnit4HW.put(key, ZERO);
            this.passedUnit1HW.put(key, Boolean.FALSE);
            this.passedUnit2HW.put(key, Boolean.FALSE);
            this.passedUnit3HW.put(key, Boolean.FALSE);
            this.passedUnit4HW.put(key, Boolean.FALSE);
        }

        this.passedUsers = "Y".equals(context.student.licensed);
        this.lastUsersTry = null;
        this.lastSRTry = null;
        this.lastR1Try = null;
        this.lastR2Try = null;
        this.lastR3Try = null;
        this.lastR4Try = null;
        this.lastU1Try = null;
        this.lastU2Try = null;
        this.lastU3Try = null;
        this.lastU4Try = null;

        for (final RawStexam e : context.exams) {

            final String type = e.examType;

            if ("Q".equals(type)) {
                if ("N".equals(e.passed)) {
                    ++this.failedTriesOnUsers;
                }
                if (this.lastUsersTry == null || this.lastUsersTry.isBefore(e.examDt)) {
                    this.lastUsersTry = e.examDt;
                }
                continue;
            }

            if (!this.reg.course.equals(e.course) || "G".equals(e.passed)) {
                continue;
            }

            final int unit = e.unit.intValue();

            if (unit == 0) {
                if (this.lastSRTry == null || this.lastSRTry.isBefore(e.examDt)) {
                    this.lastSRTry = e.examDt;
                }
                if ("Y".equals(e.passed)) {
                    this.passedSR = true;
                } else {
                    ++this.failedTriesOnSR;
                }
            } else if (unit == 1) {

                if ("R".equals(type)) {
                    if (this.lastR1Try == null || this.lastR1Try.isBefore(e.examDt)) {
                        this.lastR1Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedRE1 = true;
                        if (theMilestones.r1 != null && !e.examDt.isAfter(theMilestones.r1)) {
                            this.re1Score = 3;
                        }
                    } else {
                        ++this.failedTriesOnRE1;
                    }
                } else if ("U".equals(type)) {
                    if (this.lastU1Try == null || this.lastU1Try.isBefore(e.examDt)) {
                        this.lastU1Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedUE1 = true;
                        if (e.examScore != null) {
                            this.ue1Score = Math.max(this.ue1Score, e.examScore.intValue());
                        }
                    } else {
                        ++this.failedTriesOnUE1;
                    }
                }
            } else if (unit == 2) {
                if ("R".equals(type)) {
                    if (this.lastR2Try == null || this.lastR2Try.isBefore(e.examDt)) {
                        this.lastR2Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedRE2 = true;
                        if (theMilestones.r2 != null && !e.examDt.isAfter(theMilestones.r2)) {
                            this.re2Score = 3;
                        }
                    } else {
                        ++this.failedTriesOnRE2;
                    }
                } else if ("U".equals(type)) {
                    if (this.lastU2Try == null || this.lastU2Try.isBefore(e.examDt)) {
                        this.lastU2Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedUE2 = true;
                        if (e.examScore != null) {
                            this.ue2Score = Math.max(this.ue2Score, e.examScore.intValue());
                        }
                    } else {
                        ++this.failedTriesOnUE2;
                    }
                }
            } else if (unit == 3) {
                if ("R".equals(type)) {
                    if (this.lastR3Try == null || this.lastR3Try.isBefore(e.examDt)) {
                        this.lastR3Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedRE3 = true;
                        if (theMilestones.r3 != null && !e.examDt.isAfter(theMilestones.r3)) {
                            this.re3Score = 3;
                        }
                    } else {
                        ++this.failedTriesOnRE3;
                    }
                } else if ("U".equals(type)) {
                    if (this.lastU3Try == null || this.lastU3Try.isBefore(e.examDt)) {
                        this.lastU3Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedUE3 = true;
                        if (e.examScore != null) {
                            this.ue3Score = Math.max(this.ue3Score, e.examScore.intValue());
                        }
                    } else {
                        ++this.failedTriesOnUE3;
                    }
                }
            } else if (unit == 4) {
                if ("R".equals(type)) {
                    if (this.lastR4Try == null || this.lastR4Try.isBefore(e.examDt)) {
                        this.lastR4Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedRE4 = true;
                        if (theMilestones.r4 != null && !e.examDt.isAfter(theMilestones.r4)) {
                            this.re4Score = 3;
                        }
                    } else {
                        ++this.failedTriesOnRE4;
                    }
                } else if ("U".equals(type)) {
                    if (this.lastU4Try == null || this.lastU4Try.isBefore(e.examDt)) {
                        this.lastU4Try = e.examDt;
                    }
                    if ("Y".equals(e.passed)) {
                        this.passedUE4 = true;
                        if (e.examScore != null) {
                            this.ue4Score = Math.max(this.ue4Score, e.examScore.intValue());
                        }
                    } else {
                        ++this.failedTriesOnUE4;
                    }
                }
            } else if (unit == 5 && "F".equals(type)) {
                if ("Y".equals(e.passed)) {
                    this.passedFIN = true;
                    if (e.examScore != null) {
                        this.finScore = Math.max(this.finScore, e.examScore.intValue());
                    }
                } else {
                    ++this.failedTriesOnFIN;
                }
            }
        }

        if (!this.passedFIN) {
            // Count final exam tries after the final due date

            if (context.today.isAfter(theMilestones.fin)) {
                int finalsAfterDue = 0;
                for (final RawStexam e : context.exams) {
                    if (!this.reg.course.equals(e.course) || "G".equals(e.passed)) {
                        continue;
                    }

                    if (("F".equals(e.examType) && e.unit.intValue() == 5)
                            && e.examDt.isAfter(theMilestones.fin)) {
                        ++finalsAfterDue;
                    }
                }

                this.lastTryAvailable = finalsAfterDue < theMilestones.lastTryCount;

                if (!this.lastTryAvailable) {
                    this.blocked = true;
                }
            }

            if (context.today.isAfter(theMilestones.last)) {
                this.blocked = true;
            }
        }

        this.totalScore = this.re1Score + this.re2Score + this.re3Score + this.re4Score
                + this.ue1Score + this.ue2Score + this.ue3Score + this.ue4Score + this.finScore;
        this.maxPossibleScore = this.re1Score + this.re2Score + this.re3Score + this.re4Score + 60;

        for (final RawSthomework h : context.homeworks) {
            if (!this.reg.course.equals(h.course) || "G".equals(h.passed)) {
                continue;
            }

            final int unit = h.unit.intValue();

            if (unit == 1) {
                this.triesOnUnit1HW.compute(h.objective, (k, count) -> Integer.valueOf(count.intValue() + 1));

                if ("Y".equals(h.passed)) {
                    this.passedUnit1HW.put(h.objective, Boolean.TRUE);
                }

                final int obj = h.objective.intValue();
                if (obj == 1) {
                    if (this.lastH11Try == null || this.lastH11Try.isBefore(h.hwDt)) {
                        this.lastH11Try = h.hwDt;
                    }
                } else if (obj == 2) {
                    if (this.lastH12Try == null || this.lastH12Try.isBefore(h.hwDt)) {
                        this.lastH12Try = h.hwDt;
                    }
                } else if (obj == 3) {
                    if (this.lastH13Try == null || this.lastH13Try.isBefore(h.hwDt)) {
                        this.lastH13Try = h.hwDt;
                    }
                } else if (obj == 4) {
                    if (this.lastH14Try == null || this.lastH14Try.isBefore(h.hwDt)) {
                        this.lastH14Try = h.hwDt;
                    }
                } else if (obj == 5) {
                    if (this.lastH15Try == null || this.lastH15Try.isBefore(h.hwDt)) {
                        this.lastH15Try = h.hwDt;
                    }
                }
            } else if (unit == 2) {
                this.triesOnUnit2HW.compute(h.objective, (k, count) -> Integer.valueOf(count.intValue() + 1));

                if ("Y".equals(h.passed)) {
                    this.passedUnit2HW.put(h.objective, Boolean.TRUE);
                }

                final int obj = h.objective.intValue();
                if (obj == 1) {
                    if (this.lastH21Try == null || this.lastH21Try.isBefore(h.hwDt)) {
                        this.lastH21Try = h.hwDt;
                    }
                } else if (obj == 2) {
                    if (this.lastH22Try == null || this.lastH22Try.isBefore(h.hwDt)) {
                        this.lastH22Try = h.hwDt;
                    }
                } else if (obj == 3) {
                    if (this.lastH23Try == null || this.lastH23Try.isBefore(h.hwDt)) {
                        this.lastH23Try = h.hwDt;
                    }
                } else if (obj == 4) {
                    if (this.lastH24Try == null || this.lastH24Try.isBefore(h.hwDt)) {
                        this.lastH24Try = h.hwDt;
                    }
                } else if (obj == 5) {
                    if (this.lastH25Try == null || this.lastH25Try.isBefore(h.hwDt)) {
                        this.lastH25Try = h.hwDt;
                    }
                }
            } else if (unit == 3) {
                this.triesOnUnit3HW.compute(h.objective, (k, count) -> Integer.valueOf(count.intValue() + 1));

                if ("Y".equals(h.passed)) {
                    this.passedUnit3HW.put(h.objective, Boolean.TRUE);
                }

                final int obj = h.objective.intValue();
                if (obj == 1) {
                    if (this.lastH31Try == null || this.lastH31Try.isBefore(h.hwDt)) {
                        this.lastH31Try = h.hwDt;
                    }
                } else if (obj == 2) {
                    if (this.lastH32Try == null || this.lastH32Try.isBefore(h.hwDt)) {
                        this.lastH32Try = h.hwDt;
                    }
                } else if (obj == 3) {
                    if (this.lastH33Try == null || this.lastH33Try.isBefore(h.hwDt)) {
                        this.lastH33Try = h.hwDt;
                    }
                } else if (obj == 4) {
                    if (this.lastH34Try == null || this.lastH34Try.isBefore(h.hwDt)) {
                        this.lastH34Try = h.hwDt;
                    }
                } else if (obj == 5) {
                    if (this.lastH35Try == null || this.lastH35Try.isBefore(h.hwDt)) {
                        this.lastH35Try = h.hwDt;
                    }
                }
            } else if (unit == 4) {
                this.triesOnUnit4HW.compute(h.objective, (k, count) -> Integer.valueOf(count.intValue() + 1));

                if ("Y".equals(h.passed)) {
                    this.passedUnit4HW.put(h.objective, Boolean.TRUE);
                }

                final int obj = h.objective.intValue();
                if (obj == 1) {
                    if (this.lastH41Try == null || this.lastH41Try.isBefore(h.hwDt)) {
                        this.lastH41Try = h.hwDt;
                    }
                } else if (obj == 2) {
                    if (this.lastH42Try == null || this.lastH42Try.isBefore(h.hwDt)) {
                        this.lastH42Try = h.hwDt;
                    }
                } else if (obj == 3) {
                    if (this.lastH43Try == null || this.lastH43Try.isBefore(h.hwDt)) {
                        this.lastH43Try = h.hwDt;
                    }
                } else if (obj == 4) {
                    if (this.lastH44Try == null || this.lastH44Try.isBefore(h.hwDt)) {
                        this.lastH44Try = h.hwDt;
                    }
                } else if (obj == 5) {
                    if (this.lastH45Try == null || this.lastH45Try.isBefore(h.hwDt)) {
                        this.lastH45Try = h.hwDt;
                    }
                }
            }
        }

        // TODO: Compute situation

        // Compute urgency
        final LocalDate today = LocalDate.now();

        if (this.metPrereq) {
            this.urgency = 0;
        } else {
            this.urgency = 5;
        }

        if (!this.passedUsers && !theMilestones.us.isAfter(today)) {
            // Log.info("Adding 1 for late on Users");
            ++this.urgency;
        }
        if (!this.passedSR && !theMilestones.sr.isAfter(today)) {
            // Log.info("Adding 1 for late on SR");
            ++this.urgency;
        }

        if ((!this.passedUnit1HW.get(ONE).booleanValue()) && !theMilestones.h11.isAfter(today)) {
            // Log.info("Adding 1 for late on H1.1");
            ++this.urgency;
        }
        if ((!this.passedUnit1HW.get(TWO).booleanValue()) && !theMilestones.h12.isAfter(today)) {
            // Log.info("Adding 1 for late on H1.2");
            ++this.urgency;
        }
        if ((!this.passedUnit1HW.get(THREE).booleanValue()) && !theMilestones.h13.isAfter(today)) {
            // Log.info("Adding 1 for late on H1.3");
            ++this.urgency;
        }
        if ((!this.passedUnit1HW.get(FOUR).booleanValue()) && !theMilestones.h14.isAfter(today)) {
            // Log.info("Adding 1 for late on H1.4");
            ++this.urgency;
        }
        if ((!this.passedUnit1HW.get(FIVE).booleanValue()) && !theMilestones.h15.isAfter(today)) {
            // Log.info("Adding 1 for late on H1.5");
            ++this.urgency;
        }
        if (!this.passedRE1 && !theMilestones.r1.isAfter(today)) {
            // Log.info("Adding 3 for late on RE1");
            this.urgency += 3;
        }
        if (!this.passedUE1 && !theMilestones.u1.isAfter(today)) {
            // Log.info("Adding 2 for late on UE1");
            this.urgency += 2;
        }

        if ((!this.passedUnit2HW.get(ONE).booleanValue()) && !theMilestones.h21.isAfter(today)) {
            // Log.info("Adding 1 for late on H2.1");
            ++this.urgency;
        }
        if ((!this.passedUnit2HW.get(TWO).booleanValue()) && !theMilestones.h22.isAfter(today)) {
            // Log.info("Adding 1 for late on H2.2");
            ++this.urgency;
        }
        if ((!this.passedUnit2HW.get(THREE).booleanValue()) && !theMilestones.h23.isAfter(today)) {
            // Log.info("Adding 1 for late on H2.3");
            ++this.urgency;
        }
        if ((!this.passedUnit2HW.get(FOUR).booleanValue()) && !theMilestones.h24.isAfter(today)) {
            // Log.info("Adding 1 for late on H2.4");
            ++this.urgency;
        }
        if ((!this.passedUnit2HW.get(FIVE).booleanValue()) && !theMilestones.h25.isAfter(today)) {
            // Log.info("Adding 1 for late on H2.5");
            ++this.urgency;
        }
        if (!this.passedRE2 && !theMilestones.r2.isAfter(today)) {
            // Log.info("Adding 3 for late on RE2");
            this.urgency += 3;
        }
        if (!this.passedUE2 && !theMilestones.u2.isAfter(today)) {
            // Log.info("Adding 2 for late on UE2");
            this.urgency += 2;
        }

        if ((!this.passedUnit3HW.get(ONE).booleanValue()) && !theMilestones.h31.isAfter(today)) {
            // Log.info("Adding 1 for late on H3.1");
            ++this.urgency;
        }
        if ((!this.passedUnit3HW.get(TWO).booleanValue()) && !theMilestones.h32.isAfter(today)) {
            // Log.info("Adding 1 for late on H3.2");
            ++this.urgency;
        }
        if ((!this.passedUnit3HW.get(THREE).booleanValue()) && !theMilestones.h33.isAfter(today)) {
            // Log.info("Adding 1 for late on H3.3");
            ++this.urgency;
        }
        if ((!this.passedUnit3HW.get(FOUR).booleanValue()) && !theMilestones.h34.isAfter(today)) {
            // Log.info("Adding 1 for late on H3.4");
            ++this.urgency;
        }
        if ((!this.passedUnit3HW.get(FIVE).booleanValue()) && !theMilestones.h35.isAfter(today)) {
            // Log.info("Adding 1 for late on H3.5");
            ++this.urgency;
        }
        if (!this.passedRE3 && !theMilestones.r3.isAfter(today)) {
            // Log.info("Adding 3 for late on RE3");
            this.urgency += 3;
        }
        if (!this.passedUE3 && !theMilestones.u3.isAfter(today)) {
            // Log.info("Adding 2 for late on UE3");
            this.urgency += 2;
        }

        if ((!this.passedUnit4HW.get(ONE).booleanValue()) && !theMilestones.h41.isAfter(today)) {
            // Log.info("Adding 1 for late on H4.1");
            ++this.urgency;
        }
        if ((!this.passedUnit4HW.get(TWO).booleanValue()) && !theMilestones.h42.isAfter(today)) {
            // Log.info("Adding 1 for late on H4.2");
            ++this.urgency;
        }
        if ((!this.passedUnit4HW.get(THREE).booleanValue()) && !theMilestones.h43.isAfter(today)) {
            // Log.info("Adding 1 for late on H4.3");
            ++this.urgency;
        }
        if ((!this.passedUnit4HW.get(FOUR).booleanValue()) && !theMilestones.h44.isAfter(today)) {
            // Log.info("Adding 1 for late on H4.4");
            ++this.urgency;
        }
        if ((!this.passedUnit4HW.get(FIVE).booleanValue()) && !theMilestones.h45.isAfter(today)) {
            // Log.info("Adding 1 for late on H4.5");
            ++this.urgency;
        }
        if (!this.passedRE4 && !theMilestones.r4.isAfter(today)) {
            // Log.info("Adding 3 for late on RE4");
            this.urgency += 3;
        }
        if (!this.passedUE4 && !theMilestones.u4.isAfter(today)) {
            // Log.info("Adding 2 for late on UE4");
            this.urgency += 2;
        }

        if (this.passedFIN) {
            if (this.totalScore < 54) {
                this.urgency += 3;
            }
        } else {
            final LocalDate finMinus4 = theMilestones.fin.minusDays(4L);

            if (!finMinus4.isAfter(today)) {

                if (finMinus4.equals(today)) {
                    ++this.urgency;
                } else {
                    final LocalDate finMinus2 = theMilestones.fin.minusDays(2L);

                    if (finMinus2.equals(today)) {
                        this.urgency += 3;
                    } else {
                        final LocalDate finMinus1 = theMilestones.fin.minusDays(1L);

                        if (finMinus1.equals(today)) {
                            this.urgency += 5;
                        } else if (theMilestones.fin.equals(today)) {
                            this.urgency += 7;
                        } else if (!today.isAfter(theMilestones.last)) {
                            this.urgency += 9;
                        } else {
                            final LocalDate lastPlus3 = theMilestones.last.plusDays(3L);
                            if (today.isAfter(lastPlus3)) {
                                this.urgency += 5;
                            } else {
                                this.urgency += 9;
                            }
                        }
                    }
                }
            }
        }

        // Determine current milestone

        if (!this.metPrereq) {
            this.currentMilestone = EMilestone.PREREQ;
        } else if (!this.started) {
            this.currentMilestone = EMilestone.START;
        } else if (!this.passedUsers) {
            this.currentMilestone = EMilestone.USERS;
        } else if (!this.passedSR) {
            this.currentMilestone = EMilestone.SR;
        } else if (!Boolean.TRUE.equals(this.passedUnit1HW.get(ONE))) {
            this.currentMilestone = EMilestone.HW11;
        } else if (!Boolean.TRUE.equals(this.passedUnit1HW.get(TWO))) {
            this.currentMilestone = EMilestone.HW12;
        } else if (!Boolean.TRUE.equals(this.passedUnit1HW.get(THREE))) {
            this.currentMilestone = EMilestone.HW13;
        } else if (!Boolean.TRUE.equals(this.passedUnit1HW.get(FOUR))) {
            this.currentMilestone = EMilestone.HW14;
        } else if (!Boolean.TRUE.equals(this.passedUnit1HW.get(FIVE))) {
            this.currentMilestone = EMilestone.HW15;
        } else if (!this.passedRE1) {
            this.currentMilestone = EMilestone.RE1;
        } else if (!this.passedUE1) {
            this.currentMilestone = EMilestone.UE1;
        } else if (!Boolean.TRUE.equals(this.passedUnit2HW.get(ONE))) {
            this.currentMilestone = EMilestone.HW21;
        } else if (!Boolean.TRUE.equals(this.passedUnit2HW.get(TWO))) {
            this.currentMilestone = EMilestone.HW22;
        } else if (!Boolean.TRUE.equals(this.passedUnit2HW.get(THREE))) {
            this.currentMilestone = EMilestone.HW23;
        } else if (!Boolean.TRUE.equals(this.passedUnit2HW.get(FOUR))) {
            this.currentMilestone = EMilestone.HW24;
        } else if (!Boolean.TRUE.equals(this.passedUnit2HW.get(FIVE))) {
            this.currentMilestone = EMilestone.HW25;
        } else if (!this.passedRE2) {
            this.currentMilestone = EMilestone.RE2;
        } else if (!this.passedUE2) {
            this.currentMilestone = EMilestone.UE2;
        } else if (!Boolean.TRUE.equals(this.passedUnit3HW.get(ONE))) {
            this.currentMilestone = EMilestone.HW31;
        } else if (!Boolean.TRUE.equals(this.passedUnit3HW.get(TWO))) {
            this.currentMilestone = EMilestone.HW32;
        } else if (!Boolean.TRUE.equals(this.passedUnit3HW.get(THREE))) {
            this.currentMilestone = EMilestone.HW33;
        } else if (!Boolean.TRUE.equals(this.passedUnit3HW.get(FOUR))) {
            this.currentMilestone = EMilestone.HW34;
        } else if (!Boolean.TRUE.equals(this.passedUnit3HW.get(FIVE))) {
            this.currentMilestone = EMilestone.HW35;
        } else if (!this.passedRE3) {
            this.currentMilestone = EMilestone.RE3;
        } else if (!this.passedUE3) {
            this.currentMilestone = EMilestone.UE3;
        } else if (!Boolean.TRUE.equals(this.passedUnit4HW.get(ONE))) {
            this.currentMilestone = EMilestone.HW41;
        } else if (!Boolean.TRUE.equals(this.passedUnit4HW.get(TWO))) {
            this.currentMilestone = EMilestone.HW42;
        } else if (!Boolean.TRUE.equals(this.passedUnit4HW.get(THREE))) {
            this.currentMilestone = EMilestone.HW43;
        } else if (!Boolean.TRUE.equals(this.passedUnit4HW.get(FOUR))) {
            this.currentMilestone = EMilestone.HW44;
        } else if (!Boolean.TRUE.equals(this.passedUnit4HW.get(FIVE))) {
            this.currentMilestone = EMilestone.HW45;
        } else if (!this.passedRE4) {
            this.currentMilestone = EMilestone.RE4;
        } else if (!this.passedUE4) {
            this.currentMilestone = EMilestone.UE4;
        } else if (this.passedFIN) {
            if (this.totalScore < 54) {
                this.currentMilestone = EMilestone.PASS;
            } else if ((this.totalScore < 62 && this.maxPossibleScore >= 62)
                    || (this.totalScore < 65 && this.maxPossibleScore >= 65)) {
                this.currentMilestone = EMilestone.MAX;
            }
        } else if (theMilestones.fin.isBefore(today)) {
            if (this.lastTryAvailable) {
                this.currentMilestone = EMilestone.F1;
            }
        } else {
            this.currentMilestone = EMilestone.FIN;
        }

        LocalDate lastActivity = context.activeTerm.startDate;
        for (final RawSthomework hw : context.homeworks) {
            if (lastActivity.isBefore(hw.hwDt)) {
                lastActivity = hw.hwDt;
            }
        }
        for (final RawStexam ex : context.exams) {
            if (lastActivity.isBefore(ex.examDt)) {
                lastActivity = ex.examDt;
            }
        }
        int days = 0;
        while (lastActivity.isBefore(context.today)) {
            ++days;
            lastActivity = lastActivity.plusDays(1L);
        }

        this.daysSinceLastActivity = days;
        this.daysSinceLastMessage = MsgUtils.latestMessageWeekdaysAgo(context);
    }

    /**
     * Tests whether the student has passed the objective 1.1 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw11() {

        return !Boolean.TRUE.equals(this.passedUnit1HW.get(ONE));
    }

    /**
     * Tests whether the student has passed the objective 1.2 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw12() {

        return !Boolean.TRUE.equals(this.passedUnit1HW.get(TWO));
    }

    /**
     * Tests whether the student has passed the objective 1.3 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw13() {

        return !Boolean.TRUE.equals(this.passedUnit1HW.get(THREE));
    }

    /**
     * Tests whether the student has passed the objective 1.4 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw14() {

        return !Boolean.TRUE.equals(this.passedUnit1HW.get(FOUR));
    }

    /**
     * Tests whether the student has passed the objective 1.5 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw15() {

        return !Boolean.TRUE.equals(this.passedUnit1HW.get(FIVE));
    }

    /**
     * Tests whether the student has passed the objective 2.1 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw21() {

        return !Boolean.TRUE.equals(this.passedUnit2HW.get(ONE));
    }

    /**
     * Tests whether the student has passed the objective 2.2 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw22() {

        return !Boolean.TRUE.equals(this.passedUnit2HW.get(TWO));
    }

    /**
     * Tests whether the student has passed the objective 2.3 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw23() {

        return !Boolean.TRUE.equals(this.passedUnit2HW.get(THREE));
    }

    /**
     * Tests whether the student has passed the objective 2.4 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw24() {

        return !Boolean.TRUE.equals(this.passedUnit2HW.get(FOUR));
    }

    /**
     * Tests whether the student has passed the objective 2.5 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw25() {

        return !Boolean.TRUE.equals(this.passedUnit2HW.get(FIVE));
    }

    /**
     * Tests whether the student has passed the objective 3.1 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw31() {

        return !Boolean.TRUE.equals(this.passedUnit3HW.get(ONE));
    }

    /**
     * Tests whether the student has passed the objective 3.2 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw32() {

        return !Boolean.TRUE.equals(this.passedUnit3HW.get(TWO));
    }

    /**
     * Tests whether the student has passed the objective 3.3 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw33() {

        return !Boolean.TRUE.equals(this.passedUnit3HW.get(THREE));
    }

    /**
     * Tests whether the student has passed the objective 3.4 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw34() {

        return !Boolean.TRUE.equals(this.passedUnit3HW.get(FOUR));
    }

    /**
     * Tests whether the student has passed the objective 3.5 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw35() {

        return !Boolean.TRUE.equals(this.passedUnit3HW.get(FIVE));
    }

    /**
     * Tests whether the student has passed the objective 4.1 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw41() {

        return !Boolean.TRUE.equals(this.passedUnit4HW.get(ONE));
    }

    /**
     * Tests whether the student has passed the objective 4.2 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw42() {

        return !Boolean.TRUE.equals(this.passedUnit4HW.get(TWO));
    }

    /**
     * Tests whether the student has passed the objective 4.3 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw43() {

        return !Boolean.TRUE.equals(this.passedUnit4HW.get(THREE));
    }

    /**
     * Tests whether the student has passed the objective 4.4 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw44() {

        return !Boolean.TRUE.equals(this.passedUnit4HW.get(FOUR));
    }

    /**
     * Tests whether the student has passed the objective 4.5 homework assignment.
     *
     * @return true if that assignment was passed
     */
    public boolean needsHw45() {

        return !Boolean.TRUE.equals(this.passedUnit4HW.get(FIVE));
    }

    /**
     * Tests whether the user is in a special category.
     *
     * @param category the category
     * @return true if the user is in the category
     */
    public boolean isInSpecial(final String category) {

        boolean found = false;

        final LocalDate today = LocalDate.now();

        if (this.specials != null) {
            for (final RawSpecialStus row : this.specials) {
                if (row.stuType.equals(category) && row.isActive(today)) {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }
}
