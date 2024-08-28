package dev.mathops.app.canvas;

import dev.mathops.app.canvas.calls.ApiV1Sections;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.app.canvas.calls.ApiV1Courses;
import dev.mathops.app.canvas.data.UserInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The main window for the Canvas Exerciser application.
 */
final class MainWindow extends JFrame implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 1281465014988259200L;

    /** Command categories. */
    private static final String[][] CATEGORIES = {
            {"Account Calendars",
                    "List available account calendars",
                    "Get a single account calendar",
                    "Update a calendar's visibility",
                    "Update many calendars' visibility",
                    "List all account calendars",
                    "Count of all visible account calendars"},
            {"Account Domain Lookups",
                    "Search account domains"},
            {"Account Notifications",
                    "Index of active global notification for the user",
                    "Show a global notification",
                    "Close notification for user",
                    "Create a global notification",
                    "Update a global notification"},
            {"Account Reports",
                    "List Available Reports",
                    "Start a Report",
                    "Index of Reports",
                    "Status of a Report",
                    "Delete a Report"},
            {"Accounts",
                    "List accounts",
                    "Get accounts that admins can manage",
                    "List accounts for course admins",
                    "Get a single account",
                    "Settings",
                    "Permissions",
                    "Get the sub-accounts of an account",
                    "Get the Terms of Service",
                    "Get help links",
                    "Get the manually-created courses sub-account for the domain root account",
                    "List active courses in an account",
                    "Update an account",
                    "Delete a user from the root account"},
            {"Subaccounts",
                    "Create a new sub-account",
                    "Delete a sub-account"},
            {"Accounts (LTI)",
                    "Get account"},
            {"Admins",
                    "Make an account admin",
                    "Remove account admin",
                    "List account admins"},
            {"Analytics",
                    "Get department-level participation data",
                    "Get department-level grade data",
                    "Get department-level statistics",
                    "Get department-level statistics, broken down by subaccount",
                    "Get course-level participation data",
                    "Get course-level assignment data",
                    "Get course-level student summary data",
                    "Get user-in-a-course-level participation data",
                    "Get user-in-a-course-level assignment data",
                    "Get user-in-a-course-level messaging data"},
            {"Announcement External Feeds",
                    "List external feeds",
                    "Create an external feed",
                    "Delete an external feed"},
            {"Announcements",
                    "List announcements"},
            {"API Token Scopes",
                    "List scopes"},
            {"Appointment Groups",
                    "List appointment groups",
                    "Create an appointment group",
                    "Get a single appointment group",
                    "Update an appointment group",
                    "Delete an appointment group",
                    "List user participants",
                    "List student group participants",
                    "Get next appointment"},
            {"Assignment Extensions",
                    "Set extensions for student assignment submissions"},
            {"Assignment Groups",
                    "List assignment groups",
                    "Get an Assignment Group",
                    "Create an Assignment Group",
                    "Edit an Assignment Group",
                    "Destroy an Assignment Group"},
            {"Assignments",
                    "Delete an assignment",
                    "List assignments",
                    "List assignments for user",
                    "Duplicate assignnment",
                    "Get a single assignment",
                    "Create an assignment",
                    "Edit an assignment",
                    "Bulk update assignment dates"},
            {"Assignment Overrides",
                    "List assignment overrides",
                    "Get a single assignment override",
                    "Redirect to the assignment override for a group",
                    "Redirect to the assignment override for a section",
                    "Create an assignment override",
                    "Update an assignment override",
                    "Delete an assignment override",
                    "Batch retrieve overrides in a course",
                    "Batch create overrides in a course",
                    "Batch update overrides in a course"},
            {" Authentication Providers",
                    "List authentication providers",
                    "Add authentication provider",
                    "Update authentication provider",
                    "Get authentication provider",
                    "Delete authentication provider",
                    "Show account auth settings",
                    "Update account auth settings"},
            {"Authentications Log",
                    "Query by login",
                    "Query by account",
                    "Query by user"},
            {"Blackout Dates",
                    "List blackout dates",
                    "Get a single blackout date",
                    "New Blackout Date",
                    "Create Blackout Date",
                    "Update Blackout Date",
                    "Delete Blackout Date",
                    "Update a list of Blackout Dates"},
            {"Blueprint Management",
                    "Get blueprint information",
                    "Get associated course information",
                    "Update associated courses",
                    "Begin a migration to push to associated courses",
                    "Set or remove restrictions on a blueprint course object",
                    "Get unsynced changes"},
            {"Blueprint Course History",
                    "List blueprint migrations",
                    "Show a blueprint migration",
                    "Get migration details"},
            {"Associated Course History",
                    "List blueprint subscriptions",
                    "List blueprint imports",
                    "Show a blueprint import",
                    "Get import details"},
            {"Bookmarks",
                    "List bookmarks",
                    "Create bookmark",
                    "Get bookmark",
                    "Update bookmark",
                    "Delete bookmark"},
            {"Brand Configs",
                    "Get the brand config variables that should be used for this domain"},
            {"Calendar Events",
                    "List calendar events",
                    "List calendar events for a user",
                    "Create a calendar event",
                    "Get a single calendar event or assignment",
                    "Reserve a time slot",
                    "Update a calendar event",
                    "Delete a calendar event",
                    "Save enabled account calendar",
                    "sSet a course timetable",
                    "Get course timetable",
                    "Create or update events directly for a course timetable"},
            {"Collaborations",
                    "List collaborations",
                    "List members of a collaboration",
                    "List potential members"},
            {"CommMessages",
                    "List of CommMessages for a user"},
            {"Communication Channels",
                    "List user communication channels",
                    "Create a communication channel",
                    "Delete a communication channel",
                    "Delete a push notification endpoint"},
            {"Conferences",
                    "List conferences",
                    "List conferences for the current user"},
            {"Content Exports",
                    "List content exports",
                    "Show content export",
                    "Export content"},
            {"Migration Issues",
                    "List migration issues",
                    "Get a migration issue",
                    "Update a migration issue"},
            {"Content Migrations",
                    "List content migrations",
                    "Get a content migration",
                    "Create a content migration",
                    "Update a content migration",
                    "List Migration Systems",
                    "List items for selective import",
                    "Get asset id mapping"},
            {"Content Security Policy Settings",
                    "Get current settings for account or course",
                    "Enable, disable, or clear explicit CSP setting",
                    "Lock or unlock current CSP settings for sub-accounts and courses",
                    "Add an allowed domain to account",
                    "Add multiple allowed domains to an account",
                    "Retrieve reported CSP Violations for account",
                    "Remove a domain from account"},
            {"Content Shares",
                    "Create a content share",
                    "List content shares",
                    "Get unread shares count",
                    "Get content share",
                    "Remove content share",
                    "Add users to content share",
                    "Update a content share"},
            {"Conversations",
                    "List conversations",
                    "Create a conversation",
                    "Get running batches",
                    "Get a single conversation",
                    "Edit a conversation",
                    "Mark all as read",
                    "Delete a conversation",
                    "Add recipients",
                    "Add a message",
                    "Delete a message",
                    "Batch update conversations",
                    "Find recipients",
                    "Unread count"},
            {"Course Audit log",
                    "Query by course",
                    "Query by account"},
            {"Course Pace",
                    "Show a Course pace",
                    "Create a Course pace",
                    "Update a Course pace",
                    "Delete a Course pace"},
            {"Course Quiz Extensions",
                    "Set extensions for student quiz submissions"},
            {"Courses",
                    "List your courses",
                    "List courses for a user",
                    "Get user progress",
                    "Create a new course",
                    "Upload a file",
                    "List students",
                    "List users in course",
                    "List recently logged in students",
                    "Get single user",
                    "Search for content share users",
                    "Preview processed html",
                    "Course activity stream",
                    "Course activity stream summary",
                    "Course TODO items",
                    "Delete/Conclude a course",
                    "Get course settings",
                    "Update course settings",
                    "Return test student for course",
                    "Get a single course",
                    "Update a course",
                    "Update courses",
                    "Reset a course",
                    "Get effective due dates",
                    "Permissions",
                    "Get bulk user progress",
                    "Remove quiz migration alert",
                    "Get course copy status",
                    "Copy course content"},
            {"Custom Gradebook Columns",
                    "List custom gradebook columns",
                    "Create a custom gradebook column",
                    "Update a custom gradebook column",
                    "Delete a custom gradebook column",
                    "Reorder custom columns"},
            {"Custom Gradebook Column Data",
                    "List entries for a column",
                    "Update column data",
                    "Bulk update column data"},
            {"Discussion Topics",
                    "List discussion topics",
                    "Create a new discussion topic",
                    "Update a topicDelete a topic",
                    "Reorder pinned topics",
                    "Update an entry",
                    "Delete an entry",
                    "Get a single topic",
                    "Get the full topic",
                    "Post an entry",
                    "Duplicate discussion topic",
                    "List topic entries",
                    "Post a reply",
                    "List entry replies",
                    "List entries",
                    "Mark topic as read",
                    "Mark topic as unread",
                    "Mark all entries as read",
                    "Mark all entries as unread",
                    "Mark entry as read",
                    "Mark entry as unread",
                    "Rate entrySubscribe to a topic",
                    "Unsubscribe from a topic"},
            {"Enrollment Terms",
                    "Create enrollment term",
                    "Update enrollment term",
                    "Delete enrollment term",
                    "List enrollment terms",
                    "Retrieve enrollment term"},
            {"Enrollments",
                    "List enrollments",
                    "Enrollment by ID",
                    "Enroll a user",
                    "Conclude, deactivate, or delete an enrollment",
                    "Accept Course Invitation",
                    "Reject Course Invitation",
                    "Re-activate an enrollment",
                    "Adds last attended date to student enrollment in course"},
            {"ePortfolios",
                    "Get all ePortfolios for a User",
                    "Get an ePortfolio",
                    "Delete an ePortfolio",
                    "Get ePortfolio Pages",
                    "Moderate an ePortfolio",
                    "Moderate all ePortfolios for a User",
                    "Restore a deleted ePortfolio"},
            {"ePub Exports",
                    "List courses with their latest ePub export",
                    "Create ePub Export",
                    "Show ePub export"},
            {"Error Reports",
                    "Create Error Report"},
            {"External Tools",
                    "List external tools",
                    "Get a sessionless launch url for an external tool",
                    "Get a single external tool",
                    "Create an external tool",
                    "Edit an external tool",
                    "Delete an external tool",
                    "Add tool to RCE Favorites",
                    "Remove tool from RCE Favorites",
                    "Get visible course navigation tools",
                    "Get visible course navigation tools for a single course"},
            {"Favorites",
                    "List favorite courses",
                    "List favorite groups",
                    "Add course to favorites",
                    "Add group to favorites",
                    "Remove course from favorites",
                    "Remove group from favorites",
                    "Reset course favorites",
                    "Reset group favorites"},
            {"Feature Flags",
                    "List features",
                    "List enabled features",
                    "List environment features",
                    "Get feature flag",
                    "Set feature flag",
                    "Remove feature flag"},
            {"Files",
                    "Get quota information",
                    "List files",
                    "Get public inline preview url",
                    "Get file",
                    "Translate file reference",
                    "Update file",
                    "Delete file",
                    "Get icon metadata",
                    "Reset link verifier"},
            {"Folders",
                    "List folders",
                    "List all folders",
                    "Resolve path",
                    "Get folder",
                    "Update folder",
                    "Create folder",
                    "Delete folder",
                    "Upload a file",
                    "Copy a file",
                    "Copy a folder",
                    "Get uploaded media folder for user"},
            {"Usage Rights",
                    "Set usage rights",
                    "Remove usage rights",
                    "List licenses"},
            {"Grade Change Log",
                    "Query by assignment",
                    "Query by course",
                    "Query by student",
                    "Query by grader",
                    "Advanced query"},
            {"Gradebook History",
                    "Days in gradebook history for this course",
                    "Details for a given date in gradebook history for this course",
                    "Lists submissions",
                    "List uncollated submission versions"},
            {"Grading Period Sets",
                    "List grading period sets",
                    "Create a grading period set",
                    "Update a grading period set",
                    "Delete a grading period set"},
            {"Grading Periods",
                    "List grading periods",
                    "Get a single grading period",
                    "Update a single grading period",
                    "Delete a grading period",
                    "Batch update grading periods"},
            {"Grading Standards",
                    "Create a new grading standard",
                    "List the grading standards available in a context",
                    "Get a single grading standard in a context"},
            {"Group Categories",
                    "List group categories for a context",
                    "Get a single group category",
                    "Create a Group Category",
                    "Import category groups",
                    "Update a Group Category",
                    "Delete a Group Category",
                    "List groups in group category",
                    "export groups in and users in category",
                    "List users in group category",
                    "Assign unassigned members"},
            {"Groups",
                    "List your groups",
                    "List the groups available in a context",
                    "Get a single groupCreate a group",
                    "Edit a groupDelete a groupList group's users",
                    "Upload a file",
                    "Preview processed html",
                    "Group activity stream",
                    "Group activity stream summary",
                    "Permissions"},
            {"Group Memberships",
                    "Invite others to a group",
                    "List group memberships",
                    "Get a single group membership",
                    "Create a membership",
                    "Update a membership",
                    "Leave a group"},
            {"History",
                    "List recent history for a user"},
            {"InstAccess tokens",
                    "Create InstAccess token"},
            {"JWTs",
                    "Create JWT",
                    "Refresh JWT"},
            {"Late Policy",
                    "Get a late policy",
                    "Create a late policy",
                    "Patch a late policy"},
            {"Line Items",
                    "Create a Line Item",
                    "Update a Line Item",
                    "Show a Line Item",
                    "List line Items",
                    "Delete a Line Item"},
            {"LiveAssessments",
                    "Create live assessment results",
                    "List live assessment results",
                    "Create or find a live assessment",
                    "List live assessments"},
            {"Logins",
                    "List user logins",
                    "Kickoff password recovery flow",
                    "Create a user login",
                    "Edit a user login",
                    "Delete a user login"},
            {"Media Tracks",
                    "List media tracks for a Media Object",
                    "Update Media Tracks"},
            {"Media Objects",
                    "List Media Objects",
                    "Update Media Object"},
            {"Moderation Set",
                    "List students selected for moderation",
                    "Select students for moderation"},
            {"Provisional Grades",
                    "Bulk select provisional grades",
                    "Show provisional grade status for a student",
                    "Select provisional grade",
                    "Publish provisional grades for an assignment"},
            {"Anonymous Provisional Grades",
                    "Show provisional grade status for a student"},
            {"Modules List",
                    "List modules",
                    "Show module",
                    "Create a module",
                    "Update a module",
                    "Delete module",
                    "Re-lock module progressions"},
            {"Module Items",
                    "List module items",
                    "Show module item",
                    "Create a module item",
                    "Update a module item",
                    "Select a mastery path",
                    "Delete module item",
                    "Mark module item as done/not done",
                    "Get module item sequence",
                    "Mark module item read"},
            {"Names and Role",
                    "List Course Memberships",
                    "List Group Memberships"},
            {"New Quizzes",
                    "Get a new quiz",
                    "List new quizzes",
                    "Create a new quiz",
                    "Update a single quiz",
                    "Delete a new quiz"},
            {"New Quiz Items",
                    "Get a quiz item",
                    "List quiz items",
                    "Create a quiz item",
                    "Update a quiz item",
                    "Delete a quiz item"},
            {"Notification Preferences",
                    "List preferences",
                    "List of preference categories",
                    "Get a preference",
                    "Update a preference",
                    "Update preferences by category",
                    "Update multiple preferences"},
            {"Originality Reports",
                    "Create an Originality Report",
                    "Edit an Originality Report",
                    "Show an Originality Report"},
            {"Outcome Groups",
                    "Redirect to root outcome group for context",
                    "Get all outcome groups for context",
                    "Get all outcome links for context",
                    "Show an outcome group",
                    "Update an outcome group",
                    "Delete an outcome group",
                    "List linked outcomes",
                    "Create/link an outcome",
                    "Unlink an outcome",
                    "List subgroups",
                    "Create a subgroup",
                    "Import an outcome group"},
            {"Outcome Imports",
                    "Import Outcomes",
                    "Get Outcome import status",
                    "Get IDs of outcome groups created after successful import"},
            {"Outcome Results",
                    "Get outcome results",
                    "Get outcome result rollups"},
            {"Outcomes",
                    "Show an outcome",
                    "Update an outcome",
                    "Get aligned assignments for an outcome in a course for a particular student"},
            {"Pages",
                    "Show front page",
                    "Duplicate page",
                    "Update/create front page",
                    "List pages",
                    "Create page",
                    "Show page",
                    "Update/create page",
                    "Delete page",
                    "List revisions",
                    "Show revision",
                    "Revert to revision"},
            {"Peer Reviews",
                    "Get all Peer Reviews",
                    "Create Peer Review",
                    "Delete Peer Review"},
            {"Plagiarism Detection Platform Assignments",
                    "Get a single assignment (lti)"},
            {"Plagiarism Detection Platform Users",
                    "Get a single user (lti)",
                    "Get all users in a group (lti)"},
            {"Plagiarism Detection Submissions",
                    "Get a single submission",
                    "Get the history of a single submission"},
            {"Planner",
                    "List planner items"},
            {"Planner Notes",
                    "List planner notes",
                    "Show a planner note",
                    "Update a planner note",
                    "Create a planner note",
                    "Delete a planner note"},
            {"Planner Overrides",
                    "List planner overrides",
                    "Show a planner override",
                    "Update a planner override",
                    "Create a planner override",
                    "Delete a planner override"},
            {"Poll Sessions",
                    "List poll sessions for a poll",
                    "Get the results for a single poll session",
                    "Create a single poll session",
                    "Update a single poll session",
                    "Delete a poll session",
                    "Open a poll session",
                    "Close an opened poll session",
                    "List opened poll sessions",
                    "List closed poll sessions"},
            {"PollChoices",
                    "List poll choices in a poll",
                    "Get a single poll choice",
                    "Create a single poll choice",
                    "Update a single poll choice",
                    "Delete a poll choice"},
            {"Polls",
                    "List polls",
                    "Get a single poll",
                    "Create a single poll",
                    "Update a single poll",
                    "Delete a poll"},
            {"PollSubmissions",
                    "Get a single poll submission",
                    "Create a single poll submission"},
            {"Proficiency Ratings",
                    "Create/update proficiency ratings",
                    "Get proficiency ratings"},
            {"Progress",
                    "Query progress",
                    "Cancel progress",
                    "Query progress (lti)"},
            {"Public JWK",
                    "Update Public JWK"},
            {"Quiz Assignment Overrides",
                    "Retrieve assignment-overridden dates for Classic Quizzes",
                    "Retrieve assignment-overridden dates for New Quizzes"},
            {"Quiz Extensions",
                    "Set extensions for student quiz submissions"},
            {"Quiz IP Filters",
                    "Get available quiz IP filters"},
            {"Quiz Question Groups",
                    "Get a single quiz group",
                    "Create a question group",
                    "Update a question group",
                    "Delete a question group",
                    "Reorder question groups"},
            {"Quiz Questions",
                    "List questions in a quiz or a submission",
                    "Get a single quiz question",
                    "Create a single quiz question",
                    "Update an existing quiz question",
                    "Delete a quiz question"},
            {"Quiz Reports",
                    "Retrieve all quiz reports",
                    "Create a quiz report",
                    "Get a quiz report",
                    "Abort the generation of a report, or remove a previously generated one"},
            {"Quiz Statistics",
                    "Fetching the latest quiz statistics"},
            {"Quiz Submission Events",
                    "Submit captured events",
                    "Retrieve captured events"},
            {"Quiz Submission Files",
                    "Upload a file"},
            {"Quiz Submission Questions",
                    "Get all quiz submission questions",
                    "Answering questions",
                    "Get a formatted student numerical answer",
                    "Flagging a question",
                    "Unflagging a question"},
            {"Quiz Submission User List",
                    "Send a message to unsubmitted or submitted users for the quiz"},
            {"Quiz Submissions",
                    "Get all quiz submissions",
                    "Get the quiz submission",
                    "Get a single quiz submission",
                    "Create the quiz submission (start a quiz-taking session)",
                    "Update student question scores and comments",
                    "Complete the quiz submission (turn it in)",
                    "Get current quiz submission times"},
            {"Quizzes",
                    "List quizzes in a course",
                    "Get a single quiz",
                    "Create a quiz",
                    "Edit a quiz",
                    "Delete a quiz",
                    "Reorder quiz items",
                    "Validate quiz access code"},
            {"Result",
                    "Show a collection of Results",
                    "Show a Result"},
            {"Roles",
                    "List roles",
                    "Get a single role",
                    "Create a new role",
                    "Deactivate a role",
                    "Activate a role",
                    "Update a role"},
            {"Rubrics",
                    "Create a single rubric",
                    "Update a single rubric",
                    "Delete a single rubric",
                    "List rubricsGet a single rubric"},
            {"RubricAssessments",
                    "Create a single rubric assessment",
                    "Update a single rubric assessment",
                    "Delete a single rubric assessment"},
            {"RubricAssociations",
                    "Create a Rubric",
                    "Association",
                    "Update a RubricAssociation",
                    "Delete a RubricAssociation"},
            {"Score",
                    "Create a Score"},
            {"Search",
                    "Find recipients",
                    "List all courses"},
            {"Sections",
                    "List course sections",
                    "Create course section",
                    "Cross-list a Section",
                    "De-cross-list a Section",
                    "Edit a section",
                    "Get section information",
                    "Delete a section"},
            {"Services",
                    "Get Kaltura config",
                    "Start Kaltura session"},
            {"Shared Brand Configs",
                    "Share a BrandConfig (Theme)",
                    "Update a shared theme",
                    "Un-share a BrandConfig (Theme)"},
            {"SIS Import Errors",
                    "Get SIS import error list"},
            {"SIS Imports",
                    "Get SIS import list",
                    "Get the current importing SIS import",
                    "Import SIS dataGet SIS import status",
                    "Restore workflow_states of SIS imported items",
                    "Abort SIS import",
                    "Abort all pending SIS imports"},
            {"SIS Integration",
                    "Retrieve assignments enabled for grade export to SIS",
                    "Disable assignments currently enabled for grade export to SIS"},
            {"Submission Comments",
                    "Edit a submission comment",
                    "Delete a submission comment",
                    "Upload a file"},
            {"Submissions",
                    "Submit an assignment",
                    "List assignment submissions",
                    "List submissions for multiple assignments",
                    "Get a single submission",
                    "Get a single submission by anonymous id",
                    "Upload a file",
                    "Grade or comment on a submission",
                    "Grade or comment on a submission by anonymous id",
                    "List gradeable students",
                    "List multiple assignments gradeable students",
                    "Grade or comment on multiple submissions",
                    "Mark submission as read",
                    "Mark submission as unread",
                    "Mark bulk submissions as read",
                    "Mark submission item as read",
                    "Get rubric assessments read state",
                    "Mark rubric assessments as read",
                    "Get document annotations read state",
                    "Mark document annotations as read",
                    "Submission Summary"},
            {"Tabs",
                    "List available tabs for a course or group",
                    "Update a tab for a course"},
            {"User Observees",
                    "List observees",
                    "List observers",
                    "Add an observee with credentials",
                    "Show an observee",
                    "Show an observer",
                    "Add an observee",
                    "Remove an observee",
                    "Create observer pairing code"},
            {"Users",
                    "List users in account",
                    "List the activity stream",
                    "Activity stream summary",
                    "List the TODO items",
                    "List counts for todo items",
                    "List upcoming assignments, calendar events",
                    "List Missing Submissions",
                    "Hide a stream item",
                    "Hide all stream items",
                    "Upload a file",
                    "Show user details",
                    "Self register a user",
                    "Update user settings",
                    "Get custom colors",
                    "Get custom color",
                    "Update custom color",
                    "Get dashboard positions",
                    "Update dashboard positions",
                    "Edit a user",
                    "Terminate all user sessions",
                    "Merge user into another user",
                    "Split merged users into separate users",
                    "Get a Pandata Events jwt token and its expiration date",
                    "Get a users most recently graded submissions",
                    "Get user profile",
                    "List avatar options",
                    "List user page views"},
            {"Custom Data Store",
                    "Store custom data",
                    "Load custom data",
                    "Delete custom data"},
            {"Course Nicknames",
                    "List course nicknames",
                    "Get course nickname",
                    "Set course nickname",
                    "Remove course nickname",
                    "Clear course nicknames"},
            {"Webhooks Subscriptions for Plagiarism Platform",
                    "Create a Webhook Subscription",
                    "Delete a Webhook Subscription",
                    "Show a single Webhook Subscription",
                    "Update a Webhook Subscription",
                    "List all Webhook Subscription for a tool proxy"}};

    /** The Canvas API object. */
    private final CanvasApi api;

    /** The content panel. */
    private final JPanel content;

    /** The center panel. */
    private final JPanel centerPanel;

    /** The card layout for the center panel. */
    private final CardLayout centerCards;

    /** The currently visible center panel; null if none. */
    private JPanel currentCenter = null;

    /**
     * Constructs a new {@code MainWindow}. Called in the AWT event thread.
     *
     * @param theApi      the Canvas API object
     * @param theUserInfo the canvas user object
     */
    MainWindow(final CanvasApi theApi, final UserInfo theUserInfo) {

        super(Res.get(Res.TITLE));

        this.api = theApi;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final String name = theUserInfo.getDisplayName();
        final String windowTitle = SimpleBuilder.concat("Connected to Canvas [",
                this.api.getCanvasHost(), "] as [", name, "]");
        setTitle(windowTitle);

        this.content = new JPanel(new StackedBorderLayout());
        this.content.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setContentPane(this.content);

        final JButton dummy = new JButton("Foo");
        final int unitStep = dummy.getPreferredSize().height;

        // Left side - list of API categories

        final JPanel leftPane = makeLeftPanel();
        final JScrollPane leftScroll = new JScrollPane(leftPane);
        final JScrollBar leftVScroll = leftScroll.getVerticalScrollBar();
        leftScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        leftVScroll.setUnitIncrement(unitStep);
        leftVScroll.setBlockIncrement(unitStep * 10);

        this.content.add(leftScroll, StackedBorderLayout.WEST);

        // Center - list of calls for selected category

        this.centerCards = new CardLayout();
        this.centerPanel = makeCenterPanel();
        this.content.add(this.centerPanel, StackedBorderLayout.WEST);

        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice selected = env.getDefaultScreenDevice();
        final Rectangle bounds = selected.getDefaultConfiguration().getBounds();
        this.content.setPreferredSize(new Dimension(bounds.width * 6 / 7, bounds.height * 6 / 7));
        pack();

        final Dimension size = getSize();
        setLocation(bounds.x + (bounds.width - size.width) / 2, bounds.y + (bounds.height - size.height) / 2);
    }

    /**
     * Creates the left-hand panel.
     *
     * @return the panel
     */
    private JPanel makeLeftPanel() {

        final JPanel panel = new JPanel(new StackedBorderLayout());

        for (final String[] category : CATEGORIES) {
            addCategoryButton(panel, category[0]);
        }

        return panel;
    }

    /**
     * Adds a button for a command category.
     *
     * @param panel the panel to which to add the button
     * @param label the button label
     */
    private void addCategoryButton(final JPanel panel, final String label) {

        final JButton btn = new JButton(label);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setActionCommand(label);
        btn.addActionListener(this);
        panel.add(btn, StackedBorderLayout.NORTH);
    }

    /**
     * Creates the center panel.
     *
     * @return the panel
     */
    private JPanel makeCenterPanel() {

        final JPanel panel = new JPanel(this.centerCards);

        for (final String[] category : CATEGORIES) {
            addCard(panel, category);
        }

        return panel;
    }

    /**
     * Adds a card panel for a command to the center panel.
     *
     * @param panel    the panel to which to add the card
     * @param category the category with commands
     */
    private void addCard(final JPanel panel, final String[] category) {

        final JPanel card = new JPanel(new StackedBorderLayout());

        final String cat = category[0];
        panel.add(card, cat);

        final int len = category.length;
        if (len > 1) {
            for (int i = 1; i < len; ++i) {
                final String cmd = category[i];
                final JButton btn = new JButton(cmd);
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setActionCommand(cat + "|" + cmd);
                btn.addActionListener(this);
                card.add(btn, StackedBorderLayout.NORTH);
            }
        } else {
            card.add(new JLabel(category[0]), StackedBorderLayout.NORTH);
        }
    }

    /**
     * Called on the AWT event dispatch thread when a button is pressed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        boolean searching = true;
        for (final String[] category : CATEGORIES) {
            if (category[0].equals(cmd)) {
                searching = false;
                this.centerCards.show(this.centerPanel, cmd);
            }
        }

        if (searching) {
            // Search for an API command that the command matches
            final int divider = cmd.indexOf('|');
            if (divider == -1) {
                Log.warning("Unexpected command: " + cmd);
            } else {
                final String catPart = cmd.substring(0, divider);
                final String actPart = cmd.substring(divider + 1);

                for (final String[] category : CATEGORIES) {
                    if (category[0].equals(catPart)) {

                        for (final String test : category) {
                            if (test.equals(actPart)) {
                                fireAction(catPart, actPart);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fires an action.
     *
     * @param category the category label
     * @param action   the action label
     */
    private void fireAction(final String category, final String action) {

        Log.warning("C:", category, "  A:", action);

        if (this.currentCenter != null) {
            this.content.remove(this.currentCenter);
            this.currentCenter = null;
        }

        if ("Courses".equals(category)) {
            if ("List your courses".equals(action)) {
                final ApiV1Courses pane = new ApiV1Courses(this.api);

                this.content.add(pane, StackedBorderLayout.CENTER);
                this.currentCenter = pane;
                this.content.invalidate();
                this.content.revalidate();
                repaint();
            }
        } else if ("Sections".equals(category)) {
            if ("List course sections".equals(action)) {
                final ApiV1Sections pane = new ApiV1Sections(this.api);

                this.content.add(pane, StackedBorderLayout.CENTER);
                this.currentCenter = pane;
                this.content.invalidate();
                this.content.revalidate();
                repaint();
            }
        }
    }
}
