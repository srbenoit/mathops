package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStudent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * A panel that supports management of special student populations.
 */
class SpecialStudentsPanel extends AdminPanelBase implements ActionListener, ListSelectionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7586828613832230724L;

    /** An action command. */
    private static final String ADD_STUDENTS = "ADD_STUDENTS";

    /** An action command. */
    private static final String DEL_STUDENTS = "DEL_STUDENTS";

    /** An action command. */
    private static final String CHANGE_SORT = "CHANGE_SORT";

    /** All known special categories. */
    private static final String[] CATEGORIES = {
            "--- choose ---", "ADMIN", "ATHLETE",
            "BOOKSTO", "DCE", "DCEN",
            "ENGRPLC", "ENGRSTU", "LOCKDWN", "MPT3",
            "M384", "ORIENTN", "PCT117",
            "PCT118", "PCT124", "PCT125",
            "PCT126", "PROCTOR", "RAMWORK",
            "RIUSEPU", "STEVE", "TUTOR"};

    /** The ways rows can be sorted. */
    private static final String[] SORTS = {//
            "Name", "Start Date", "End Date",
            "CSU ID"};

    /** The data cache. */
    private final Cache cache;

    /** The model for the student types list. */
    private final DefaultListModel<String> typesModel;

    /** The list of special student types. */
    private final JList<String> typesList;

    /** The model for the students list. */
    private final DefaultListModel<String> studentsModel;

    /** The list of students in the selected type. */
    private final JList<String> studentsList;

    /** The category field for addition of new records. */
    private final JComboBox<String> addCategoryField;

    /** The start date for addition of new records. */
    private final JTextField addStartField;

    /** The end date for addition of new records. */
    private final JTextField addEndField;

    /** The list of students to add. */
    private final JTextArea addStudentsList;

    /** The preferred width of a scroll bar. */
    private final int scrollWidth;

    /** Button to delete records. */
    private final JButton delete;

    /** The sort selector. */
    private final JComboBox<String> sortBy;

    /**
     * Constructs a new {@code SpecialStudentsPanel}.
     *
     * @param theCache the data cache
     */
    SpecialStudentsPanel(final Cache theCache) {

        super();
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;

        // Left side: list of special categories

        final JPanel col1 = makeOffWhitePanel(new BorderLayout(5, 5));
        col1.setBackground(Skin.LIGHTEST);

        add(col1, StackedBorderLayout.WEST);

        col1.add(makeHeader("Categories", false), BorderLayout.NORTH);

        this.typesModel = new DefaultListModel<>();
        this.typesList = new JList<>(this.typesModel);
        this.typesList.setPreferredSize(new Dimension(150, 150));
        this.typesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        col1.add(new JScrollPane(this.typesList), BorderLayout.WEST);

        this.typesList.getSelectionModel().addListSelectionListener(this);

        final JPanel remainder1 = new JPanel(new BorderLayout(5, 5));
        remainder1.setBackground(Skin.LIGHTEST);
        add(remainder1, StackedBorderLayout.CENTER);

        // Next column - list of students in selected category
        final JPanel col2 = new JPanel(new BorderLayout(5, 5));
        col2.setBackground(Skin.LIGHTEST);
        remainder1.add(col2, BorderLayout.WEST);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        topFlow.setBackground(Skin.LIGHTEST);
        col2.add(topFlow, BorderLayout.NORTH);
        topFlow.add(makeHeader("Students", false));
        topFlow.add(Box.createRigidArea(new Dimension(50, 1)));
        topFlow.add(new JLabel("Sort by:"));
        this.sortBy = new JComboBox<>(SORTS);
        this.sortBy.addActionListener(this);
        this.sortBy.setActionCommand(CHANGE_SORT);
        topFlow.add(this.sortBy);

        this.studentsModel = new DefaultListModel<>();
        this.studentsList = new JList<>(this.studentsModel);
        final JScrollPane scroll = new JScrollPane(this.studentsList);
        scroll.setPreferredSize(new Dimension(400, 50));
        this.scrollWidth = scroll.getVerticalScrollBar().getPreferredSize().width;
        this.studentsList.setPreferredSize(new Dimension(300 - this.scrollWidth, 50));
        this.studentsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        col2.add(scroll, BorderLayout.WEST);
        this.studentsList.getSelectionModel().addListSelectionListener(this);

        this.delete = new JButton("Delete");
        this.delete.setActionCommand(DEL_STUDENTS);
        this.delete.addActionListener(this);
        this.delete.setEnabled(false);
        col2.add(this.delete, BorderLayout.SOUTH);

        final JPanel remainder2 = new JPanel(new BorderLayout(5, 5));
        remainder2.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.LIGHT_GRAY));
        remainder2.setBackground(Skin.LIGHTEST);
        remainder1.add(remainder2, BorderLayout.CENTER);

        // Next column - ability to add new population
        final JPanel col3 = new JPanel(new BorderLayout(5, 5));
        col3.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        col3.setBackground(Skin.LIGHTEST);
        remainder2.add(col3, BorderLayout.WEST);

        col3.add(makeHeader("Add Students", false), BorderLayout.NORTH);

        final JPanel addPane = new JPanel(new BorderLayout(5, 5));
        addPane.setBackground(Skin.LIGHTEST);
        col3.add(addPane, BorderLayout.CENTER);

        final JPanel north = new JPanel(new BorderLayout());
        addPane.add(north, BorderLayout.NORTH);

        final JPanel northtop = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        northtop.setBackground(Skin.LIGHTEST);
        northtop.add(new JLabel("Category:  "));
        this.addCategoryField = new JComboBox<>(CATEGORIES);
        this.addCategoryField.setMaximumRowCount(CATEGORIES.length);
        northtop.add(this.addCategoryField);
        north.add(northtop, BorderLayout.NORTH);

        final JPanel northctr = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        northctr.setBackground(Skin.LIGHTEST);
        northctr.add(new JLabel("Start:  "));
        this.addStartField = new JTextField(9);
        northctr.add(this.addStartField);
        north.add(northctr, BorderLayout.CENTER);

        final JPanel northbot = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        northbot.setBackground(Skin.LIGHTEST);
        northbot.add(new JLabel("End:  "));
        this.addEndField = new JTextField(9);
        northbot.add(this.addEndField);
        north.add(northbot, BorderLayout.SOUTH);

        final JPanel center = new JPanel(new BorderLayout(3, 3));
        center.setBackground(Skin.LIGHTEST);
        center.add(new JLabel("CSU ID numbers:"), BorderLayout.NORTH);
        addPane.add(center, BorderLayout.CENTER);
        this.addStudentsList = new JTextArea();
        center.add(new JScrollPane(this.addStudentsList), BorderLayout.CENTER);

        final JButton btn = new JButton("Add Students");
        btn.setActionCommand(ADD_STUDENTS);
        btn.addActionListener(this);
        addPane.add(btn, BorderLayout.SOUTH);

        refreshCategories();
    }

    /**
     * Refreshes the list of categories.
     */
    private void refreshCategories() {

        try {
            final Map<String, Integer> types = new TreeMap<>();

            final List<RawSpecialStus> all = RawSpecialStusLogic.INSTANCE.queryAll(this.cache);
            for (final RawSpecialStus row : all) {
                final Integer count = types.get(row.stuType);
                if (count == null) {
                    types.put(row.stuType, Integer.valueOf(1));
                } else {
                    types.put(row.stuType, Integer.valueOf(count.intValue() + 1));
                }
            }

            this.typesModel.clear();
            for (final Map.Entry<String, Integer> type : types.entrySet()) {
                final String txt = type.getKey() + " (" + type.getValue()
                        + ")";
                this.typesModel.addElement(txt);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query special student records.", ex);
        }
    }

    /**
     * Called when the selection in a list has changed.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        final Object src = e.getSource();

        if (src == this.typesList.getSelectionModel()) {
            refreshStudentList();
        } else if (src == this.studentsList.getSelectionModel()) {

            final int[] indices = this.studentsList.getSelectedIndices();
            this.delete.setEnabled(indices != null && indices.length != 0);
        }
    }

    /**
     * Populates the student list based on category and sort order selections.
     */
    private void refreshStudentList() {

        this.studentsModel.clear();

        String selType = this.typesList.getSelectedValue();
        if (selType != null) {

            final int spc = selType.indexOf(' ');
            if (spc != -1) {
                selType = selType.substring(0, spc);
            }

            try {
                final List<RawSpecialStus> list = RawSpecialStusLogic.queryByType(this.cache, selType);

                for (final String entry : makeSortedList(list)) {
                    this.studentsModel.addElement(entry);
                }

                final FontMetrics metr = this.studentsList.getFontMetrics(this.studentsList.getFont());
                final int h = 1 + this.studentsModel.getSize() * (metr.getHeight() + 4);
                this.studentsList.setPreferredSize(new Dimension(398 - this.scrollWidth, h));
            } catch (final SQLException ex) {
                Log.warning("Failed to query special student records.");
            }
        }
    }

    /**
     * Generates a list of strings that correspond to a list of special student records, sorted by the current sort
     * setting.
     *
     * @param unsorted the unsorted list of special student records
     * @return the sorted list of strings
     * @throws SQLException if there is an error querying the database
     */
    private List<String> makeSortedList(final List<RawSpecialStus> unsorted) throws SQLException {

        final List<String> sorted = new ArrayList<>(unsorted.size());

        // Sorts: "Name", "Start Date", "End Date", "CSU ID"

        final int sortIndex = this.sortBy.getSelectedIndex();

        if (sortIndex == 0) {
            // Sort by name: construct strings first, then sort strings alphabetically
            for (final RawSpecialStus record : unsorted) {
                sorted.add(makeString(record));
            }
            Collections.sort(sorted);
        } else {
            // Sort records first, then build strings from the sorted list
            final List<RawSpecialStus> work = new ArrayList<>(unsorted);

            if (sortIndex == 1) {
                work.sort(new RawSpecialStus.StartDateComparator());
            } else if (sortIndex == 2) {
                work.sort(new RawSpecialStus.EndDateComparator());
            } else {
                // Sort by CSU ID - the native sort order of RawSpecialStus objects
                Collections.sort(work);
            }

            for (final RawSpecialStus record : work) {
                sorted.add(makeString(record));
            }
        }

        return sorted;
    }

    /**
     * Creates the string representation of a special student record.
     *
     * @param record the special student record
     * @return the string representation
     * @throws SQLException if there is an error querying student data
     */
    private String makeString(final RawSpecialStus record) throws SQLException {

        final HtmlBuilder htm = new HtmlBuilder(100);

        final RawStudent stu = RawStudentLogic.query(this.cache, record.stuId, false);

        if (stu == null) {
            htm.add(record.stuId);
        } else {
            htm.add(stu.lastName, ", ");
            if (stu.prefName == null) {
                htm.add(stu.firstName);
            } else {
                htm.add(stu.prefName);
            }
            htm.add(" (", stu.stuId, ")");
        }

        if (record.startDt != null && record.endDt != null) {
            htm.add(" [",
                    TemporalUtils.FMT_MDY_COMPACT.format(record.startDt), //
                    "-",
                    TemporalUtils.FMT_MDY_COMPACT.format(record.endDt), //
                    "]");
        }

        return htm.toString();

    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (ADD_STUDENTS.equals(cmd)) {
            final Object cat = this.addCategoryField.getSelectedItem();
            if (cat != null) {
                String catStr = cat.toString();
                if (catStr.length() > 7) {
                    catStr = catStr.substring(0, 7);
                }

                // Log.info("Category = " + catStr);

                try {
                    final LocalDate startDt = interpretDate(this.addStartField.getText());
                    final LocalDate endDt = interpretDate(this.addEndField.getText());

                    // Log.info("Start = " + start);
                    // Log.info("End = " + end);

                    final String txt = this.addStudentsList.getText();
                    final String[] lines = txt.split("\n");

                    final Collection<String> stuIds = new ArrayList<>(lines.length);
                    for (final String line : lines) {
                        final String validated = validateStuId(line);
                        if (validated != null) {
                            stuIds.add(validated);
                        } else {
                            throw new IllegalArgumentException("Invalid student ID: "
                                    + line);
                        }
                    }

                    for (final String stuId : stuIds) {
                        final RawSpecialStus toAdd =
                                new RawSpecialStus(stuId, catStr, startDt, endDt);

                        try {
                            boolean exists = false;
                            final List<RawSpecialStus> existing =
                                    RawSpecialStusLogic.queryByStudent(this.cache, stuId);
                            for (final RawSpecialStus test : existing) {
                                if (test.stuType.equals(catStr)
                                        && Objects.equals(test.startDt, startDt)
                                        && Objects.equals(test.endDt, endDt)) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (exists) {
                                Log.info("Skipping existing record for ", stuId);
                            } else {
                                RawSpecialStusLogic.INSTANCE.insert(this.cache, toAdd);
                            }
                        } catch (final SQLException ex) {
                            Log.warning("Failed to insert record.", ex);
                            JOptionPane.showMessageDialog(this, "Failed to insert a record");
                        }
                    }

                    this.addStartField.setText(CoreConstants.EMPTY);
                    this.addEndField.setText(CoreConstants.EMPTY);
                    this.addStudentsList.setText(CoreConstants.EMPTY);
                } catch (final IllegalArgumentException ex) {
                    Log.warning(ex);
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }

            refreshCategories();
        } else if (DEL_STUDENTS.equals(cmd)) {

            String selType = this.typesList.getSelectedValue();
            final int spc = selType.indexOf(' ');
            if (spc != -1) {
                selType = selType.substring(0, spc);
            }
            Log.info("Category = " + selType);

            final List<String> selStus = this.studentsList.getSelectedValuesList();

            for (final String row : selStus) {
                final String raw;
                final int lParen = row.indexOf('(');
                if (lParen == -1) {
                    raw = row;
                } else {
                    final int rParen = row.indexOf(')', lParen);
                    if (rParen == -1) {
                        raw = row;
                    } else {
                        raw = row.substring(lParen + 1, rParen);
                    }
                }

                final String stuId = validateStuId(raw);

                if (stuId != null) {
                    try {
                        final List<RawSpecialStus> hitRows =
                                RawSpecialStusLogic.queryByStudent(this.cache, stuId);

                        for (final RawSpecialStus hitRow : hitRows) {
                            if (selType.equals(hitRow.stuType)) {
                                Log.info("Delete '", selType,
                                        "' row for ", stuId);
                                RawSpecialStusLogic.INSTANCE.delete(this.cache, hitRow);
                            }
                        }
                    } catch (final SQLException ex) {
                        Log.warning("Failed to delete record.", ex);
                    }
                }
            }

            refreshCategories();
        } else if (CHANGE_SORT.equals(cmd)) {
            refreshStudentList();
        }
    }

    /**
     * Attempts to interpret text as a local date. If the text is null, null is returned; If the text is invalid, an
     * exception is thrown.
     *
     * @param txt the test to interpret
     * @return the interpreted date
     * @throws IllegalArgumentException if the text is invalid
     */
    private static LocalDate interpretDate(final String txt) throws IllegalArgumentException {

        final LocalDate result;

        if (txt == null || txt.isEmpty()) {
            result = null;
        } else {
            final String trimmed = txt.trim();

            if (trimmed.length() == 6) {
                // 010299 = MMDDYY
                try {
                    final int intval = Integer.parseInt(trimmed);
                    final int month = intval / 10000;
                    final int day = (intval / 100) % 100;
                    final int year = 2000 + (intval % 100);

                    result = LocalDate.of(year, month, day);

                } catch (final NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid date fomat.", ex);
                }
            } else {
                // TODO: other formats
                throw new IllegalArgumentException("Invalid date fomat.");
            }
        }

        return result;
    }

    /**
     * Validates a student ID.
     *
     * @param raw the raw student ID to validate
     * @return the validated ID
     */
    private static String validateStuId(final String raw) {

        final String validated;

        final String trimmed = raw.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY)
                .replace(CoreConstants.SPC, CoreConstants.EMPTY);
        try {
            Integer.parseInt(trimmed);
            validated = trimmed;
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid students ID: " + raw, ex);
        }

        return validated;
    }
}
