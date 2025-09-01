package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.db.Cache;
import dev.mathops.app.ops.snapin.CanvasCourseIdMap;
import dev.mathops.app.ops.snapin.messaging.CanvasMessageSend;
import dev.mathops.db.schema.RawRecordConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for senders connected to Canvas for each course.
 */
public final class CanvasMessageSenders {

    /** The mail senders, map from course to map from section to sender. */
    private final Map<String, Map<String, CanvasMessageSend>> senders;

    /**
     * Constructs a new {@code CanvasMessageSenders}.
     *
     * @param theCache    the cache
     * @param accessToken the Canvas access token
     */
    public CanvasMessageSenders(final Cache theCache, final String accessToken) {

        this.senders = new HashMap<>(5);
        final Map<String, CanvasMessageSend> senders117 = new HashMap<>(4);
        final Map<String, CanvasMessageSend> senders118 = new HashMap<>(4);
        final Map<String, CanvasMessageSend> senders124 = new HashMap<>(4);
        final Map<String, CanvasMessageSend> senders125 = new HashMap<>(4);
        final Map<String, CanvasMessageSend> senders126 = new HashMap<>(4);
        this.senders.put(RawRecordConstants.M117, senders117);
        this.senders.put(RawRecordConstants.M118, senders118);
        this.senders.put(RawRecordConstants.M124, senders124);
        this.senders.put(RawRecordConstants.M125, senders125);
        this.senders.put(RawRecordConstants.M126, senders126);
        this.senders.put(RawRecordConstants.MATH117, senders117);
        this.senders.put(RawRecordConstants.MATH118, senders118);
        this.senders.put(RawRecordConstants.MATH124, senders124);
        this.senders.put(RawRecordConstants.MATH125, senders125);
        this.senders.put(RawRecordConstants.MATH126, senders126);

        final CanvasCourseIdMap courseIdMap = new CanvasCourseIdMap();

        final CanvasMessageSend sender117ri = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M117, "002"));
//        senders117.put("001", sender117ri);
        senders117.put("002", sender117ri);

        final CanvasMessageSend sender117ce = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M117, "801"));
        senders117.put("401", sender117ce);
        senders117.put("801", sender117ce);
//        senders117.put("809", sender117ce);

//        final CanvasMessageSend sender118ri = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
//                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M118, "001"));
//        senders118.put("001", sender118ri);
//        senders118.put("002", sender118ri);

        final CanvasMessageSend sender118ce = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M118, "801"));
        senders118.put("401", sender118ce);
        senders118.put("801", sender118ce);
//        senders118.put("809", sender118ce);

//        final CanvasMessageSend sender124ri = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
//                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M124, "001"));
//        senders124.put("001", sender124ri);
//        senders124.put("002", sender124ri);

        final CanvasMessageSend sender124ce = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M124, "801"));
        senders124.put("401", sender124ce);
        senders124.put("801", sender124ce);
//        senders124.put("809", sender124ce);

//        final CanvasMessageSend sender125ri = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
//                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M125, "001"));
//        senders125.put("001", sender125ri);
//        senders125.put("002", sender125ri);

        final CanvasMessageSend sender125ce = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M125, "801"));
        senders125.put("401", sender125ce);
        senders125.put("801", sender125ce);
//        senders125.put("809", sender125ce);

//        final CanvasMessageSend sender126ri = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
//                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M126, "001"));
//        senders126.put("001", sender126ri);
//        senders126.put("002", sender126ri);

        final CanvasMessageSend sender126ce = new CanvasMessageSend(theCache, "https://colostate.instructure.com",
                accessToken, courseIdMap.getCanvasId(RawRecordConstants.M126, "801"));
        senders126.put("401", sender126ce);
        senders126.put("801", sender126ce);
//        senders126.put("809", sender126ce);
    }

    /**
     * Gets the Canvas sender for a specific course and section.
     *
     * @param course the course
     * @param sect   the section
     * @return the sender; {@code null} if none found
     */
    CanvasMessageSend getSender(final String course, final String sect) {

        CanvasMessageSend result = null;

        final Map<String, CanvasMessageSend> inner = this.senders.get(course);
        if (inner != null) {
            result = inner.get(sect);
        }

        return result;
    }
}
