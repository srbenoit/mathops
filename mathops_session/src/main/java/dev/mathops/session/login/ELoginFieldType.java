package dev.mathops.session.login;

/**
 * Possible types of fields a login form may have.
 */
enum ELoginFieldType {

    /** A text field (plaintext, visible to the user). */
    TEXT,

    /** A password field (hidden/obscured as the user types). */
    PASSWORD,

    /** A selection from several options. */
    SELECT
}
