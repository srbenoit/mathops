package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A utility class to work with "stmpe" records.
 *
 * <pre>
 * Table:  'stmpe'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * stu_id               char(9)                   no      PK
 * version              char(5)                   no      PK
 * academic_yr          char(4)                   no
 * exam_dt              date                      no      PK
 * start_time           integer                   yes
 * finish_time          integer                   no      PK
 * last_name            char(30)                  yes
 * first_name           char(30)                  yes
 * middle_initial       char(1)                   yes
 * seq_nbr              smallint                  yes
 * serial_nbr           integer                   yes
 * sts_a                smallint                  yes
 * sts_117              smallint                  yes
 * sts_118              smallint                  yes
 * sts_124              smallint                  yes
 * sts_125              smallint                  yes
 * sts_126              smallint                  yes
 * placed               char(1)                   no
 * how_validated        char(1)                   yes
 * </pre>
 */
public enum RawStmpeLogic {
    ;

    /** A commonly used string. */
    private static final String POOOO = "POOOO";

    /** A commonly used string. */
    private static final String PPPPP = "PPPPP";

    /** A commonly used string. */
    private static final String MPTUN = "MPTUN";

    /** A commonly used string. */
    private static final String MPTTC = "MPTTC";

    /** A commonly used string. */
    private static final String MPTPU = "MPTPU";

    /** A commonly used string. */
    private static final String Y = "Y";

    /** A commonly used string. */
    private static final String N = "N";

    /** Test student IDs who almost placed into MATH 117. */
    private static final List<String> almostPlacedOnMPEHas2MCE;

    /** Test student IDs placed into MATH 117 on an MCE, has 1 MPE and 1 MCE. */
    private static final List<String> placedInto117OnMCEHas1MPE1MCE;

    /** Test student IDs placed into MATH 117 on an MCE, has 1 MPE. */
    private static final List<String> placedInto117OnMCEHas1MPE;

    /** Test student IDs placed out of MATH 117 on an MCE, has 1 MPE. */
    private static final List<String> placedOutOf117OnMCEHas1MPE;

    /** Test student IDs placed out of MATH 117-124 on an MPE, has 2 MCE. */
    private static final List<String> placedOutOf117to124OnMPEHas2MCE;

    /** Test student IDs that did not place on MPE, has 2 MCE. */
    private static final List<String> didNotPlaceOnMPEHas2MCE;

    /** Test student IDs placed out of MATH 117-118 on an MCE, has 1 MPE and 1 MCE. */
    private static final List<String> placedOutOf117to118OnMCEHas1MPE1MCE;

    /** Test student IDs that placed out of MATH 117-125 on MPE, has 2 MCE. */
    private static final List<String> placedOutOf117to125OnMPEHas2MCE;

    /** Test student IDs that have no placement results, no attempts remaining. */
    private static final List<String> noResultsNoAttempts;

    /** Test student IDs placed out of MATH 117-124 on an MPE, no attempts remaining. */
    private static final List<String> placedOutOf117to124OnMPEHas0;

    /** Test student IDs placed out of MATH 117 on an MPE, no attempts remaining. */
    private static final List<String> placedOutOf117OnMPEHas0;

    static {
        almostPlacedOnMPEHas2MCE = Arrays.asList("999011121", "999011125", "999012121", "999012125",
                "999012421", "999012425", "999021131", "999021221", "999021261", "999022221", "999022261");

        placedInto117OnMCEHas1MPE1MCE = Arrays.asList("999011122", "999011124", "999012122", "999012124",
                "999012422", "999012424", "999031011", "999033111");

        placedInto117OnMCEHas1MPE = List.of("999033121");

        placedOutOf117OnMCEHas1MPE = Arrays.asList("999011123", "999012123", "999012423");

        placedOutOf117to124OnMPEHas2MCE = Arrays.asList("999011126", "999012126", "999012426");

        didNotPlaceOnMPEHas2MCE = Arrays.asList("999011131", "999012131", "999012431", "999021141",
                "999021231", "999021271", "999022141", "999022231", "999022271", "999031021", "999032011");

        placedOutOf117to118OnMCEHas1MPE1MCE = Arrays.asList("999011132", "999012132", "999012432", "999033221");

        placedOutOf117to125OnMPEHas2MCE = Arrays.asList("999011133", "999011221", "999011321", "999012133",
                "999012221", "999012321", "999012433", "999012461", "999012521", "999034011");

        noResultsNoAttempts = Arrays.asList("999011141", "999011231", "999011331", "999012141",
                "999012231", "999012331", "999012441", "999012471", "999012531", "999021151",
                "999021241", "999021281", "999022151", "999022241", "999022281");

        placedOutOf117to124OnMPEHas0 = Arrays.asList("999011142", "999012142", "999012442");

        placedOutOf117OnMPEHas0 = Arrays.asList("999011241", "999011341", "999012241", "999012341",
                "999012481", "999012541", "999033231");
    }

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawStmpe record) throws SQLException {

        if (record.stuId == null || record.version == null || record.academicYr == null
            || record.examDt == null || record.finishTime == null || record.placed == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final boolean result;

        if (record.stuId.startsWith("99")) {
            Log.info("Skipping insert of RawStmpe for test student:");
            Log.info("stu_id: ", record.stuId);
            result = false;
        } else {
            // Adjust serial number if needed to avoid collision with existing record
            if (record.serialNbr != null) {
                for (int i = 0; i < 1000; ++i) {
                    final Integer existing = LogicUtils.executeSimpleIntQuery(cache,
                            "SELECT COUNT(*) FROM stmpe WHERE serial_nbr=" + record.serialNbr);

                    if (existing == null || existing.longValue() == 0L) {
                        break;
                    }
                    record.serialNbr = Long.valueOf(record.serialNbr.longValue() + 1L);
                }
            }

            final String sql = SimpleBuilder.concat(
                    "INSERT INTO stmpe (stu_id,version,academic_yr,exam_dt,start_time,finish_time,last_name,",
                    "first_name,middle_initial,seq_nbr,serial_nbr,sts_a,sts_117,sts_118,sts_124,sts_125,sts_126,",
                    "placed,how_validated) VALUES (",
                    LogicUtils.sqlStringValue(record.stuId), ",",
                    LogicUtils.sqlStringValue(record.version), ",",
                    LogicUtils.sqlStringValue(record.academicYr), ",",
                    LogicUtils.sqlDateValue(record.examDt), ",",
                    LogicUtils.sqlIntegerValue(record.startTime), ",",
                    LogicUtils.sqlIntegerValue(record.finishTime), ",",
                    LogicUtils.sqlStringValue(record.lastName), ",",
                    LogicUtils.sqlStringValue(record.firstName), ",",
                    LogicUtils.sqlStringValue(record.middleInitial), ",",
                    LogicUtils.sqlIntegerValue(record.seqNbr), ",",
                    LogicUtils.sqlLongValue(record.serialNbr), ",",
                    LogicUtils.sqlIntegerValue(record.stsA), ",",
                    LogicUtils.sqlIntegerValue(record.sts117), ",",
                    LogicUtils.sqlIntegerValue(record.sts118), ",",
                    LogicUtils.sqlIntegerValue(record.sts124), ",",
                    LogicUtils.sqlIntegerValue(record.sts125), ",",
                    LogicUtils.sqlIntegerValue(record.sts126), ",",
                    LogicUtils.sqlStringValue(record.placed), ",",
                    LogicUtils.sqlStringValue(record.howValidated), ")");

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            try (final Statement stmt = conn.createStatement()) {
                result = stmt.executeUpdate(sql) == 1;

                if (result) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } finally {
                Cache.checkInConnection(conn);
            }
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
    public static boolean delete(final Cache cache, final RawStmpe record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM stmpe",
                " WHERE version=", LogicUtils.sqlStringValue(record.version),
                " AND stu_id=", LogicUtils.sqlStringValue(record.stuId),
                " AND exam_dt=", LogicUtils.sqlDateValue(record.examDt),
                " AND finish_time=", LogicUtils.sqlIntegerValue(record.finishTime));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {

            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Deletes a record, and also deletes associated RawStmpeqa records.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean deleteExamAndAnswers(final Cache cache, final RawStmpe record) throws SQLException {

        RawStmpeqaLogic.deleteAllForExam(cache, record);

        return delete(cache, record);
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmpe> queryAll(final Cache cache) throws SQLException {

        return executeQuery(cache, "SELECT * FROM stmpe");
    }

    /**
     * Retrieves all placement exams for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of placement exams
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmpe> queryByStudent(final Cache cache, final String stuId) throws SQLException {

        final List<RawStmpe> result;

        if (stuId.startsWith("99")) {
            result = queryByTestStudent(stuId);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM stmpe ",
                    "WHERE stu_id=", LogicUtils.sqlStringValue(stuId));

            return executeQuery(cache, sql);
        }

        return result;
    }

    /**
     * Retrieves all placement exams for a student.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return the list of placement exams
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmpe> queryLegalByStudent(final Cache cache, final String stuId) throws SQLException {

        final List<RawStmpe> result;

        if (stuId.startsWith("99")) {
            result = queryByTestStudent(stuId);
        } else {
            final String sql = SimpleBuilder.concat("SELECT * FROM stmpe ",
                    "WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                    " AND (placed='Y' OR placed='N')");

            return executeQuery(cache, sql);
        }

        return result;
    }

    /**
     * Queries for the students with test data relating to math placement.
     *
     * @param studentId the ID of the student to query
     * @param result    the list to which to add results
     */
    private static void getPlacementTestStudent(final String studentId, final Collection<? super RawStmpe> result) {

        if (RawStudentLogic.validate99PLStudentId(studentId)) {

            int index = 0;

            final char c6 = studentId.charAt(5);
            final char c7 = studentId.charAt(6);

            final int[] subtests = new int[6];
            if (c6 == '1') {
                subtests[0] = 5;
            } else if (c6 == '2') {
                subtests[0] = 4;
                subtests[1] = 7;
                if (c7 >= '2') {
                    subtests[1] = 9;
                }
            } else if (c6 == '3') {
                subtests[0] = 4;
                subtests[1] = 7;
                subtests[2] = 5;
                if (c7 >= '2') {
                    subtests[1] = 9;
                    if (c7 >= '3') {
                        subtests[2] = 6;
                    }
                }
            } else if (c6 == '4') {
                subtests[0] = 4;
                subtests[1] = 7;
                subtests[2] = 5;
                subtests[3] = 6;
                if (c7 >= '2') {
                    subtests[1] = 9;
                    if (c7 >= '3') {
                        subtests[2] = 6;
                        if (c7 >= '4') {
                            subtests[3] = 7;
                        }
                    }
                }
            } else if (c6 == '5') {
                subtests[0] = 4;
                subtests[1] = 7;
                subtests[2] = 5;
                subtests[4] = 5;
                if (c7 >= '2') {
                    subtests[1] = 9;
                    if (c7 >= '3') {
                        subtests[2] = 6;
                        if (c7 >= '5') {
                            subtests[4] = 7;
                        }
                    }
                }
            } else if (c6 == '6') {
                subtests[0] = 4;
                subtests[1] = 7;
                subtests[2] = 5;
                subtests[3] = 6;
                subtests[4] = 5;
                if (c7 >= '2') {
                    subtests[1] = 9;
                    if (c7 >= '3') {
                        subtests[2] = 6;
                        if (c7 >= '4') {
                            subtests[3] = 7;
                            if (c7 == '5') {
                                subtests[4] = 6;
                            } else if (c7 == '6') {
                                subtests[4] = 6;
                            }
                        }
                    }
                }
            } else if (c6 == '7') {
                subtests[0] = 4;
                subtests[1] = 7;
                subtests[2] = 5;
                subtests[4] = 5;
                subtests[5] = 5;
                if (c7 >= '2') {
                    subtests[1] = 9;
                    if (c7 >= '3') {
                        subtests[2] = 6;
                        if (c7 >= '5') {
                            subtests[4] = 6;
                            if (c7 >= '7') {
                                subtests[5] = 6;
                            }
                        }
                    }
                }
            } else if (c6 == '8') {
                subtests[0] = 4;
                subtests[1] = 7;
                subtests[2] = 5;
                subtests[3] = 6;
                subtests[4] = 5;
                subtests[5] = 5;
                if (c7 >= '2') {
                    subtests[1] = 9;
                    if (c7 >= '3') {
                        subtests[2] = 6;
                        if (c7 == '4') {
                            subtests[3] = 7;
                        } else if (c7 == '5') {
                            subtests[4] = 6;
                        } else if (c7 == '6') {
                            subtests[3] = 7;
                            subtests[4] = 6;
                        } else if (c7 == '7') {
                            subtests[4] = 6;
                            subtests[5] = 6;
                        } else if (c7 == '8') {
                            subtests[3] = 7;
                            subtests[4] = 6;
                            subtests[5] = 6;
                        }
                    }
                }
            }

            final char c5 = studentId.charAt(4);

            final String placed = c6 == '0' ? N : Y;

            if ("345".indexOf(c5) >= 0) {
                result.add(makeRecord(studentId, index, POOOO, placed, subtests));
                ++index;
            }

            if ("1245".indexOf(c5) >= 0) {
                result.add(makeRecord(studentId, index, PPPPP, placed, subtests));
                ++index;

                if ("25".indexOf(c5) >= 0) {
                    result.add(makeRecord(studentId, index, PPPPP, N, 0, 0, 0, 0, 0, 0));
                }
            }

        } else {
            Log.warning("Invalid test student ID: " + studentId);
        }
    }

    /**
     * Queries for the students with test data relating to math plan.
     *
     * @param studentId the ID of the student to query
     * @param result    the list to which to add results
     */
    private static void getMathPlanTestStudent(final String studentId, final Collection<? super RawStmpe> result) {

        if (almostPlacedOnMPEHas2MCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, POOOO, N, 3, 5, 0, 0, 0, 0));
        } else if (placedInto117OnMCEHas1MPE1MCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, Y, 5, 0, 0, 0, 0, 0));
        } else if (placedInto117OnMCEHas1MPE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, Y, 5, 0, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 1, PPPPP, N, 0, 0, 0, 0, 0, 0));
        } else if (placedOutOf117OnMCEHas1MPE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, Y, 4, 7, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 1, PPPPP, N, 0, 0, 0, 0, 0, 0));
        } else if (placedOutOf117to124OnMPEHas2MCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, Y, 4, 7, 5, 6, 0, 0));
        } else if (didNotPlaceOnMPEHas2MCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, POOOO, N, 0, 0, 0, 0, 0, 0));
        } else if (placedOutOf117to118OnMCEHas1MPE1MCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, Y, 4, 7, 5, 0, 0, 0));
        } else if (placedOutOf117to125OnMPEHas2MCE.contains(studentId)) {
            result.add(makeRecord(studentId, 0, POOOO, Y, 4, 7, 5, 6, 5, 0));
        } else if (noResultsNoAttempts.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, N, 0, 0, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 1, PPPPP, N, 0, 0, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 2, POOOO, N, 0, 0, 0, 0, 0, 0));
        } else if (placedOutOf117to124OnMPEHas0.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, N, 0, 0, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 1, PPPPP, N, 0, 0, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 2, POOOO, Y, 4, 7, 5, 6, 0, 0));
        } else if (placedOutOf117OnMPEHas0.contains(studentId)) {
            result.add(makeRecord(studentId, 0, PPPPP, N, 0, 0, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 1, PPPPP, N, 0, 0, 0, 0, 0, 0));
            result.add(makeRecord(studentId, 2, POOOO, Y, 4, 7, 0, 0, 0, 0));
        }
    }

    /**
     * Queries for the students with test data relating to a checkin (IDs beginning with "99CI").
     *
     * @param studentId the ID of the student to query
     * @param result    the list to which to add results
     */
    private static void getCheckinTestStudent(final String studentId, final Collection<? super RawStmpe> result) {

        final char ch5 = studentId.charAt(4);
        final char ch6 = studentId.charAt(5);

        if (ch5 == 'M' && ch6 == 'P') {
            // Math Placement test cases

            // Last three digits are test case. 8-th digit governs attempts used.
            final char ch8 = studentId.charAt(7);
            if (ch8 == '1') {
                result.add(makeRecord(studentId, 0, PPPPP, N, 0, 0, 0, 0, 0, 0));
            } else if (ch8 == '2') {
                result.add(makeRecord(studentId, 0, PPPPP, N, 0, 0, 0, 0, 0, 0));
                result.add(makeRecord(studentId, 1, PPPPP, N, 0, 0, 0, 0, 0, 0));
            } else if (ch8 == '3') {
                result.add(makeRecord(studentId, 0, POOOO, N, 0, 0, 0, 0, 0, 0));
            } else if (ch8 == '4') {
                result.add(makeRecord(studentId, 0, POOOO, N, 0, 0, 0, 0, 0, 0));
                result.add(makeRecord(studentId, 1, PPPPP, N, 0, 0, 0, 0, 0, 0));
            } else if (ch8 == '5') {
                result.add(makeRecord(studentId, 0, MPTTC, N, 0, 0, 0, 0, 0, 0));
            } else if (ch8 == '6') {
                result.add(makeRecord(studentId, 0, MPTTC, N, 0, 0, 0, 0, 0, 0));
                result.add(makeRecord(studentId, 1, MPTPU, N, 0, 0, 0, 0, 0, 0));
            } else if (ch8 == '7') {
                result.add(makeRecord(studentId, 0, MPTUN, N, 0, 0, 0, 0, 0, 0));
            } else if (ch8 == '8') {
                result.add(makeRecord(studentId, 0, MPTUN, N, 0, 0, 0, 0, 0, 0));
                result.add(makeRecord(studentId, 1, MPTPU, N, 0, 0, 0, 0, 0, 0));
            }
        }
    }

    /**
     * Makes a placement attempt record.
     *
     * @param stuId           the student ID
     * @param index           the index of the attempt
     * @param version         the exam version
     * @param placementResult the placement result ("Y" or "N")
     * @param subtests        the subtest scores
     * @return the record
     */
    private static RawStmpe makeRecord(final String stuId, final int index, final String version,
                                       final String placementResult, final int... subtests) {

        final String[] firstLast = RawStudentLogic.genStudentName(stuId);

        final Long serial = Long.valueOf(Integer.MAX_VALUE + stuId.hashCode() + index);
        final LocalDateTime whenFinished = LocalDateTime.now();
        final LocalDateTime whenStarted = whenFinished.minusHours(1L);
        final String howValidated = PPPPP.equals(version) ? "P" : "S";

        return new RawStmpe(stuId, version, "1718", whenFinished.toLocalDate(),
                Integer.valueOf(TemporalUtils.minuteOfDay(whenStarted)),
                Integer.valueOf(TemporalUtils.minuteOfDay(whenFinished)), firstLast[1], firstLast[0],
                null, null, serial, Integer.valueOf(subtests[0]), Integer.valueOf(subtests[1]),
                Integer.valueOf(subtests[2]), Integer.valueOf(subtests[3]),
                Integer.valueOf(subtests[4]), Integer.valueOf(subtests[5]), placementResult,
                howValidated);
    }

    /**
     * Queries for the set of placement attempts on record for a test student.
     *
     * @param studentId the ID of the student to query
     * @return the list of records that matched the criteria, a zero-length array if none matched
     */
    private static List<RawStmpe> queryByTestStudent(final String studentId) {

        final List<RawStmpe> result = new ArrayList<>(3);

        if (studentId != null && studentId.length() == 9 && studentId.startsWith("99")) {

            final char ch3 = studentId.charAt(2);
            final char ch4 = studentId.charAt(3);

            if (ch3 == 'P' && ch4 == 'L') {
                getPlacementTestStudent(studentId, result);
            } else if (ch3 == '9') {
                getMathPlanTestStudent(studentId, result);
            } else if (ch3 == 'C' && ch4 == 'I') {
                getCheckinTestStudent(studentId, result);
            }
        } else {
            Log.warning("Invalid test student ID: " + studentId);
        }

        return result;
    }

    /**
     * Counts placement attempts for a particular student on a particular version.
     *
     * @param cache   the data cache
     * @param stuId   the student ID
     * @param version the version of the exam
     * @return A 2-integer array, where [0] contains the number of legal online attempts, and [1] contains the number of
     *         legal proctored attempts; or {@code null} on any error
     * @throws SQLException if there is an error accessing the database
     */
    public static int[] countLegalAttempts(final Cache cache, final String stuId,
                                           final String version) throws SQLException {

        final int[] result;

        if (stuId.startsWith("99")) {
            result = countTestStudentLegalAttempts(stuId, version);
        } else {
            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

            // Count legal unproctored attempts
            final String sql1 = SimpleBuilder.concat("SELECT COUNT(*) FROM stmpe ",
                    " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                    " AND version=", LogicUtils.sqlStringValue(version),
                    " AND (placed='Y' OR placed='N') ",
                    " AND ((how_validated!='P' AND how_validated!='C') ",
                    "  OR how_validated IS NULL)");

            final int numOnline = executeIntQuery(cache, sql1);

            // Count legal proctored attempts
            final String sql2 = SimpleBuilder.concat("SELECT COUNT(*) FROM stmpe ",
                    " WHERE stu_id=", LogicUtils.sqlStringValue(stuId),
                    " AND version=", LogicUtils.sqlStringValue(version),
                    " AND (placed='Y' OR placed='N') ",
                    " AND (how_validated='P' OR how_validated='C')");

            final int numProctored = executeIntQuery(cache, sql2);

            result = new int[]{numOnline, numProctored};
        }

        return result;
    }

    /**
     * Retrieves a count of the legal attempts on record for a test student.
     *
     * @param studentId the ID of the student to query
     * @param examId    the exam ID for which to query
     * @return A 2-integer array, where [0] contains the number of legal online attempts, and [1] contains the number of
     *         legal proctored attempts
     */
    public static int[] countTestStudentLegalAttempts(final String studentId, final String examId) {

        final int[] result = new int[2];

        final boolean wasProctored = PPPPP.equals(examId) || MPTTC.equals(examId) || MPTPU.equals(examId)
                                     || "MPTRW".equals(examId);

        if (noResultsNoAttempts.contains(studentId)
            || placedOutOf117to124OnMPEHas0.contains(studentId)
            || placedOutOf117OnMPEHas0.contains(studentId)) {

            // Student has used all attempts
            if (POOOO.equals(examId) || MPTUN.equals(examId)) {
                result[0] = 1;
            } else if (wasProctored) {
                result[1] = 2;
            }

        } else if (almostPlacedOnMPEHas2MCE.contains(studentId)
                   || placedOutOf117to124OnMPEHas2MCE.contains(studentId)
                   || didNotPlaceOnMPEHas2MCE.contains(studentId)
                   || placedOutOf117to125OnMPEHas2MCE.contains(studentId)) {

            // Student has used MPE attempt, still has 2 MCE attempts
            if (POOOO.equals(examId) || MPTUN.equals(examId)) {
                result[0] = 1;
            }

        } else if (placedInto117OnMCEHas1MPE1MCE.contains(studentId)
                   || placedOutOf117to118OnMCEHas1MPE1MCE.contains(studentId)) {

            // Student has used one MCE attempt, still has one MPE and one MCE attempt
            if (wasProctored) {
                result[1] = 1;
            }

        } else if (placedInto117OnMCEHas1MPE.contains(studentId) || placedOutOf117OnMCEHas1MPE.contains(studentId)) {

            // Student has used both MCE attempts, still has MPE attempt
            if (wasProctored) {
                result[1] = 2;
            }
        }

        return result;
    }

    /**
     * Retrieves all {@code StudentPlacementAttempt} records created in the past N days (counting today), organized into
     * one list per day, sorted by activity date.
     *
     * @param cache   the data cache
     * @param history the list to which to add N lists of records
     * @param numDays the number of days (N)
     * @param today   today's date
     * @throws SQLException if there is an error accessing the database
     */
    public static void getHistory(final Cache cache, final Collection<? super List<RawStmpe>> history,
                                  final int numDays, final LocalDate today) throws SQLException {

        history.clear();

        // Get the earliest date for which to return data...
        LocalDate earliest;
        if (numDays <= 1) {
            earliest = today;
        } else {
            earliest = today.minus(Period.ofDays(numDays - 1));
        }

        final List<RawStmpe> all = queryOnOrAfter(cache, earliest);
        all.sort(new RawStmpe.FinishDateTimeComparator());

        int start = 0;
        int position = 0;
        final int size = all.size();
        for (int i = 0; i < numDays; ++i) {

            while (position < size && all.get(position).examDt.equals(earliest)) {
                ++position;
            }

            final List<RawStmpe> daily = new ArrayList<>(position - start);
            for (int j = start; j < position; ++j) {
                daily.add(all.get(j));
            }
            history.add(daily);
            start = position;

            earliest = earliest.plusDays(1L);
        }
    }

    /**
     * Retrieves all records that occurred on or after a specified date.
     *
     * @param cache    the data cache
     * @param earliest the earliest date to return
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawStmpe> queryOnOrAfter(final Cache cache, final LocalDate earliest) throws SQLException {

        final String sql = SimpleBuilder.concat("SELECT * FROM stmpe WHERE exam_dt>=",
                LogicUtils.sqlDateValue(earliest), " AND (placed='Y' OR placed='N')");

        return executeQuery(cache, sql);
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawStmpe> executeQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawStmpe> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawStmpe.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Executes a query that returns an integer.
     *
     * @param cache the data cache
     * @param sql   the SQL to execute
     * @return the result; 0 if query returned no records
     * @throws SQLException if there is an error accessing the database
     */
    private static int executeIntQuery(final Cache cache, final String sql) throws SQLException {

        int result = 0;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = rs.getInt(1);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}
