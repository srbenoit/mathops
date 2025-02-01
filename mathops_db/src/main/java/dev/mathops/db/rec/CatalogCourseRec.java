package dev.mathops.db.rec;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.enums.EGradeMode;
import dev.mathops.db.enums.EOfferingTermName;
import dev.mathops.db.type.CatalogCourseNumber;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;

/**
 * A "catalog course" record.
 */
public final class CatalogCourseRec extends RecBase implements Comparable<CatalogCourseRec> {

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_FRESHMAN = 0x0001;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_SOPHOMORE = 0x0002;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_JUNIOR = 0x0004;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_SENIOR = 0x0008;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_SENIOR_5_YEAR = 0x0010;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_SENIOR_POST_BACC = 0x0020;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_SENIOR_SECOND_BACC = 0x0040;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_UNDERGRADUATE = 0x007F;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_GRADUATE = 0x0100;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_PROFESSIONAL = 0x0200;

    /** A possible component in the "allow" bit flag field. */
    public static final int ALLOW_SELF_IMPROVEMENT = 0x0400;

    /** A field name for serialization of records. */
    private static final String FLD_COURSE_NUMBER = "course_number";

    /** A field name for serialization of records. */
    private static final String FLD_TITLE = "title";

    /** A field name for serialization of records. */
    private static final String FLD_DESCRIPTION = "description";

    /** A field name for serialization of records. */
    private static final String FLD_PREREQUISITE = "prerequisite";

    /** A field name for serialization of records. */
    private static final String FLD_REGISTRATION_INFO = "registration_info";

    /** A field name for serialization of records. */
    private static final String FLD_RESTRICTION = "restriction";

    /** A field name for serialization of records. */
    private static final String FLD_TERMS_OFFERED = "terms_offered";

    /** A field name for serialization of records. */
    private static final String FLD_GRADE_MODE = "grade_mode";

    /** A field name for serialization of records. */
    private static final String FLD_SPECIAL_COURSE_FEE = "special_course_fee";

    /** A field name for serialization of records. */
    private static final String FLD_ADDITIONAL_INFO = "additional_info";

    /** A field name for serialization of records. */
    private static final String FLD_GT_CODE = "gt_code";

    /** A field name for serialization of records. */
    private static final String FLD_MIN_CREDITS = "min_credits";

    /** A field name for serialization of records. */
    private static final String FLD_MAX_CREDITS = "max_credits";

    /**
     * The course number field value, such as "MATH 126".  This represents the course_id, prefix, and number fields,
     * which must be set together as a unit.
     */
    public final CatalogCourseNumber courseNumber;

    /** The 'title' field value, such as "Topology". */
    public final String title;

    /** The 'description' field value, with the catalog course description. */
    public final String description;

    /** The 'prerequisite' field value. */
    public final String prerequisite;

    /** The 'registration_info' field value from the catalog. */
    public final String registrationInfo;

    /** The 'restriction' field value from the catalog. */
    public final String restriction;

    /** The 'terms_offered' field value, with the terms in which the course is offered. */
    public final EnumSet<EOfferingTermName> termsOffered;

    /** The 'grade_mode' field value from the catalog. */
    public final EGradeMode gradeMode;

    /** The 'special_course_fee' field value from the catalog. */
    public final String specialCourseFee;

    /** The 'additional_info' field value from the catalog. */
    public final String additionalInfo;

    /** The 'gt_code' field value from the catalog, such as "MA1". */
    public final String gtCode;

    /** The minimum (or fixed) number of credits. */
    public final Integer minCredits;

    /** The maximum (or fixed) number of credits. */
    public final Integer maxCredits;

    /**
     * Constructs a new {@code CatalogCourseRec}.
     *
     * @param theCourseNumber     the catalog course number
     * @param theTitle            the 'title' field value
     * @param theDescription      the 'description' field value
     * @param thePrerequisite     the 'prerequisite' field value
     * @param theRegistrationInfo the 'registration_info' field value
     * @param theRestriction      the 'restriction' field value
     * @param theTermsOffered     the 'terms_offered' field value
     * @param theGradeMode        the 'grade_mode" field value
     * @param theSpecialCourseFee the 'special_course_fee' field value
     * @param theAdditionalInfo   the 'additional_info' field value
     * @param theGtCode           the 'gt_code' field value
     * @param theMinCredits       the minimum (or fixed) number of credits
     * @param theMaxCredits       the maximum (or fixed) number of credits
     */
    public CatalogCourseRec(final CatalogCourseNumber theCourseNumber, final String theTitle,
                            final String theDescription, final String thePrerequisite, final String theRegistrationInfo,
                            final String theRestriction, final Collection<EOfferingTermName> theTermsOffered,
                            final EGradeMode theGradeMode, final String theSpecialCourseFee,
                            final String theAdditionalInfo, final String theGtCode, final Integer theMinCredits,
                            final Integer theMaxCredits) {

        super();

        this.courseNumber = theCourseNumber;
        this.title = theTitle;
        this.description = theDescription;
        this.prerequisite = thePrerequisite;
        this.registrationInfo = theRegistrationInfo;
        this.restriction = theRestriction;
        this.termsOffered = EnumSet.copyOf(theTermsOffered);
        this.gradeMode = theGradeMode;
        this.specialCourseFee = theSpecialCourseFee;
        this.additionalInfo = theAdditionalInfo;
        this.gtCode = theGtCode;
        this.minCredits = theMinCredits;
        this.maxCredits = theMaxCredits;
    }

    /**
     * Parses a string representation of the set of terms during which a catalog says the course is offered.
     *
     * @param offeringString the string representation from the catalog, such as "Fall, Spring."
     * @return the generated set
     * @throws IllegalArgumentException if the provided string cannot be parsed
     */
    public static EnumSet<EOfferingTermName> parseTermsOffered(final String offeringString) {

        final EnumSet<EOfferingTermName> set = EnumSet.noneOf(EOfferingTermName.class);

        final String[] list = offeringString.split(CoreConstants.COMMA);
        for (final String item : list) {
            final String trimmed = item.trim().replace(".", "").toLowerCase(Locale.ROOT);

            if (trimmed.startsWith("fall")) {
                switch (trimmed) {
                    case "fall" -> set.add(EOfferingTermName.EVERY_FALL);
                    case "fall (even years)" -> set.add(EOfferingTermName.FALL_EVEN_YEARS);
                    case "fall (odd years)" -> set.add(EOfferingTermName.FALL_ODD_YEARS);
                    case "fall (every third year)" -> set.add(EOfferingTermName.FALL_THIRD_YEARS);
                    case "fall (as needed)", "fall offered as needed" -> set.add(EOfferingTermName.FALL_AS_NEEDED);
                    case "fall spring" -> {
                        set.add(EOfferingTermName.EVERY_FALL);
                        set.add(EOfferingTermName.EVERY_SPRING);
                    }
                    default -> throw new IllegalArgumentException("Invalid term name in list: '" + trimmed + "'");
                }
            } else if (trimmed.startsWith("spring")) {
                switch (trimmed) {
                    case "spring" -> set.add(EOfferingTermName.EVERY_SPRING);
                    case "spring (even years)" -> set.add(EOfferingTermName.SPRING_EVEN_YEARS);
                    case "spring (odd years)" -> set.add(EOfferingTermName.SPRING_ODD_YEARS);
                    case "spring (every third year)" -> set.add(EOfferingTermName.SPRING_THIRD_YEARS);
                    case "spring (as needed)", "spring offered as needed" ->
                            set.add(EOfferingTermName.SPRING_AS_NEEDED);
                    default -> throw new IllegalArgumentException("Invalid term name in list: '" + trimmed + "'");
                }
            } else if (trimmed.startsWith("summer")) {
                switch (trimmed) {
                    case "summer" -> set.add(EOfferingTermName.EVERY_SUMMER);
                    case "summer (even years)" -> set.add(EOfferingTermName.SUMMER_EVEN_YEARS);
                    case "summer (odd years)" -> set.add(EOfferingTermName.SUMMER_ODD_YEARS);
                    case "summer (every third year)" -> set.add(EOfferingTermName.SUMMER_THIRD_YEARS);
                    case "summer (as needed)", "summer offered as needed" ->
                            set.add(EOfferingTermName.SUMMER_AS_NEEDED);
                    default -> throw new IllegalArgumentException("Invalid term name in list: '" + trimmed + "'");
                }
            } else {
                throw new IllegalArgumentException("Invalid term name in list: '" + trimmed + "'");
            }
        }

        return set;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CatalogCourseRec o) {

        return compareAllowingNull(this.courseNumber, o.courseNumber);
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_COURSE_NUMBER, this.courseNumber);
        htm.add(DIVIDER);
        appendField(htm, FLD_TITLE, this.title);
        htm.add(DIVIDER);
        appendField(htm, FLD_DESCRIPTION, this.description);
        htm.add(DIVIDER);
        appendField(htm, FLD_PREREQUISITE, this.prerequisite);
        htm.add(DIVIDER);
        appendField(htm, FLD_REGISTRATION_INFO, this.registrationInfo);
        htm.add(DIVIDER);
        appendField(htm, FLD_RESTRICTION, this.restriction);
        htm.add(DIVIDER);
        if (this.termsOffered == null) {
            appendField(htm, FLD_TERMS_OFFERED, null);
        } else {
            htm.add(FLD_TERMS_OFFERED, "=");
            boolean comma = false;
            for (final EOfferingTermName offerTerm : this.termsOffered) {
                if (comma) {
                    htm.add(',');
                }
                htm.add(offerTerm.label);
                comma = true;
            }
        }
        htm.add(DIVIDER);
        appendField(htm, FLD_GRADE_MODE, this.gradeMode);
        htm.add(DIVIDER);
        appendField(htm, FLD_SPECIAL_COURSE_FEE, this.specialCourseFee);
        htm.add(DIVIDER);
        appendField(htm, FLD_ADDITIONAL_INFO, this.additionalInfo);
        htm.add(DIVIDER);
        appendField(htm, FLD_GT_CODE, this.gtCode);
        htm.add(DIVIDER);
        appendField(htm, FLD_MIN_CREDITS, this.minCredits);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAX_CREDITS, this.maxCredits);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.courseNumber)
               + Objects.hashCode(this.title)
               + Objects.hashCode(this.description)
               + Objects.hashCode(this.prerequisite)
               + Objects.hashCode(this.registrationInfo)
               + Objects.hashCode(this.restriction)
               + Objects.hashCode(this.termsOffered)
               + Objects.hashCode(this.gradeMode)
               + Objects.hashCode(this.specialCourseFee)
               + Objects.hashCode(this.additionalInfo)
               + Objects.hashCode(this.gtCode)
               + Objects.hashCode(this.minCredits)
               + Objects.hashCode(this.maxCredits);
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
        } else if (obj instanceof final CatalogCourseRec rec) {
            equal = Objects.equals(this.courseNumber, rec.courseNumber)
                    && Objects.equals(this.title, rec.title)
                    && Objects.equals(this.description, rec.description)
                    && Objects.equals(this.prerequisite, rec.prerequisite)
                    && Objects.equals(this.registrationInfo, rec.registrationInfo)
                    && Objects.equals(this.restriction, rec.restriction)
                    && Objects.equals(this.termsOffered, rec.termsOffered)
                    && this.gradeMode == rec.gradeMode
                    && Objects.equals(this.specialCourseFee, rec.specialCourseFee)
                    && Objects.equals(this.additionalInfo, rec.additionalInfo)
                    && Objects.equals(this.gtCode, rec.gtCode)
                    && Objects.equals(this.minCredits, rec.minCredits)
                    && Objects.equals(this.maxCredits, rec.maxCredits);
        } else {
            equal = false;
        }

        return equal;
    }
}
