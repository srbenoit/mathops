package dev.mathops.app.adm.forms;

import dev.mathops.app.adm.AdminMainWindow;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The "Tables" pane.
 */
public class FormsTabPane extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The number of columns to display. */
    private static final int NUM_COLUMNS = 6;

    /** The names of all the tables, in alphabetical order. */
    private static final String[] TABLE_NAMES = {"assignment",
            "admin_hold", "applicant", "calcs",
            "campus_calendar", "challenge_fee", "client_pc",
            "course", "crsection", "csection",
            "cunit", "cuobjective", "cusection",
            "ddcode", "dddomain", "ddelement",
            "ddelement_report", "ddelement_screen",
            "ddreport", "ddscreen", "ddtable",
            "ddtable_element", "delphi", "delphi_check",
            "discipline", "dont_submit", "dup_registr",
            "etext", "etext_course", "etext_key",
            "exam", "examqa", "except_stu",
            "fcr", "fcr_student", "fcrstu",
            "ffr_trns", "final_croll", "grade_roll",
            "grading_std", "high_schools", "hold_type",
            "index_descriptions", "index_frequency",
            "logins", "mdstudent",
            "milestone", "mpe", "mpe_credit",
            "mpe_log", "mpecr_denied", "msg",
            "msg_lookup", "newstu", "next_campus_calendar",
            "next_csection", "next_milestone",
            "next_pace_track_rule", "next_remote_mpe",
            "next_semester_calendar", "pace_appeals",
            "pace_track_rule", "pacing_rules",
            "pacing_structure", "parameters", "pending_exam",
            "plc_fee", "prereq", "prev_appeals",
            "prev_extensions", "prev_stlmiss", "prev_stlock",
            "prev_stmilestone", "prev_stterm", "remote_mpe",
            "resource", "semester_calendar", "special_stus",
            "stc", "stchallenge", "stchallengeqa",
            "stcourse", "stcuobjective", "stetext",
            "stexam", "sthomework", "sthwqa",
            "stmathplan", "stmdscores", "stmilestone",
            "stmpe", "stmpeqa", "stmsg",
            "stpace_summary", "stqa", "stresource",
            "stsurveyqa", "stterm", "student",
            "stuid_tables", "stvisit", "surveyqa",
            "sysmenuitems", "sysmenus", "term",
            "testing_centers", "user_clearance", "users",
            "which_db", "zip_code"};

    /** The card ID of the buttons card. */
    private static final String BUTTONS_CARD = "BUTTONS";

    /** The data cache. */
    private final Cache cache;

    /** The card layout. */
    private final CardLayout cards;

    /** The table forms. */
    private final Map<String, AbstractForm> forms;

    /**
     * Constructs a new {@code FormsTabPane}.
     *
     * @param theCache         the data cache
     */
    public FormsTabPane(final Cache theCache, final Object theRenderingHint) {

        super(null);

        setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        setBackground(Skin.LIGHT);
        setPreferredSize(AdminMainWindow.PREF_SIZE);

        this.cards = new CardLayout();
        this.forms = new HashMap<>(TABLE_NAMES.length);
        setLayout(this.cards);

        this.cache = theCache;

        add(makeButtonGrid(), BUTTONS_CARD);

        for (final String table : TABLE_NAMES) {
            final JPanel card = makeTablePanel(table, theRenderingHint);
            add(card, table);
        }

        this.cards.show(this, BUTTONS_CARD);
    }

    /**
     * Creates the panel that presents the grid of table names as buttons.
     *
     * @return the button grid panel
     */
    private JPanel makeButtonGrid() {

        final JPanel result = new JPanel(new GridLayout(1, NUM_COLUMNS, 3, 3));
        result.setBackground(Color.WHITE);

        // Create all UI elements (buttons and labels)
        final List<JComponent> components = new ArrayList<>(TABLE_NAMES.length + 20);
        char cur = '-';
        int maxH = 0;
        int maxW = 0;
        for (final String tableName : TABLE_NAMES) {
            final char first = tableName.charAt(0);
            if (cur != first) {
                final JPanel wrap = new JPanel(new BorderLayout());
                wrap.setBackground(Color.WHITE);

                final JLabel lbl = new JLabel("\u2014 " + Character.toUpperCase(first) + " \u2014");

                lbl.setFont(Skin.BOLD_12_FONT);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                wrap.add(lbl, BorderLayout.SOUTH);
                components.add(wrap);
                cur = first;
            }
            final JButton button = new JButton(tableName);
            button.setFont(Skin.BUTTON_13_FONT);

            button.setActionCommand(tableName);
            button.addActionListener(this);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            components.add(button);

            final Dimension sz = button.getPreferredSize();
            maxH = Math.max(sz.height, maxH);
            maxW = Math.max(sz.width, maxW);
        }

        final Dimension newSz = new Dimension(maxW, maxH);
        for (final JComponent comp : components) {
            comp.setPreferredSize(newSz);
        }

        // See how many should go in a column, trying to to leave a trailing letter heading at
        // the bottom of a column
        int perColumn = (components.size() + NUM_COLUMNS - 1) / NUM_COLUMNS;
        int index = perColumn - 1;
        while (index < components.size()) {
            if (components.get(index) instanceof JLabel) {
                ++perColumn;
                break;
            }
            index += perColumn;
        }

        // Arrange into columns
        index = 0;
        int end = perColumn;
        for (int i = 0; i < NUM_COLUMNS; ++i) {
            final JPanel colPane = new JPanel(new BorderLayout());
            colPane.setBackground(Color.WHITE);
            result.add(colPane);
            JPanel curPane = colPane;

            while (index < end - 1) {
                final JComponent c = components.get(index);
                curPane.add(c, BorderLayout.NORTH);
                final JPanel newPane = new JPanel(new BorderLayout());
                newPane.setBackground(Color.WHITE);
                curPane.add(newPane, BorderLayout.CENTER);
                curPane = newPane;
                ++index;
            }

            if (index < end && !(components.get(index) instanceof JLabel)) {
                curPane.add(components.get(index), BorderLayout.NORTH);
                final JPanel newPane = new JPanel(new BorderLayout());
                newPane.setBackground(Color.WHITE);
                curPane.add(newPane, BorderLayout.CENTER);
                ++index;
            }

            end = Math.min(index + perColumn, components.size());
        }

        return result;
    }

    /**
     * Creates a pane for a table.
     *
     * @param tableName        the table name
     * @return the pane
     */
    private JPanel makeTablePanel(final String tableName, final Object theRenderingHint) {

        final JPanel result = new JPanel(new BorderLayout());
        result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final JPanel top = new JPanel(new BorderLayout());

        final JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        final JLabel prefix = new JLabel("Table: ");
        prefix.setForeground(Skin.LABEL_COLOR);
        prefix.setFont(Skin.BIG_HEADER_18_FONT);
        header.add(prefix);

        final JLabel nameLabel = new JLabel(tableName);
        nameLabel.setFont(Skin.BIG_HEADER_18_FONT);
        header.add(nameLabel);
        top.add(header, BorderLayout.WEST);

        final JButton back = new JButton(Res.get(Res.BACK_BTN));
        back.setActionCommand(BUTTONS_CARD);
        back.addActionListener(this);
        top.add(back, BorderLayout.EAST);

        result.add(top, BorderLayout.NORTH);

        AbstractForm form = null;

        if ("assignment".equals(tableName)) {
            form = new AssignmentForm(this.cache);
            this.forms.put(tableName, form);
        } else if ("admin_hold".equals(tableName)) {
            form = new AdminHoldForm(this.cache);
            this.forms.put(tableName, form);
        } else if ("csection".equals(tableName)) {
            form = new CSectionForm(this.cache, theRenderingHint);
            this.forms.put(tableName, form);
        }

        if (form == null) {
            // Log.warning("Unimplemented form: " + tableName);
        } else {
            result.add(form, BorderLayout.CENTER);
        }

        return result;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        final AbstractForm form = this.forms.get(cmd);
        if (form == null) {
            Log.warning("No form for '", cmd, "'");
        } else {
            form.activate();
        }

        this.cards.show(this, cmd);
    }
}
