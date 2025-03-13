package dev.mathops.db.rec.main;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code FacilityRec} class.
 */
final class TestFacilityRec {

    /** A field name. */
    private static final String TEST_FACILITY_ID = "PRECALC LC";

    /** A field name. */
    private static final String TEST_NAME = "Precalculus Learning Center";

    /** A field name. */
    private static final String TEST_BUILDING = "Weber";

    /** A field name. */
    private static final String TEST_ROOM = "137";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "facility=PRECALC LC",
            "name=Precalculus Learning Center",
            "building=Weber",
            "room=137");

    /**
     * Constructs a new {@code TestFacilityRec}.
     */
    TestFacilityRec() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final FacilityRec obj = new FacilityRec(TEST_FACILITY_ID, TEST_NAME, TEST_BUILDING, TEST_ROOM);

        assertEquals(TEST_FACILITY_ID, obj.facility, "Invalid facility value after constructor");
        assertEquals(TEST_NAME, obj.name, "Invalid name value after constructor");
        assertEquals(TEST_BUILDING, obj.building, "Invalid building value after constructor");
        assertEquals(TEST_ROOM, obj.room, "Invalid room value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final FacilityRec obj = new FacilityRec(TEST_FACILITY_ID, TEST_NAME, TEST_BUILDING, TEST_ROOM);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
