package dev.mathops.web.websocket.help;

import dev.mathops.session.ImmutableSessionInfo;

/**
 * Identification and name of a student, usable as a map key, and sortable.
 */
public final class StudentKey implements Comparable<StudentKey> {

    /** The student ID. */
    public final String studentId;

    /** The student first name. */
    public final String firstName;

    /** The student last name. */
    public final String lastName;

    /** The student screen name. */
    public final String screenName;

    /**
     * Constructs a new {@code ConversationKey}.
     *
     * @param theStudentId  the student ID
     * @param theFirstName  the student first name
     * @param theLastName   the student last name
     * @param theScreenName the student screen name
     */
    public StudentKey(final String theStudentId, final String theFirstName,
                      final String theLastName, final String theScreenName) {

        if (theStudentId == null || theFirstName == null || theLastName == null || theScreenName == null) {
            throw new IllegalArgumentException("Invalid arguments to construction of StudentKey");
        }

        this.studentId = theStudentId;
        this.firstName = theFirstName;
        this.lastName = theLastName;
        this.screenName = theScreenName;
    }

    /**
     * Constructs a {@code StudentKey} from a login session.
     *
     * @param loginSession the login session
     */
    public StudentKey(final ImmutableSessionInfo loginSession) {

        if (loginSession == null) {
            throw new IllegalArgumentException("Login session may not be null");
        }

        this.studentId = loginSession.getEffectiveUserId();
        this.firstName = loginSession.getEffectiveFirstName();
        this.lastName = loginSession.getEffectiveLastName();
        this.screenName = loginSession.getEffectiveScreenName();
    }

    /**
     * Generates a hash code for the key.
     */
    @Override
    public int hashCode() {

        return this.studentId.hashCode() + this.firstName.hashCode() + this.lastName.hashCode()
                + this.screenName.hashCode();
    }

    /**
     * Generates a hash code for the key.
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (this == obj) {
            equal = true;
        } else if (obj instanceof final StudentKey sk) {
            equal = this.studentId.equals(sk.studentId) && this.firstName.equals(sk.firstName)
                    && this.lastName.equals(sk.lastName) && this.screenName.equals(sk.screenName);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this {@code ConversationListKey} to another for order. Keys are compared first on student last name,
     * then on student first name, then on student ID.
     *
     * @param o the other {@code ConversationListKey}
     * @return -1, 0, or 1 as this object is less than, equal to, or greater than {@code o}
     */
    @Override
    public int compareTo(final StudentKey o) {

        int result = this.lastName.compareTo(o.lastName);
        if (result == 0) {
            result = this.firstName.compareTo(o.firstName);
            if (result == 0) {
                result = this.studentId.compareTo(o.studentId);
                if (result == 0) {
                    result = this.screenName.compareTo(o.screenName);
                }
            }
        }

        return result;
    }
}
