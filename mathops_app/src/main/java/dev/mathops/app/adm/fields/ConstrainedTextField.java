package dev.mathops.app.adm.fields;

import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.FlowLayout;
import java.io.Serial;

/**
 * A text field where the length is constrained.
 */
public final class ConstrainedTextField extends AbstractStringField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2775761624767952119L;

    /** The field. */
    private final JTextField field;

    /**
     * Constructs a new {@code ConstrainedTextField}.
     *
     * @param theName          the field name
     * @param maxLen           the maximum length
     * @param theCharset       the set of allowed characters
     */
    public ConstrainedTextField(final String theName, final int maxLen, final String theCharset) {

        super(theName);
        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        setBackground(Skin.LIGHT);

        this.field = new JTextField(maxLen);
        this.field.setBackground(Skin.FIELD_BG);
        this.field.setDocument(new StringDoc(maxLen, theCharset));
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
     * Gets the value as a String.
     *
     * @return the value, null if the field is empty
     */
    @Override
    public String getStringValue() {

        String result = null;

        final String txt = this.field.getText();
        if (txt != null && !txt.isEmpty()) {
            result = txt;
        }

        return result;
    }

    /**
     * Sets the field value.
     *
     * @param value the new value
     */
    @Override
    public void setValue(final Object value) {

        if (value instanceof String) {
            this.field.setText((String) value);
        } else {
            this.field.setText(CoreConstants.EMPTY);
        }
    }

    /**
     * A text field data model that limits length.
     */
    static final class StringDoc extends PlainDocument {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 6485904415668813834L;

        /** The maximum length. */
        private final int limit;

        /** The set of allowed characters (null if no constraints). */
        private final String charset;

        /**
         * Constructs a new {@code JTextFieldLimit}.
         *
         * @param theLimit   the maximum length
         * @param theCharset the set of allowed characters
         */
        /* default */ StringDoc(final int theLimit, final String theCharset) {

            super();
            this.limit = theLimit;
            this.charset = theCharset;
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

            if (str != null){
                final int curLen = getLength();

                // Filter non-allowed characters from the string being added
                final String filtered;
                if (this.charset == null) {
                    filtered = str;
                } else {
                    final StringBuilder build = new StringBuilder(str.length());
                    final int len = str.length();
                    for (int i = 0; i < len; ++i) {
                        final char ch = str.charAt(i);
                        if (this.charset.indexOf(ch) >= 0) {
                            build.append(ch);
                        }
                    }
                    filtered = build.toString();
                }

                if ((curLen + filtered.length()) <= this.limit) {
                    super.insertString(offs, filtered, a);
                } else {
                    final int allow = this.limit - curLen;
                    if (allow > 0) {
                        super.insertString(offs, filtered.substring(0, allow), a);
                    }
                }
            }
        }
    }
}
