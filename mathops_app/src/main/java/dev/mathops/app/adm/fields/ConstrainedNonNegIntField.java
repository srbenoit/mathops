package dev.mathops.app.adm.fields;

import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.db.reclogic.query.EComparison;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.FlowLayout;
import java.io.Serial;

/**
 * A text field that requires entries be a valid integer between fixed bounds.
 */
public final class ConstrainedNonNegIntField extends AbstractLongField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4502543294169404014L;

    /** The field. */
    private final JTextField field;

    /** The underlying document. */
    private final IntegerDoc doc;

    /**
     * Constructs a new {@code ConstrainedNonNegIntField}.
     *
     * @param theName          the field name
     * @param allowZero        true if a zero value is allowed
     * @param max              the maximum value
     */
    public ConstrainedNonNegIntField(final String theName, final boolean allowZero, final long max) {

        super(theName);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setBackground(Skin.LIGHT);

        this.doc = new IntegerDoc(allowZero, max);

        this.field = new JTextField((int) Math.ceil(Math.log10((double) max)));
        this.field.setBackground(Skin.FIELD_BG);
        this.field.setDocument(this.doc);
        add(this.field);
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
     * Gets the parsed comparison, as indicated by "=", ">", "<", ">=", "<=", or "<>" at the start of the text
     *
     * @return the comparison; null if there is no comparison indicated
     */
    public EComparison getComparison() {

        return this.doc.comparison;
    }

    /**
     * Gets the value as an integer.
     *
     * @return the value, null if the field is empty
     */
    @Override
    public Integer getIntegerValue() {

        final Long longValue = this.doc.getParsed();

        return longValue == null ? null : Integer.valueOf(longValue.intValue());
    }

    /**
     * Gets the value as an integer.
     *
     * @return the value, null if the field is empty
     */
    @Override
    public Long getLongValue() {

        return this.doc.getParsed();
    }

    /**
     * Sets the field value.
     *
     * @param value the new value
     */
    @Override
    public void setValue(final Object value) {

        if (value instanceof Long) {
            this.field.setText(value.toString());
        } else {
            this.field.setText(CoreConstants.EMPTY);
        }
    }

    /**
     * A text field data model that allows only digits and constraints its value to lie within a range of non-negative
     * integers.
     */
    static final class IntegerDoc extends PlainDocument {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 5421550742717780706L;

        /** True to allow 0. */
        private final boolean zeroAllowed;

        /** The maximum value. */
        private final long max;

        /** The numeric comparison, if found while parsing. */
        private EComparison comparison;

        /** The parsed value; null if field does not contain valid integer. */
        private Long parsed;

        /**
         * Constructs a new {@code JIntFieldLimit}.
         *
         * @param isZeroAllowed true if zero is allowed
         * @param theMax        the maximum value
         */
        IntegerDoc(final boolean isZeroAllowed, final long theMax) {

            super();

            this.zeroAllowed = isZeroAllowed;
            this.max = theMax;
        }

        /**
         * Gets the parsed comparison, as indicated by "=", ">", "<", ">=", "<=", or "<>" at the start of the text
         *
         * @return the comparison; null if there is no comparison indicated
         */
        public EComparison getComparison() {

            return this.comparison;
        }

        /**
         * Gets the parsed integer value.
         *
         * @return the integer value; null if field does not contain a valid integer
         */
        Long getParsed() {

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
        public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {

            this.comparison = null;
            this.parsed = null;

            if (str != null) {
                if(!str.isEmpty()) {
                    if (str.startsWith("<>")) {
                        this.comparison = EComparison.UNEQUAL;
                    } else if (str.startsWith("<=")) {
                        this.comparison = EComparison.LESS_THAN_OR_EQUAL;
                    } else if (str.startsWith(">=")) {
                        this.comparison = EComparison.GREATER_THAN_OR_EQUAL;
                    } else if (str.charAt(0) == '<') {
                        this.comparison = EComparison.LESS_THAN;
                    } else if (str.charAt(0) == '>') {
                        this.comparison = EComparison.GREATER_THAN;
                    } else if (str.charAt(0) == '=') {
                        this.comparison = EComparison.EQUAL;
                    }
                }

                final int len = str.length();
                boolean hasNonDigit = false;
                for (int i = 0; i < len; ++i) {
                    final char ch = str.charAt(i);
                    if (ch < '0' || ch > '9') {
                        hasNonDigit = true;
                        break;
                    }
                }

                String filtered;

                if (hasNonDigit) {
                    final StringBuilder build = new StringBuilder(str.length() - 1);
                    for (int i = 0; i < len; ++i) {
                        final char ch = str.charAt(i);
                        if (ch >= '0' && ch <= '9') {
                            build.append(ch);
                        }
                    }
                    filtered = build.toString();
                } else {
                    filtered = str;
                }

                final int curLen = getLength();
                if (curLen == 0 && !this.zeroAllowed) {
                    if ("0".equals(filtered)) {
                        return;
                    }

                    while (!filtered.isEmpty() && filtered.charAt(0) == '0') {
                        filtered = filtered.substring(1);
                    }
                }

                int filteredLen = filtered.length();
                if (filteredLen > 0) {
                    final String existing = this.getText(0, curLen);

                    while (filteredLen > 0) {
                        final String test = existing + filtered;
                        try {
                            final long value = Long.parseLong(test);
                            if (value <= this.max) {
                                this.parsed = Long.valueOf(value);
                                super.insertString(offs, filtered, a);
                                break;
                            }
                        } catch (final NumberFormatException ex) {
                            // No action
                        }

                        --filteredLen;
                        filtered = filtered.substring(0, filteredLen);
                    }
                }
            }
        }
    }
}
