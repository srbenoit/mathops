package dev.mathops.app.ops.snapin;

import dev.mathops.db.old.rawrecord.RawRecordConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that maps a course ID and section to a Canvas course ID.
 * <p>
 * For the moment, this class contains hard-codes - this data should be added to the CSECTION table int the database.
 */
public final class CanvasCourseIdMap {

    /** Map from course ID to map from section to Canvas course ID. */
    private final Map<String, Map<String, Long>> mappings;

    /**
     * Constructs a new {@code CanvasCourseIdMap}.
     */
    public CanvasCourseIdMap() {

        this.mappings = new HashMap<>(5);

        final Map<String, Long> m117 = new HashMap<>(4);
        m117.put("001", Long.valueOf(189937L));
        m117.put("002", Long.valueOf(191453L));
        m117.put("801", Long.valueOf(187864L));
        m117.put("809", Long.valueOf(190785L));
        this.mappings.put(RawRecordConstants.M117, m117);

        final Map<String, Long> m118 = new HashMap<>(4);
        m118.put("001", Long.valueOf(189942L));
        m118.put("002", Long.valueOf(191461L));
        m118.put("801", Long.valueOf(187859L));
        m118.put("809", Long.valueOf(190790L));
        this.mappings.put(RawRecordConstants.M118, m118);

        final Map<String, Long> m124 = new HashMap<>(4);
        m124.put("001", Long.valueOf(189946L));
        m124.put("002", Long.valueOf(191466L));
        m124.put("801", Long.valueOf(187854L));
        m124.put("809", Long.valueOf(190800L));
        this.mappings.put(RawRecordConstants.M124, m124);

        final Map<String, Long> m125 = new HashMap<>(4);
        m125.put("001", Long.valueOf(189949L));
        m125.put("002", Long.valueOf(191470L));
        m125.put("801", Long.valueOf(187849L));
        this.mappings.put(RawRecordConstants.M125, m125);

        final Map<String, Long> m126 = new HashMap<>(4);
        m126.put("001", Long.valueOf(189954L));
        m126.put("002", Long.valueOf(191473L));
        m126.put("801", Long.valueOf(187845L));
        this.mappings.put(RawRecordConstants.M126, m126);
    }

    /**
     * Returns the Canvas course ID associated with a course and section.
     *
     * @param course the course, such as "M 117"
     * @param sect   the section, such as "001"
     * @return the Canvas course ID; null if none found
     */
    public Long getCanvasId(final String course, final String sect) {

        Long result = null;

        final Map<String, Long> inner = this.mappings.get(course);

        if (inner != null) {
            result = inner.get(sect);
        }

        return result;
    }
}
