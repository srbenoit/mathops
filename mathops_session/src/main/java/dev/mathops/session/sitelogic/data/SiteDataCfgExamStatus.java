package dev.mathops.session.sitelogic.data;

import java.time.LocalDate;

/**
 * A container for the student's status relative to a single exam in a course/unit.
 */
public final class SiteDataCfgExamStatus extends SiteDataCfgStatusBase {

    /** The date when the student first passed the exam. */
    public LocalDate firstPassingDate;

    /** The highest possible score earned so far on the exam. */
    int highestPossibleScore;

    /** The configured mastery score for the exam. */
    public int masteryScore;

    /** The highest raw score earned so far on the exam. */
    int highestRawScore;

    /** The highest passing score earned so far on the exam. */
    public int highestPassingScore;

    /** The counted score for the exam toward the course total score. */
    public int countedScore;

    /** True if student passed the exam on time. */
    public boolean passedOnTime;

    /** Points earned by completing the exam on time. */
    public int onTimePoints;

    /** Total number of attempts allowed on the exam. */
    int totalAttemptsAllowed;

    /** Number of attempts used so far. */
    public int totalAttemptsSoFar;

    /** Number of attempts earned per passing review exam (for proctored exams). */
    int attemptsPerPassingReview;

    /** Number of attempts taken since the last passing review exam (for proctored exams). */
    int attemptsSinceLastPassingReview;

    /** The deadline date for completing the exam. */
    LocalDate deadlineDate;

    /** The deadline date for last-try attempts (null if none). */
    LocalDate lastTryDeadline;

    /** The number of last-try attempts allowed (after deadline, until last-try deadline). */
    Integer lastTryAttemptsAllowed;

    /** The number of last-try attempts used so far (after deadline, until last-try deadline). */
    int lastTryAttemptsSoFar;

    /** Flag indicating computed store is synthetic. */
    public boolean synthetic;

    /**
     * Constructs a new {@code SiteDataCfgExamStatus}.
     */
    SiteDataCfgExamStatus() {

        super();

        this.synthetic = false;
    }
}
