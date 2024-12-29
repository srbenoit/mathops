package dev.mathops.db.type;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.enums.ETermName;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.NotSerializableException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

/**
 * A container for key that identifies a term.
 */
public final class TermKey implements Serializable, Comparable<TermKey> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8816964238654081688L;

    /** The term name. */
    public final ETermName name;

    /** The 2-letter term code. */
    public final String termCode;

    /** The term year. */
    public final Integer year;

    /** The short (2-digit) term year. */
    public final Integer shortYear;

    /** The derived long term string, like "Fall, 2017". */
    public final String longString;

    /** The derived short term string, like "FA17". */
    public final String shortString;

    /**
     * Constructs a new {@code TermKey}.
     *
     * @param theName the term name
     * @param theYear the term year
     */
    public TermKey(final ETermName theName, final int theYear) {

        if (theName == null) {
            throw new IllegalArgumentException("Name may not be null");
        }

        this.name = theName;
        this.termCode = theName.termName;
        this.year = Integer.valueOf(theYear);

        final int intYear = this.year.intValue();
        final Integer shortYr = Integer.valueOf(intYear % 100);

        final HtmlBuilder builder = new HtmlBuilder(50);
        builder.add(this.name.fullName, ", ").add(intYear);

        final String longStr = builder.toString();

        builder.reset();

        builder.add(this.name.termName);
        builder.add(intYear / 10 % 10);
        builder.add(intYear % 10);

        final String shortStr = builder.toString();

        this.shortYear = shortYr;
        this.longString = longStr;
        this.shortString = shortStr;
    }

    /**
     * Constructs a new {@code TermKey}.
     *
     * @param theName the term name
     * @param theYear the term year
     */
    public TermKey(final ETermName theName, final Integer theYear) {

        if (theName == null || theYear == null) {
            throw new IllegalArgumentException("tern name and year may not be null");
        }

        this.name = theName;
        this.termCode = theName.termName;
        this.year = theYear;

        final int intYear = this.year.intValue();
        final Integer shortYr = Integer.valueOf(intYear % 100);

        final HtmlBuilder builder = new HtmlBuilder(50);
        builder.add(this.name.fullName, ", ").add(intYear);

        final String longStr = builder.toString();

        builder.reset();

        builder.add(this.name.termName);
        builder.add(intYear / 10 % 10);
        builder.add(intYear % 10);

        final String shortStr = builder.toString();

        this.shortYear = shortYr;
        this.longString = longStr;
        this.shortString = shortStr;
    }

    /**
     * Constructs a new {@code TermKey}.
     *
     * @param theNumeric the numeric code
     */
    public TermKey(final Integer theNumeric) {

        if (theNumeric == null) {
            throw new IllegalArgumentException("numeric value may not be null");
        }

        final int yy = theNumeric.intValue() / 100;
        final int code = theNumeric.intValue() % 100;

        final ETermName term;
        if (code == 10) {
            term = ETermName.SPRING;
        } else if (code == 60) {
            term = ETermName.SUMMER;
        } else {
            term = ETermName.FALL;
        }

        this.name = term;
        this.termCode = term.termName;
        this.year = Integer.valueOf(yy);
        this.shortYear = Integer.valueOf(yy % 100);

        final HtmlBuilder builder = new HtmlBuilder(50);
        builder.add(this.name.fullName, ", ").add(yy);
        this.longString = builder.toString();

        builder.reset();
        builder.add(this.name.termName);
        builder.add(yy / 10 % 10);
        builder.add(yy % 10);
        this.shortString = builder.toString();
    }

    /**
     * Constructs a new {@code TermKey}.
     *
     * @param theNumeric the numeric code
     */
    public TermKey(final int theNumeric) {

        final int yy = theNumeric / 100;
        final int code = theNumeric % 100;

        final ETermName term;
        if (code == 10) {
            term = ETermName.SPRING;
        } else if (code == 60) {
            term = ETermName.SUMMER;
        } else {
            term = ETermName.FALL;
        }

        this.name = term;
        this.termCode = term.termName;
        this.year = Integer.valueOf(yy);
        this.shortYear = Integer.valueOf(yy % 100);

        final HtmlBuilder builder = new HtmlBuilder(50);
        builder.add(this.name.fullName, ", ").add(yy);
        this.longString = builder.toString();

        builder.reset();
        builder.add(this.name.termName);
        builder.add(yy / 10 % 10);
        builder.add(yy % 10);
        this.shortString = builder.toString();
    }

    /**
     * Extracts the term name and term year from a short term string (such as "FA17") and stores the values in the name
     * and year fields.
     *
     * @param shortTermString the short term string
     * @throws IllegalArgumentException if the short term string cannot be parsed
     */
    public TermKey(final String shortTermString) throws IllegalArgumentException {

        if (shortTermString == null) {
            throw new IllegalArgumentException(Res.get(Res.NULL_SHORT_TERM));
        }

        if (shortTermString.length() == 4) {
            final ETermName term = ETermName.forName(shortTermString.substring(0, 2));

            if (term == null) {
                try {
                    // Maybe it's just a year, like 2017? If so, assume SPRING term.
                    final int intYear = Integer.parseInt(shortTermString);
                    if (intYear > 2000 && intYear < 3000) {
                        this.name = ETermName.SPRING;
                        this.year = Integer.valueOf(intYear);
                    } else {
                        final String msg = Res.fmt(Res.BAD_SHORT_TERM, shortTermString);
                        throw new IllegalArgumentException(msg);
                    }
                } catch (final NumberFormatException ex) {
                    final String msg = Res.fmt(Res.BAD_SHORT_TERM, shortTermString);
                    throw new IllegalArgumentException(msg, ex);
                }
            } else {
                try {
                    final String yearString = shortTermString.substring(2);
                    final int yearValue = Integer.parseInt(yearString);

                    this.year = yearValue >= 80 ? Integer.valueOf(1900 + yearValue) : Integer.valueOf(2000 + yearValue);
                    this.name = term;
                } catch (final NumberFormatException ex) {
                    final String msg = Res.fmt(Res.BAD_SHORT_TERM, shortTermString, ex);
                    throw new IllegalArgumentException(msg);
                }
            }
        } else if (shortTermString.length() == 2) {
            try {
                // Maybe it's just a 2-digit year, like 17? If so, assume SPRING term.
                final int intYear = Integer.parseInt(shortTermString);
                if (intYear >= 0 && intYear < 40) {
                    this.name = ETermName.SPRING;
                    this.year = Integer.valueOf(2000 + intYear);
                } else {
                    final String msg = Res.fmt(Res.BAD_SHORT_TERM, shortTermString);
                    throw new IllegalArgumentException(msg);
                }
            } catch (final NumberFormatException ex) {
                final String msg = Res.fmt(Res.BAD_SHORT_TERM, shortTermString);
                throw new IllegalArgumentException(msg, ex);
            }
        } else {
            final String msg = Res.fmt(Res.BAD_SHORT_TERM, shortTermString);
            throw new IllegalArgumentException(msg);
        }

        final int intYear = this.year.intValue();
        final Integer shortYr = Integer.valueOf(intYear % 100);

        final HtmlBuilder builder = new HtmlBuilder(50);
        builder.add(this.name.fullName, ", ").add(intYear);

        final String longStr = builder.toString();

        builder.reset();

        builder.add(this.name.termName);
        builder.add(intYear / 10 % 10);
        builder.add(intYear % 10);

        final String shortStr = builder.toString();

        this.shortYear = shortYr;
        this.longString = longStr;
        this.shortString = shortStr;
        this.termCode = this.name.termName;
    }

    /**
     * Generates the "Banner" format string for the term. This is a 6-digit format with the year in the first four
     * digits, then "10", "60", or "90" in the last two (for Spring, Summer, and Fall, respectively).
     *
     * @return the Banner format string
     */
    public int toNumeric() {

        final int termNumeric;

        if (this.name == ETermName.SPRING) {
            termNumeric = 10;
        } else {
            termNumeric = this.name == ETermName.SUMMER ? 60 : 90;
        }

        return this.year.intValue() * 100 + termNumeric;
    }

    /**
     * Generates the term schema name for this term, such as "sm23". This is just the lowercase version of the short
     * term string.
     *
     * @return the term schema name
     */
    public String termSchemaName() {

        return this.shortString.toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a Banner-format term string ("201790") into a {@code TermKey} object.
     *
     * @param bannerTermString the term string in Banner format
     * @return the parsed {@code TermKey}.
     * @throws IllegalArgumentException if the term string cannot be parsed
     */
    public static TermKey parseNumericString(final String bannerTermString) throws IllegalArgumentException {

        if (bannerTermString == null) {
            throw new IllegalArgumentException(Res.get(Res.NULL_LONG_TERM));
        }
        if (bannerTermString.length() != 6) {
            throw new IllegalArgumentException(Res.fmt(Res.BAD_LONG_TERM, bannerTermString));
        }

        final String yearStr = bannerTermString.substring(0, 4);
        final String termStr = bannerTermString.substring(4);
        final int year = Integer.parseInt(yearStr);

        final ETermName term;
        if ("10".equals(termStr)) {
            term = ETermName.SPRING;
        } else if ("60".equals(termStr)) {
            term = ETermName.SUMMER;
        } else {
            term = ETermName.FALL;
        }

        return new TermKey(term, year);
    }

    /**
     * Parses a long term string ("Fall, 2017") into a {@code TermKey} object.
     *
     * @param longTermString the long term string
     * @return the parsed {@code TermKey}.
     * @throws IllegalArgumentException if the long term string cannot be parsed
     */
    public static TermKey parseLongString(final String longTermString) throws IllegalArgumentException {

        if (longTermString == null) {
            final String s = Res.get(Res.NULL_LONG_TERM);
            throw new IllegalArgumentException(s);
        }

        final int comma = longTermString.indexOf(CoreConstants.COMMA_CHAR);
        if (comma == -1) {
            final String msg = Res.fmt(Res.BAD_LONG_TERM, longTermString);
            throw new IllegalArgumentException(msg);
        }

        final String termStr = longTermString.substring(0, comma).trim();
        final String yearStr = longTermString.substring(comma + 1).trim();

        final ETermName term = ETermName.forFullName(termStr);
        if (term == null) {
            final String msg = Res.fmt(Res.BAD_LONG_TERM, longTermString);
            throw new IllegalArgumentException(msg);
        }

        final int year = Integer.parseInt(yearStr);

        return new TermKey(term, year);
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final TermKey o) {

        int result = this.year.compareTo(o.year);

        if (result == 0) {
            final int nameIndex = this.name.ordinal();
            final int otherNameIndex = o.name.ordinal();
            result = Integer.compare(nameIndex, otherNameIndex);
        }

        return result;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.name.hashCode() + this.year.hashCode();
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
        } else if (obj instanceof final TermKey key) {
            equal = this.name == key.name && this.year.equals(key.year);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Returns a new {@code TermKey} object that represents N terms after this term (including Summer terms).
     *
     * <p>
     * For example, if {@code add(2)} is called on a {@code TermKey} representing Fall, 2019, the result will be a
     * {@code TermKey} representing Summer, 2020.
     *
     * @param count the number of terms to add to this term
     * @return the new {@code TermKey}
     */
    public TermKey add(final int count) {

        ETermName newName = this.name;
        int newYear = this.year.intValue();

        for (int i = 0; i < count; ++i) {
            if (newName == ETermName.FALL) {
                newName = ETermName.SPRING;
                ++newYear;
            } else if (newName == ETermName.SPRING) {
                newName = ETermName.SUMMER;
            } else {
                newName = ETermName.FALL;
            }
        }

        return new TermKey(newName, newYear);
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    public String serializedString() {

        return SimpleBuilder.concat("name=", this.name,
                ",termCode=", this.termCode,
                ",year=", this.year,
                ",shortYear=", this.shortYear,
                ",longString=", this.longString,
                ",shortString=", this.shortString);
    }

    /**
     * Gets the string representation of the term.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.longString;
    }

    /**
     * Implementation of read/write for serialization.
     *
     * @param in the input stream
     * @throws NotSerializableException always
     */
    @Serial
    private void readObject(final java.io.ObjectInputStream in) throws NotSerializableException {

        throw new NotSerializableException("dev.mathops.db.type.TermKey");
    }

    /**
     * Implementation of read/write for serialization.
     *
     * @param out the output stream
     * @throws NotSerializableException always
     */
    @Serial
    private void writeObject(final java.io.ObjectOutputStream out) throws NotSerializableException {

        throw new NotSerializableException("dev.mathops.db.type.TermKey");
    }
}
