package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.ESchema;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Color;
import java.awt.Dimension;

/*
 * A panel that displays a tree of the various schemas and tables in the database system.*
 */
final class SchemaTableTree extends JPanel {

    /** The tree selection model. */
    private final TreeSelectionModel selectionModel;

    /**
     * Constructs a new {@code SchemaTableTree}.
     *
     * @param listener the listener to be notified when the tree selection changes
     */
    SchemaTableTree(final TreeSelectionListener listener) {

        super(new StackedBorderLayout());

        setPreferredSize(new Dimension(200, 200));

        final Color background = getBackground();
        final boolean isLight = InterfaceUtils.isLight(background);
        final Color accent = InterfaceUtils.createAccentColor(background, isLight);

        final Border rightLine = BorderFactory.createMatteBorder(0, 0, 0, 1, accent);
        setBorder(rightLine);

        final TreeNode root = buildTreeNodes();
        final TreeModel treeModel = new DefaultTreeModel(root);

        final JTree tree = new JTree(treeModel);
        this.selectionModel = tree.getSelectionModel();
        this.selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(listener);

        final JScrollPane scroll = new JScrollPane(tree);
        add(scroll, StackedBorderLayout.CENTER);
    }

    /**
     * Gets the selected schema and table name.
     *
     * @return the selected schema/table object; null if none is selected
     */
    public SchemaTable getSelection() {

        SchemaTable result = null;

        final TreePath path = this.selectionModel.getSelectionPath();
        if (path != null) {
            final Object last = path.getLastPathComponent();
            if (last instanceof final DefaultMutableTreeNode node) {
                final Object user = node.getUserObject();
                if (user instanceof final SchemaTable schemaTable) {
                    result = schemaTable;
                }
            }
        }

        return result;
    }

    /**
     * Creates the tree of nodes.
     *
     * @return the root node
     */
    private static TreeNode buildTreeNodes() {

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        final MutableTreeNode legacySchema = buildLegacySchema();
        root.add(legacySchema);
        final MutableTreeNode systemSchema = buildMathOpsSchema();
        root.add(systemSchema);
        final MutableTreeNode mainSchema = buildMainSchema();
        root.add(mainSchema);
        final MutableTreeNode externSchema = buildExternSchema();
        root.add(externSchema);
        final MutableTreeNode analyticsSchema = buildAnalyticsSchema();
        root.add(analyticsSchema);
        final MutableTreeNode termSchema = buildTermSchema();
        root.add(termSchema);
        final MutableTreeNode liveSchema = buildLiveSchema();
        root.add(liveSchema);
        final MutableTreeNode odsSchema = buildOdsSchema();
        root.add(odsSchema);

        return root;
    }

    /**
     * Creates the tree nodes representing the "Legacy" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildLegacySchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("Legacy Schema");

        final DefaultMutableTreeNode adminHold = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "admin_hold"));
        schema.add(adminHold);
        final DefaultMutableTreeNode applicant = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "applicant"));
        schema.add(applicant);
        final DefaultMutableTreeNode calcs = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "calcs"));
        schema.add(calcs);
        final DefaultMutableTreeNode campusCalendar = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "campus_calendar"));
        schema.add(campusCalendar);
        final DefaultMutableTreeNode challengeFee = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "challenge_fee"));
        schema.add(challengeFee);
        final DefaultMutableTreeNode clientPc = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "client_pc"));
        schema.add(clientPc);
        final DefaultMutableTreeNode cohort = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "cohort"));
        schema.add(cohort);
        final DefaultMutableTreeNode course = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "course"));
        schema.add(course);
        final DefaultMutableTreeNode csection = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "csection"));
        schema.add(csection);
        final DefaultMutableTreeNode cunit = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "cunit"));
        schema.add(cunit);
        final DefaultMutableTreeNode cuobjective = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "cuobjective"));
        schema.add(cuobjective);
        final DefaultMutableTreeNode cusection = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "cusection"));
        schema.add(cusection);
        final DefaultMutableTreeNode discipline = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "discipline"));
        schema.add(discipline);
        final DefaultMutableTreeNode dontSubmit = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "dont_submit"));
        schema.add(dontSubmit);
        final DefaultMutableTreeNode dupRegistr = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "dup_registr"));
        schema.add(dupRegistr);
        final DefaultMutableTreeNode etext = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "etext"));
        schema.add(etext);
        final DefaultMutableTreeNode etextCourse = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "etext_course"));
        schema.add(etextCourse);
        final DefaultMutableTreeNode etextKey = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "etext_key"));
        schema.add(etextKey);
        final DefaultMutableTreeNode exam = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "exam"));
        schema.add(exam);
        final DefaultMutableTreeNode examqa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "examqa"));
        schema.add(examqa);
        final DefaultMutableTreeNode exceptStu = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "except_stu"));
        schema.add(exceptStu);
        final DefaultMutableTreeNode ffrTrns = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "ffr_trns"));
        schema.add(ffrTrns);
        final DefaultMutableTreeNode finalCroll = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "final_croll"));
        schema.add(finalCroll);
        final DefaultMutableTreeNode gradeRoll = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "grade_roll"));
        schema.add(gradeRoll);
        final DefaultMutableTreeNode gradingStd = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "grading_std"));
        schema.add(gradingStd);
        final DefaultMutableTreeNode highSchools = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "high_schools"));
        schema.add(highSchools);
        final DefaultMutableTreeNode holdType = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "hold_type"));
        schema.add(holdType);
        final DefaultMutableTreeNode lesson = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "lesson"));
        schema.add(lesson);
        final DefaultMutableTreeNode lessonComponent = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "lesson_component"));
        schema.add(lessonComponent);
        final DefaultMutableTreeNode logins = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "logins"));
        schema.add(logins);
        final DefaultMutableTreeNode milestone = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "milestone"));
        schema.add(milestone);
        final DefaultMutableTreeNode milestone_appeal = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "milestone_appeal"));
        schema.add(milestone_appeal);
        final DefaultMutableTreeNode mpe = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "mpe"));
        schema.add(mpe);
        final DefaultMutableTreeNode mpecrDenied = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "mpecr_denied"));
        schema.add(mpecrDenied);
        final DefaultMutableTreeNode mpeCredit = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "mpe_credit"));
        schema.add(mpeCredit);
        final DefaultMutableTreeNode mpe_log = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "mpe_log"));
        schema.add(mpe_log);
        final DefaultMutableTreeNode mpscorequeue = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "mpscorequeue"));
        schema.add(mpscorequeue);
        final DefaultMutableTreeNode msg = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "msg"));
        schema.add(msg);
        final DefaultMutableTreeNode msgLookup = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "msg_lookup"));
        schema.add(msgLookup);
        final DefaultMutableTreeNode newstu = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "newstu"));
        schema.add(newstu);
        final DefaultMutableTreeNode paceAppeals = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "pace_appeals"));
        schema.add(paceAppeals);
        final DefaultMutableTreeNode pacingRules = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "pacing_rules"));
        schema.add(pacingRules);
        final DefaultMutableTreeNode pacingStructure = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "pacing_structure"));
        schema.add(pacingStructure);
        final DefaultMutableTreeNode parameters = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "parameters"));
        schema.add(parameters);
        final DefaultMutableTreeNode pendingExam = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "pending_exam"));
        schema.add(pendingExam);
        final DefaultMutableTreeNode plcFee = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "plc_fee"));
        schema.add(plcFee);
        final DefaultMutableTreeNode prereq = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "prereq"));
        schema.add(prereq);
        final DefaultMutableTreeNode remoteMpe = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "remote_mpe"));
        schema.add(remoteMpe);
        final DefaultMutableTreeNode resource = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "resource"));
        schema.add(resource);
        final DefaultMutableTreeNode semesterCalendar = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "semester_calendar"));
        schema.add(semesterCalendar);
        final DefaultMutableTreeNode specialStus = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "special_stus"));
        schema.add(specialStus);
        final DefaultMutableTreeNode stchallenge = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stchallenge"));
        schema.add(stchallenge);
        final DefaultMutableTreeNode stchallengeqa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stchallengeqa"));
        schema.add(stchallengeqa);
        final DefaultMutableTreeNode stcourse = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stcourse"));
        schema.add(stcourse);
        final DefaultMutableTreeNode stcunit = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stcunit"));
        schema.add(stcunit);
        final DefaultMutableTreeNode stcuobjective = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stcuobjective"));
        schema.add(stcuobjective);
        final DefaultMutableTreeNode stetext = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stetext"));
        schema.add(stetext);
        final DefaultMutableTreeNode stexam = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stexam"));
        schema.add(stexam);
        final DefaultMutableTreeNode sthomework = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "sthomework"));
        schema.add(sthomework);
        final DefaultMutableTreeNode sthwqa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "sthwqa"));
        schema.add(sthwqa);
        final DefaultMutableTreeNode stmathplan = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stmathplan"));
        schema.add(stmathplan);
        final DefaultMutableTreeNode stmilestone = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stmilestone"));
        schema.add(stmilestone);
        final DefaultMutableTreeNode stmpe = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stmpe"));
        schema.add(stmpe);
        final DefaultMutableTreeNode stmpeqa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stmpeqa"));
        schema.add(stmpeqa);
        final DefaultMutableTreeNode stmsg = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stmsg"));
        schema.add(stmsg);
        final DefaultMutableTreeNode stpaceSummary = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stpace_summary"));
        schema.add(stpaceSummary);
        final DefaultMutableTreeNode stqa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stqa"));
        schema.add(stqa);
        final DefaultMutableTreeNode stresource = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stresource"));
        schema.add(stresource);
        final DefaultMutableTreeNode stsurveyqa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stsurveyqa"));
        schema.add(stsurveyqa);
        final DefaultMutableTreeNode stterm = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stterm"));
        schema.add(stterm);
        final DefaultMutableTreeNode student = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "student"));
        schema.add(student);
        final DefaultMutableTreeNode stvisit = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "stvisit"));
        schema.add(stvisit);
        final DefaultMutableTreeNode surveyqa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "surveyqa"));
        schema.add(surveyqa);
        final DefaultMutableTreeNode term = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "term"));
        schema.add(term);
        final DefaultMutableTreeNode testingCenters = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "testing_centers"));
        schema.add(testingCenters);
        final DefaultMutableTreeNode treePath = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "tree_path"));
        schema.add(treePath);
        final DefaultMutableTreeNode userClearance = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "user_clearance"));
        schema.add(userClearance);
        final DefaultMutableTreeNode users = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "users"));
        schema.add(users);
        final DefaultMutableTreeNode whichDb = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "which_db"));
        schema.add(whichDb);
        final DefaultMutableTreeNode zipCode = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LEGACY, "zip_code"));
        schema.add(zipCode);

        return schema;
    }

    /**
     * Creates the tree nodes representing the "MathOps" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildMathOpsSchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("MathOps Schema");

        return schema;
    }

    /**
     * Creates the tree nodes representing the "Main" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildMainSchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("Main Schema");

        final DefaultMutableTreeNode courseSurvey = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "course_survey"));
        schema.add(courseSurvey);
        final DefaultMutableTreeNode courseSurveyItem = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "course_survey_item"));
        schema.add(courseSurveyItem);
        final DefaultMutableTreeNode courseSurveyItemChoice = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "course_survey_item_choice"));
        schema.add(courseSurveyItemChoice);
        final DefaultMutableTreeNode facility = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "facility"));
        schema.add(facility);
        final DefaultMutableTreeNode facilityClosure = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "facility_closure"));
        schema.add(facilityClosure);
        final DefaultMutableTreeNode facilityHours = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "facility_hours"));
        schema.add(facilityHours);
        final DefaultMutableTreeNode ltiRegistration = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "lti_registration"));
        schema.add(ltiRegistration);
        final DefaultMutableTreeNode standardAssignment = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "standard_assignment"));
        schema.add(standardAssignment);
        final DefaultMutableTreeNode standardsCourse = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "standards_course"));
        schema.add(standardsCourse);
        final DefaultMutableTreeNode standardsCourseModule = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "standards_course_module"));
        schema.add(standardsCourseModule);
        final DefaultMutableTreeNode standardsCourseStandard = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "standards_course_standard"));
        schema.add(standardsCourseStandard);
        final DefaultMutableTreeNode whichDb = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.MAIN, "which_db"));
        schema.add(whichDb);

        return schema;
    }

    /**
     * Creates the tree nodes representing the "Extern" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildExternSchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("Extern Schema");

        return schema;
    }

    /**
     * Creates the tree nodes representing the "Analytics" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildAnalyticsSchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("Analytics Schema");

        return schema;
    }

    /**
     * Creates the tree nodes representing the "Term" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildTermSchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("Term Schema");

        final DefaultMutableTreeNode courseSectionSurvey = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "course_section_survey"));
        schema.add(courseSectionSurvey);
        final DefaultMutableTreeNode courseSurveyResponse = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "course_survey_response"));
        schema.add(courseSurveyResponse);
        final DefaultMutableTreeNode courseSurveyResponseItemChoice = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "course_survey_response_item_choice"));
        schema.add(courseSurveyResponseItemChoice);
        final DefaultMutableTreeNode courseSurveyResponseItemText = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "course_survey_response_item_text"));
        schema.add(courseSurveyResponseItemText);
        final DefaultMutableTreeNode ltiContext = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "lti_context"));
        schema.add(ltiContext);
        final DefaultMutableTreeNode ltiContextCourseSection = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "lti_context_course_section"));
        schema.add(ltiContextCourseSection);
        final DefaultMutableTreeNode standardAssignmentAttempt = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "standard_assignment_attempt"));
        schema.add(standardAssignmentAttempt);
        final DefaultMutableTreeNode standardAssignmentAttemptQa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "standard_assignment_attempt_qa"));
        schema.add(standardAssignmentAttemptQa);
        final DefaultMutableTreeNode standardsCourseGradingSystem = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "standards_course_grading_system"));
        schema.add(standardsCourseGradingSystem);
        final DefaultMutableTreeNode standardsCourseSection = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "standards_course_section"));
        schema.add(standardsCourseSection);
        final DefaultMutableTreeNode stabdardsMilestone = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "standards_milestone"));
        schema.add(stabdardsMilestone);
        final DefaultMutableTreeNode studentCourseMastery = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "student_course_mastery"));
        schema.add(studentCourseMastery);
        final DefaultMutableTreeNode studentPreference = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "student_preference"));
        schema.add(studentPreference);
        final DefaultMutableTreeNode studentStandardsMilestone = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "student_standards_milestone"));
        schema.add(studentStandardsMilestone);
        final DefaultMutableTreeNode whichDb = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.TERM, "which_db"));
        schema.add(whichDb);

        return schema;
    }

    /**
     * Creates the tree nodes representing the "Live" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildLiveSchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("Live Schema");

        final DefaultMutableTreeNode liveRegFa = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LIVE, "live_reg_fa"));
        schema.add(liveRegFa);
        final DefaultMutableTreeNode liveRegSp = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LIVE, "live_reg_sp"));
        schema.add(liveRegSp);
        final DefaultMutableTreeNode liveRegSm = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LIVE, "live_reg_sm"));
        schema.add(liveRegSm);
        final DefaultMutableTreeNode liveStudent = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LIVE, "live_student"));
        schema.add(liveStudent);
        final DefaultMutableTreeNode liveTransferCredit = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LIVE, "live_transfer_credit"));
        schema.add(liveTransferCredit);
        final DefaultMutableTreeNode liveCsuCredit = new DefaultMutableTreeNode(
                new SchemaTable(ESchema.LIVE, "live_csu_credit"));
        schema.add(liveCsuCredit);

        return schema;
    }

    /**
     * Creates the tree nodes representing the "Ods" schema.
     *
     * @return the tree node representing the schema
     */
    private static MutableTreeNode buildOdsSchema() {

        final DefaultMutableTreeNode schema = new DefaultMutableTreeNode("Ods Schema");

        return schema;
    }
}
