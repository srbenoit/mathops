package dev.mathops.app.adm.forms;

import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.fields.AbstractField;
import dev.mathops.app.adm.fields.ConstrainedNonNegIntField;
import dev.mathops.app.adm.fields.ConstrainedTextField;
import dev.mathops.app.adm.fields.DateField;
import dev.mathops.app.adm.fields.DateTimeField;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serial;

/**
 * The base class for all form panels.
 */
abstract class AbstractForm extends JPanel implements ActionListener {

    /** Valid characters in a text field constrained to 'Y' or 'N'. */
    static final String YN = "YN";

    /** Valid characters in a text field constrained to digits. */
    static final String DIGITS = "0123456789";

    /** Valid characters in a text field constrained to uppercase letters. */
    static final String UC_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Valid characters in a text field constrained to letters or simple punctuation marks. */
    static final String LETTERS_PUNC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz .'-";

    /** Valid characters in a text field constrained to letters or digits. */
    static final String LETTERS_DIGITS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /** Valid characters in a text field constrained to letters, digits, or punctuation marks. */
    static final String LETTERS_DIGITS_PUNC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 .'-_~[]{}()<>/:;";

    /** A commonly used string. */
    private static final String AND = " AND";

    /** An action command. */
    static final String QUERY_CMD = "QUERY";

    /** An action command. */
    static final String ADD_CMD = "ADD";

    /** An action command. */
    static final String EXECUTE_CMD = "EXECUTE";

    /** An action command. */
    static final String INSERT_CMD = "INSERT";

    /** An action command. */
    static final String PREV_CMD = "PREV";

    /** An action command. */
    static final String NEXT_CMD = "NEXT";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2713239521885208627L;

    /** The query execute button. */
    private final JButton execute;

    /** The insert button. */
    private final JButton insert;

    /** Cursor navigation button. */
    private final JButton prev;

    /** Cursor navigation button. */
    private final JButton next;

    /** Status text. */
    private final JLabel status;

    /**
     * Constructs a new {@code AbstractForm}.
     */
    AbstractForm() {

        super(new StackedBorderLayout(5, 5));

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        buttons.setBackground(Color.WHITE);
        add(buttons, StackedBorderLayout.NORTH);

        final JButton query = new JButton("Query");
        query.setMnemonic(KeyEvent.VK_Q);
        query.setActionCommand(QUERY_CMD);
        query.addActionListener(this);
        buttons.add(query);

        final JButton add = new JButton("Add");
        add.setMnemonic(KeyEvent.VK_A);
        add.setActionCommand(ADD_CMD);
        add.addActionListener(this);
        buttons.add(add);

        final JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);

        final JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        actions.setBackground(Color.WHITE);

        this.execute = new JButton("Execute Query");
        this.execute.setMnemonic(KeyEvent.VK_X);
        this.execute.setActionCommand(EXECUTE_CMD);
        this.execute.addActionListener(this);
        this.execute.setEnabled(false);
        actions.add(this.execute);

        this.insert = new JButton("Insert");
        this.insert.setMnemonic(KeyEvent.VK_I);
        this.insert.setActionCommand(INSERT_CMD);
        this.insert.addActionListener(this);
        this.insert.setEnabled(false);
        actions.add(this.insert);

        this.prev = new JButton("Previous");
        this.prev.setActionCommand(PREV_CMD);
        this.prev.setMnemonic(KeyEvent.VK_P);
        this.prev.addActionListener(this);
        this.prev.setEnabled(false);
        actions.add(this.prev);

        this.next = new JButton("Next");
        this.next.setActionCommand(NEXT_CMD);
        this.next.setMnemonic(KeyEvent.VK_N);
        this.next.addActionListener(this);
        this.next.setEnabled(false);
        actions.add(this.next);

        bottom.add(actions, BorderLayout.LINE_START);

        this.status = new JLabel(CoreConstants.SPC);
        this.status.setFont(Skin.MEDIUM_15_FONT);
        this.status.setHorizontalAlignment(SwingConstants.RIGHT);
        bottom.add(this.status, BorderLayout.CENTER);

        add(bottom, StackedBorderLayout.SOUTH);
    }

    /**
     * Called when the form is activated. This may re-query the underlying table to (for example) refresh the list of
     * terms represented if data is segregated by term, or to populate drop-downs with results from a "select distinct"
     */
    public abstract void activate();

    /**
     * Enables the query execute button, and disables action buttons that are not appropriate when querying.
     */
    final void enableQuery() {

        this.execute.setEnabled(true);
        this.insert.setEnabled(false);
        this.prev.setEnabled(false);
        this.next.setEnabled(false);
    }

    /**
     * Enables the insert button, and disables action buttons that are not appropriate when inserting.
     */
    final void enableInsert() {

        this.execute.setEnabled(false);
        this.insert.setEnabled(true);
        this.prev.setEnabled(false);
        this.next.setEnabled(false);
    }

    /**
     * Disables both query execution and insert (used when viewing query results).
     */
    final void disableQueryInsert() {

        this.execute.setEnabled(false);
        this.insert.setEnabled(false);
    }

    /**
     * Enables the "previous" button.
     *
     * @param enabled true to enable
     */
    final void setPrevEnabled(final boolean enabled) {

        this.prev.setEnabled(enabled);
    }

    /**
     * Enables the "next" button.
     *
     * @param enabled true to enable
     */
    final void setNextEnabled(final boolean enabled) {

        this.next.setEnabled(enabled);
    }

    /**
     * Sets the status text.
     *
     * @param txt the status text
     */
    final void setStatus(final String txt) {

        this.status.setText(txt);
    }

    /**
     * Creates a collection of labels, all the same width, for a collection of field names.
     *
     * @param fieldNames       the field names
     * @return the labels
     */
    static JLabel[] makeFieldLabels(final String... fieldNames) {

        final JLabel[] result = new JLabel[fieldNames.length];

        int maxW = 0;
        int maxH = 0;
        final int len = result.length;
        for (int i = 0; i < len; ++i) {
            result[i] = new JLabel(fieldNames[i] + ": ");
            result[i].setHorizontalAlignment(SwingConstants.RIGHT);
            result[i].setFont(Skin.BODY_12_FONT);
            final Dimension pref = result[i].getPreferredSize();
            maxW = Math.max(maxW, pref.width);
            maxH = Math.max(maxH, pref.height);
        }
        final Dimension newPref = new Dimension(maxW, maxH);
        for (final JLabel antialiasedJLabel : result) {
            antialiasedJLabel.setPreferredSize(newPref);
        }

        return result;
    }

    /**
     * Creates a panel with flow layout that holds a label and a component.
     *
     * @param label     the label
     * @param component the component
     * @return the flow panel
     */
    static JPanel makeFlow(final JLabel label, final JComponent component) {

        final JPanel result = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 1));

        result.setBackground(Skin.OFF_WHITE_GREEN);
        result.add(label);
        result.add(component);

        return result;
    }

    /**
     * Creates a text field with a specified maximum length.
     *
     * @param theName          the field name
     * @param maxLength        the length
     * @param charset          the set of allowed characters; null if no restrictions
     * @return the field
     */
    static ConstrainedTextField makeTextField(final String theName, final int maxLength, final String charset) {

        return new ConstrainedTextField(theName, maxLength, charset);
    }

    /**
     * Creates an integer field that only allows entries within fixed limits.
     *
     * @param theName          the field name
     * @param zeroAllowed      true to allow a zero value
     * @param max              the maximum
     * @return the field
     */
    static ConstrainedNonNegIntField makeIntField(final String theName, final boolean zeroAllowed, final long max) {

        return new ConstrainedNonNegIntField(theName, zeroAllowed, max);
    }

    /**
     * Creates a text field that accepts dates.
     *
     * @param theName          the field name
     * @return the field
     */
    static DateField makeDateField(final String theName) {

        final DateField field = new DateField(theName);
        field.setBackground(Skin.OFF_WHITE_GREEN);

        return field;
    }

    /**
     * Creates a text field that accepts date/times.
     *
     * @param theName          the field name
     * @return the field
     */
    static DateTimeField makeDateTimeField(final String theName) {

        final DateTimeField field = new DateTimeField(theName);
        field.setBackground(Skin.OFF_WHITE_GREEN);

        return field;
    }

    /**
     * Appends a where clause, with optional preceding "AND".
     *
     * @param field the field
     * @param and   true to include "AND" before the clause
     * @param s     the {@code StringBuilder} to which to append
     * @return the new "and" value (true if the field had a value and a clause was added)
     */
    static boolean appendWhere(final AbstractField field, final boolean and, final StringBuilder s) {

        boolean newAnd = and;

        if (field.hasValue()) {
            if (and) {
                s.append(AND);
            }
            s.append(' ').append(field.getName()).append("=?");
            newAnd = true;
        }

        return newAnd;
    }
}
