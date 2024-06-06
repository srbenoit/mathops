package dev.mathops.app.adm.forms;

import dev.mathops.app.adm.GenericRecord;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.fields.ConstrainedNonNegIntField;
import dev.mathops.app.adm.fields.ConstrainedTextField;
import dev.mathops.app.adm.fields.DateField;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawCsection;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The "csection" table form.
 */
final class CSectionForm extends AbstractForm implements ListSelectionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7006157832311757882L;

    /** An empty term key array. */
    private static final TermKey[] EMPTY_TERM_KEY_ARRAY = new TermKey[0];

    /** The data cache. */
    private final Cache cache;

    /** The 'course' field. */
    private final ConstrainedTextField courseIdField;

    /** The 'sect' field. */
    private final ConstrainedTextField sectField;

    /** The 'section_id' field. */
    private final ConstrainedTextField sectionIdField;

    /** The 'aries_start_dt' field. */
    private final DateField ariesStartDtField;

    /** The 'aries_end_dt' field. */
    private final DateField ariesEndDtField;

    /** The 'start_dt' field. */
    private final DateField startDtField;

    /** The 'exam_delete_dt' field. */
    private final DateField examDeleteDtField;

    /** The 'instrn_type' field. */
    private final ConstrainedTextField instrnTypeField;

    /** The 'instructor' field. */
    private final ConstrainedTextField instructorField;

    /** The 'campus' field. */
    private final ConstrainedTextField campusField;

    /** The 'pacing_structure' field. */
    private final ConstrainedTextField pacingStructureField;

    /** The 'mtgDays' field. */
    private final ConstrainedTextField mtgDaysField;

    /** The 'classroom_id' field. */
    private final ConstrainedTextField classroomId;

    /** The 'last_stcrs_creat_dt' field. */
    private final DateField lastStcrsCreatDtField;

    /** The 'grading_str' field. */
    private final ConstrainedTextField gradingStdField;

    /** The 'a_min_score' field. */
    private final ConstrainedNonNegIntField aMinScoreField;

    /** The 'b_min_score' field. */
    private final ConstrainedNonNegIntField bMinScoreField;

    /** The 'c_min_score' field. */
    private final ConstrainedNonNegIntField cMinScoreField;

    /** The 'd_min_score' field. */
    private final ConstrainedNonNegIntField dMinScoreField;

    /** The 'survey_id' field. */
    private final ConstrainedTextField surveyIdField;

    /** The 'course_label_shown' field. */
    private final ConstrainedTextField courseLabelShownField;

    /** The 'display_score' field. */
    private final ConstrainedTextField displayScoreField;

    /** The 'display_grade_scale' field. */
    private final ConstrainedTextField displayGradeScaleField;

    /** The 'count_in_max_courses' field. */
    private final ConstrainedTextField countInMaxCoursesField;

    /** The 'online' field. */
    private final ConstrainedTextField onlineField;

    /** The 'bogus' field. */
    private final ConstrainedTextField bogusField;

    /** The 'canvas_id' field. */
    private final ConstrainedTextField canvasIdField;

    /** The 'subterm' field. */
    private final ConstrainedTextField subtermField;

    /** Results of a query. */
    private final List<GenericRecord> queryResult;

    /** The table of results. */
    private final JTableCsection table;

    /** The list of terms from which to select. */
    private final JList<TermKey> termsList;

    /** Cursor pointing the current query result. */
    private int cursor = 0;

    /** The currently selected term. */
    private TermKey selectedTerm = null;

    /**
     * Constructs a new {@code CSectionForm}.
     *
     * @param theCache         the data cache
     */
    CSectionForm(final Cache theCache) {

        super();

        this.cache = theCache;
        this.queryResult = new ArrayList<>(50);

        final String[] fields = {RawCsection.FLD_COURSE, RawCsection.FLD_SECT,
                RawCsection.FLD_SECTION_ID, RawCsection.FLD_ARIES_START_DT,
                RawCsection.FLD_ARIES_END_DT, RawCsection.FLD_START_DT, RawCsection.FLD_EXAM_DELETE_DT,
                RawCsection.FLD_INSTRN_TYPE, RawCsection.FLD_INSTRUCTOR, RawCsection.FLD_CAMPUS,
                RawCsection.FLD_PACING_STRUCTURE, RawCsection.FLD_MTG_DAYS,
                RawCsection.FLD_CLASSROOM_ID, RawCsection.FLD_LST_STCRS_CREAT_DT,
                RawCsection.FLD_GRADING_STD, RawCsection.FLD_A_MIN_SCORE, RawCsection.FLD_B_MIN_SCORE,
                RawCsection.FLD_C_MIN_SCORE, RawCsection.FLD_D_MIN_SCORE, RawCsection.FLD_SURVEY_ID,
                RawCsection.FLD_COURSE_LABEL_SHOWN, RawCsection.FLD_DISPLAY_SCORE,
                RawCsection.FLD_DISPLAY_GRADE_SCALE, RawCsection.FLD_COUNT_IN_MAX_COURSES,
                RawCsection.FLD_ONLINE, RawCsection.FLD_BOGUS, RawCsection.FLD_CANVAS_ID,
                RawCsection.FLD_SUBTERM};

        this.termsList = new JList<>(new DefaultListModel<>());
        this.termsList.setPreferredSize(new Dimension(100, 100));
        this.termsList.addListSelectionListener(this);

        final JScrollPane termsScroll = new JScrollPane(this.termsList);
        termsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(termsScroll, StackedBorderLayout.WEST);

        final JPanel left = new JPanel(new BorderLayout());
        left.setBackground(Color.WHITE);
        add(left, StackedBorderLayout.WEST);

        final JPanel center = new JPanel(new BorderLayout());
        add(center, StackedBorderLayout.CENTER);

        final JLabel[] labels = makeFieldLabels(fields);

        this.courseIdField = makeTextField(fields[0], 6, LETTERS_DIGITS);
        this.sectField = makeTextField(fields[1], 4, DIGITS);
        this.sectionIdField = makeTextField(fields[2], 6, DIGITS);
        this.ariesStartDtField = makeDateField(fields[3]);
        this.ariesEndDtField = makeDateField(fields[4]);
        this.startDtField = makeDateField(fields[5]);
        this.examDeleteDtField = makeDateField(fields[6]);
        this.instrnTypeField = makeTextField(fields[7], 6, UC_LETTERS);
        this.instructorField = makeTextField(fields[8], 30, LETTERS_PUNC);
        this.campusField = makeTextField(fields[9], 2, LETTERS_DIGITS);
        this.pacingStructureField = makeTextField(fields[10], 1, UC_LETTERS);
        this.mtgDaysField = makeTextField(fields[11], 5, UC_LETTERS);
        this.classroomId = makeTextField(fields[12], 14, LETTERS_DIGITS);
        this.lastStcrsCreatDtField = makeDateField(fields[13]);
        this.gradingStdField = makeTextField(fields[14], 3, LETTERS_DIGITS);
        this.aMinScoreField = makeIntField(fields[15], false, 999L);
        this.bMinScoreField = makeIntField(fields[16], false, 999L);
        this.cMinScoreField = makeIntField(fields[17], false, 999L);
        this.dMinScoreField = makeIntField(fields[18], false, 999L);
        this.surveyIdField = makeTextField(fields[19], 5, LETTERS_DIGITS);
        this.courseLabelShownField = makeTextField(fields[20], 1, YN);
        this.displayScoreField = makeTextField(fields[21], 1, YN);
        this.displayGradeScaleField = makeTextField(fields[22], 1, YN);
        this.countInMaxCoursesField = makeTextField(fields[23], 1, YN);
        this.onlineField = makeTextField(fields[24], 1, YN);
        this.bogusField = makeTextField(fields[25], 1, YN);
        this.canvasIdField = makeTextField(fields[26], 40, LETTERS_DIGITS_PUNC);
        this.subtermField = makeTextField(fields[27], 4, LETTERS_DIGITS);

        this.courseIdField.setEnabled(false);
        this.sectField.setEnabled(false);
        this.sectionIdField.setEnabled(false);
        this.ariesStartDtField.setEnabled(false);
        this.ariesEndDtField.setEnabled(false);
        this.startDtField.setEnabled(false);
        this.examDeleteDtField.setEnabled(false);
        this.instrnTypeField.setEnabled(false);
        this.instructorField.setEnabled(false);
        this.campusField.setEnabled(false);
        this.pacingStructureField.setEnabled(false);
        this.mtgDaysField.setEnabled(false);
        this.classroomId.setEnabled(false);
        this.lastStcrsCreatDtField.setEnabled(false);
        this.gradingStdField.setEnabled(false);
        this.aMinScoreField.setEnabled(false);
        this.bMinScoreField.setEnabled(false);
        this.cMinScoreField.setEnabled(false);
        this.dMinScoreField.setEnabled(false);
        this.surveyIdField.setEnabled(false);
        this.courseLabelShownField.setEnabled(false);
        this.displayScoreField.setEnabled(false);
        this.displayGradeScaleField.setEnabled(false);
        this.countInMaxCoursesField.setEnabled(false);
        this.onlineField.setEnabled(false);
        this.bogusField.setEnabled(false);
        this.canvasIdField.setEnabled(false);
        this.subtermField.setEnabled(false);

        final JPanel current = new JPanel(new StackedBorderLayout(0, 0));
        current.setBackground(Skin.OFF_WHITE_GREEN);
        current.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        left.add(current, BorderLayout.PAGE_START);

        current.add(makeFlow(labels[0], this.courseIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[1], this.sectField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[2], this.sectionIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[3], this.ariesStartDtField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[4], this.ariesEndDtField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[5], this.startDtField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[6], this.examDeleteDtField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[7], this.instrnTypeField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[8], this.instructorField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[9], this.campusField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[10], this.pacingStructureField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[11], this.mtgDaysField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[12], this.classroomId), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[13], this.lastStcrsCreatDtField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[14], this.gradingStdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[15], this.aMinScoreField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[16], this.bMinScoreField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[17], this.cMinScoreField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[18], this.dMinScoreField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[19], this.surveyIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[20], this.courseLabelShownField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[21], this.displayScoreField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[22], this.displayGradeScaleField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[23], this.countInMaxCoursesField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[24], this.onlineField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[25], this.bogusField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[26], this.canvasIdField), StackedBorderLayout.NORTH);
        current.add(makeFlow(labels[27], this.subtermField), StackedBorderLayout.NORTH);

        this.table = new JTableCsection();
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.table.setFillsViewportHeight(true);
        this.table.getSelectionModel().addListSelectionListener(this);
        this.table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        final JScrollPane scroll = new JScrollPane(this.table);

        center.add(scroll, BorderLayout.LINE_START);
    }

    /**
     * Called when the form is activated. This may re-query the underlying table to (for example) refresh the list of
     * terms represented if data is segregated by term, or to populate drop-downs with results from a "select distinct"
     */
    @Override
    public void activate() {

        final String sql = "SELECT distinct term, term_yr FROM term";

        final List<TermKey> keys = new ArrayList<>(50);

        try (final Statement stmt = this.cache.conn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final ETermName termName = ETermName.forName(rs.getString(1));

                    if (termName != null) {
                        final int termYr = rs.getInt(2);
                        if (termYr > 79) {
                            keys.add(new TermKey(termName, 1900 + termYr));
                        } else {
                            keys.add(new TermKey(termName, 2000 + termYr));
                        }
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            setStatus("ERROR: failed to query list of terms.");
        }

        keys.sort(null);
        Collections.reverse(keys);

        this.termsList.setListData(keys.toArray(EMPTY_TERM_KEY_ARRAY));
        this.termsList.setPreferredSize(null);
        this.termsList.revalidate();

        this.selectedTerm = null;
        disableQueryInsert();
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (QUERY_CMD.equals(cmd)) {
            doQuery();
        } else if (ADD_CMD.equals(cmd)) {
            doAdd();
        } else if (EXECUTE_CMD.equals(cmd)) {
            doExecute();
        } else if (INSERT_CMD.equals(cmd)) {
            // TODO:
        } else if (PREV_CMD.equals(cmd)) {
            this.table.setRowSelectionInterval(this.cursor - 1, this.cursor - 1);
        } else if (NEXT_CMD.equals(cmd)) {
            this.table.setRowSelectionInterval(this.cursor + 1, this.cursor + 1);
        }
    }

    /**
     * Handles a click on the "Query" button, which enables the "Execute" button and all the fields.
     */
    private void doQuery() {

        // Enable fields and set focus in first field
        this.courseIdField.setEnabled(true);
        this.sectField.setEnabled(true);
        this.sectionIdField.setEnabled(true);
        this.ariesStartDtField.setEnabled(true);
        this.ariesEndDtField.setEnabled(true);
        this.startDtField.setEnabled(true);
        this.examDeleteDtField.setEnabled(true);
        this.instrnTypeField.setEnabled(true);
        this.instructorField.setEnabled(true);
        this.campusField.setEnabled(true);
        this.pacingStructureField.setEnabled(true);
        this.mtgDaysField.setEnabled(true);
        this.classroomId.setEnabled(true);
        this.lastStcrsCreatDtField.setEnabled(true);
        this.gradingStdField.setEnabled(true);
        this.aMinScoreField.setEnabled(true);
        this.bMinScoreField.setEnabled(true);
        this.cMinScoreField.setEnabled(true);
        this.dMinScoreField.setEnabled(true);
        this.surveyIdField.setEnabled(true);
        this.courseLabelShownField.setEnabled(true);
        this.displayScoreField.setEnabled(true);
        this.displayGradeScaleField.setEnabled(true);
        this.countInMaxCoursesField.setEnabled(true);
        this.onlineField.setEnabled(true);
        this.bogusField.setEnabled(true);
        this.canvasIdField.setEnabled(true);
        this.subtermField.setEnabled(true);

        this.table.clear();
        showRecord(null);

        this.courseIdField.setEditable(true);
        this.sectField.setEditable(true);
        this.sectionIdField.setEditable(true);
        this.ariesStartDtField.setEditable(true);
        this.ariesEndDtField.setEditable(true);
        this.startDtField.setEditable(true);
        this.examDeleteDtField.setEditable(true);
        this.instrnTypeField.setEditable(true);
        this.instructorField.setEditable(true);
        this.campusField.setEditable(true);
        this.pacingStructureField.setEditable(true);
        this.mtgDaysField.setEditable(true);
        this.classroomId.setEditable(true);
        this.lastStcrsCreatDtField.setEditable(true);
        this.gradingStdField.setEditable(true);
        this.aMinScoreField.setEditable(true);
        this.bMinScoreField.setEditable(true);
        this.cMinScoreField.setEditable(true);
        this.dMinScoreField.setEditable(true);
        this.surveyIdField.setEditable(true);
        this.courseLabelShownField.setEditable(true);
        this.displayScoreField.setEditable(true);
        this.displayGradeScaleField.setEditable(true);
        this.countInMaxCoursesField.setEditable(true);
        this.onlineField.setEditable(true);
        this.bogusField.setEditable(true);
        this.canvasIdField.setEditable(true);
        this.subtermField.setEditable(true);

        enableQuery();
        setStatus("Query: fill in fields to match, click [Execute].");

        this.courseIdField.requestFocus();
    }

    /**
     * Handles a click on the "Add" button, which enables the "Insert" button and all the fields.
     */
    private void doAdd() {

        // Enable fields and set focus in first field
        this.courseIdField.setEnabled(true);
        this.sectField.setEnabled(true);
        this.sectionIdField.setEnabled(true);
        this.ariesStartDtField.setEnabled(true);
        this.ariesEndDtField.setEnabled(true);
        this.startDtField.setEnabled(true);
        this.examDeleteDtField.setEnabled(true);
        this.instrnTypeField.setEnabled(true);
        this.instructorField.setEnabled(true);
        this.campusField.setEnabled(true);
        this.pacingStructureField.setEnabled(true);
        this.mtgDaysField.setEnabled(true);
        this.classroomId.setEnabled(true);
        this.lastStcrsCreatDtField.setEnabled(true);
        this.gradingStdField.setEnabled(true);
        this.aMinScoreField.setEnabled(true);
        this.bMinScoreField.setEnabled(true);
        this.cMinScoreField.setEnabled(true);
        this.dMinScoreField.setEnabled(true);
        this.surveyIdField.setEnabled(true);
        this.courseLabelShownField.setEnabled(true);
        this.displayScoreField.setEnabled(true);
        this.displayGradeScaleField.setEnabled(true);
        this.countInMaxCoursesField.setEnabled(true);
        this.onlineField.setEnabled(true);
        this.bogusField.setEnabled(true);
        this.canvasIdField.setEnabled(true);
        this.subtermField.setEnabled(true);

        this.courseIdField.setEditable(true);
        this.sectField.setEditable(true);
        this.sectionIdField.setEditable(true);
        this.ariesStartDtField.setEditable(true);
        this.ariesEndDtField.setEditable(true);
        this.startDtField.setEditable(true);
        this.examDeleteDtField.setEditable(true);
        this.instrnTypeField.setEditable(true);
        this.instructorField.setEditable(true);
        this.campusField.setEditable(true);
        this.pacingStructureField.setEditable(true);
        this.mtgDaysField.setEditable(true);
        this.classroomId.setEditable(true);
        this.lastStcrsCreatDtField.setEditable(true);
        this.gradingStdField.setEditable(true);
        this.aMinScoreField.setEditable(true);
        this.bMinScoreField.setEditable(true);
        this.cMinScoreField.setEditable(true);
        this.dMinScoreField.setEditable(true);
        this.surveyIdField.setEditable(true);
        this.courseLabelShownField.setEditable(true);
        this.displayScoreField.setEditable(true);
        this.displayGradeScaleField.setEditable(true);
        this.countInMaxCoursesField.setEditable(true);
        this.onlineField.setEditable(true);
        this.bogusField.setEditable(true);
        this.canvasIdField.setEditable(true);
        this.subtermField.setEditable(true);

        enableInsert();
        setStatus("Add: fill in fields, click [Insert].");

        this.courseIdField.requestFocus();
    }

    /**
     * Handles a click on the "Execute" button, which performs the query and populates the table and fields.
     */
    private void doExecute() {

        this.table.clear();
        this.queryResult.clear();
        this.cursor = 0;

        if (this.selectedTerm == null) {
            setStatus("Select term in order to query");
        } else {
            try {
                final StringBuilder builder = new StringBuilder(50);

                builder.append("SELECT ").append(this.courseIdField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.sectField.getName()).append(CoreConstants.COMMA_CHAR) //
                        .append(this.sectionIdField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.ariesStartDtField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.ariesEndDtField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.startDtField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.examDeleteDtField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.instrnTypeField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.instructorField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.campusField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.pacingStructureField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.mtgDaysField.getName()).append(CoreConstants.COMMA_CHAR) //
                        .append(this.classroomId.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.lastStcrsCreatDtField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.gradingStdField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.aMinScoreField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.bMinScoreField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.cMinScoreField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.dMinScoreField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.surveyIdField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.courseLabelShownField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.displayScoreField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.displayGradeScaleField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.countInMaxCoursesField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.onlineField.getName()).append(CoreConstants.COMMA_CHAR) //
                        .append(this.bogusField.getName()).append(CoreConstants.COMMA_CHAR) //
                        .append(this.canvasIdField.getName()).append(CoreConstants.COMMA_CHAR)
                        .append(this.subtermField.getName())
                        .append(" FROM csection WHERE term='")
                        .append(this.selectedTerm.termCode).append("' AND term_yr=")
                        .append(this.selectedTerm.shortYear);

                final boolean hasWhere = this.courseIdField.hasValue() || this.sectField.hasValue()
                        || this.sectionIdField.hasValue() || this.ariesStartDtField.hasValue()
                        || this.ariesEndDtField.hasValue() || this.startDtField.hasValue()
                        || this.examDeleteDtField.hasValue() || this.instrnTypeField.hasValue()
                        || this.instructorField.hasValue() || this.campusField.hasValue()
                        || this.pacingStructureField.hasValue() || this.mtgDaysField.hasValue()
                        || this.classroomId.hasValue() || this.lastStcrsCreatDtField.hasValue()
                        || this.gradingStdField.hasValue() || this.aMinScoreField.hasValue()
                        || this.bMinScoreField.hasValue() || this.cMinScoreField.hasValue()
                        || this.dMinScoreField.hasValue() || this.surveyIdField.hasValue()
                        || this.courseLabelShownField.hasValue() || this.displayScoreField.hasValue()
                        || this.displayGradeScaleField.hasValue()
                        || this.countInMaxCoursesField.hasValue() || this.onlineField.hasValue()
                        || this.bogusField.hasValue() || this.canvasIdField.hasValue()
                        || this.subtermField.hasValue();

                if (hasWhere) {
                    appendWhere(this.courseIdField, true, builder);
                    appendWhere(this.sectField, true, builder);
                    appendWhere(this.sectionIdField, true, builder);
                    appendWhere(this.ariesStartDtField, true, builder);
                    appendWhere(this.ariesEndDtField, true, builder);
                    appendWhere(this.startDtField, true, builder);
                    appendWhere(this.examDeleteDtField, true, builder);
                    appendWhere(this.instrnTypeField, true, builder);
                    appendWhere(this.instructorField, true, builder);
                    appendWhere(this.campusField, true, builder);
                    appendWhere(this.pacingStructureField, true, builder);
                    appendWhere(this.mtgDaysField, true, builder);
                    appendWhere(this.classroomId, true, builder);
                    appendWhere(this.lastStcrsCreatDtField, true, builder);
                    appendWhere(this.gradingStdField, true, builder);
                    appendWhere(this.aMinScoreField, true, builder);
                    appendWhere(this.bMinScoreField, true, builder);
                    appendWhere(this.cMinScoreField, true, builder);
                    appendWhere(this.dMinScoreField, true, builder);
                    appendWhere(this.surveyIdField, true, builder);
                    appendWhere(this.courseLabelShownField, true, builder);
                    appendWhere(this.displayScoreField, true, builder);
                    appendWhere(this.displayGradeScaleField, true, builder);
                    appendWhere(this.countInMaxCoursesField, true, builder);
                    appendWhere(this.onlineField, true, builder);
                    appendWhere(this.bogusField, true, builder);
                    appendWhere(this.canvasIdField, true, builder);
                    appendWhere(this.subtermField, true, builder);
                }

                try (final PreparedStatement ps = this.cache.conn.prepareStatement(builder.toString())) {
                    if (hasWhere) {
                        int index = 1;
                        if (this.courseIdField.hasValue()) {
                            ps.setString(index, this.courseIdField.getStringValue());
                            ++index;
                        }
                        if (this.sectField.hasValue()) {
                            ps.setString(index, this.sectField.getStringValue());
                            ++index;
                        }
                        if (this.sectionIdField.hasValue()) {
                            ps.setString(index, this.sectionIdField.getStringValue());
                            ++index;
                        }
                        if (this.ariesStartDtField.hasValue()) {
                            ps.setDate(index, Date.valueOf(this.ariesStartDtField.getDateTimeValue()));
                            ++index;
                        }
                        if (this.ariesEndDtField.hasValue()) {
                            ps.setDate(index, Date.valueOf(this.ariesEndDtField.getDateTimeValue()));
                            ++index;
                        }
                        if (this.startDtField.hasValue()) {
                            ps.setDate(index, Date.valueOf(this.startDtField.getDateTimeValue()));
                            ++index;
                        }
                        if (this.examDeleteDtField.hasValue()) {
                            ps.setDate(index, Date.valueOf(this.examDeleteDtField.getDateTimeValue()));
                            ++index;
                        }
                        if (this.instrnTypeField.hasValue()) {
                            ps.setString(index, this.instrnTypeField.getStringValue());
                            ++index;
                        }
                        if (this.instructorField.hasValue()) {
                            ps.setString(index, this.instructorField.getStringValue());
                            ++index;
                        }
                        if (this.campusField.hasValue()) {
                            ps.setString(index, this.campusField.getStringValue());
                            ++index;
                        }
                        if (this.pacingStructureField.hasValue()) {
                            ps.setString(index, this.pacingStructureField.getStringValue());
                            ++index;
                        }
                        if (this.mtgDaysField.hasValue()) {
                            ps.setString(index, this.mtgDaysField.getStringValue());
                            ++index;
                        }
                        if (this.classroomId.hasValue()) {
                            ps.setString(index, this.classroomId.getStringValue());
                            ++index;
                        }
                        if (this.lastStcrsCreatDtField.hasValue()) {
                            ps.setDate(index,
                                    Date.valueOf(this.lastStcrsCreatDtField.getDateTimeValue()));
                            ++index;
                        }
                        if (this.gradingStdField.hasValue()) {
                            ps.setString(index, this.gradingStdField.getStringValue());
                            ++index;
                        }
                        if (this.aMinScoreField.hasValue()) {
                            ps.setLong(index, this.aMinScoreField.getLongValue().longValue());
                            ++index;
                        }
                        if (this.bMinScoreField.hasValue()) {
                            ps.setLong(index, this.bMinScoreField.getLongValue().longValue());
                            ++index;
                        }
                        if (this.cMinScoreField.hasValue()) {
                            ps.setLong(index, this.cMinScoreField.getLongValue().longValue());
                            ++index;
                        }
                        if (this.dMinScoreField.hasValue()) {
                            ps.setLong(index, this.dMinScoreField.getLongValue().longValue());
                            ++index;
                        }
                        if (this.surveyIdField.hasValue()) {
                            ps.setString(index, this.surveyIdField.getStringValue());
                            ++index;
                        }
                        if (this.courseLabelShownField.hasValue()) {
                            ps.setString(index, this.courseLabelShownField.getStringValue());
                            ++index;
                        }
                        if (this.displayScoreField.hasValue()) {
                            ps.setString(index, this.displayScoreField.getStringValue());
                            ++index;
                        }
                        if (this.displayGradeScaleField.hasValue()) {
                            ps.setString(index, this.displayGradeScaleField.getStringValue());
                            ++index;
                        }
                        if (this.countInMaxCoursesField.hasValue()) {
                            ps.setString(index, this.countInMaxCoursesField.getStringValue());
                            ++index;
                        }
                        if (this.onlineField.hasValue()) {
                            ps.setString(index, this.onlineField.getStringValue());
                            ++index;
                        }
                        if (this.bogusField.hasValue()) {
                            ps.setString(index, this.bogusField.getStringValue());
                            ++index;
                        }
                        if (this.canvasIdField.hasValue()) {
                            ps.setString(index, this.canvasIdField.getStringValue());
                            ++index;
                        }
                        if (this.subtermField.hasValue()) {
                            ps.setString(index, this.subtermField.getStringValue());
                            ++index;
                        }
                    }

                    try (final ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final GenericRecord rec = new GenericRecord(28);
                            rec.put(this.courseIdField.getName(), rs.getString(1));
                            rec.put(this.sectField.getName(), rs.getString(2));
                            rec.put(this.sectionIdField.getName(), rs.getString(3));
                            final Date dt4 = rs.getDate(4);
                            if (dt4 != null) {
                                rec.put(this.ariesStartDtField.getName(), dt4.toLocalDate());
                            }
                            final Date dt5 = rs.getDate(5);
                            if (dt5 != null) {
                                rec.put(this.ariesEndDtField.getName(), dt5.toLocalDate());
                            }
                            final Date dt6 = rs.getDate(6);
                            if (dt6 != null) {
                                rec.put(this.startDtField.getName(), dt6.toLocalDate());
                            }
                            final Date dt7 = rs.getDate(7);
                            if (dt7 != null) {
                                rec.put(this.examDeleteDtField.getName(), dt7.toLocalDate());
                            }
                            rec.put(this.instrnTypeField.getName(), rs.getString(8));
                            rec.put(this.instructorField.getName(), rs.getString(9));
                            rec.put(this.campusField.getName(), rs.getString(10));
                            rec.put(this.pacingStructureField.getName(), rs.getString(11));
                            rec.put(this.mtgDaysField.getName(), rs.getString(12));
                            rec.put(this.classroomId.getName(), rs.getString(13));
                            final Date dt14 = rs.getDate(14);
                            if (dt14 != null) {
                                rec.put(this.lastStcrsCreatDtField.getName(), dt14.toLocalDate());
                            }
                            rec.put(this.gradingStdField.getName(), rs.getString(15));
                            final long num16 = rs.getLong(16);
                            if (!rs.wasNull()) {
                                rec.put(this.aMinScoreField.getName(), Long.valueOf(num16));
                            }
                            final long num17 = rs.getLong(17);
                            if (!rs.wasNull()) {
                                rec.put(this.bMinScoreField.getName(), Long.valueOf(num17));
                            }
                            final long num18 = rs.getLong(18);
                            if (!rs.wasNull()) {
                                rec.put(this.cMinScoreField.getName(), Long.valueOf(num18));
                            }
                            final long num19 = rs.getLong(19);
                            if (!rs.wasNull()) {
                                rec.put(this.dMinScoreField.getName(), Long.valueOf(num19));
                            }
                            rec.put(this.surveyIdField.getName(), rs.getString(20));
                            rec.put(this.courseLabelShownField.getName(), rs.getString(21));
                            rec.put(this.displayScoreField.getName(), rs.getString(22));
                            rec.put(this.displayGradeScaleField.getName(), rs.getString(23));
                            rec.put(this.countInMaxCoursesField.getName(), rs.getString(24));
                            rec.put(this.onlineField.getName(), rs.getString(25));
                            rec.put(this.bogusField.getName(), rs.getString(26));
                            rec.put(this.canvasIdField.getName(), rs.getString(27));
                            rec.put(this.subtermField.getName(), rs.getString(28));

                            this.queryResult.add(rec);
                        }
                    }

                    if (this.queryResult.isEmpty()) {
                        setStatus("Query Result: (no results)");
                    } else {
                        disableQueryInsert();

                        this.courseIdField.setEditable(false);
                        this.sectField.setEditable(false);
                        this.sectionIdField.setEditable(false);
                        this.ariesStartDtField.setEditable(false);
                        this.ariesEndDtField.setEditable(false);
                        this.startDtField.setEditable(false);
                        this.examDeleteDtField.setEditable(false);
                        this.instrnTypeField.setEditable(false);
                        this.instructorField.setEditable(false);
                        this.campusField.setEditable(false);
                        this.pacingStructureField.setEditable(false);
                        this.mtgDaysField.setEditable(false);
                        this.classroomId.setEditable(false);
                        this.lastStcrsCreatDtField.setEditable(false);
                        this.gradingStdField.setEditable(false);
                        this.aMinScoreField.setEditable(false);
                        this.bMinScoreField.setEditable(false);
                        this.cMinScoreField.setEditable(false);
                        this.dMinScoreField.setEditable(false);
                        this.surveyIdField.setEditable(false);
                        this.courseLabelShownField.setEditable(false);
                        this.displayScoreField.setEditable(false);
                        this.displayGradeScaleField.setEditable(false);
                        this.countInMaxCoursesField.setEditable(false);
                        this.onlineField.setEditable(false);
                        this.bogusField.setEditable(false);
                        this.canvasIdField.setEditable(false);
                        this.subtermField.setEditable(false);

                        setStatus("Query Result: Viewing record 0 of " + this.queryResult.size());

                        this.table.addData(this.queryResult);
                        this.cursor = -1;
                        this.table.setRowSelectionInterval(0, 0);

                        setPrevEnabled(false);
                        setNextEnabled(this.queryResult.size() > 1);
                    }
                }

            } catch (final SQLException ex) {
                Log.warning(ex);
                setStatus("ERROR: " + ex.getMessage());
            }
        }
    }

    /**
     * Called when the selected row in the table view changes.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        final Object src = e.getSource();

        if (src == this.termsList) {
            this.selectedTerm = this.termsList.getSelectedValue();
            if (this.selectedTerm == null) {
                disableQueryInsert();
            } else {
                enableQuery();
            }
        } else if (src == this.table) {

            final int row = this.table.getSelectedRow();

            if (row == -1) {
                // TODO:

            } else if (row < this.queryResult.size() && row != this.cursor) {
                this.cursor = row;
                showRecord(this.queryResult.get(row));

                setNextEnabled(row + 1 < this.queryResult.size());
                setPrevEnabled(row > 0);

                setStatus("Query Result: Viewing record " + (this.cursor + 1) + " of " + this.queryResult.size());
            }
        } else {
            Log.info("Unrecognized source: ", src.getClass().getName());
        }
    }

    /**
     * Shows a result record.
     *
     * @param rec the result record
     */
    private void showRecord(final Map<String, Object> rec) {

        if (rec == null) {
            this.courseIdField.setValue(null);
            this.sectField.setValue(null);
            this.sectionIdField.setValue(null);
            this.ariesStartDtField.setValue(null);
            this.ariesEndDtField.setValue(null);
            this.startDtField.setValue(null);
            this.examDeleteDtField.setValue(null);
            this.instrnTypeField.setValue(null);
            this.instructorField.setValue(null);
            this.campusField.setValue(null);
            this.pacingStructureField.setValue(null);
            this.mtgDaysField.setValue(null);
            this.classroomId.setValue(null);
            this.lastStcrsCreatDtField.setValue(null);
            this.gradingStdField.setValue(null);
            this.aMinScoreField.setValue(null);
            this.bMinScoreField.setValue(null);
            this.cMinScoreField.setValue(null);
            this.dMinScoreField.setValue(null);
            this.surveyIdField.setValue(null);
            this.courseLabelShownField.setValue(null);
            this.displayScoreField.setValue(null);
            this.displayGradeScaleField.setValue(null);
            this.countInMaxCoursesField.setValue(null);
            this.onlineField.setValue(null);
            this.bogusField.setValue(null);
            this.canvasIdField.setValue(null);
            this.subtermField.setValue(null);
        } else {
            this.courseIdField.setValue(rec.get(this.courseIdField.getName()));
            this.sectField.setValue(rec.get(this.sectField.getName()));
            this.sectionIdField.setValue(rec.get(this.sectionIdField.getName()));
            this.ariesStartDtField.setValue(rec.get(this.ariesStartDtField.getName()));
            this.ariesEndDtField.setValue(rec.get(this.ariesEndDtField.getName()));
            this.startDtField.setValue(rec.get(this.startDtField.getName()));
            this.examDeleteDtField.setValue(rec.get(this.examDeleteDtField.getName()));
            this.instrnTypeField.setValue(rec.get(this.instrnTypeField.getName()));
            this.instructorField.setValue(rec.get(this.instructorField.getName()));
            this.campusField.setValue(rec.get(this.campusField.getName()));
            this.pacingStructureField.setValue(rec.get(this.pacingStructureField.getName()));
            this.mtgDaysField.setValue(rec.get(this.mtgDaysField.getName()));
            this.classroomId.setValue(rec.get(this.classroomId.getName()));
            this.lastStcrsCreatDtField.setValue(rec.get(this.lastStcrsCreatDtField.getName()));
            this.gradingStdField.setValue(rec.get(this.gradingStdField.getName()));
            this.aMinScoreField.setValue(rec.get(this.aMinScoreField.getName()));
            this.bMinScoreField.setValue(rec.get(this.bMinScoreField.getName()));
            this.cMinScoreField.setValue(rec.get(this.cMinScoreField.getName()));
            this.dMinScoreField.setValue(rec.get(this.dMinScoreField.getName()));
            this.surveyIdField.setValue(rec.get(this.surveyIdField.getName()));
            this.courseLabelShownField.setValue(rec.get(this.courseLabelShownField.getName()));
            this.displayScoreField.setValue(rec.get(this.displayScoreField.getName()));
            this.displayGradeScaleField.setValue(rec.get(this.displayGradeScaleField.getName()));
            this.countInMaxCoursesField.setValue(rec.get(this.countInMaxCoursesField.getName()));
            this.onlineField.setValue(rec.get(this.onlineField.getName()));
            this.bogusField.setValue(rec.get(this.bogusField.getName()));
            this.canvasIdField.setValue(rec.get(this.canvasIdField.getName()));
            this.subtermField.setValue(rec.get(this.subtermField.getName()));
        }
    }

    /**
     * A table to present admin_hold query result rows.
     */
    static final class JTableCsection extends JTable {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -1045126800811414214L;

        /** The table model. */
        private final CsectionTableModel model;

        /**
         * Constructs a new {@code JTableCsection}.
         */
        JTableCsection() {

            super(new CsectionTableModel(), new CsectionTableColumnModel());

            setFont(Skin.MONO_12_FONT);

            this.model = (CsectionTableModel) getModel();

            final Dimension spacing = getIntercellSpacing();
            setPreferredSize(new Dimension(360 + 6 * spacing.width, 5 * getRowHeight() + 6 * spacing.height));

            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }

        /**
         * Clears the table, removing all rows.
         */
        void clear() {

            int numRows = this.model.getRowCount();
            while (numRows > 0) {
                --numRows;
                this.model.removeRow(numRows);
            }
        }

        /**
         * Adds data from a list of {@code DeadlineRow} records.
         *
         * @param data the data to add
         */
        void addData(final Collection<? extends GenericRecord> data) {

            final int size = data.size();

            for (final GenericRecord rec : data) {
                final String[] row = new String[28];

                row[0] = objToString(rec.get(RawCsection.FLD_COURSE));
                row[1] = objToString(rec.get(RawCsection.FLD_SECT));
                row[2] = objToString(rec.get(RawCsection.FLD_SECTION_ID));
                row[3] = objToString(rec.get(RawCsection.FLD_ARIES_START_DT));
                row[4] = objToString(rec.get(RawCsection.FLD_ARIES_END_DT));
                row[5] = objToString(rec.get(RawCsection.FLD_START_DT));
                row[6] = objToString(rec.get(RawCsection.FLD_EXAM_DELETE_DT));
                row[7] = objToString(rec.get(RawCsection.FLD_INSTRN_TYPE));
                row[8] = objToString(rec.get(RawCsection.FLD_INSTRUCTOR));
                row[9] = objToString(rec.get(RawCsection.FLD_CAMPUS));
                row[10] = objToString(rec.get(RawCsection.FLD_PACING_STRUCTURE));
                row[11] = objToString(rec.get(RawCsection.FLD_MTG_DAYS));
                row[12] = objToString(rec.get(RawCsection.FLD_CLASSROOM_ID));
                row[13] = objToString(rec.get(RawCsection.FLD_LST_STCRS_CREAT_DT));
                row[14] = objToString(rec.get(RawCsection.FLD_GRADING_STD));
                row[15] = objToString(rec.get(RawCsection.FLD_A_MIN_SCORE));
                row[16] = objToString(rec.get(RawCsection.FLD_B_MIN_SCORE));
                row[17] = objToString(rec.get(RawCsection.FLD_C_MIN_SCORE));
                row[18] = objToString(rec.get(RawCsection.FLD_D_MIN_SCORE));
                row[19] = objToString(rec.get(RawCsection.FLD_SURVEY_ID));
                row[20] = objToString(rec.get(RawCsection.FLD_COURSE_LABEL_SHOWN));
                row[21] = objToString(rec.get(RawCsection.FLD_DISPLAY_SCORE));
                row[22] = objToString(rec.get(RawCsection.FLD_DISPLAY_GRADE_SCALE));
                row[23] = objToString(rec.get(RawCsection.FLD_COUNT_IN_MAX_COURSES));
                row[24] = objToString(rec.get(RawCsection.FLD_ONLINE));
                row[25] = objToString(rec.get(RawCsection.FLD_BOGUS));
                row[26] = objToString(rec.get(RawCsection.FLD_CANVAS_ID));
                row[27] = objToString(rec.get(RawCsection.FLD_SUBTERM));

                this.model.addRow(row);
            }
        }

        /**
         * Generates the string representation of an object that could be null.
         *
         * @param obj the object
         * @return the string representation; an empty string if the object is null
         */
        private static String objToString(final Object obj) {

            return obj == null ? CoreConstants.EMPTY : obj.toString();
        }
    }

    /**
     * The table model for the table.
     */
    static final class CsectionTableModel extends DefaultTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = 2070292572327304815L;

        /**
         * Constructs a new {@code CsectionTableModel}.
         */
        CsectionTableModel() {

            super();

            addColumn(RawCsection.FLD_COURSE);
            addColumn(RawCsection.FLD_SECT);
            addColumn(RawCsection.FLD_SECTION_ID);
            addColumn(RawCsection.FLD_ARIES_START_DT);
            addColumn(RawCsection.FLD_ARIES_END_DT);
            addColumn(RawCsection.FLD_START_DT);
            addColumn(RawCsection.FLD_EXAM_DELETE_DT);
            addColumn(RawCsection.FLD_INSTRN_TYPE);
            addColumn(RawCsection.FLD_INSTRUCTOR);
            addColumn(RawCsection.FLD_CAMPUS);
            addColumn(RawCsection.FLD_PACING_STRUCTURE);
            addColumn(RawCsection.FLD_MTG_DAYS);
            addColumn(RawCsection.FLD_CLASSROOM_ID);
            addColumn(RawCsection.FLD_LST_STCRS_CREAT_DT);
            addColumn(RawCsection.FLD_GRADING_STD);
            addColumn(RawCsection.FLD_A_MIN_SCORE);
            addColumn(RawCsection.FLD_B_MIN_SCORE);
            addColumn(RawCsection.FLD_C_MIN_SCORE);
            addColumn(RawCsection.FLD_D_MIN_SCORE);
            addColumn(RawCsection.FLD_SURVEY_ID);
            addColumn(RawCsection.FLD_COURSE_LABEL_SHOWN);
            addColumn(RawCsection.FLD_DISPLAY_SCORE);
            addColumn(RawCsection.FLD_DISPLAY_GRADE_SCALE);
            addColumn(RawCsection.FLD_COUNT_IN_MAX_COURSES);
            addColumn(RawCsection.FLD_ONLINE);
            addColumn(RawCsection.FLD_BOGUS);
            addColumn(RawCsection.FLD_CANVAS_ID);
            addColumn(RawCsection.FLD_SUBTERM);
        }
    }

    /**
     * The column model for the table.
     */
    static final class CsectionTableColumnModel extends DefaultTableColumnModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -2122526045817542947L;

        /**
         * Constructs a new {@code CsectionTableColumnModel}.
         */
        CsectionTableColumnModel() {

            super();

            addColumn(makeColumn(0, RawCsection.FLD_COURSE, 70, 210));
            addColumn(makeColumn(1, RawCsection.FLD_SECT, 50, 150));
            addColumn(makeColumn(2, RawCsection.FLD_SECTION_ID, 50, 150));
            addColumn(makeColumn(3, RawCsection.FLD_ARIES_START_DT, 50, 150));
            addColumn(makeColumn(4, RawCsection.FLD_ARIES_END_DT, 50, 150));
            addColumn(makeColumn(5, RawCsection.FLD_START_DT, 50, 150));
            addColumn(makeColumn(6, RawCsection.FLD_EXAM_DELETE_DT, 50, 150));
            addColumn(makeColumn(7, RawCsection.FLD_INSTRN_TYPE, 50, 150));
            addColumn(makeColumn(8, RawCsection.FLD_INSTRUCTOR, 50, 150));
            addColumn(makeColumn(9, RawCsection.FLD_CAMPUS, 50, 150));
            addColumn(makeColumn(10, RawCsection.FLD_PACING_STRUCTURE, 50, 150));
            addColumn(makeColumn(11, RawCsection.FLD_MTG_DAYS, 50, 150));
            addColumn(makeColumn(12, RawCsection.FLD_CLASSROOM_ID, 50, 150));
            addColumn(makeColumn(13, RawCsection.FLD_LST_STCRS_CREAT_DT, 50, 150));
            addColumn(makeColumn(14, RawCsection.FLD_GRADING_STD, 50, 150));
            addColumn(makeColumn(15, RawCsection.FLD_A_MIN_SCORE, 50, 150));
            addColumn(makeColumn(16, RawCsection.FLD_B_MIN_SCORE, 50, 150));
            addColumn(makeColumn(17, RawCsection.FLD_C_MIN_SCORE, 50, 150));
            addColumn(makeColumn(18, RawCsection.FLD_D_MIN_SCORE, 50, 150));
            addColumn(makeColumn(19, RawCsection.FLD_SURVEY_ID, 50, 150));
            addColumn(makeColumn(20, RawCsection.FLD_COURSE_LABEL_SHOWN, 50, 150));
            addColumn(makeColumn(21, RawCsection.FLD_DISPLAY_SCORE, 50, 150));
            addColumn(makeColumn(22, RawCsection.FLD_DISPLAY_GRADE_SCALE, 50, 150));
            addColumn(makeColumn(23, RawCsection.FLD_COUNT_IN_MAX_COURSES, 50, 150));
            addColumn(makeColumn(24, RawCsection.FLD_ONLINE, 50, 150));
            addColumn(makeColumn(25, RawCsection.FLD_BOGUS, 50, 150));
            addColumn(makeColumn(26, RawCsection.FLD_CANVAS_ID, 50, 150));
            addColumn(makeColumn(27, RawCsection.FLD_SUBTERM, 50, 150));
        }

        /**
         * Makes a column for the results table.
         *
         * @param index    the column index
         * @param label    the header label
         * @param width    the column width
         * @param maxWidth the max column width
         * @return the column object
         */
        private static TableColumn makeColumn(final int index, final String label, final int width,
                                              final int maxWidth) {

            final TableColumn col = new TableColumn(index, width);

            col.setHeaderValue(label);
            col.setResizable(false);
            col.setMinWidth(width);
            col.setMaxWidth(maxWidth);

            return col;
        }
    }
}
