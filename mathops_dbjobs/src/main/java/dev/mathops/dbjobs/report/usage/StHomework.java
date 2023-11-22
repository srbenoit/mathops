package dev.mathops.dbjobs.report.usage;

import java.time.LocalDateTime;

/**
 * A student-homework record.
 */
final class StHomework {

    /** The time the exam was started. */
    final LocalDateTime startTime;

    /** The time the exam was finished. */
    final LocalDateTime finishTime;

    /**
     * Constructs a new {@code StHomework}.
     *
     * @param theStartTime  the time the exam was started
     * @param theFinishTime the time the exam was finished
     */
    StHomework(final LocalDateTime theStartTime, final LocalDateTime theFinishTime) {

        this.startTime = theStartTime;
        this.finishTime = theFinishTime;
    }
}
