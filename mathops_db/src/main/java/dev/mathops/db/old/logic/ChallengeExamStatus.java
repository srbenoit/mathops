package dev.mathops.db.old.logic;

/**
 * A data class containing student status relative to a challenge exam.
 */
public final class ChallengeExamStatus {

    /** The exam ID of the available challenge exam; {@code null} if not available. */
    public final String availableExamId;

    /**
     * The reason the challenge exam is not available ({@code null} if {@code availableExamId} contains a value;
     * populated if {@code availableExamId} is {@code null}).
     */
    public final String reasonUnavailable;

    /**
     * Constructs a new {@code ChallengeExamStatus}.
     *
     * @param theAvailableExamId   the exam ID of the available challenge exam; {@code null} if not available
     * @param theReasonUnavailable the reason the challenge exam is not available ({@code null} if
     *                             {@code theAvailableExamId} is present; populated if {@code theAvailableExamId} is
     *                             {@code null})
     */
    ChallengeExamStatus(final String theAvailableExamId, final String theReasonUnavailable) {

        this.availableExamId = theAvailableExamId;
        this.reasonUnavailable = theReasonUnavailable;
    }
}
