package dev.mathops.db.rec;

import dev.mathops.db.type.TermKey;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * An immutable record of a live registration record.
 */
public final class LiveReg implements Serializable, Comparable<LiveReg> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2060716177823202260L;

    /** The term data. */
    public final TermKey term;

    /** The anticipated graduation term data. */
    public final TermKey anticGradTerm;

    /** The student ID. */
    public final String studentId;

    /** The student's internal ID. */
    public final Integer internalId;

    /** The student's last name. */
    public final String lastName;

    /** The student's first name. */
    public final String firstName;

    /** The student's class level. */
    public final String classLevel;

    /** The student's home college. */
    public final String college;

    /** The student's home department. */
    public final String department;

    /** The student's primary major. */
    public final String major1;

    /** The student's number of transfer credits. */
    public final String numTransferCredits;

    /** The student's high school code. */
    public final String highSchoolCode;

    /** The student's high school GPA. */
    public final String highSchoolGpa;

    /** The student's high school class rank. */
    public final Integer highSchoolClassRank;

    /** The student's high school class size. */
    public final Integer highSchoolClassSize;

    /** The student's ACT math score. */
    public final Integer actScore;

    /** The student's SAT math score. */
    public final Integer satScore;

    /** The student's SAT (Revised) math score. */
    public final Integer satrScore;

    /** The student's AP math score. */
    public final String apScore;

    /** The student's residency. */
    public final String residency;

    /** The student's birthdate. */
    public final LocalDate birthDate;

    /** The student's gender. */
    public final String gender;

    /** The student's email. */
    public final String email;

    /** The student's adviser's email. */
    public final String adviserEmail;

    /** The student's campus. */
    public final String campus;

    /** The student's admit type. */
    public final String admitType;

    /** The course ID. */
    public final String courseId;

    /** The section number. */
    public final String sectionNum;

    /** The grading option. */
    public final String gradingOption;

    /** The registration status. */
    public final String registrationStatus;

    /** The instruction type. */
    public String instructionType;

    /**
     * Constructs a new {@code LiveReg}.
     *
     * @param theTerm                the term
     * @param theAnticGradTerm       the anticipated graduation term
     * @param theStudentId           the student ID
     * @param theInternalId          the internal ID
     * @param theLastName            the student's last name
     * @param theFirstName           the student's first name
     * @param theClassLevel          the student's class level
     * @param theCollege             the college
     * @param theDepartment          the department
     * @param theMajor1              the primary major
     * @param theNumTransferCredits  the number of transfer credits
     * @param theHighSchoolCode      the high school code
     * @param theHighSchoolGpa       the high school GPA
     * @param theHighSchoolClassRank the high school class rank
     * @param theHighSchoolClassSize the high school class size
     * @param theActScore            the ACT score
     * @param theSatScore            the SAT score
     * @param theSatrScore           the revised SAT score
     * @param theApScore             the AP score
     * @param theResidency           the residency
     * @param theBirthDate           the birthdate
     * @param theGender              the gender
     * @param theEmail               the email address
     * @param theAdviserEmail        the adviser's email address
     * @param theCampus              the campus
     * @param theAdmitType           the admit type
     * @param theCourseId            the course ID
     * @param theSectionNum          the section number
     * @param theGradingOption       the grading option
     * @param theRegistrationStatus  the registration status
     * @param theInstructionType     the instruction type
     */
    public LiveReg(final TermKey theTerm, final TermKey theAnticGradTerm, final String theStudentId,
                   final Integer theInternalId, final String theLastName, final String theFirstName,
                   final String theClassLevel, final String theCollege, final String theDepartment,
                   final String theMajor1, final String theNumTransferCredits, final String theHighSchoolCode,
                   final String theHighSchoolGpa, final Integer theHighSchoolClassRank,
                   final Integer theHighSchoolClassSize, final Integer theActScore, final Integer theSatScore,
                   final Integer theSatrScore, final String theApScore, final String theResidency,
                   final LocalDate theBirthDate, final String theGender, final String theEmail,
                   final String theAdviserEmail, final String theCampus, final String theAdmitType,
                   final String theCourseId, final String theSectionNum, final String theGradingOption,
                   final String theRegistrationStatus, final String theInstructionType) {

        if (theTerm == null) {
            throw new IllegalArgumentException("Exam ID may not be null");
        }
        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theInternalId == null) {
            throw new IllegalArgumentException("Internal ID may not be null");
        }
        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theSectionNum == null) {
            throw new IllegalArgumentException("Section number may not be null");
        }

        this.term = theTerm;
        this.anticGradTerm = theAnticGradTerm;
        this.studentId = theStudentId;
        this.internalId = theInternalId;
        this.lastName = theLastName;
        this.firstName = theFirstName;
        this.classLevel = theClassLevel;
        this.college = theCollege;
        this.department = theDepartment;
        this.major1 = theMajor1;
        this.numTransferCredits = theNumTransferCredits;
        this.highSchoolCode = theHighSchoolCode;
        this.highSchoolGpa = theHighSchoolGpa;
        this.highSchoolClassRank = theHighSchoolClassRank;
        this.highSchoolClassSize = theHighSchoolClassSize;
        this.actScore = theActScore;
        this.satScore = theSatScore;
        this.satrScore = theSatrScore;
        this.apScore = theApScore;
        this.residency = theResidency;
        this.birthDate = theBirthDate;
        this.gender = theGender;
        this.email = theEmail;
        this.adviserEmail = theAdviserEmail;
        this.campus = theCampus;
        this.admitType = theAdmitType;
        this.courseId = theCourseId;
        this.sectionNum = theSectionNum;
        this.gradingOption = theGradingOption;
        this.registrationStatus = theRegistrationStatus;
        this.instructionType = theInstructionType;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.term.hashCode() //
                + Objects.hashCode(this.anticGradTerm) //
                + this.studentId.hashCode() //
                + this.internalId.hashCode() //
                + Objects.hashCode(this.lastName) //
                + Objects.hashCode(this.firstName) //
                + Objects.hashCode(this.classLevel) //
                + Objects.hashCode(this.college) //
                + Objects.hashCode(this.department) //
                + Objects.hashCode(this.major1) //
                + Objects.hashCode(this.numTransferCredits) //
                + Objects.hashCode(this.highSchoolCode) //
                + Objects.hashCode(this.highSchoolGpa) //
                + Objects.hashCode(this.highSchoolClassRank) //
                + Objects.hashCode(this.highSchoolClassSize) //
                + Objects.hashCode(this.actScore) //
                + Objects.hashCode(this.satScore) //
                + Objects.hashCode(this.satrScore) //
                + Objects.hashCode(this.apScore) //
                + Objects.hashCode(this.residency) //
                + Objects.hashCode(this.birthDate) //
                + Objects.hashCode(this.gender) //
                + Objects.hashCode(this.email) //
                + Objects.hashCode(this.adviserEmail) //
                + Objects.hashCode(this.campus) //
                + Objects.hashCode(this.admitType) //
                + this.courseId.hashCode() //
                + this.sectionNum.hashCode() //
                + Objects.hashCode(this.gradingOption) //
                + Objects.hashCode(this.registrationStatus) //
                + Objects.hashCode(this.instructionType);
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
        } else if (obj instanceof final LiveReg reg) {
            equal = this.term.equals(reg.term)
                    && Objects.equals(this.anticGradTerm, reg.anticGradTerm)
                    && this.studentId.equals(reg.studentId) //
                    && this.internalId.equals(reg.internalId) //
                    && Objects.equals(this.lastName, reg.lastName)
                    && Objects.equals(this.firstName, reg.firstName)
                    && Objects.equals(this.classLevel, reg.classLevel)
                    && Objects.equals(this.college, reg.college)
                    && Objects.equals(this.department, reg.department)
                    && Objects.equals(this.major1, reg.major1)
                    && Objects.equals(this.numTransferCredits, reg.numTransferCredits)
                    && Objects.equals(this.highSchoolCode, reg.highSchoolCode)
                    && Objects.equals(this.highSchoolGpa, reg.highSchoolGpa)
                    && Objects.equals(this.highSchoolClassRank, reg.highSchoolClassRank)
                    && Objects.equals(this.highSchoolClassSize, reg.highSchoolClassSize)
                    && Objects.equals(this.actScore, reg.actScore)
                    && Objects.equals(this.satScore, reg.satScore)
                    && Objects.equals(this.satrScore, reg.satrScore)
                    && Objects.equals(this.apScore, reg.apScore)
                    && Objects.equals(this.residency, reg.residency)
                    && Objects.equals(this.birthDate, reg.birthDate)
                    && Objects.equals(this.gender, reg.gender)
                    && Objects.equals(this.email, reg.email)
                    && Objects.equals(this.adviserEmail, reg.adviserEmail)
                    && Objects.equals(this.campus, reg.campus)
                    && Objects.equals(this.admitType, reg.admitType)
                    && this.courseId.equals(reg.courseId) //
                    && this.sectionNum.equals(reg.sectionNum) //
                    && Objects.equals(this.gradingOption, reg.gradingOption)
                    && Objects.equals(this.registrationStatus, reg.registrationStatus)
                    && Objects.equals(this.instructionType, reg.instructionType);
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

        return this.courseId;
    }

    /**
     * Updates the instruction type in this record.
     *
     * @param theInstructionType the instruction type
     */
    public void setInstructionType(final String theInstructionType) {

        this.instructionType = theInstructionType;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final LiveReg o) {

        int result = this.term.compareTo(o.term);

        if (result == 0) {
            result = this.studentId.compareTo(o.studentId);
            if (result == 0) {
                result = this.courseId.compareTo(o.courseId);
                if (result == 0) {
                    result = this.sectionNum.compareTo(o.sectionNum);
                }
            }
        }

        return result;
    }
}
