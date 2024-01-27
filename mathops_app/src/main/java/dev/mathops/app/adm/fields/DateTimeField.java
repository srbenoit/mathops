package dev.mathops.app.adm.fields;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.reclogic.query.EComparison;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.FlowLayout;
import java.io.Serial;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A text field that requires its value be a valid date/time.
 */
public final class DateTimeField extends AbstractField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2895715954327394655L;

    /** Placeholder for a blank parsed date. */
    private static final String BLANK_DATE_TIME = "                 ";

    /** Commonly used string. */
    private static final String BAD_DATE = "Invalid date: ";

    /** Commonly used string. */
    private static final String BAD_TIME = "Invalid time: ";

    /** The field. */
    private final JTextField field;

    /** The label. */
    private final JLabel label;

    /** The underlying document. */
    private final DateTimeDoc doc;

    /**
     * Constructs a new {@code DateTimeField}.
     *
     * @param theName the field name
     */
    public DateTimeField(final String theName) {

        super(theName);

        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setBackground(Skin.LIGHT);

        this.label = new JLabel(BLANK_DATE_TIME);
        this.field = new JTextField(14);
        this.doc = new DateTimeDoc(this.field, this.label);

        this.field.setBackground(Skin.FIELD_BG);
        this.field.setDocument(this.doc);
        add(this.field);

        add(new JLabel("  "));

        add(this.label);
    }

    /**
     * Sets the enabled status of the field.
     *
     * @param enabled true if enabled; false if not
     */
    @Override
    public void setEnabled(final boolean enabled) {

        this.field.setEnabled(enabled);
    }

    /**
     * Tests whether the field is enabled.
     *
     * @return true if enabled; false if not
     */
    @Override
    public boolean isEnabled() {

        return this.field.isEnabled();
    }

    /**
     * Sets the editable status of the field.
     *
     * @param editable true if editable; false if not
     */
    @Override
    public void setEditable(final boolean editable) {

        this.field.setEditable(editable);
    }

    /**
     * Tests whether the field has a non-empty value.
     *
     * @return true if the field has a value.
     */
    @Override
    public boolean hasValue() {

        final String txt = this.field.getText();

        return txt != null && !txt.isEmpty();
    }

    /**
     * Requests focus.
     */
    @Override
    public void requestFocus() {

        this.field.requestFocus();
    }

    /**
     * Gets the parsed comparison, as indicated by ">", "<", ">=", "<=", or "<>" at the start of the text
     *
     * @return the comparison; null if there is no comparison indicated
     */
    public EComparison getComparison() {

        return this.doc.parsedComparison;
    }

    /**
     * Gets the date portion of the value as a {@code LocalDate}.
     *
     * @return the value, null if the field is empty or invalid or is just a time
     */
    public LocalDate getDateValue() {

        return this.doc.getParsedDate();
    }

    /**
     * Gets the time portion of the value as a {@code LocalTime}.
     *
     * @return the value, null if the field is empty or invalid or is just a date
     */
    public LocalTime getTimeValue() {

        return this.doc.getParsedTime();
    }

    /**
     * Sets the field value.
     *
     * @param value the new value
     */
    @Override
    public void setValue(final Object value) {

        // if (value == null) {
        // Log.info("Set date to null");
        // } else {
        // Log.info("Set date to " + value.toString() + " ("
        // + value.getClass().getName() + ")");
        // }

        if (value instanceof final LocalDateTime dateTimeValue) {
            this.field.setText(TemporalUtils.FMT_MDY_HMS.format(dateTimeValue));
        } else {
            this.field.setText(CoreConstants.EMPTY);
            this.label.setText(BLANK_DATE_TIME);
        }
    }

    /**
     * A text field data model that limits length.
     */
    static final class DateTimeDoc extends PlainDocument {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 5319084825116166968L;

        /** The text field. */
        private final JTextField field;

        /** The label that shows the parsed date when valid. */
        private final JLabel label;

        /** The date/time comparison, if found while parsing. */
        private EComparison parsedComparison;

        /** The parsed date value, if parsing succeeded; null if not. */
        private LocalDate parsedDate;

        /** The parsed time value, if parsing succeeded; null if not. */
        private LocalTime parsedTime;

        /**
         * Constructs a new {@code DateTimeDoc}.
         *
         * @param theField the text field
         * @param theLabel the label that shows the parsed date when valid
         */
        /* default */ DateTimeDoc(final JTextField theField, final JLabel theLabel) {

            super();

            this.field = theField;
            this.label = theLabel;
        }

        /**
         * Gets the parsed comparison.
         *
         * @return the comparison; null if none found
         */
        public EComparison getParsedComparison() {

            return this.parsedComparison;
        }

        /**
         * Gets the parsed date value.
         *
         * @return the date value; null if field does not contain a valid date
         */
        LocalDate getParsedDate() {

            return this.parsedDate;
        }

        /**
         * Gets the parsed time value.
         *
         * @return the time value; null if field does not contain a valid time
         */
        LocalTime getParsedTime() {

            return this.parsedTime;
        }

        /**
         * Inserts a string.
         *
         * @param offs the position
         * @param str    the string to insert
         * @param a   the attribute set
         */
        @Override
        public void insertString(final int offs, final String str,
                                 final AttributeSet a) throws BadLocationException {

            super.insertString(offs, str, a);

            this.parsedComparison = null;
            this.parsedDate = null;
            this.parsedTime = null;

            final int len = getLength();
            if (len > 0) {
                final String current = getText(0, len);

                // If the string starts with a comparison indicator ("=", "<", "<=", ">", ">=",
                // "<>"), what
                // follows could be a full date/time, or just a date or just a time.

                final String trimmed;
                EComparison comparison = null;
                if (!current.isEmpty() && current.charAt(0) == '=') {
                    comparison = EComparison.EQUAL;
                    trimmed = current.substring(1).trim();
                } else if (current.startsWith("<=")) {
                    comparison = EComparison.LESS_THAN_OR_EQUAL;
                    trimmed = current.substring(2).trim();
                } else if (!current.isEmpty() && current.charAt(0) == '<') {
                    comparison = EComparison.LESS_THAN;
                    trimmed = current.substring(1).trim();
                } else if (current.startsWith(">=")) {
                    comparison = EComparison.GREATER_THAN_OR_EQUAL;
                    trimmed = current.substring(2).trim();
                } else if (!current.isEmpty() && current.charAt(0) == '>') {
                    comparison = EComparison.GREATER_THAN;
                    trimmed = current.substring(1).trim();
                } else if (current.startsWith("<>")
                        || current.startsWith("!=")) {
                    comparison = EComparison.UNEQUAL;
                    trimmed = current.substring(2).trim();
                } else {
                    trimmed = current.trim();
                }

                // Attempt to parse the new value - "1/2/[20]23 10:11:12" or "2023/12/31 23:59:59"

                final int space = trimmed.indexOf(' ');
                if (space == -1) {
                    // No space - must be either a date or a time
                    if (trimmed.indexOf(':') == -1) {
                        final LocalDate theDate = parseDate(trimmed);
                        if (theDate == null) {
                            this.label.setText(CoreConstants.SPC);
                        } else {
                            this.parsedDate = theDate;
                            this.parsedComparison = comparison;
                            updateLabel();
                        }
                    } else {
                        final LocalTime theTime = parseTime(trimmed);
                        if (theTime == null) {
                            this.label.setText(CoreConstants.SPC);
                        } else {
                            this.parsedTime = theTime;
                            this.parsedComparison = comparison;
                            updateLabel();
                        }
                    }
                } else {

                    final LocalDate theDate = parseDate(trimmed.substring(0, space));
                    if (theDate == null) {
                        this.label.setText(CoreConstants.SPC);
                    } else {
                        final LocalTime theTime = parseTime(trimmed.substring(space + 1));
                        if (theTime == null) {
                            this.label.setText(CoreConstants.SPC);
                        } else {
                            this.parsedDate = theDate;
                            this.parsedTime = theTime;
                            this.parsedComparison = comparison;
                            updateLabel();
                        }
                    }
                }
            }

            final String txt = this.field.getText();
            if (this.parsedDate == null && this.parsedTime == null && txt != null && !txt.isEmpty()) {
                this.field.setBackground(Skin.FIELD_ERROR_BG);
            } else {
                this.field.setBackground(Skin.FIELD_BG);
            }
        }

        /**
         * Attempts to parse a date.
         *
         * @param toParse the string to parse
         * @return the parsed date if successful; null if not
         */
        static LocalDate parseDate(final String toParse) {

            LocalDate result = null;

            final int firstSlash = toParse.indexOf('/');

            try {
                if (firstSlash == -1) {
                    final int firstDash = toParse.indexOf('-');
                    if (firstDash == -1) {
                        final int firstSpace = toParse.indexOf(' ');

                        if (firstSpace == -1) {
                            Log.warning(BAD_DATE + toParse);
                        } else {
                            final int comma = toParse.indexOf(',');

                            if (comma == -1) {
                                final int lastSpace = toParse.lastIndexOf(' ');

                                if (lastSpace == firstSpace) {
                                    Log.warning(BAD_DATE + toParse);
                                } else {
                                    // Format: Dec 31 1999 or Dec. 31 1999 or December 31 1999

                                    final int mm = parseMonthString(toParse.substring(0, firstSpace));
                                    final int dd = Integer.parseInt(toParse.substring(firstSpace + 1, lastSpace).trim());
                                    final int yy = Integer.parseInt(toParse.substring(lastSpace + 1));

                                    final int year = yy > 1000 ? yy : yy >= 70 ? 1900 + yy : 2000 + yy;

                                    result = LocalDate.of(year, mm, dd);
                                }
                            } else if (comma > firstSpace) {
                                // Format: Dec 31, 1999 or Dec. 31, 1999 or December 31, 1999

                                final int mm = parseMonthString(toParse.substring(0, firstSpace));
                                final int dd = Integer.parseInt(toParse.substring(firstSpace + 1, comma).trim());
                                final int yy = Integer.parseInt(toParse.substring(comma + 1).trim());

                                final int year = yy > 1000 ? yy : yy >= 70 ? 1900 + yy : 2000 + yy;

                                result = LocalDate.of(year, mm, dd);
                            } else {
                                Log.warning(BAD_DATE + toParse);
                            }
                        }
                    } else {
                        final int secondDash = toParse.indexOf('-', firstDash + 1);
                        if (secondDash == -1) {
                            Log.warning(BAD_DATE + toParse);
                        } else {
                            // Format: 1999-12-31 or 1999-1-2
                            final int year = Integer.parseInt(toParse.substring(0, firstDash));
                            final int mm = Integer.parseInt(toParse.substring(firstDash + 1, secondDash));
                            final int dd = Integer.parseInt(toParse.substring(secondDash + 1));

                            result = LocalDate.of(year, mm, dd);
                        }
                    }
                } else {
                    final int secondSlash = toParse.indexOf('/', firstSlash + 1);

                    if (secondSlash == -1) {
                        Log.warning(BAD_DATE + toParse);
                    } else {
                        // Formats: 12/31/1999 or 12/31/99
                        final int mm = Integer.parseInt(toParse.substring(0, firstSlash));
                        final int dd = Integer.parseInt(toParse.substring(firstSlash + 1, secondSlash));
                        final int yy = Integer.parseInt(toParse.substring(secondSlash + 1));
                        final int year = yy > 1000 ? yy : yy >= 70 ? 1900 + yy : 2000 + yy;

                        result = LocalDate.of(year, mm, dd);
                    }
                }
            } catch (final NumberFormatException | DateTimeException ex) {
                Log.warning(BAD_DATE + toParse, ex);
            }

            return result;
        }

        /**
         * Attempts to parse a time. "00:00" or "00:00:00" format, possibly ending in "AM" or "PM".
         *
         * @param toParse the string to parse
         * @return the parsed time if successful; null if not
         */
        static LocalTime parseTime(final String toParse) {

            final String lowercase = toParse.toLowerCase(Locale.US);

            LocalTime result = null;

            // Check for AM/PM indicator, parse it, and remove it if present
            final boolean isPm = lowercase.endsWith("pm");
            final boolean isAm = lowercase.endsWith("am");

            final String trimmed;
            if (isPm || isAm) {
                trimmed = lowercase.substring(0, lowercase.length() - 2).trim();
            } else {
                trimmed = lowercase;
            }

            final int colon1 = trimmed.indexOf(':');

            if (colon1 == -1) {
                Log.warning(BAD_TIME + toParse);
            } else {
                final int colon2 = trimmed.indexOf(':', colon1 + 1);

                try {
                    final int hr = Integer.parseInt(trimmed.substring(0, colon1).trim());
                    if (colon2 == -1) {
                        // 00:00 format
                        final int mm = Integer.parseInt(trimmed.substring(colon1 + 1));
                        result = LocalTime.of(isPm ? hr + 12 : hr, mm);
                    } else {
                        // 00:00:00 format
                        final int mm = Integer.parseInt(trimmed.substring(colon1 + 1, colon2));
                        final int ss = Integer.parseInt(trimmed.substring(colon2 + 1));
                        result = LocalTime.of(isPm ? hr + 12 : hr, mm, ss);
                    }
                } catch (final NumberFormatException | DateTimeException ex) {
                    Log.warning(BAD_TIME + toParse, ex);
                }
            }

            return result;
        }

        /**
         * Updates the label to reflect a successfully parsed date.
         */
        private void updateLabel() {

            final StringBuilder build = new StringBuilder(30);

            final Locale loc = Locale.getDefault();

            if (this.parsedDate != null) {
                build.append(this.parsedDate.getMonth().getDisplayName(TextStyle.SHORT, loc))
                        .append(CoreConstants.SPC).append(this.parsedDate.getDayOfMonth())
                        .append(CoreConstants.COMMA).append(CoreConstants.SPC)
                        .append(this.parsedDate.getYear());
                if (this.parsedTime != null) {
                    build.append(CoreConstants.SPC);
                }
            }

            if (this.parsedTime != null) {
                build.append(this.parsedTime.getHour()).append(CoreConstants.COLON);
                final int min = this.parsedTime.getMinute();
                if (min < 10) {
                    build.append('0');
                }
                build.append(min).append(CoreConstants.COLON);
                final int sec = this.parsedTime.getSecond();
                if (sec < 10) {
                    build.append('0');
                }
                build.append(sec);
            }

            this.label.setText(build.toString());
        }

        /**
         * Parses a month string.
         *
         * @param str the string
         * @return the parsed month (1 to 12) or -1 if unable to parse
         */
        private static int parseMonthString(final String str) {

            final String lower = str.toLowerCase(Locale.US);

            return switch (lower) {
                case "jan", "jan.", "january" -> 1;
                case "feb", "feb.", "february" -> 2;
                case "mar", "mar.", "march" -> 3;
                case "apr", "apr.", "april" -> 4;
                case "may", "may." -> 5;
                case "jun", "jun.", "june" -> 6;
                case "jul", "jul.", "july" -> 7;
                case "aug", "aug.", "august" -> 8;
                case "sep", "sep.", "sept", "sept.", "september" -> 9;
                case "oct", "oct.", "october" -> 10;
                case "nov", "nov.", "november" -> 11;
                case "dec", "dec.", "december" -> 12;
                default -> -1;
            };
        }
    }
}
