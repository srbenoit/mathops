package dev.mathops.web.site.tutorial.precalc;

/**
 * Possible eligibility status codes for the proctored tutorial
 */
public enum EEligibility {

    /** Student is eligible now. */
    ELIGIBLE,

    /** Student has not yet passed unit 4 review exam. */
    INELIGIBLE_RE4_NOT_PASSED,

    /** Student must re-pass the unit 4 review exam to earn 2 more attempts. */
    INELIGIBLE_MUST_REPASS_RE4,

    /** Student has already passed the exam - no need to retake it. */
    ELIGIBLE_BUT_ALREADY_PASSED
}
