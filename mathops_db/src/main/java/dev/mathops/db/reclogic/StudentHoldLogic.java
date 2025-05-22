package dev.mathops.db.reclogic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.ESchema;
import dev.mathops.db.rec.StudentHoldRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class to work with stu_unit_mastery records.
 */
public abstract class StudentHoldLogic implements IRecLogic<StudentHoldRec> {

    /** Hold messages directed to staff/administrators. */
    private static final Map<String, String> STAFF_HOLD_MSG = new HashMap<>(40);

    /** Hold messages directed to students. */
    private static final Map<String, String> STUDENT_HOLD_MSG = new HashMap<>(40);

    /** Empty list to return when there are no records. */
    private static final List<StudentHoldRec> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>(0));

    static {
        STAFF_HOLD_MSG.put("01",
                "unauthorized grading option selected by student - have student talk w/ director");
        STAFF_HOLD_MSG.put("02",
                "warn student they are registered for more than recommended # of courses in SM");
        STAFF_HOLD_MSG.put("03",
                "registered for both course and placement section -refer to a director!");
        STAFF_HOLD_MSG.put("04",
                "can never re-register for course -- see discipline table for details");
        STAFF_HOLD_MSG.put("05",
                "deferred F issued to student -- see discipline table for details");
        STAFF_HOLD_MSG.put("06",
                "discretionary administrative hold -- have student speak with a director");
        STAFF_HOLD_MSG.put("07",
                "Profile info specifies different registration format than is in STUDENT record");
        STAFF_HOLD_MSG.put("08",
                "student is behind course pacing schedule -- WARN STUDENT ACCORDINGLY");
        STAFF_HOLD_MSG.put("09",
                "student is behind unit pacing schedule -- WARN STUDENT ACCORDINGLY");
        STAFF_HOLD_MSG.put("10",
                "student has mixed instr and stu managed courses, registering thru both RI and CE");
        STAFF_HOLD_MSG.put("11",
                "answer sheet not turned in");
        STAFF_HOLD_MSG.put("12",
                "conditionally issued exam not verified");
        STAFF_HOLD_MSG.put("13",
                "licensed, but not registered in section for which batch exam was administered");
        STAFF_HOLD_MSG.put("14",
                "duplicate RI & CE registration for same course -- CHECK ARIES!");
        STAFF_HOLD_MSG.put("15",
                "User's survey says student is taking both stu managed and instructor led");
        STAFF_HOLD_MSG.put("16",
                "duplicate 001 & 401 registration for same course -CHECK ARIES!");
        STAFF_HOLD_MSG.put("18",
                "submitted an MPE attempt that cannot be counted");
        STAFF_HOLD_MSG.put("19",
                "not licensed and not registered in sect for which batch exam was administered");
        STAFF_HOLD_MSG.put("20",
                "course prereq not satisfied for an exam taken during a batch administration");
        STAFF_HOLD_MSG.put("21",
                "tried to create stcourse record after deadline date based on User's survey");
        STAFF_HOLD_MSG.put("22",
                "tried to create stu managed stcourse row after add deadline date so not registered");
        STAFF_HOLD_MSG.put("23",
                "officially registered for two different Precalculus formats -- needs to change registration");
        STAFF_HOLD_MSG.put("24",
                "tried to take batch exam for course/sect in which open_status = 'N'");
        STAFF_HOLD_MSG.put("25",
                "student is working on an INC but has re-registered for course in error");
        STAFF_HOLD_MSG.put("26",
                "registered for placement credit that has been declined");
        STAFF_HOLD_MSG.put("27",
                "has registered for placement credit but is not eligible");
        STAFF_HOLD_MSG.put("28",
                "*** 4 WEEK SUMMER STUDENT *** GIVE STUDENT SPECIAL GRADING HANDOUT");
        STAFF_HOLD_MSG.put("29",
                "*** M120B student who is also registered for another course -- PROBLEM!!! ***");
        STAFF_HOLD_MSG.put("30",
                "student has not met weekly progress requirement and may no longer test");
        STAFF_HOLD_MSG.put("31",
                "have student talk with a director");
        STAFF_HOLD_MSG.put("32",
                "student has lost their ID -- ask director to check collection box");
        STAFF_HOLD_MSG.put("33",
                "student may have lost personal items -- check lost/found box & ask director");
        STAFF_HOLD_MSG.put("34",
                "have student talk with Anita (or Steve)");
        STAFF_HOLD_MSG.put("36",
                "REMIND STUDENT: their RENTAL CALC needs to be returned by the last day of finals");
        STAFF_HOLD_MSG.put("41",
                "student has an OVERDUE IN-HOUSE VIDEO --ask to return immediately!");
        STAFF_HOLD_MSG.put("42",
                "student has an OVERDUE IN-HOUSE TEXT -- ask to return it or check it out again");
        STAFF_HOLD_MSG.put("43",
                "student has a LATE OVERNIGHT VIDEO -- ask to return by the end of the day!");
        STAFF_HOLD_MSG.put("44",
                "student has a LATE OVERNIGHT TEXTBOOK -- ask to return by the end of the day!");
        STAFF_HOLD_MSG.put("45",
                "student has an OVERDUE IN-HOUSE CALC -- ask to return immediately!");
        STAFF_HOLD_MSG.put("46",
                "student has an OVERDUE RENTAL CALC -- must be returned immediately!");
        STAFF_HOLD_MSG.put("47",
                "student has an OVERDUE RENTAL CALC MANUAL -- must be returned immediately!");
        STAFF_HOLD_MSG.put("48",
                "Student has rented a calc --> SEND TO OFFICE for copy of RENTAL AGREEMENT");
        STAFF_HOLD_MSG.put("49",
                "Student needs to get a new CSU ID card because we cannot identify properly");
        STAFF_HOLD_MSG.put("50",
                "FINAL WARNING to student: return TI-83 calc or be billed $100 replacement fee");
        STAFF_HOLD_MSG.put("51",
                "student has OVERDUE HEADPHONES -- ask to return immediately!");
        STAFF_HOLD_MSG.put("52",
                "student has an OVERDUE PADLOCK --ask to return it immediately!");
        STAFF_HOLD_MSG.put("53",
                "student has an OVERDUE IPAD -- ask to return it immediately!");
        STAFF_HOLD_MSG.put("54",
                "student has an OVERDUE LAPTOP -- ask to return it immediately!");

        STUDENT_HOLD_MSG.put("02",
                "You are registered for more courses than we recommend for the Summer term.  "
                + "We advise you to work during the full 12 week Summer Session to complete all five courses.");
        STUDENT_HOLD_MSG.put("03",
                "You have registered for both the PLACEMENT CREDIT AND REGULAR sections of the same class - "
                + "please fix your registration by withdrawing from sect 001");
        STUDENT_HOLD_MSG.put("06",
                "There is a problem that requires you speak with a director immediately.");
        STUDENT_HOLD_MSG.put("07",
                "Your Profile responses do not match the Registrar's information provided to us.");
        STUDENT_HOLD_MSG.put("14",
                "you have registered for the same course through RamWeb and CSU Online - "
                + "please fix your registration (sections 001 & 801)");
        STUDENT_HOLD_MSG.put("15",
                "Your Profile says you are taking both student managed and instructor led classes");
        STUDENT_HOLD_MSG.put("16",
                "You have registered for the same course through RamWeb *TWICE* - "
                + "please  fix your registration      (sections 001 & 401)");
        STUDENT_HOLD_MSG.put("18",
                "You submitted a Math Placement attempt when you had no remaining attempts.");
        STUDENT_HOLD_MSG.put("21",
                "It is too late to register for one of   the courses you listed in your Profile");
        STUDENT_HOLD_MSG.put("22",
                "Your Profile response said you are adding a course whose add deadline has passed");
        STUDENT_HOLD_MSG.put("23",
                "You are registered for two different formats; pick *all* on-campus or *all* CSU Online sections");
        STUDENT_HOLD_MSG.put("25",
                "You are working on an INCOMPLETE but have re-registered for the course in error");
        STUDENT_HOLD_MSG.put("27",
                "You have registered for placement credit but are not eligible for that credit");
        STUDENT_HOLD_MSG.put("30",
                "By missing weekly progress, you are locked out of working on your Precalculus courses");
        STUDENT_HOLD_MSG.put("31",
                "You must speak with a Precalculus Center director as soon as possible (Weber 137)");
        STUDENT_HOLD_MSG.put("32",
                "We have your CSU ID card in the Precalculus Center (Weber 137).  Please pick it up soon.");
        STUDENT_HOLD_MSG.put("33",
                "We may have some personal items of yours in the Precalculus Center.  "
                + "Please check with us in Weber  137.");
        STUDENT_HOLD_MSG.put("34",
                "You must speak with a Precalculus Center director as soon as possible (WB 137)");
        STUDENT_HOLD_MSG.put("42",
                "You have an OVERDUE TEXTBOOK - Please return it or check it out again.");
        STUDENT_HOLD_MSG.put("44",
                "You have an OVERDUE TEXTBOOK - Please return it immediately!");
        STUDENT_HOLD_MSG.put("45",
                "You have OVERDUE TI CALCULATOR - please return immediately!");
        STUDENT_HOLD_MSG.put("46",
                "You have an OVERDUE RENTAL CALC -- it needs to be returned immediately!");
        STUDENT_HOLD_MSG.put("47",
                "You have OVERDUE RENTAL CALC MANUAL that must be returned immediately!");
        STUDENT_HOLD_MSG.put("48",
                "Pick up your copy of the TI-83 calculator rental agreement in the Precalculus Center");
        STUDENT_HOLD_MSG.put("49",
                "You need to get a new CSU ID; it is not possible to fully identify you from ID");
        STUDENT_HOLD_MSG.put("50",
                "FINAL WARNING: return the TI-84 calculator immediately that you borrowed from "
                + "the Resource Desk, or you will be billed a $100 replacement fee.");
        STUDENT_HOLD_MSG.put("51",
                "You have OVERDUE HEADPHONES -- please return them immediately!");
        STUDENT_HOLD_MSG.put("52",
                "You have an OVERDUE PADLOCK - please return it immediately!");
        STUDENT_HOLD_MSG.put("53",
                "You have an OVERDUE IPAD - please return it immediately!");
        STUDENT_HOLD_MSG.put("54",
                "You have an OVERDUE LAPTOP - please return it immediately!");
    }

    /**
     * Private constructor to prevent direct instantiation.
     */
    private StudentHoldLogic() {

        super();
    }

    /**
     * Gets the instance of {@code StudentUnitMasteryLogic} appropriate to a cache. The result will depend on the
     * database installation type of the PRIMARY schema configuration in cache's database profile.
     *
     * @param cache the cache
     * @return the appropriate {@code StudentUnitMasteryLogic} object (null if none found)
     */
    public static StudentHoldLogic get(final Cache cache) {

        final EDbProduct type = IRecLogic.getDbType(cache, ESchema.LEGACY);

        StudentHoldLogic result = null;
        if (type == EDbProduct.INFORMIX) {
            result = Informix.INSTANCE;
        } else if (type == EDbProduct.POSTGRESQL) {
            result = Postgres.INSTANCE;
        }

        return result;
    }

    /**
     * Gets all admin_hold records for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of admin_hold records
     * @throws SQLException if there is an error accessing the database
     */
    public abstract List<StudentHoldRec> queryByStudent(Cache cache, String stuId) throws SQLException;

    /**
     * Gets a single admin_hold record.
     *
     * @param cache  the data cache
     * @param stuId  the student ID
     * @param holdId the hold ID
     * @return the admin_hold record; null if not found
     * @throws SQLException if there is an error accessing the database
     */
    public abstract StudentHoldRec query(Cache cache, String stuId, String holdId) throws SQLException;

    /**
     * Tests whether a student has a fatal (sev_admin_hold = 'F') hold.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return true if a fatal hold was found; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public abstract boolean hasFatalHold(Cache cache, String stuId) throws SQLException;

    /**
     * Deletes all admin_hold records with a specified hold ID.
     *
     * @param cache  the data cache
     * @param holdId the hold ID to delete
     * @return the number of holds deleted
     * @throws SQLException if there is an error accessing the database
     */
    public abstract int deleteAllByHoldId(Cache cache, String holdId) throws SQLException;

    /**
     * Updates the date on an admin hold record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public abstract boolean updateAdminHoldDate(Cache cache, StudentHoldRec record) throws SQLException;

    /**
     * Gets the student-oriented message for an administrative hold.
     *
     * @param holdId the hold ID
     * @return the message; an empty string if none is configured
     */
    public static String getStudentMessage(final String holdId) {

        final String msg = STUDENT_HOLD_MSG.get(holdId);

        return msg == null ? CoreConstants.EMPTY : msg;
    }

    /**
     * Gets the staff-oriented message for an administrative hold.
     *
     * @param holdId the hold ID
     * @return the message; an empty string if none is configured
     */
    public static String getStaffMessage(final String holdId) {

        final String msg = STAFF_HOLD_MSG.get(holdId);

        return msg == null ? CoreConstants.EMPTY : msg;
    }

    /**
     * Retrieves all special student records for a test student.
     *
     * @param studentId the student ID
     * @return the test student; an empty list if the student ID is not a valid test student ID
     */
    private static List<StudentHoldRec> queryByTestStudent(final String studentId) {

        final List<StudentHoldRec> result;

        if (studentId != null && studentId.length() == 9 && studentId.startsWith("99")) {
            final char ch3 = studentId.charAt(2);
            final char ch4 = studentId.charAt(3);

            if (ch3 == 'C' && ch4 == 'I') {
                result = getCheckinTestStudentHold(studentId);
            } else {
                result = EMPTY_LIST;
            }
        } else {
            Log.warning("Invalid test student ID: " + studentId);
            result = EMPTY_LIST;
        }

        return result;
    }

    /**
     * Retrieves all special student records for a test student.
     *
     * @param studentId the student ID
     * @param holdId    the ID of the hold to query
     * @return the test data; {@code null} if the student ID is not a valid test student ID
     */
    private static StudentHoldRec queryByTestStudentHold(final String studentId, final String holdId) {

        final StudentHoldRec result;

        if (studentId != null && studentId.length() == 9 && studentId.startsWith("99")) {
            final char ch3 = studentId.charAt(2);
            final char ch4 = studentId.charAt(3);

            if (ch3 == 'C' && ch4 == 'I') {
                result = getCheckinTestStudentHold(studentId, holdId);
            } else {
                result = null;
            }
        } else {
            Log.warning("Invalid test student ID: " + studentId);
            result = null;
        }

        return result;
    }

    /**
     * Gets all student hold records for a test student configured to test some aspect of Math Placement.
     *
     * @param studentId the student ID
     * @return the student hold records
     */
    private static List<StudentHoldRec> getCheckinTestStudentHold(final String studentId) {

        final List<StudentHoldRec> result;

        final char ch5 = studentId.charAt(4);
        final char ch6 = studentId.charAt(5);

        if (ch5 == 'M' && ch6 == 'P') {
            // Math Placement test cases

            // Last three digits are test case. Last digit governs holds present.

            final char ch9 = studentId.charAt(8);
            if (ch9 == '7') {
                result = new ArrayList<>(1);
                result.add(makeTestStudentHold(studentId));
            } else {
                result = EMPTY_LIST;
            }
        } else {
            result = EMPTY_LIST;
        }

        return result;
    }

    /**
     * Gets all student hold records for a test student configured to test some aspect of Math Placement.
     *
     * @param studentId the student ID
     * @param holdId    the hold ID
     * @return the student hold record
     */
    private static StudentHoldRec getCheckinTestStudentHold(final String studentId, final String holdId) {

        final StudentHoldRec result;

        final char ch5 = studentId.charAt(4);
        final char ch6 = studentId.charAt(5);

        if (ch5 == 'M' && ch6 == 'P') {
            // Math Placement test cases

            // Last three digits are test case. Last digit governs holds present.

            final char ch9 = studentId.charAt(8);
            if ((ch9 == '7') && "46".equals(holdId)) {
                result = makeTestStudentHold(studentId);
            } else {
                result = null;
            }
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Makes a single test special student record.
     *
     * @param studentId the student ID
     * @return the record
     */
    private static StudentHoldRec makeTestStudentHold(final String studentId) {

        return new StudentHoldRec(studentId, "46", "F", Integer.valueOf(0), LocalDate.now());
    }

    /**
     * A subclass of {@code StudentHoldLogic} designed for the Informix schema.
     */
    public static final class Informix extends StudentHoldLogic {

        /** A single instance. */
        public static final Informix INSTANCE = new Informix();

        /** A field name. */
        public static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        public static final String FLD_HOLD_ID = "hold_id";

        /** A field name. */
        public static final String FLD_SEV_ADMIN_HOLD = "sev_admin_hold";

        /** A field name. */
        public static final String FLD_TIMES_DISPLAY = "times_display";

        /** A field name. */
        public static final String FLD_CREATE_DT = "create_dt";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StudentHoldRec record) throws SQLException {

            final boolean result;

            if (record.stuId.startsWith("99")) {
                result = false;
            } else {
                final String sql = SimpleBuilder.concat(
                        "INSERT INTO admin_hold (stu_id,hold_id,sev_admin_hold,times_display,create_dt) VALUES (",
                        sqlStringValue(record.stuId), ",",
                        sqlStringValue(record.holdId), ",",
                        sqlStringValue(record.sevAdminHold), ",",
                        sqlIntegerValue(record.timesDisplay), ",",
                        sqlDateValue(record.createDt), ")");

                result = doUpdateOneRow(cache, ESchema.LEGACY, sql);
            }

            return result;
        }

        /**
         * Deletes a record.
         *
         * @param cache  the data cache
         * @param record the record to delete
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean delete(final Cache cache, final StudentHoldRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM admin_hold WHERE stu_id=",
                    sqlStringValue(record.stuId), " AND hold_id=", sqlStringValue(record.holdId));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentHoldRec> queryAll(final Cache cache) throws SQLException {

            return doListQuery(cache, ESchema.LEGACY, "SELECT * FROM admin_hold");
        }

        /**
         * Gets all admin_hold records for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of admin_hold records
         * @throws SQLException if there is an error accessing the database
         */
        public List<StudentHoldRec> queryByStudent(final Cache cache, final String stuId) throws SQLException {

            final List<StudentHoldRec> result;

            if (stuId.startsWith("99")) {
                result = queryByTestStudent(stuId);
            } else {
                final String sql = SimpleBuilder.concat("SELECT * FROM admin_hold WHERE stu_id=",
                        sqlStringValue(stuId));

                result = new ArrayList<>(10);

                final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql)) {

                    while (rs.next()) {
                        result.add(fromResultSet(rs));
                    }
                } finally {
                    Cache.checkInConnection(conn);
                }
            }

            return result;
        }

        /**
         * Gets a single admin_hold record.
         *
         * @param cache  the data cache
         * @param stuId  the student ID
         * @param holdId the hold ID
         * @return the admin_hold record; null if not found
         * @throws SQLException if there is an error accessing the database
         */
        public StudentHoldRec query(final Cache cache, final String stuId, final String holdId) throws SQLException {

            StudentHoldRec result = null;

            if (stuId.startsWith("99")) {
                result = queryByTestStudentHold(stuId, holdId);
            } else {
                final HtmlBuilder sql = new HtmlBuilder(100);

                sql.add("SELECT * FROM admin_hold WHERE stu_id=", sqlStringValue(stuId),
                        "   AND hold_id=", sqlStringValue(holdId));

                final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql.toString())) {

                    if (rs.next()) {
                        result = fromResultSet(rs);
                    }
                } finally {
                    Cache.checkInConnection(conn);
                }
            }

            return result;
        }

        /**
         * Tests whether a student has a fatal (sev_admin_hold = 'F') hold.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return true if a fatal hold was found; false if not
         * @throws SQLException if there is an error accessing the database
         */
        public boolean hasFatalHold(final Cache cache, final String stuId) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT count(*) FROM admin_hold WHERE stu_id=", sqlStringValue(stuId),
                    "   AND sev_admin_hold='F'");

            boolean result = false;

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql.toString())) {

                if (rs.next()) {
                    result = rs.getInt(1) > 0;
                }
            } finally {
                Cache.checkInConnection(conn);
            }

            return result;
        }

        /**
         * Deletes all admin_hold records with a specified hold ID.
         *
         * @param cache  the data cache
         * @param holdId the hold ID to delete
         * @return the number of holds deleted
         * @throws SQLException if there is an error accessing the database
         */
        public int deleteAllByHoldId(final Cache cache, final String holdId) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("DELETE FROM admin_hold WHERE hold_id=", sqlStringValue(holdId));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            final int count;
            try (final Statement stmt = conn.createStatement()) {
                count = stmt.executeUpdate(sql.toString());
                conn.commit();
            } finally {
                Cache.checkInConnection(conn);
            }

            return count;
        }

        /**
         * Updates the date on an admin hold record.
         *
         * @param cache  the data cache
         * @param record the record to delete
         * @return {@code true} if successful; {@code false} otherwise
         * @throws SQLException if there is an error accessing the database
         */
        public boolean updateAdminHoldDate(final Cache cache, final StudentHoldRec record) throws SQLException {

            final boolean result;

            if (record.stuId.startsWith("99")) {
                result = false;
            } else {
                final String sql = SimpleBuilder.concat("UPDATE admin_hold ",
                        "   SET create_dt=", sqlDateValue(record.createDt),
                        " WHERE stu_id=", sqlStringValue(record.stuId),
                        "   AND hold_id=", sqlStringValue(record.holdId));

                result = doUpdateOneRow(cache, ESchema.LEGACY, sql);
            }

            return result;
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public StudentHoldRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentHoldRec result = new StudentHoldRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.holdId = getStringField(rs, FLD_HOLD_ID);
            result.sevAdminHold = getStringField(rs, FLD_SEV_ADMIN_HOLD);
            result.timesDisplay = getIntegerField(rs, FLD_TIMES_DISPLAY);
            result.createDt = getDateField(rs, FLD_CREATE_DT);

            return result;
        }
    }

    /**
     * A subclass of {@code StudentUnitMasteryLogic} designed for the PostgreSQL schema.
     */
    public static final class Postgres extends StudentHoldLogic {

        /** A single instance. */
        public static final Postgres INSTANCE = new Postgres();

        /** A field name. */
        public static final String FLD_STU_ID = "stu_id";

        /** A field name. */
        public static final String FLD_HOLD_ID = "hold_id";

        /** A field name. */
        public static final String FLD_SEV_ADMIN_HOLD = "sev_admin_hold";

        /** A field name. */
        public static final String FLD_TIMES_DISPLAY = "times_display";

        /** A field name. */
        public static final String FLD_CREATE_DT = "create_dt";

        /**
         * Inserts a new record.
         *
         * @param cache  the data cache
         * @param record the record to insert
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean insert(final Cache cache, final StudentHoldRec record) throws SQLException {

            final boolean result;

            if (record.stuId.startsWith("99")) {
                result = false;
            } else {
                final String sql = SimpleBuilder.concat(
                        "INSERT INTO admin_hold (stu_id,hold_id,sev_admin_hold,times_display,create_dt) VALUES (",
                        sqlStringValue(record.stuId), ",",
                        sqlStringValue(record.holdId), ",",
                        sqlStringValue(record.sevAdminHold), ",",
                        sqlIntegerValue(record.timesDisplay), ",",
                        sqlPgDateValue(record.createDt), ")");

                result = doUpdateOneRow(cache, ESchema.LEGACY, sql);
            }

            return result;
        }

        /**
         * Deletes a record.
         *
         * @param cache  the data cache
         * @param record the record to delete
         * @return {@code true} if successful; {@code false} if not
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public boolean delete(final Cache cache, final StudentHoldRec record) throws SQLException {

            final String sql = SimpleBuilder.concat("DELETE FROM admin_hold WHERE stu_id=",
                    sqlStringValue(record.stuId), " AND hold_id=", sqlStringValue(record.holdId));

            return doUpdateOneRow(cache, ESchema.LEGACY, sql);
        }

        /**
         * Queries every record in the database.
         *
         * @param cache the data cache
         * @return the complete set of records in the database
         * @throws SQLException if there is an error performing the query
         */
        @Override
        public List<StudentHoldRec> queryAll(final Cache cache) throws SQLException {

            final String schemaPrefix = cache.getSchemaPrefix(ESchema.TERM);

            final String sql = SimpleBuilder.concat("SELECT * FROM ", schemaPrefix, ".admin_hold");

            return doListQuery(cache, ESchema.LEGACY, sql);
        }

        /**
         * Gets all admin_hold records for a student.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return the list of admin_hold records
         * @throws SQLException if there is an error accessing the database
         */
        public List<StudentHoldRec> queryByStudent(final Cache cache, final String stuId) throws SQLException {

            final List<StudentHoldRec> result;

            if (stuId.startsWith("99")) {
                result = queryByTestStudent(stuId);
            } else {
                final String sql = SimpleBuilder.concat("SELECT * FROM admin_hold WHERE stu_id=",
                        sqlStringValue(stuId));

                result = new ArrayList<>(10);

                final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql)) {

                    while (rs.next()) {
                        result.add(fromResultSet(rs));
                    }
                } finally {
                    Cache.checkInConnection(conn);
                }
            }

            return result;
        }

        /**
         * Gets a single admin_hold record.
         *
         * @param cache  the data cache
         * @param stuId  the student ID
         * @param holdId the hold ID
         * @return the admin_hold record; null if not found
         * @throws SQLException if there is an error accessing the database
         */
        public StudentHoldRec query(final Cache cache, final String stuId, final String holdId) throws SQLException {

            StudentHoldRec result = null;

            if (stuId.startsWith("99")) {
                result = queryByTestStudentHold(stuId, holdId);
            } else {
                final HtmlBuilder sql = new HtmlBuilder(100);

                sql.add("SELECT * FROM admin_hold WHERE stu_id=", sqlStringValue(stuId),
                        "   AND hold_id=", sqlStringValue(holdId));

                final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql.toString())) {

                    if (rs.next()) {
                        result = fromResultSet(rs);
                    }
                } finally {
                    Cache.checkInConnection(conn);
                }
            }

            return result;
        }

        /**
         * Tests whether a student has a fatal (sev_admin_hold = 'F') hold.
         *
         * @param cache the data cache
         * @param stuId the student ID
         * @return true if a fatal hold was found; false if not
         * @throws SQLException if there is an error accessing the database
         */
        public boolean hasFatalHold(final Cache cache, final String stuId) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("SELECT count(*) FROM admin_hold WHERE stu_id=", sqlStringValue(stuId),
                    "   AND sev_admin_hold='F'");

            boolean result = false;

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql.toString())) {

                if (rs.next()) {
                    result = rs.getInt(1) > 0;
                }
            } finally {
                Cache.checkInConnection(conn);
            }

            return result;
        }

        /**
         * Deletes all admin_hold records with a specified hold ID.
         *
         * @param cache  the data cache
         * @param holdId the hold ID to delete
         * @return the number of holds deleted
         * @throws SQLException if there is an error accessing the database
         */
        public int deleteAllByHoldId(final Cache cache, final String holdId) throws SQLException {

            final HtmlBuilder sql = new HtmlBuilder(100);

            sql.add("DELETE FROM admin_hold WHERE hold_id=", sqlStringValue(holdId));

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            final int count;
            try (final Statement stmt = conn.createStatement()) {
                count = stmt.executeUpdate(sql.toString());
                conn.commit();
            } finally {
                Cache.checkInConnection(conn);
            }

            return count;
        }

        /**
         * Updates the date on an admin hold record.
         *
         * @param cache  the data cache
         * @param record the record to delete
         * @return {@code true} if successful; {@code false} otherwise
         * @throws SQLException if there is an error accessing the database
         */
        public boolean updateAdminHoldDate(final Cache cache, final StudentHoldRec record) throws SQLException {

            final boolean result;

            if (record.stuId.startsWith("99")) {
                result = false;
            } else {
                final String sql = SimpleBuilder.concat("UPDATE admin_hold ",
                        "   SET create_dt=", sqlPgDateValue(record.createDt),
                        " WHERE stu_id=", sqlStringValue(record.stuId),
                        "   AND hold_id=", sqlStringValue(record.holdId));

                result = doUpdateOneRow(cache, ESchema.LEGACY, sql);
            }

            return result;
        }

        /**
         * Extracts a record from a result set.
         *
         * @param rs the result set from which to retrieve the record
         * @return the record
         * @throws SQLException if there is an error accessing the database
         */
        @Override
        public StudentHoldRec fromResultSet(final ResultSet rs) throws SQLException {

            final StudentHoldRec result = new StudentHoldRec();

            result.stuId = getStringField(rs, FLD_STU_ID);
            result.holdId = getStringField(rs, FLD_HOLD_ID);
            result.sevAdminHold = getStringField(rs, FLD_SEV_ADMIN_HOLD);
            result.timesDisplay = getIntegerField(rs, FLD_TIMES_DISPLAY);
            result.createDt = getDateField(rs, FLD_CREATE_DT);

            return result;
        }
    }
}
