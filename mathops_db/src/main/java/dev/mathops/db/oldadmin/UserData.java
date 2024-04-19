package dev.mathops.db.oldadmin;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawUserClearanceLogic;
import dev.mathops.db.old.rawrecord.RawUserClearance;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data about the logged-in user.
 *
 * <p>
 * Clearance functions include:
 * <ul>
 *     <li>LOAN</li>
 *     <li>PACING</li>
 *     <li>LOCK</li>
 *     <li>CAIC</li>
 *     <li>PENDEX</li>
 *     <li>FORMS</li>
 *     <li>COUPON</li>
 *     <li>ADHOLD</li>
 *     <li>COURSE</li>
 *     <li>PLACE</li>
 *     <li>ACTIVE</li>
 *     <li>DISCIP</li>
 *     <li>TSTCTR/li>
 *     <li>APPEAL</li>
 * </ul>
 */
class UserData {

    /** The list of permissions found. */
    private final List<RawUserClearance> permissions;

    /**
     * Constructs a new {@code UserData}.
     *
     * @param cache the cache to use to query user data
     */
    UserData(final Cache cache, final String username) {

        List<RawUserClearance> list = null;

        try {
            list = RawUserClearanceLogic.queryAllForLogin(cache, username);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        this.permissions = list == null ? new ArrayList<>(0) : list;
    }

    /**
     * Gets the clearance type for a specified clearance function.
     *
     * @param clearFunction the clearance function
     * @return the type; {@code null} if the function was not found
     */
    public Integer getClearType(final String clearFunction) {

        Integer result = null;

        if (clearFunction != null) {
            for (final RawUserClearance row : this.permissions) {
                if (clearFunction.equals(row.clearFunction)) {
                    result = row.clearType;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets the clearance password for a specified clearance function.
     *
     * @param clearFunction the clearance function
     * @return the type; {@code null} if the function was not found
     */
    public String getClearPassword(final String clearFunction) {

        String result = null;

        if (clearFunction != null) {
            for (final RawUserClearance row : this.permissions) {
                if (clearFunction.equals(row.clearFunction)) {
                    result = row.clearPasswd;
                    break;
                }
            }
        }

        return result;
    }
}
