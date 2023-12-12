package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawTreePath} class.
 */
final class TestRawTreePath {

    /** A field name. */
    private static final String TEST_IDENT = "1.2.3.4.5";

    /** A field name. */
    private static final String TEST_PARENT_IDENT = "1.2.3.4";

    /** A field name. */
    private static final Integer TEST_DEPTH = Integer.valueOf(8);

    /** A field name. */
    private static final Integer TEST_SORT_ORDER = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_LABEL = "Foo";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "ident=1.2.3.4.5",
            "parent_ident=1.2.3.4",
            "depth=8",
            "sort_order=1",
            "label=Foo");

    /**
     * Constructs a new {@code IvtRawTreePath}.
     */
    TestRawTreePath() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawTreePath obj = new RawTreePath(TEST_IDENT, TEST_PARENT_IDENT, TEST_DEPTH, TEST_SORT_ORDER, TEST_LABEL);

        assertEquals(TEST_IDENT, obj.ident, "Invalid ident value after constructor");
        assertEquals(TEST_PARENT_IDENT, obj.parentIdent, "Invalid parent_ident value after constructor");
        assertEquals(TEST_DEPTH, obj.depth, "Invalid depth value after constructor");
        assertEquals(TEST_SORT_ORDER, obj.sortOrder, "Invalid sort_order value after constructor");
        assertEquals(TEST_LABEL, obj.label, "Invalid label value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawTreePath obj = new RawTreePath(TEST_IDENT, TEST_PARENT_IDENT, TEST_DEPTH, TEST_SORT_ORDER, TEST_LABEL);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawTreePath obj = RawTreePath.parse(EXPECT_SER99);

        assertEquals(TEST_IDENT, obj.ident, "Invalid ident value after deserialization");
        assertEquals(TEST_PARENT_IDENT, obj.parentIdent, "Invalid parent_ident value after deserialization");
        assertEquals(TEST_DEPTH, obj.depth, "Invalid depth value after deserialization");
        assertEquals(TEST_SORT_ORDER, obj.sortOrder, "Invalid sort_order value after deserialization");
        assertEquals(TEST_LABEL, obj.label, "Invalid label value after deserialization");
    }
}
