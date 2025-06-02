package dev.mathops.web.host.course.lti;

/**
 * Possible results of an OAuth request validation.
 */
public enum EOAuthRequestVerifyResult {

    /** Request was verified. */
    VERIFIED,

    /** Request had no "Host" header. */
    MISSING_HOST,

    /** Request had no "oauth_signature" parameter. */
    MISSING_SIGNATURE,

    /** Request had no "oauth_signature_method" parameter. */
    MISSING_SIGNATURE_METHOD,

    /** Request had no "oauth_version" parameter that was not "1.0". */
    BAD_OAUTH_VERSION,

    /** Computed signature does not match provided signature. */
    SIGNATURE_MISMATCH
}
