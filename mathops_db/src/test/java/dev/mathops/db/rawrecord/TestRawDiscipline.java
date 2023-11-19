package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawDiscipline} class.
 */
final class TestRawDiscipline {

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final LocalDate TEST_DT_INCIDENT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final String TEST_INCIDENT_TYPE = "12";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_CHEAT_DESC = "Magic 8-ball";

    /** A field name. */
    private static final String TEST_ACTION_TYPE = "01";

    /** A field name. */
    private static final String TEST_ACTION_COMMENT = "Confiscated 8-ball";

    /** A field name. */
    private static final String TEST_INTERVIEWER = "Jane Doe";

    /** A field name. */
    private static final String TEST_PROCTOR = "John Doe";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "stu_id=888888888",
            "dt_incident=2023-01-02",
            "incident_type=12",
            "course=M 117",
            "unit=2",
            "cheat_desc=Magic 8-ball",
            "action_type=01",
            "action_comment=Confiscated 8-ball",
            "interviewer=Jane Doe",
            "proctor=John Doe");

    /**
     * Constructs a new {@code IvtRawDiscipline}.
     */
    TestRawDiscipline() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawDiscipline obj = new RawDiscipline(TEST_STU_ID, TEST_DT_INCIDENT,
                TEST_INCIDENT_TYPE, TEST_COURSE, TEST_UNIT, TEST_CHEAT_DESC, TEST_ACTION_TYPE,
                TEST_ACTION_COMMENT, TEST_INTERVIEWER, TEST_PROCTOR);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after constructor");
        assertEquals(TEST_DT_INCIDENT, obj.dtIncident, "Invalid dt_incident value after constructor");
        assertEquals(TEST_INCIDENT_TYPE, obj.incidentType, "Invalid incident_type value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_CHEAT_DESC, obj.cheatDesc, "Invalid cheat_desc value after constructor");
        assertEquals(TEST_ACTION_TYPE, obj.actionType, "Invalid action_type value after constructor");
        assertEquals(TEST_ACTION_COMMENT, obj.actionComment, "Invalid action_comment value after constructor");
        assertEquals(TEST_INTERVIEWER, obj.interviewer, "Invalid interviewer value after constructor");
        assertEquals(TEST_PROCTOR, obj.proctor, "Invalid proctor value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawDiscipline obj = new RawDiscipline(TEST_STU_ID, TEST_DT_INCIDENT,
                TEST_INCIDENT_TYPE, TEST_COURSE, TEST_UNIT, TEST_CHEAT_DESC, TEST_ACTION_TYPE,
                TEST_ACTION_COMMENT, TEST_INTERVIEWER, TEST_PROCTOR);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawDiscipline obj = RawDiscipline.parse(EXPECT_SER99);

        assertEquals(TEST_STU_ID, obj.stuId, "Invalid stu_id value after deserialization");
        assertEquals(TEST_DT_INCIDENT, obj.dtIncident, "Invalid dt_incident value after deserialization");
        assertEquals(TEST_INCIDENT_TYPE, obj.incidentType, "Invalid incident_type value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after deserialization");
        assertEquals(TEST_CHEAT_DESC, obj.cheatDesc, "Invalid cheat_desc value after deserialization");
        assertEquals(TEST_ACTION_TYPE, obj.actionType, "Invalid action_type value after deserialization");
        assertEquals(TEST_ACTION_COMMENT, obj.actionComment, "Invalid action_comment value after deserialization");
        assertEquals(TEST_INTERVIEWER, obj.interviewer, "Invalid interviewer value after deserialization");
        assertEquals(TEST_PROCTOR, obj.proctor, "Invalid proctor value after deserialization");
    }
}
