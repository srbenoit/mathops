package dev.mathops.db.enums;

/**
 * The permitted roles for users of the system.
 */
public enum ERole {

    /** Actions the web server takes without a user logged in. */
    XWEB_SERVER("WEB"),

    /** A bookstore staff-member. */
    BOOKSTORE("BKS"),

    /** A guest. */
    GUEST("GUE"),

    /** A student. */
    STUDENT("STU"),

    /** A tutor. */
    TUTOR("TUT"),

    /** A student adviser. */
    ADVISER("ADV", STUDENT, GUEST),

    /** An exam proctor. */
    PROCTOR("PRO"),

    /** A course instructor. */
    INSTRUCTOR("INS", PROCTOR, TUTOR, STUDENT, GUEST),

    /** Testing Center checkin/checkout stations. */
    CHECKIN_CHECKOUT("CHK"),

    /** The resource lending desk. */
    RESOURCE_DESK("RES"),

    /** An office staff member. */
    OFFICE_STAFF("STF", RESOURCE_DESK, CHECKIN_CHECKOUT, INSTRUCTOR, ADVISER, PROCTOR,
            TUTOR, STUDENT, GUEST),

    /** A director. */
    DIRECTOR("DIR", OFFICE_STAFF, RESOURCE_DESK, CHECKIN_CHECKOUT, INSTRUCTOR, ADVISER,
            PROCTOR, TUTOR, STUDENT, GUEST, BOOKSTORE),

    /** An administrator. */
    ADMINISTRATOR("ADM", DIRECTOR, OFFICE_STAFF, RESOURCE_DESK, CHECKIN_CHECKOUT,
            INSTRUCTOR, ADVISER, PROCTOR, TUTOR, STUDENT, GUEST, BOOKSTORE),

    /** A system administrator (all permissions). */
    SYSADMIN("SYS", ADMINISTRATOR, DIRECTOR, OFFICE_STAFF, RESOURCE_DESK,
            CHECKIN_CHECKOUT, INSTRUCTOR, PROCTOR, ADVISER, TUTOR, STUDENT,
            GUEST, BOOKSTORE);

    /** A short three-letter abbreviation of the role. */
    public final String abbrev;

    /** The set of roles a given role can act as. */
    private final ERole[] canActAs;

    /**
     * Constructs a new {@code ERole}.
     *
     * @param theAbbrev   the abbreviation
     * @param theCanActAs the set of roles a given role can act as
     */
    ERole(final String theAbbrev, final ERole... theCanActAs) {

        this.abbrev = theAbbrev;
        this.canActAs = theCanActAs.clone();
    }

    /**
     * Tests whether users working under a given role can act as users under another role.
     *
     * @param target the target role
     * @return {@code true} if the role can act as the target role; {@code false} if not
     */
    public boolean canActAs(final ERole target) {

        boolean canAct = false;

        if (target == this) {
            canAct = true;
        } else {
            for (final ERole test : this.canActAs) {
                if (test == target) {
                    canAct = true;
                    break;
                }
            }
        }

        return canAct;
    }

    /**
     * Gets the role that corresponds to a particular abbreviation.
     *
     * @param theAbbrev the abbreviation
     * @return the corresponding role, {@code null} if no role corresponds to the provided abbreviation
     */
    public static ERole fromAbbrev(final String theAbbrev) {

        ERole role = null;

        for (final ERole test : values()) {

            if (test.abbrev.equals(theAbbrev)) {
                role = test;
                break;
            }
        }

        return role;
    }
}
