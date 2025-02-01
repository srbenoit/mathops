package dev.mathops.db.rec;

import dev.mathops.db.type.TermKey;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * An immutable record of a live student record.
 */
public final class LiveStudent implements Serializable, Comparable<LiveStudent> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6492895555044085174L;

    /** The student ID. */
    public final String studentId;

    /** The student's internal ID (PIDM). */
    public final Integer internalId;

    /** The student's last name. */
    public final String lastName;

    /** The student's first name. */
    public final String firstName;

    /** The student preferred first name. */
    public final String prefFirstName;

    /** The student's middle name. */
    public final String middleInitial;

    /** The student college code. */
    public final String collegeCode;

    /** The student department code. */
    public final String departmentCode;

    /** The student program of study code. */
    public final String programCode;

    /** The student major code. */
    private final String majorCode;

    /** The student minor code. */
    public final String minorCode;

    /** The student's high school code. */
    public final String highSchoolCode;

    /** The student's high school GPA. */
    public final String highSchoolGpa;

    /** The high school class rank. */
    public final Integer highSchoolClassRank;

    /** The high school class size. */
    public final Integer highSchoolClassSize;

    /** The ACT math score. */
    public final Integer actScore;

    /** The SAT math score. */
    public final Integer satScore;

    /** The SAT (revised) math score. */
    public final Integer satrScore;

    /** The residency. */
    public final String residency;

    /** The admission term. */
    public final TermKey admitTerm;

    /** The admit type. */
    public final String admitType;

    /** The estimated graduation date. */
    public final LocalDate estGradDate;

    /** The student's birthdate. */
    public final LocalDate birthDate;

    /** The gender. */
    public final String gender;

    /** The student's email address. */
    public final String email;

    /** The adviser's email address. */
    public final String adviserEmail;

    /** The student's state. */
    public final String state;

    /** The student's campus. */
    public final String campus;

    /**
     * Constructs a new {@code LiveStudent}.
     *
     * @param theStudentId           the student ID
     * @param theInternalId          the student's internal ID
     * @param theLastName            the student's last name
     * @param theFirstName           the stuednt's first name
     * @param thePrefName            the student's preferred first name
     * @param theMiddleInitial       the student's middle initial
     * @param theCollegeCode         the college code
     * @param theDepartmentCode      the department code
     * @param theProgramCode         the program code
     * @param theMajorCode           the major code
     * @param theMinorCode           the minor code
     * @param theHighSchoolCode      the high school code
     * @param theHighSchoolGpa       the high school GPA
     * @param theHighSchoolClassRank the high school class rank
     * @param theHighSchoolClassSize the high school class size
     * @param theActScore            the ACT score
     * @param theSatScore            the SAT score
     * @param theSatrScore           the revised SAT score
     * @param theResidency           the residency
     * @param theAdmitTerm           the admit term
     * @param theAdmitType           the admit type
     * @param theEstGradDate         the estimated graduation date
     * @param theBirthDate           the student's birthdate
     * @param theGender              the student's gender
     * @param theEmail               the student's email
     * @param theAdviserEmail        the adviser's email
     * @param theState               the state
     * @param theCampus              the campus
     */
    public LiveStudent(final String theStudentId, final Integer theInternalId,
                       final String theLastName, final String theFirstName, final String thePrefName,
                       final String theMiddleInitial, final String theCollegeCode, final String theDepartmentCode,
                       final String theProgramCode, final String theMajorCode, final String theMinorCode,
                       final String theHighSchoolCode, final String theHighSchoolGpa,
                       final Integer theHighSchoolClassRank, final Integer theHighSchoolClassSize,
                       final Integer theActScore, final Integer theSatScore, final Integer theSatrScore,
                       final String theResidency, final TermKey theAdmitTerm, final String theAdmitType,
                       final LocalDate theEstGradDate, final LocalDate theBirthDate, final String theGender,
                       final String theEmail, final String theAdviserEmail, final String theState,
                       final String theCampus) {

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theInternalId == null) {
            throw new IllegalArgumentException("Internal ID may not be null");
        }

        this.studentId = theStudentId;
        this.internalId = theInternalId;
        this.lastName = theLastName;
        this.firstName = theFirstName;
        this.prefFirstName = thePrefName;
        this.middleInitial = theMiddleInitial;
        this.collegeCode = theCollegeCode;
        this.departmentCode = theDepartmentCode;
        this.programCode = theProgramCode;
        this.majorCode = theMajorCode;
        this.minorCode = theMinorCode;
        this.highSchoolCode = theHighSchoolCode;
        this.highSchoolGpa = theHighSchoolGpa;
        this.highSchoolClassRank = theHighSchoolClassRank;
        this.highSchoolClassSize = theHighSchoolClassSize;
        this.actScore = theActScore;
        this.satScore = theSatScore;
        this.satrScore = theSatrScore;
        this.residency = theResidency;
        this.admitTerm = theAdmitTerm;
        this.admitType = theAdmitType;
        this.estGradDate = theEstGradDate;
        this.birthDate = theBirthDate;
        this.gender = theGender;
        this.email = theEmail;
        this.adviserEmail = theAdviserEmail;
        this.state = theState;
        this.campus = theCampus;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.studentId) //
                + Objects.hashCode(this.internalId) //
                + Objects.hashCode(this.lastName) //
                + Objects.hashCode(this.firstName) //
                + Objects.hashCode(this.prefFirstName) //
                + Objects.hashCode(this.middleInitial) //
                + Objects.hashCode(this.collegeCode) //
                + Objects.hashCode(this.departmentCode) //
                + Objects.hashCode(this.programCode) //
                + Objects.hashCode(this.majorCode) //
                + Objects.hashCode(this.minorCode) //
                + Objects.hashCode(this.highSchoolCode) //
                + Objects.hashCode(this.highSchoolGpa) //
                + Objects.hashCode(this.highSchoolClassRank) //
                + Objects.hashCode(this.highSchoolClassSize) //
                + Objects.hashCode(this.actScore) //
                + Objects.hashCode(this.satScore) //
                + Objects.hashCode(this.satrScore) //
                + Objects.hashCode(this.residency) //
                + Objects.hashCode(this.admitTerm) //
                + Objects.hashCode(this.admitType) //
                + Objects.hashCode(this.estGradDate) //
                + Objects.hashCode(this.birthDate) //
                + Objects.hashCode(this.gender) //
                + Objects.hashCode(this.email) //
                + Objects.hashCode(this.adviserEmail) //
                + Objects.hashCode(this.state) //
                + Objects.hashCode(this.campus);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final LiveStudent stu) {
            equal = Objects.equals(this.studentId, stu.studentId)
                    && Objects.equals(this.internalId, stu.internalId)
                    && Objects.equals(this.lastName, stu.lastName)
                    && Objects.equals(this.firstName, stu.firstName)
                    && Objects.equals(this.prefFirstName, stu.prefFirstName)
                    && Objects.equals(this.middleInitial, stu.middleInitial)
                    && Objects.equals(this.collegeCode, stu.collegeCode)
                    && Objects.equals(this.departmentCode, stu.departmentCode)
                    && Objects.equals(this.programCode, stu.programCode)
                    && Objects.equals(this.majorCode, stu.majorCode)
                    && Objects.equals(this.minorCode, stu.minorCode)
                    && Objects.equals(this.highSchoolCode, stu.highSchoolCode)
                    && Objects.equals(this.highSchoolGpa, stu.highSchoolGpa)
                    && Objects.equals(this.highSchoolClassRank, stu.highSchoolClassRank)
                    && Objects.equals(this.highSchoolClassSize, stu.highSchoolClassSize)
                    && Objects.equals(this.actScore, stu.actScore)
                    && Objects.equals(this.satScore, stu.satScore)
                    && Objects.equals(this.satrScore, stu.satrScore)
                    && Objects.equals(this.residency, stu.residency)
                    && Objects.equals(this.admitTerm, stu.admitTerm)
                    && Objects.equals(this.admitType, stu.admitType)
                    && Objects.equals(this.estGradDate, stu.estGradDate)
                    && Objects.equals(this.birthDate, stu.birthDate)
                    && Objects.equals(this.gender, stu.gender)
                    && Objects.equals(this.email, stu.email)
                    && Objects.equals(this.adviserEmail, stu.adviserEmail)
                    && Objects.equals(this.state, stu.state)
                    && Objects.equals(this.campus, stu.campus);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.majorCode;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final LiveStudent o) {

        return this.studentId.compareTo(o.studentId);
    }
}
