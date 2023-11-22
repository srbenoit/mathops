package dev.mathops.dbjobs.report.usage;

import java.time.LocalDateTime;

/**
 * A student-exam record.
 */
final class StExam {

    /** The time the exam was started. */
    final LocalDateTime startTime;

    /** The time the exam was finished. */
    final LocalDateTime finishTime;

    /**
     * Constructs a new {@code StExam}.
     *
     * @param theStartTime  the time the exam was started
     * @param theFinishTime the time the exam was finished
     */
    StExam(final LocalDateTime theStartTime, final LocalDateTime theFinishTime) {

        this.startTime = theStartTime;
        this.finishTime = theFinishTime;
    }
}
