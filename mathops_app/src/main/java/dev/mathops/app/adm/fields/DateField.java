package dev.mathops.app.adm.fields;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.FlowLayout;
import java.io.Serial;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * A text field that requires its value be a valid date.
 */
public final class DateField extends AbstractField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2895715954327394655L;

    /** Placeholder for a blank parsed date. */
    private static final String BLANK_DATE = "                 ";

    /** Commonly used string. */
    private static final String BAD_DATE = "Invalid date: ";

    /** The field. */
    private final JTextField field;

    /** The label. */
    private final JLabel label;

    /** The underlying document. */
    private final DateDoc doc;

    /**
     * Constructs a new {@code DateField}.
     *
     * @param theName          the field name
     */
    public DateField(final String theName) {

        super(theName);

        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setBackground(Skin.LIGHT);

        this.label = new JLabel(BLANK_DATE);
        this.field = new JTextField(8);
        this.doc = new DateDoc(this.field, this.label);

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
     * Gets the value as a {@code LocalDate}.
     *
     * @return the value, null if the field is empty or invalid
     */
    public LocalDate getDateTimeValue() {

        return this.doc.getParsed();
    }

    /**
     * Sets the field value.
     *
     * @param value the new value
     */
    public void setValue(final Object value) {

        // if (value == null) {
        // Log.info("Set date to null");
        // } else {
        // Log.info("Set date to " + value.toString() + " ("
        // + value.getClass().getName() + ")");
        // }

        if (value instanceof final LocalDate dateValue) {
            this.field.setText(TemporalUtils.FMT_MDY.format(dateValue));
        } else {
            this.field.setText(CoreConstants.EMPTY);
            this.label.setText(BLANK_DATE);
        }
    }

    /**
     * A text field data model that limits length.
     */
    static final class DateDoc extends PlainDocument {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 5319084825116166968L;

        /** The text field. */
        private final JTextField field;

        /** The label that shows the parsed date when valid. */
        private final JLabel label;

        /** The parsed date value, if parsing succeeded; null if not. */
        private LocalDate parsed;

        /**
         * Constructs a new {@code DateDoc}.
         *
         * @param theField the text field
         * @param theLabel the label that shows the parsed date when valid
         */
        DateDoc(final JTextField theField, final JLabel theLabel) {

            super();

            this.field = theField;
            this.label = theLabel;
        }

        /**
         * Gets the parsed date value.
         *
         * @return the date value; null if field does not contain a valid date
         */
        LocalDate getParsed() {

            return this.parsed;
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

            this.parsed = null;

            final int len = getLength();
            final String current = getText(0, len);

            // Attempt to parse the new value

            try {
                if (len == 6) {
                    // Informix Format: 123199 (representing Dec. 21, 1999)
                    final int value = Integer.parseInt(current);
                    final int mm = value / 10000;
                    final int dd = (value / 100) % 100;
                    final int yy = value % 100;
                    final int year = yy >= 70 ? 1900 + yy : 2000 + yy;

                    this.parsed = LocalDate.of(year, mm, dd);
                    updateLabel();
                } else {
                    final int firstSlash = current.indexOf('/');

                    if (firstSlash == -1) {
                        final int firstDash = current.indexOf('-');
                        if (firstDash == -1) {
                            final int firstSpace = current.indexOf(' ');

                            if (firstSpace == -1) {
                                Log.warning(BAD_DATE + current);
                            } else {
                                final int comma = current.indexOf(',');

                                if (comma == -1) {
                                    final int lastSpace = current.lastIndexOf(' ');

                                    if (lastSpace == firstSpace) {
                                        Log.warning(BAD_DATE + current);
                                    } else {
                                        // Format: "Dec 31 1999" or "Dec. 31 1999" or "December 31 1999"

                                        final int mm = parseMonthString(current.substring(0, firstSpace));
                                        final int dd = Integer.parseInt(
                                                current.substring(firstSpace + 1, lastSpace).trim());
                                        final int yy = Integer.parseInt(current.substring(lastSpace + 1));

                                        final int year = yy > 1000 ? yy : yy >= 70 ? 1900 + yy : 2000 + yy;

                                        this.parsed = LocalDate.of(year, mm, dd);
                                        updateLabel();
                                    }
                                } else if (comma > firstSpace) {
                                    // Format: "Dec 31, 1999" or "Dec. 31, 1999" or "December 31, 1999"

                                    final int mm = parseMonthString(current.substring(0, firstSpace));
                                    final int dd = Integer.parseInt(current.substring(firstSpace + 1, comma).trim());
                                    final int yy = Integer.parseInt(current.substring(comma + 1).trim());

                                    final int year = yy > 1000 ? yy : yy >= 70 ? 1900 + yy : 2000 + yy;

                                    this.parsed = LocalDate.of(year, mm, dd);
                                    updateLabel();
                                } else {
                                    Log.warning(BAD_DATE + current);
                                }
                            }
                        } else {
                            final int secondDash = current.indexOf('-', firstDash + 1);
                            if (secondDash == -1) {
                                Log.warning(BAD_DATE + current);
                            } else {
                                // Format: "1999-12-31" or "1999-1-2"
                                final int yy = Integer.parseInt(current.substring(0, firstDash));
                                final int mm = Integer.parseInt(current.substring(firstDash + 1, secondDash));
                                final int dd = Integer.parseInt(current.substring(secondDash + 1));

                                this.parsed = LocalDate.of(yy, mm, dd);
                                updateLabel();
                            }
                        }
                    } else {
                        final int secondSlash = current.indexOf('/', firstSlash + 1);

                        if (secondSlash == -1) {
                            Log.warning(BAD_DATE + current);
                        } else {
                            // Formats: 12/31/1999 or 12/31/99
                            final int mm = Integer.parseInt(current.substring(0, firstSlash));
                            final int dd =
                                    Integer.parseInt(current.substring(firstSlash + 1, secondSlash));
                            final int yy = Integer.parseInt(current.substring(secondSlash + 1));
                            final int year = yy > 1000 ? yy : yy >= 70 ? 1900 + yy : 2000 + yy;

                            this.parsed = LocalDate.of(year, mm, dd);
                            updateLabel();
                        }
                    }
                }
            } catch (final NumberFormatException | DateTimeException ex) {
                Log.warning(BAD_DATE + current, ex);
                this.label.setText(CoreConstants.SPC);
            }

            final String txt = this.field.getText();
            if (this.parsed == null && txt != null && !txt.isEmpty()) {
                this.field.setBackground(Skin.FIELD_ERROR_BG);
            } else {
                this.field.setBackground(Skin.FIELD_BG);
            }
        }

        /**
         * Updates the label to reflect a successfully parsed date.
         */
        private void updateLabel() {

            final StringBuilder build = new StringBuilder(30);
            final Locale loc = Locale.getDefault();
            build.append(this.parsed.getMonth().getDisplayName(TextStyle.SHORT, loc))
                    .append(CoreConstants.SPC).append(this.parsed.getDayOfMonth())
                    .append(CoreConstants.COMMA).append(CoreConstants.SPC)
                    .append(this.parsed.getYear());
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
