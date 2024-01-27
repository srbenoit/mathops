package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A card to display when the user selects the "Pick Student" option. This card includes a field to enter student ID, a
 * field to enter student name, a list of recently selected students that can be picked from, and (when a name matches
 * several records), a list of matches from which to select.
 */
final class CardPickStudent extends AdminPanelBase implements ActionListener, MouseListener {

    /** An action command. */
    private static final String QUERY_CMD = "QUERY";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The owning admin pane. */
    private final TopPanelStudent owner;

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey activeTermKey;

    /** The fixed data. */
    private final FixedData fixed;

    /** The student ID. */
    private final JTextField stuIdField;

    /** The error message. */
    private final JLabel error;

    /** The model for the history. */
    private final DefaultListModel<String> historyModel;

    /** The records associated to history entries. */
    private final List<RawStudent> historyRecords;

    /** A list from which to pick recently selected students. */
    private final JList<String> history;

    /** A scroll pane for the pick list when multiple students are found. */
    private final JScrollPane scroll;

    /** The model for the pick list. */
    private final DefaultListModel<String> pickListModel;

    /** The records associated to pick list entries. */
    private final List<RawStudent> pickListRecords;

    /** A list from which to pick when multiple students are found. */
    private final JList<String> pickList;

    /**
     * Constructs a new {@code CardPickStudent}.
     *
     * @param theOwner         the owning top-level student panel
     * @param theCache         the data cache
     * @param theFixed         the fixed data
     */
    CardPickStudent(final TopPanelStudent theOwner, final Cache theCache, final FixedData theFixed) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(getBackground());
        panel.setBorder(getBorder());

        setBackground(Skin.LT_GREEN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.owner = theOwner;
        this.cache = theCache;
        this.fixed = theFixed;

        panel.add(makeHeader("Select a student...", false), BorderLayout.PAGE_START);

        final JPanel center = makeOffWhitePanel(new BorderLayout(10, 10));
        center.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.add(center, BorderLayout.CENTER);

        final JPanel centerWest = makeOffWhitePanel(new BorderLayout());
        center.add(centerWest, BorderLayout.LINE_START);

        final JPanel fieldBox = new JPanel();
        fieldBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        fieldBox.setBackground(Skin.LIGHT);
        final LayoutManager box = new BoxLayout(fieldBox, BoxLayout.PAGE_AXIS);
        fieldBox.setLayout(box);

        final JLabel boxLabel = new JLabel("Enter student name or ID:");
        final JPanel boxLabelFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        boxLabelFlow.setBackground(Skin.LIGHT);
        boxLabel.setFont(Skin.MEDIUM_15_FONT);
        boxLabelFlow.add(boxLabel);
        fieldBox.add(boxLabelFlow);

        fieldBox.add(Box.createRigidArea(new Dimension(0, 6)));

        this.stuIdField = new JTextField(16);
        this.stuIdField.setBackground(Color.WHITE);
        this.stuIdField.setActionCommand(QUERY_CMD);
        this.stuIdField.addActionListener(this);
        this.stuIdField.setFont(Skin.MEDIUM_13_FONT);
        fieldBox.add(this.stuIdField);

        fieldBox.add(Box.createRigidArea(new Dimension(0, 6)));

        final JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        errorPanel.setBackground(Skin.LIGHT);
        this.error = new JLabel(CoreConstants.SPC);
        this.error.setFont(Skin.MEDIUM_15_FONT);
        this.error.setForeground(Skin.ERROR_COLOR);
        errorPanel.add(this.error);
        fieldBox.add(errorPanel);

        fieldBox.add(Box.createRigidArea(new Dimension(0, 6)));

        final JButton query = new JButton("Look up student");
        query.setActionCommand(QUERY_CMD);
        query.addActionListener(this);
        query.setFont(Skin.BUTTON_15_FONT);
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttons.setBackground(Skin.LIGHT);
        buttons.add(query);
        fieldBox.add(buttons);

        centerWest.add(fieldBox, BorderLayout.PAGE_START);

        final JPanel centerCenter = makeOffWhitePanel(new BorderLayout());
        center.add(centerCenter, BorderLayout.CENTER);

        this.historyModel = new DefaultListModel<>();
        this.historyRecords = new ArrayList<>(10);
        this.history = new JList<>(this.historyModel);
        this.history.setFont(Skin.BUTTON_13_FONT);
        this.history.setBorder(BorderFactory.createEtchedBorder());
        this.history.addMouseListener(this);
        this.history.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final JPanel recent = new JPanel(new BorderLayout());
        recent.setBackground(Skin.OFF_WHITE_GREEN);

        final JLabel recentLbl = new JLabel("Recently Selected Students:");
        recentLbl.setFont(Skin.MEDIUM_15_FONT);
        recent.add(recentLbl, BorderLayout.PAGE_START);

        recent.add(this.history, BorderLayout.CENTER);
        recent.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        centerWest.add(recent, BorderLayout.CENTER);

        this.pickListModel = new DefaultListModel<>();
        this.pickListRecords = new ArrayList<>(10);
        this.pickList = new JList<>(this.pickListModel);
        this.pickList.setFont(Skin.BUTTON_13_FONT);
        this.pickList.setBorder(BorderFactory.createEtchedBorder());
        this.pickList.addMouseListener(this);
        this.pickList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.scroll = new JScrollPane(this.pickList);
        this.scroll.setVisible(false);
        centerCenter.add(this.scroll, BorderLayout.LINE_START);

        TermKey key = null;
        try {
            final TermRec term = TermLogic.get(this.cache).queryActive(theCache);
            if (term == null) {
                Log.warning("No active term found");
            } else {
                key = term.term;
            }
        } catch (final SQLException ex) {
            Log.warning("Unable to query active term", ex);
        }
        this.activeTermKey = key;
    }

    /**
     * Called when the panel is shown to set focus in the student ID field.
     */
    /* default */ void focus() {

        this.stuIdField.requestFocus();
        this.history.getSelectionModel().clearSelection();
    }

    /**
     * Called when the button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (QUERY_CMD.equals(cmd)) {
            this.error.setText(CoreConstants.SPC);
            this.owner.setStudent(this.cache, null);
            this.scroll.setVisible(false);

            final String stuId = this.stuIdField.getText();
            if (stuId == null || stuId.isEmpty()) {
                this.error.setText("Enter student ID or name to search");
            } else {
                final char firstChar = stuId.charAt(0);
                if (Character.isDigit(firstChar)) {
                    queryByStudentId(stuId);
                } else {
                    queryByStudentName(stuId);
                }
            }
        }
    }

    /**
     * Queries for the student by student ID.
     *
     * @param studentId the student ID
     */
    private void queryByStudentId(final String studentId) {

        final StringBuilder actual = new StringBuilder(20);
        for (final char ch : studentId.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                actual.append(ch);
            }
        }

        try (final PreparedStatement ps = this.cache.conn.prepareStatement("SELECT * FROM student WHERE stu_id=?")) {
            ps.setString(1, actual.toString());

            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final RawStudent stuRec = RawStudent.fromResultSet(rs);
                    addToHistory(stuRec);

                    this.owner.setStudent(this.cache, new StudentData(this.cache, this.fixed, stuRec));
                    this.stuIdField.setText(CoreConstants.EMPTY);
                } else {
                    this.error.setText("Student not found");
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            this.error.setText("Query failed: " + ex.getMessage());
        }
    }

    /**
     * Queries for the student by name.
     *
     * @param name the name
     */
    private void queryByStudentName(final String name) {

        final String trimmed = name.trim();

        final int comma = trimmed.indexOf(CoreConstants.COMMA_CHAR);
        if (comma == -1) {
            // No comma - assume last name only
            if (!queryByLastName(trimmed)) {
                final String[] parts = name.split(CoreConstants.SPC);

                if (parts.length == 2) {
                    // Has one space - assume First Last
                    queryByLastAndFirstName(parts[1], parts[0]);
                } else if (parts.length == 3) {
                    // Try "Ludwig Van Beethoven", then "Billy Bob Thornton", then "John Q Public"
                    if (!queryByLastAndFirstName(parts[1] + CoreConstants.SPC + parts[2], parts[0])
                            && !queryByLastAndFirstName(parts[2], parts[0] + CoreConstants.SPC + parts[1])) {
                        queryByLastAndFirstName(parts[2], parts[0]);
                    }
                } else if (parts.length == 4) {
                    // Try "Billy Bob Van Beethoven" style
                    // Try "John Q. Van Beethoven"
                    if (!queryByLastAndFirstName(parts[2] + CoreConstants.SPC + parts[3],
                            parts[0] + CoreConstants.SPC + parts[1])
                            && !queryByLastAndFirstName(parts[2] + CoreConstants.SPC + parts[3], parts[0])) {
                        // Try "Billy Bob Q Public"
                        if (!queryByLastAndFirstName(parts[3], parts[0] + CoreConstants.SPC + parts[1])) {
                            // Try "John Q R Public"
                            queryByLastAndFirstName(parts[3], parts[0]);
                        }
                    }
                } else // Try "John Q R S T U Public"
                    if ((parts.length > 4)
                            && !queryByLastAndFirstName(parts[parts.length - 1], parts[0])) {
                        // Try "Billy Bob Q R S T U Public"
                        if (!queryByLastAndFirstName(parts[parts.length - 1],
                                parts[0] + CoreConstants.SPC + parts[1])) {
                            // Try "John Q R S T U Van Beethoven"
                            if (!queryByLastAndFirstName(
                                    parts[parts.length - 2] + CoreConstants.SPC + parts[parts.length - 1], parts[0])) {
                                // Try "Billy Bob Q R S T U Van Beethoven"
                                queryByLastAndFirstName(
                                        parts[parts.length - 2] + CoreConstants.SPC + parts[parts.length - 1],
                                        parts[0] + CoreConstants.SPC + parts[1]);
                            }
                        }
                    }
            }
        } else {
            // Has a comma - assume Last, First
            queryByLastAndFirstName(trimmed.substring(0, comma).trim(),
                    trimmed.substring(comma + 1).trim());
        }
    }

    /**
     * Queries for the student by last name.
     *
     * @param name the last name
     * @return true if student was found
     */
    private boolean queryByLastName(final String name) {

        boolean ok = false;

        final String trimmed = name.trim();
        if (trimmed.isBlank() || "\\".equals(trimmed) || "*".equals(trimmed) || "?".equals(trimmed)) {
            this.error.setText("Student not found");
        } else {

            try (final PreparedStatement ps = this.cache.conn.prepareStatement(
                    "SELECT * FROM student WHERE lower(last_name) like ?")) {
                ps.setString(1, trimmed.toLowerCase(Locale.US));

                try (final ResultSet rs = ps.executeQuery()) {

                    final List<RawStudent> found = new ArrayList<>(10);
                    while (rs.next()) {
                        final RawStudent stu = RawStudent.fromResultSet(rs);
                        found.add(stu);
                    }

                    if (found.isEmpty()) {
                        this.error.setText("Student not found");
                    } else {
                        if (found.size() == 1) {
                            final RawStudent stuRec = found.get(0);
                            addToHistory(stuRec);
                            this.owner.setStudent(this.cache, new StudentData(this.cache, this.fixed, stuRec));
                            this.stuIdField.setText(CoreConstants.EMPTY);
                        } else {
                            processStudentList(found);
                        }
                        ok = true;
                    }
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                this.error.setText("Query failed: " + ex.getMessage());
            }
        }

        return ok;
    }

    /**
     * Queries for the student by last and first name.
     *
     * @param last  the last name
     * @param first the first name
     * @return true if student was found
     */
    private boolean queryByLastAndFirstName(final String last, final String first) {

        boolean ok = false;

        try (final PreparedStatement ps = this.cache.conn.prepareStatement(
                "SELECT * FROM student WHERE lower(last_name) like ? "
                        + "AND (lower(first_name) like ? OR lower(pref_name) like ?)")) {
            ps.setString(1, last.trim().toLowerCase(Locale.US));

            String firstLower = first.trim().toLowerCase(Locale.US);
            if (!(!firstLower.isEmpty() && firstLower.charAt(firstLower.length() - 1) == '%')) {
                firstLower += "%";
            }
            ps.setString(2, firstLower);
            ps.setString(3, firstLower);

            try (final ResultSet rs = ps.executeQuery()) {
                final List<RawStudent> found = new ArrayList<>(10);
                while (rs.next()) {
                    found.add(RawStudent.fromResultSet(rs));
                }

                if (found.isEmpty()) {
                    this.error.setText("Student not found");
                } else {
                    if (found.size() == 1) {
                        final RawStudent stuRec = found.get(0);
                        addToHistory(stuRec);
                        this.owner.setStudent(this.cache, new StudentData(this.cache, this.fixed, stuRec));
                        this.stuIdField.setText(CoreConstants.EMPTY);
                    } else {
                        processStudentList(found);
                    }
                    ok = true;
                }
            }

        } catch (final SQLException ex) {
            Log.warning(ex);
            this.error.setText("Query failed: " + ex.getMessage());
        }

        return ok;
    }

    /**
     * Processes a list of matching students.
     *
     * @param found the list of students found
     * @throws SQLException if there is an error accessing the database
     */
    private void processStudentList(final List<RawStudent> found) throws SQLException {

        Collections.sort(found);

        // Sort into two lists, one with registrations, one without
        final List<RawStudent> withRegs = new ArrayList<>(found.size() / 2);
        final List<List<RawStcourse>> regs = new ArrayList<>(found.size() / 2);
        final Collection<RawStudent> noRegs = new ArrayList<>(found.size());

        for (final RawStudent r : found) {
            if (this.activeTermKey != null) {
                final List<RawStcourse> stuRegs =
                        RawStcourseLogic.getActiveForStudent(this.cache, r.stuId, this.activeTermKey);

                if (stuRegs.isEmpty()) {
                    noRegs.add(r);
                } else {
                    withRegs.add(r);
                    regs.add(stuRegs);
                }
            }
        }

        // Present list, registered students first

        this.pickListModel.removeAllElements();
        this.pickListRecords.clear();

        final int numWithRegs = withRegs.size();
        for (int i = 0; i < numWithRegs; ++i) {
            final RawStudent r = withRegs.get(i);
            final List<RawStcourse> stuRegs = regs.get(i);

            final StringBuilder screenName = new StringBuilder(100);
            screenName.append(r.getScreenName());
            screenName.append(" (");
            boolean comma = false;
            for (final RawStcourse row : stuRegs) {
                if (comma) {
                    screenName.append(", ");
                }
                comma = true;
                screenName.append(row.course);
            }
            screenName.append(")");

            this.pickListModel.addElement(screenName.toString());
            this.pickListRecords.add(r);
        }

        for (final RawStudent r : noRegs) {
            this.pickListModel.addElement(r.getScreenName());
            this.pickListRecords.add(r);
        }

        this.scroll.setVisible(true);
        this.stuIdField.setText(CoreConstants.EMPTY);

        revalidate();
    }

    /**
     * Adds a student record to the local history.
     *
     * @param stuRec the record
     */
    private void addToHistory(final RawStudent stuRec) {

        if (this.historyRecords.isEmpty()) {
            this.historyModel.addElement(stuRec.getScreenName() + " (" + stuRec.stuId + ")");
            this.historyRecords.add(stuRec);
        } else if (!this.historyRecords.contains(stuRec)) {
            int count = this.historyRecords.size();
            while (count > 20) {
                this.historyModel.remove(count - 1);
                this.historyRecords.remove(count - 1);
                --count;
            }
            this.historyModel.add(0, stuRec.getScreenName() + " (" + stuRec.stuId + ")");
            this.historyRecords.add(0, stuRec);
        }
    }

    /**
     * Called when the mouse is clicked in the pick list.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        if (e.getClickCount() == 2) {
            if (e.getSource() == this.pickList) {
                final int index = this.pickList.locationToIndex(e.getPoint());
                final RawStudent stuRec = this.pickListRecords.get(index);
                addToHistory(stuRec);

                try {
                    this.owner.setStudent(this.cache, new StudentData(this.cache, this.fixed, stuRec));
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    this.owner.setStudent(this.cache, null);
                }

                this.stuIdField.setText(CoreConstants.EMPTY);

                this.scroll.setVisible(false);
                this.pickListModel.removeAllElements();
                this.pickListRecords.clear();
            } else if (e.getSource() == this.history) {
                final int index = this.history.locationToIndex(e.getPoint());
                final RawStudent stuRec = this.historyRecords.get(index);

                try {
                    this.owner.setStudent(this.cache, new StudentData(this.cache, this.fixed, stuRec));
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    this.owner.setStudent(this.cache, null);
                }

                this.stuIdField.setText(CoreConstants.EMPTY);
            }
        }
    }

    /**
     * Called when the mouse is pressed in the pick list.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse is released in the pick list.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse enters the pick list.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse exits the pick list.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
    }
}
