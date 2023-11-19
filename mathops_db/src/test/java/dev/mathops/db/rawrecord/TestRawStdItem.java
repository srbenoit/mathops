package dev.mathops.db.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawStdItem} class.
 */
final class TestRawStdItem {

    /** A field name. */
    private static final String TEST_STD_ID = "S1.2.3";

    /** A field name. */
    private static final String TEST_ITEM_ID = "I4.5.6";

    /** A field name. */
    private static final Integer TEST_MASTERY_GRP_NBR = Integer.valueOf(100);

    /** A field name. */
    private static final Integer TEST_PRACTICE_GRP_NBR = Integer.valueOf(200);

    /** A field name. */
    private static final String TEST_FORMATIVE = "Y";

    /** A field name. */
    private static final String TEST_PLACEMENT = "N";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER, //
            "std_id=S1.2.3",
            "item_id=I4.5.6",
            "mastery_grp_nbr=100",
            "practice_grp_nbr=200",
            "formative=Y",
            "placement=N");

    /**
     * Constructs a new {@code IvtRawStdItem}.
     */
    TestRawStdItem() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStdItem obj = new RawStdItem(TEST_STD_ID, TEST_ITEM_ID, TEST_MASTERY_GRP_NBR,
                TEST_PRACTICE_GRP_NBR, TEST_FORMATIVE, TEST_PLACEMENT);

        assertEquals(TEST_STD_ID, obj.stdId, "Invalid std_id value after constructor");
        assertEquals(TEST_ITEM_ID, obj.itemId, "Invalid item_id value after constructor");
        assertEquals(TEST_MASTERY_GRP_NBR, obj.masteryGrpNbr, "Invalid version mastery_grp_nbr after constructor");
        assertEquals(TEST_PRACTICE_GRP_NBR, obj.practiceGrpNbr, "Invalid practice_grp_nbr value after constructor");
        assertEquals(TEST_FORMATIVE, obj.formative, "Invalid formative value after constructor");
        assertEquals(TEST_PLACEMENT, obj.placement, "Invalid placement value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStdItem obj = new RawStdItem(TEST_STD_ID, TEST_ITEM_ID, TEST_MASTERY_GRP_NBR,
                TEST_PRACTICE_GRP_NBR, TEST_FORMATIVE, TEST_PLACEMENT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawStdItem obj = RawStdItem.parse(EXPECT_SER99);

        assertEquals(TEST_STD_ID, obj.stdId, "Invalid std_id value after deserialization");
        assertEquals(TEST_ITEM_ID, obj.itemId, "Invalid item_id value after deserialization");
        assertEquals(TEST_MASTERY_GRP_NBR, obj.masteryGrpNbr, "Invalid version mastery_grp_nbr after deserialization");
        assertEquals(TEST_PRACTICE_GRP_NBR, obj.practiceGrpNbr,
                "Invalid practice_grp_nbr value after deserialization");
        assertEquals(TEST_FORMATIVE, obj.formative, "Invalid formative value after deserialization");
        assertEquals(TEST_PLACEMENT, obj.placement, "Invalid placement value after deserialization");
    }
}
