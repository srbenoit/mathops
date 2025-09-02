package dev.mathops.session;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.schema.ESchema;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.field.ETermName;
import dev.mathops.db.schema.LogicUtils;
import dev.mathops.db.schema.live.impl.ImplLiveRegFa;
import dev.mathops.db.schema.live.impl.ImplLiveRegSm;
import dev.mathops.db.schema.live.impl.ImplLiveRegSp;
import dev.mathops.db.schema.live.rec.LiveReg;
import dev.mathops.db.schema.main.rec.TermRec;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A cache that is actually a window to live registration data.
 */
enum LiveRegCache {
    ;

    /**
     * Queries for all live registration entries for a particular student, returning {@code null} if an error occurs,
     * and an empty list if no records are returned.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return the list of registrations, one record per current active registration
     * @throws SQLException if there is an error accessing the database
     */
    static List<LiveReg> queryLiveStudentRegs(final Cache cache, final String studentId) throws SQLException {

        List<LiveReg> result = new ArrayList<>(0);

//        Log.info("Querying live registrations for ", studentId);
        final long before = System.currentTimeMillis();

        final TermRec active = cache.getSystemData().getActiveTerm();

        final Login live = cache.profile.getLogin(ESchema.LIVE);

        try {
            final DbConnection liveConn = live.checkOutConnection();

            try {
                if (active.term.name == ETermName.SPRING) {
                    result = ImplLiveRegSp.INSTANCE.query(liveConn, studentId);
                } else if (active.term.name == ETermName.SUMMER) {
                    result = ImplLiveRegSm.INSTANCE.query(liveConn, studentId);
                } else if (active.term.name == ETermName.FALL) {
                    result = ImplLiveRegFa.INSTANCE.query(liveConn, studentId);
                } else {
                    final String msg = Res.fmt(Res.BAD_TERM_NAME, active.term.name);
                    Log.warning(msg);
                }
            } finally {
                live.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            LogicUtils.indicateBannerDown();
            Log.warning(ex);
        }

        final long after = System.currentTimeMillis();
        final String timerStr = Long.toString(after - before);
        final String msg = Res.fmt(Res.LIVE_REG_QUERY_TIMING, timerStr);
        Log.info(msg);

        return result;
    }
}
