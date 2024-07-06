package dev.mathops.session;

import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.ifaces.ILiveRegFa;
import dev.mathops.db.old.ifaces.ILiveRegSm;
import dev.mathops.db.old.ifaces.ILiveRegSp;
import dev.mathops.db.old.rawlogic.AbstractLogicModule;
import dev.mathops.db.old.rec.LiveReg;
import dev.mathops.db.old.schema.csubanner.ImplLiveRegFa;
import dev.mathops.db.old.schema.csubanner.ImplLiveRegSm;
import dev.mathops.db.old.schema.csubanner.ImplLiveRegSp;
import dev.mathops.db.old.svc.term.TermRec;

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

        Log.info("Querying live registrations for ", studentId);
        final long before = System.currentTimeMillis();

        final TermRec active = cache.getSystemData().getActiveTerm();

        final DbContext live = cache.dbProfile.getDbContext(ESchemaUse.LIVE);

        try {
            final DbConnection liveConn = live.checkOutConnection();

            try {
                if (active.term.name == ETermName.SPRING) {
                    final ILiveRegSp iface = ImplLiveRegSp.INSTANCE;

                    if (iface != null) {
                        result = iface.query(liveConn, studentId);
                    }
                } else if (active.term.name == ETermName.SUMMER) {
                    final ILiveRegSm iface = ImplLiveRegSm.INSTANCE;

                    if (iface != null) {
                        result = iface.query(liveConn, studentId);
                    }
                } else if (active.term.name == ETermName.FALL) {
                    final ILiveRegFa iface = ImplLiveRegFa.INSTANCE;

                    if (iface != null) {
                        result = iface.query(liveConn, studentId);
                    }
                } else {
                    Log.warning(Res.fmt(Res.BAD_TERM_NAME, active.term.name));
                }
            } finally {
                live.checkInConnection(liveConn);
            }
        } catch (final SQLException ex) {
            AbstractLogicModule.indicateBannerDown();
            Log.warning(ex);
        }

        final long after = System.currentTimeMillis();
        Log.info(Res.fmt(Res.LIVE_REG_QUERY_TIMING, Long.toString(after - before)));

        return result;
    }
}
