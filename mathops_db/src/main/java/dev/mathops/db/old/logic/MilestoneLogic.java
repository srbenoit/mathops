package dev.mathops.db.old.logic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawMilestoneAppealLogic;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMilestoneAppeal;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.reclogic.StudentStandardMilestoneLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Logic associated with milestones for both legacy courses and standards-based courses.
 *
 * <p>
 * This class uses {@code RawMilestone}, {@code RawStmilestone}, {@code RawStdMilestone},  and
 * {@code RawStuStdMilestone} objects.  It requires that the pace and pace track are known, and can return effective
 * milestone dates for any course index and any milestone.
 *
 * <p>
 * This class can test what types of milestone extensions are currently available to a student for a milestone, and can
 * apply "automatic" or "on-request" extensions.
 */
public enum MilestoneLogic {
    ;

    /** A fixed local time used for synthetic milestone appeals records. */
    private static final LocalTime NOON = LocalTime.of(12, 0);

    /**
     * Determines the currently effective legacy milestone dates for a student in a specified course index within a
     * given pace and pace track.
     *
     * @param cache     the data cache
     * @param termKey   the term key
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @return the resolved milestones
     * @throws IllegalArgumentException if the cache, term key, student ID, or pace track is null, the pace is less than
     *                                  1 or greater than 5, or the index is less than 1 or greater than the pace, of if
     *                                  there is not a complete set of milestones defined for the specified pace and
     *                                  track in the specified term
     * @throws SQLException             if there is an error accessing the database
     */
    public static ResolvedLegacyMilestones resolveLegacyMilestones(final Cache cache, final TermKey termKey,
                                                                   final String stuId, final String paceTrack,
                                                                   final int pace, final int index)
            throws IllegalArgumentException, SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (termKey == null) {
            throw new IllegalArgumentException("Term key may not be null");
        }
        if (stuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (paceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (pace < 1 || pace > 5) {
            throw new IllegalArgumentException("Invalid pace value (" + pace + ")");
        }
        if (index < 1 || index > pace) {
            throw new IllegalArgumentException("Invalid course index value for pace " + pace + " (" + index + ")");
        }

        final Integer paceObj = Integer.valueOf(pace);

        final List<RawMilestone> milestones = cache.getSystemData().getMilestones(termKey, paceObj, paceTrack);
        if (milestones.isEmpty()) {
            throw new IllegalArgumentException("No milestones defined for pace " + pace + " track " + paceTrack
                                               + " in " + termKey.longString);
        }

        RawMilestone re1 = null;
        RawMilestone re2 = null;
        RawMilestone re3 = null;
        RawMilestone re4 = null;
        RawMilestone fe = null;
        RawMilestone f1 = null;

        for (final RawMilestone row : milestones) {
            if (row.getIndex() == index) {
                final int unit = row.getUnit();
                if (unit == 1) {
                    if ("RE".equals(row.msType)) {
                        re1 = row;
                    }
                } else if (unit == 2) {
                    if ("RE".equals(row.msType)) {
                        re2 = row;
                    }
                } else if (unit == 3) {
                    if ("RE".equals(row.msType)) {
                        re3 = row;
                    }
                } else if (unit == 4) {
                    if ("RE".equals(row.msType)) {
                        re4 = row;
                    }
                } else if (unit == 5) {
                    if ("FE".equals(row.msType)) {
                        fe = row;
                    } else if ("F1".equals(row.msType)) {
                        f1 = row;
                    }
                }
            }
        }

        if (re1 == null || re2 == null || re3 == null || re4 == null || fe == null || f1 == null) {
            throw new IllegalArgumentException("Incomplete milestones defined for pace " + pace + " track " + paceTrack
                                               + " in " + termKey.longString);
        }

        LocalDate re1Date = re1.msDate;
        LocalDate re2Date = re2.msDate;
        LocalDate re3Date = re3.msDate;
        LocalDate re4Date = re4.msDate;
        LocalDate feDate = fe.msDate;
        LocalDate f1Date = f1.msDate;
        Integer tries = f1.nbrAtmptsAllow;

        final List<RawStmilestone> stmilestones = RawStmilestoneLogic.getStudentMilestones(cache, termKey, paceTrack,
                stuId);

        for (final RawStmilestone row : stmilestones) {
            if ("RE".equals(row.msType)) {
                if (row.msNbr.equals(re1.msNbr)) {
                    re1Date = row.msDate;
                } else if (row.msNbr.equals(re2.msNbr)) {
                    re2Date = row.msDate;
                } else if (row.msNbr.equals(re3.msNbr)) {
                    re3Date = row.msDate;
                } else if (row.msNbr.equals(re4.msNbr)) {
                    re4Date = row.msDate;
                }
            } else if ("FE".equals(row.msType)) {
                if (row.msNbr.equals(fe.msNbr)) {
                    feDate = row.msDate;
                }
            } else if ("F1".equals(row.msType)) {
                if (row.msNbr.equals(f1.msNbr)) {
                    f1Date = row.msDate;
                    if (row.nbrAtmptsAllow != null) {
                        tries = row.nbrAtmptsAllow;
                    }
                }
            }
        }

        return new ResolvedLegacyMilestones(re1Date, re2Date, re3Date, re4Date, feDate, f1Date, tries);
    }

    /**
     * Determines the currently effective standard milestone dates for a student in a specified course index within a
     * given pace and pace track.
     *
     * @param cache     the data cache
     * @param termKey   the term key
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @return the resolved milestones
     * @throws IllegalArgumentException if the cache, term key, student ID, or pace track is null, the pace is less than
     *                                  1 or greater than 5, or the index is less than 1 or greater than the pace, of if
     *                                  there is not a complete set of milestones defined for the specified pace and
     *                                  track in the specified term
     * @throws SQLException             if there is an error accessing the database
     */
    public static ResolvedStandardMilestones resolveStandardMilestones(final Cache cache, final TermKey termKey,
                                                                       final String stuId, final String paceTrack,
                                                                       final int pace, final int index)
            throws IllegalArgumentException, SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (termKey == null) {
            throw new IllegalArgumentException("Term key may not be null");
        }
        if (stuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (paceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (pace < 1 || pace > 5) {
            throw new IllegalArgumentException("Invalid pace value (" + pace + ")");
        }
        if (index < 1 || index > pace) {
            throw new IllegalArgumentException("Invalid course index value for pace " + pace + " (" + index + ")");
        }

        final Integer paceObj = Integer.valueOf(pace);
        final Integer indexObj = Integer.valueOf(index);

        final List<StandardMilestoneRec> milestones = cache.getSystemData().getStandardMilestonesForPaceTrackIndex(
                paceTrack, paceObj, indexObj);

        if (milestones.isEmpty()) {
            throw new IllegalArgumentException("No standard milestones defined for pace " + pace + " track " + paceTrack
                                               + " index " + index);
        }

        final StandardMilestoneRec[][] mastery = new StandardMilestoneRec[8][3];

        for (final StandardMilestoneRec row : milestones) {
            if ("MA".equals(row.msType)) {
                if (row.unit == null || row.objective == null) {
                    Log.warning("Unexpected standard milestone of type ", row.msType);
                    continue;
                }

                final int unit = row.unit.intValue() - 1;
                final int obj = row.objective.intValue() - 1;

                if (unit < 0 || unit > 7 || obj < 0 || obj > 2) {
                    Log.warning("Unexpected standard milestone for unit ", row.unit, " objective ", row.objective);
                    continue;
                }

                mastery[unit][obj] = row;
            }
        }

        final LocalDate[][] dates = new LocalDate[8][3];

        for (int unit = 0; unit < 7; ++unit) {
            for (int obj = 0; obj < 7; ++obj) {
                if (mastery[unit][obj] == null) {
                    throw new IllegalArgumentException("Missing standard Mastery milestone for unit " +
                                                       (unit + 1) + " objective " + (obj + 1));
                }
                dates[unit][obj] = mastery[unit][obj].msDate;
            }
        }

        final List<StudentStandardMilestoneRec> stmilestones =
                StudentStandardMilestoneLogic.get(cache).queryByStuPaceTrackPaceIndex(cache, stuId, paceTrack, paceObj,
                        indexObj);

        for (final StudentStandardMilestoneRec row : stmilestones) {
            if ("MA".equals(row.msType)) {
                if (row.unit == null || row.objective == null) {
                    Log.warning("Unexpected student standard milestone of type ", row.msType);
                    continue;
                }

                final int unit = row.unit.intValue() - 1;
                final int obj = row.objective.intValue() - 1;

                if (unit < 0 || unit > 7 || obj < 0 || obj > 2) {
                    Log.warning("Unexpected student standard milestone for unit ", row.unit, " objective ",
                            row.objective);
                    continue;
                }

                dates[unit][obj] = row.msDate;
            }
        }

        return new ResolvedStandardMilestones(dates);
    }

    /**
     * Gets the set of milestone appeals relevant to a pace track, pace, and course index.  This method converts any
     * "pace_appeals" records found into equivalent "milestone_appeal" records, with an inferred appeal type.  The
     * results of this method should not be used to perform updates.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @return the list of milestone appeals on record, ordered by appeal date/time
     * @throws IllegalArgumentException if the cache, student ID, or pace track is null, the pace is less than 1 or
     *                                  greater than 5, or the index is less than 1 or greater than the pace
     * @throws SQLException             if there is an error accessing the database
     */
    public static List<RawMilestoneAppeal> getAppeals(final Cache cache, final String stuId, final String paceTrack,
                                                      final int pace, final int index)
            throws IllegalArgumentException, SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (stuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (paceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (pace < 1 || pace > 5) {
            throw new IllegalArgumentException("Invalid pace value (" + pace + ")");
        }
        if (index < 1 || index > pace) {
            throw new IllegalArgumentException("Invalid course index value for pace " + pace + " (" + index + ")");
        }

        final List<RawMilestoneAppeal> all = RawMilestoneAppealLogic.queryByStudent(cache, stuId);
        final int count = all.size();
        final int min = Math.min(10, count);
        final List<RawMilestoneAppeal> matching = new ArrayList<>(min);

        for (final RawMilestoneAppeal test : all) {
            if (Objects.equals(test.paceTrack, paceTrack) && test.pace != null && test.pace.intValue() == pace
                && test.msNbr != null) {
                final int number = test.msNbr.intValue();

                final int numberIndex;
                if (number >= 1000) {
                    // 4321 means pace 4, course index 3, unit 2, objective 1
                    numberIndex = (number / 100) % 10;
                } else {
                    // 432 means pace 4, course index 3, unit 2
                    numberIndex = (number / 10) % 10;
                }

                if (numberIndex == index) {
                    matching.add(test);
                }
            }
        }

        // Create synthetic "RawMilestoneAppeal" records for legacy "RawPaceAppeals" records
        final List<RawPaceAppeals> legacy = RawPaceAppealsLogic.queryByStudent(cache, stuId);

        for (final RawPaceAppeals test : legacy) {
            if (Objects.equals(test.paceTrack, paceTrack) && test.pace != null && test.pace.intValue() == pace
                && test.msNbr != null) {
                final int number = test.msNbr.intValue();
                final int numberIndex = (number / 10) % 10;
                if (numberIndex == index) {
                    final LocalDateTime synthAppealDateTime = LocalDateTime.of(test.appealDt, NOON);
                    final String synthAppealType;
                    if (test.circumstances == null || test.circumstances.isEmpty()) {
                        synthAppealType = RawMilestoneAppeal.APPEAL_TYPE_OTH;
                    } else {
                        final String circumstances = test.circumstances.toLowerCase(Locale.ROOT);

                        if (circumstances.contains("sdc") || circumstances.contains("rds")) {
                            synthAppealType = RawMilestoneAppeal.APPEAL_TYPE_ACC;
                        } else if (circumstances.contains("university excused")
                                   || test.circumstances.contains("university-excused")) {
                            synthAppealType = RawMilestoneAppeal.APPEAL_TYPE_EXC;
                        } else if (circumstances.contains("family emergency")) {
                            synthAppealType = RawMilestoneAppeal.APPEAL_TYPE_FAM;
                        } else if (circumstances.contains("medical") || test.circumstances.contains("doctor")
                                   || test.circumstances.contains("hospital")) {
                            synthAppealType = RawMilestoneAppeal.APPEAL_TYPE_MED;
                        } else {
                            synthAppealType = RawMilestoneAppeal.APPEAL_TYPE_OTH;
                        }
                    }

                    final RawMilestoneAppeal synthetic = new RawMilestoneAppeal(
                            test.termKey, test.stuId, synthAppealDateTime, synthAppealType, test.pace, test.paceTrack,
                            test.msNbr, test.msType, test.msDate, test.newDeadlineDt, test.nbrAtmptsAllow,
                            test.circumstances, test.comment, test.interviewer);

                    matching.add(synthetic);
                }
            }
        }

        matching.sort(null);

        return matching;
    }

    /**
     * Determines whether a student has an available (unused) accommodation extension for a legacy milestone.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 5)
     * @param msType    the milestone type (RE or FE; F1 milestones are not appealed separately)
     * @return the number of days of SDC extension available; 0 if the extension has already been used, -1 if this
     *         student does not exist or does not have accommodation extensions available
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    public static int daysAvailableLegacyAccommodationExtension(final Cache cache, final String stuId,
                                                                final String paceTrack, final int pace, final int index,
                                                                final int unit, final String msType)
            throws IllegalArgumentException, SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (stuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (paceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (pace < 1 || pace > 5) {
            throw new IllegalArgumentException("Invalid pace value (" + pace + ")");
        }
        if (index < 1 || index > pace) {
            throw new IllegalArgumentException("Invalid course index value for pace " + pace + " (" + index + ")");
        }
        if (unit < 1 || unit > 5) {
            throw new IllegalArgumentException("Invalid unit value (" + unit + ")");
        }

        int result = 0;

        final StudentData studentData = cache.getStudent(stuId);
        if (studentData == null) {
            result = -1;
        } else {
            final RawStudent stu = studentData.getStudentRecord();
            if (stu == null || stu.extensionDays == null || stu.extensionDays.intValue() == 0) {
                result = -1;
            } else {
                result = stu.extensionDays;

                final List<RawMilestoneAppeal> all = RawMilestoneAppealLogic.queryByStudent(cache, stuId);
                for (final RawMilestoneAppeal test : all) {

                    if (RawMilestoneAppeal.APPEAL_TYPE_ACC.equals(test.appealType)
                        && Objects.equals(test.paceTrack, paceTrack) && test.pace != null
                        && test.pace.intValue() == pace && test.msNbr != null && test.msType.equals(msType)) {

                        final int number = test.msNbr.intValue();
                        if (number < 1000) {
                            // This is a legacy extension: 432 means pace 4, course index 3, unit 2
                            final int numberIndex = (number / 10) % 10;
                            final int numberUnit = number % 10;

                            if (numberIndex == index && numberUnit == unit) {
                                // Already used the accommodation appeal on this milestone.
                                result = 0;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Determines whether a student has an available (unused) free extension for a legacy milestone that they can
     * request.  The number of days of "free extension" allowed is configured as part of the pacing structure.  The
     * student's STUDENT record must have a pacing structure set when this call is made.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 5)
     * @param msType    the milestone type (RE or FE; F1 milestones are not appealed separately)
     * @return the number of days of free extension available; 0 if the extension has already been used, -1 if this
     *         student does not exist or their pacing structure does not allow free extensions
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    public static int daysAvailableLegacyFreeExtension(final Cache cache, final String stuId, final String paceTrack,
                                                       final int pace, final int index, final int unit,
                                                       final String msType)
            throws IllegalArgumentException, SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (stuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (paceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (pace < 1 || pace > 5) {
            throw new IllegalArgumentException("Invalid pace value (" + pace + ")");
        }
        if (index < 1 || index > pace) {
            throw new IllegalArgumentException("Invalid course index value for pace " + pace + " (" + index + ")");
        }
        if (unit < 1 || unit > 5) {
            throw new IllegalArgumentException("Invalid unit value (" + unit + ")");
        }

        int result = 0;

        final StudentData studentData = cache.getStudent(stuId);
        if (studentData == null) {
            Log.warning("Unable to create student data object for ", stuId);
            result = -1;
        } else {
            final SystemData systemData = cache.getSystemData();
            final TermRec active = systemData.getActiveTerm();

            final RawStudent stu = studentData.getStudentRecord();
            if (stu == null || stu.pacingStructure == null || active == null) {
                Log.warning("Unable to determine pacing structure for ", stuId);
                result = -1;
            } else {
                final RawPacingStructure pacing = systemData.getPacingStructure(stu.pacingStructure, active.term);
                if (pacing == null || pacing.freeExtensionDays == null) {
                    result = -1;
                } else {
                    result = pacing.freeExtensionDays.intValue();

                    final List<RawMilestoneAppeal> all = RawMilestoneAppealLogic.queryByStudent(cache, stuId);
                    for (final RawMilestoneAppeal test : all) {

                        if ((RawMilestoneAppeal.APPEAL_TYPE_REQ.equals(test.appealType)
                             || RawMilestoneAppeal.APPEAL_TYPE_AUT.equals(test.appealType))
                            && test.paceTrack.equals(paceTrack)
                            && test.pace != null && test.pace.intValue() == pace && test.msNbr != null
                            && test.msType.equals(msType)) {

                            final int number = test.msNbr.intValue();
                            if (number < 1000) {
                                // This is a legacy extension: 432 means pace 4, course index 3, unit 2
                                final int numberIndex = (number / 10) % 10;
                                final int numberUnit = number % 10;

                                if (numberIndex == index && numberUnit == unit) {
                                    // Already used the free appeal on this milestone.
                                    result = 0;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Determines whether a student has an available (unused) accommodation extension for a standards-based milestone.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 8)
     * @param objective the objective (1 - 3)
     * @param msType    the milestone type (RE or FE; F1 milestones are not appealed separately)
     * @return the number of days of SDC extension available; 0 if the extension has already been used, -1 if this
     *         student does not exist or does not have accommodation extensions available
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    public static int daysAvailableStandardAccommodationExtension(final Cache cache, final String stuId,
                                                                  final String paceTrack, final int pace,
                                                                  final int index, final int unit, final int objective,
                                                                  final String msType)
            throws IllegalArgumentException, SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (stuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (paceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (pace < 1 || pace > 5) {
            throw new IllegalArgumentException("Invalid pace value (" + pace + ")");
        }
        if (index < 1 || index > pace) {
            throw new IllegalArgumentException("Invalid course index value for pace " + pace + " (" + index + ")");
        }
        if (unit < 1 || unit > 8) {
            throw new IllegalArgumentException("Invalid unit value (" + unit + ")");
        }
        if (objective < 1 || objective > 3) {
            throw new IllegalArgumentException("Invalid objective value (" + objective + ")");
        }

        int result = 0;

        final StudentData studentData = cache.getStudent(stuId);
        if (studentData == null) {
            result = -1;
        } else {
            final RawStudent stu = studentData.getStudentRecord();
            if (stu == null || stu.extensionDays == null || stu.extensionDays.intValue() == 0) {
                result = -1;
            } else {
                result = stu.extensionDays;

                final List<RawMilestoneAppeal> all = RawMilestoneAppealLogic.queryByStudent(cache, stuId);
                for (final RawMilestoneAppeal test : all) {

                    if (RawMilestoneAppeal.APPEAL_TYPE_ACC.equals(test.appealType) && test.paceTrack.equals(paceTrack)
                        && test.pace != null && test.pace.intValue() == pace && test.msNbr != null
                        && test.msType.equals(msType)) {

                        final int number = test.msNbr.intValue();
                        if (number >= 1000) {
                            // This is a standards-based extension: 4321 means pace 4, course index 3, unit 2, obj. 1
                            final int numberIndex = (number / 100) % 10;
                            final int numberUnit = (number / 10) % 10;
                            final int numberObj = number % 10;

                            if (numberIndex == index && numberUnit == unit && numberObj == objective) {
                                // Already used the accommodation appeal on this milestone.
                                result = 0;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Determines whether a student has an available (unused) free extension for a standards-based milestone that they
     * can request.  The number of days of "free extension" allowed is configured as part of the pacing structure.  The
     * student's STUDENT record must have a pacing structure set when this call is made.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 5)
     * @param msType    the milestone type (RE or FE; F1 milestones are not appealed separately)
     * @return the number of days of free extension available; 0 if the extension has already been used, -1 if this
     *         student does not exist or their pacing structure does not allow free extensions
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    public static int daysAvailableStandardFreeExtension(final Cache cache, final String stuId, final String paceTrack,
                                                         final int pace, final int index, final int unit,
                                                         final int objective, final String msType)
            throws IllegalArgumentException, SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (stuId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (paceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (pace < 1 || pace > 5) {
            throw new IllegalArgumentException("Invalid pace value (" + pace + ")");
        }
        if (index < 1 || index > pace) {
            throw new IllegalArgumentException("Invalid course index value for pace " + pace + " (" + index + ")");
        }
        if (unit < 1 || unit > 8) {
            throw new IllegalArgumentException("Invalid unit value (" + unit + ")");
        }
        if (objective < 1 || objective > 3) {
            throw new IllegalArgumentException("Invalid objective value (" + objective + ")");
        }

        int result = 0;

        final StudentData studentData = cache.getStudent(stuId);
        if (studentData == null) {
            result = -1;
        } else {
            final SystemData systemData = cache.getSystemData();
            final TermRec active = systemData.getActiveTerm();

            final RawStudent stu = studentData.getStudentRecord();
            if (stu == null || stu.pacingStructure == null || active == null) {
                result = -1;
            } else {
                final RawPacingStructure pacing = systemData.getPacingStructure(stu.pacingStructure, active.term);
                if (pacing == null || pacing.freeExtensionDays == null) {
                    result = -1;
                } else {
                    result = pacing.freeExtensionDays.intValue();

                    final List<RawMilestoneAppeal> all = RawMilestoneAppealLogic.queryByStudent(cache, stuId);
                    for (final RawMilestoneAppeal test : all) {

                        if ((RawMilestoneAppeal.APPEAL_TYPE_REQ.equals(test.appealType)
                             || RawMilestoneAppeal.APPEAL_TYPE_AUT.equals(test.appealType))
                            && test.paceTrack.equals(paceTrack)
                            && test.pace != null && test.pace.intValue() == pace && test.msNbr != null
                            && test.msType.equals(msType)) {

                            final int number = test.msNbr.intValue();
                            if (number >= 1000) {
                                // This is a standards-based extension:
                                // 4321 means pace 4, course index 3, unit 2, objective 1
                                final int numberIndex = (number / 100) % 10;
                                final int numberUnit = (number / 10) % 10;
                                final int numberObj = number % 10;

                                if (numberIndex == index && numberUnit == unit && numberObj == objective) {
                                    // Already used the free appeal on this milestone.
                                    result = 0;
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Applies an accommodation extension for a student in a legacy course, based on a request by that student in the
     * website (as opposed to an interview with the student through the normal appeal process). This inserts a milestone
     * appeal record, with its new deadline date calculated from the most recent milestone appeal or pace appeal record.
     * If there is a "stmilestone" row, its deadline date is updated to the new deadline date, and if not, a new
     * "stmilestone" row is created with the new deadline date.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 5)
     * @param msType    the milestone type (RE or FE; F1 milestones are not appealed separately)
     * @return the number of days of extension added; 0 if the extension has already been used, -1 if this student does
     *         not exist or does not have accommodation extensions available (if this number is greater than 100, then
     *         the value mod 100 is the number of extension days granted, and the value divided by 100 is the number of
     *         extension days they had available, but which would have gone past the end of the term.  In this
     *         situation, the student should get a message that we were not able to add the full accommodation, and that
     *         they should contact the instructional team to discuss options if they need more time.
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    public static int applyLegacyAccommodationExtension(final Cache cache, final String stuId, final String paceTrack,
                                                        final int pace, final int index, final int unit,
                                                        final String msType)
            throws IllegalArgumentException, SQLException {

        // Make sure the student is eligible, and find out the number of days they get
        final int days = daysAvailableLegacyAccommodationExtension(cache, stuId, paceTrack, pace, index, unit, msType);
        int added = 0;

        if (days > 0) {
            final TermRec active = cache.getSystemData().getActiveTerm();

            if (active == null) {
                Log.warning("Failed to query for active term.");
                added = -1;
            } else {
                final TermKey activeKey = active.term;
                final ResolvedLegacyMilestones milestones = resolveLegacyMilestones(cache, activeKey, stuId,
                        paceTrack, pace, index);

                if ("RE".equals(msType)) {
                    if (unit == 1) {
                        final LocalDate origDate = milestones.re1();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 1, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_ACC, origDate, days);
                    } else if (unit == 2) {
                        final LocalDate origDate = milestones.re2();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 2, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_ACC, origDate, days);
                    } else if (unit == 3) {
                        final LocalDate origDate = milestones.re3();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 3, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_ACC, origDate, days);
                    } else if (unit == 4) {
                        final LocalDate origDate = milestones.re4();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 4, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_ACC, origDate, days);
                    } else {
                        Log.warning("Invalid unit number for RE extension request (" + unit + ")");
                        added = -1;
                    }
                } else if ("FE".equals(msType)) {
                    if (unit == 5) {
                        final LocalDate origDate = milestones.fe();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 5, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_ACC, origDate, days);
                    } else {
                        Log.warning("Invalid unit number for FE extension request (" + unit + ")");
                        added = -1;
                    }
                }
            }
        } else {
            Log.warning("Request to apply legacy accommodation extension when none is available.");
            added = days;
        }

        return added;
    }

    /**
     * Applies a free extension for a student in a legacy course, based on a request by that student in the website (as
     * opposed to an interview with the student through the normal appeal process). This inserts a milestone appeal
     * record, with its new deadline date calculated from the most recent milestone appeal or pace appeal record.  If
     * there is a "stmilestone" row, its deadline date is updated to the new deadline date, and if not, a new
     * "stmilestone" row is created with the new deadline date.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 5)
     * @param msType    the milestone type (RE or FE; F1 milestones are not appealed separately)
     * @return the number of days of extension added; 0 if the extension has already been used, -1 if this student does
     *         not exist or does not have accommodation extensions available (note this number may be less than the
     *         number of free extension days configured if the full extension would go past the end of the term)
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    public static int applyLegacyFreeExtension(final Cache cache, final String stuId, final String paceTrack,
                                               final int pace, final int index, final int unit, final String msType)
            throws IllegalArgumentException, SQLException {

        // Make sure the student is eligible, and find out the number of days they get
        final int days = daysAvailableLegacyFreeExtension(cache, stuId, paceTrack, pace, index, unit, msType);
        int added = 0;

        if (days > 0) {
            final TermRec active = cache.getSystemData().getActiveTerm();

            if (active == null) {
                Log.warning("Failed to query for active term.");
                added = -1;
            } else {
                final TermKey activeKey = active.term;
                final ResolvedLegacyMilestones milestones = resolveLegacyMilestones(cache, activeKey, stuId,
                        paceTrack, pace, index);

                if ("RE".equals(msType)) {
                    if (unit == 1) {
                        final LocalDate origDate = milestones.re1();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 1, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_REQ, origDate, days);
                    } else if (unit == 2) {
                        final LocalDate origDate = milestones.re2();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 2, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_REQ, origDate, days);
                    } else if (unit == 3) {
                        final LocalDate origDate = milestones.re3();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 3, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_REQ, origDate, days);
                    } else if (unit == 4) {
                        final LocalDate origDate = milestones.re4();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 4, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_REQ, origDate, days);
                    } else {
                        Log.warning("Invalid unit number for RE free extension request (" + unit + ")");
                        added = -1;
                    }
                } else if ("FE".equals(msType)) {
                    if (unit == 5) {
                        final LocalDate origDate = milestones.fe();
                        added = buildLegacyExtensionData(cache, activeKey, stuId, paceTrack, pace, index, 5, msType,
                                RawMilestoneAppeal.APPEAL_TYPE_REQ, origDate, days);
                    } else {
                        Log.warning("Invalid unit number for FE free extension request (" + unit + ")");
                        added = -1;
                    }
                }
            }
        } else {
            Log.warning("Request to apply legacy free extension when none is available.");
            added = days;
        }

        return added;
    }

    /**
     * Builds the data to denote an extension in a legacy course.
     *
     * @param cache   the data cache
     * @param current the current deadline date
     * @param days    the number of days of extension to give
     * @return the number of days of extension added; 0 if the extension has already been used, -1 if this student does
     *         not exist or does not have accommodation extensions available (if this number is greater than 100, then
     *         the value mod 100 is the number of extension days granted, and the value divided by 100 is the number of
     *         extension days they had available, but which would have gone past the end of the term.  In this
     *         situation, the student should get a message that we were not able to add the full accommodation, and that
     *         they should contact the instructional team to discuss options if they need more time.
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    private static int buildLegacyExtensionData(final Cache cache, final TermKey termKey, final String stuId,
                                                final String paceTrack, final int pace, final int index, final int unit,
                                                final String msType, final String appealType, final LocalDate current,
                                                final int days) throws SQLException {

        int added;

        final LocalDateTime now = LocalDateTime.now();
        final Integer paceObj = Integer.valueOf(pace);
        final Integer msNbr = Integer.valueOf(pace * 100 + index * 10 + unit);

        final LocalDate newDeadline = TermCalendarLogic.nextOpenDay(cache, current, days);
        if (newDeadline == null) {
            // Not enough days before the end of term
            added = days - 1;
            LocalDate shortExtension = TermCalendarLogic.nextOpenDay(cache, current, added);
            while (added > 0 && shortExtension == null) {
                --added;
                shortExtension = TermCalendarLogic.nextOpenDay(cache, current, added);
            }
            if (added > 0) {
                final String comment = "Only able to add " + added + " days before end of term, student was allowed "
                                       + days;

                final RawMilestoneAppeal appealRecord = new RawMilestoneAppeal(termKey, stuId, now, appealType, paceObj,
                        paceTrack, msNbr, msType, current, shortExtension, null, "Requested extension via website",
                        comment, "websites");

                if (!RawMilestoneAppealLogic.INSTANCE.insert(cache, appealRecord)) {
                    Log.warning("Failed to insert MILESTONE_APPEAL record for requested extension");
                }

                if (!buildStmilestone(cache, termKey, stuId, paceTrack, msNbr, msType, shortExtension)) {
                    Log.warning("Failed to update STMILESTONE with new deadline");
                }
            }
            added += 100 * days;
        } else {
            added = days;

            final RawMilestoneAppeal appealRecord = new RawMilestoneAppeal(termKey, stuId, now, appealType, paceObj,
                    paceTrack, msNbr, msType, current, newDeadline, null, "Requested extension via website",
                    CoreConstants.EMPTY, "websites");

            if (!RawMilestoneAppealLogic.INSTANCE.insert(cache, appealRecord)) {
                Log.warning("Failed to insert MILESTONE_APPEAL record for requested extension");
            }

            if (!buildStmilestone(cache, termKey, stuId, paceTrack, msNbr, msType, newDeadline)) {
                Log.warning("Failed to update STMILESTONE with new deadline");
            }
        }

        return added;
    }

    /**
     * Builds the STMILESTONE record by either updating an existing record in place or adding a new record.
     *
     * @param cache       the data cache
     * @param termKey     the term key
     * @param stuId       the student ID
     * @param paceTrack   the pace track
     * @param msNbr       the milestone number
     * @param msType      the milestone type
     * @param newDeadline the new deadline date
     * @return true if data wazS successfully updated; false if not
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean buildStmilestone(final Cache cache, final TermKey termKey, final String stuId,
                                            final String paceTrack, final Integer msNbr, final String msType,
                                            final LocalDate newDeadline)
            throws SQLException {

        final List<RawStmilestone> all = RawStmilestoneLogic.getStudentMilestones(cache, termKey, paceTrack, stuId);
        RawStmilestone existing = null;
        for (final RawStmilestone test : all) {
            if (test.msNbr.equals(msNbr) && test.msType.equals(msType)) {
                existing = test;
                break;
            }
        }

        final boolean result;
        if (existing == null) {
            final RawStmilestone add = new RawStmilestone(termKey, stuId, paceTrack, msNbr, msType, newDeadline, null);
            result = RawStmilestoneLogic.INSTANCE.insert(cache, add);
        } else {
            existing.msDate = newDeadline;
            result = RawStmilestoneLogic.update(cache, existing);
        }

        return result;
    }

    /**
     * Applies an accommodation extension for a student in a standards-based course, based on a request by that student
     * in the website (as opposed to an interview with the student through the normal appeal process). This inserts a
     * milestone appeal record, with its new deadline date calculated from the most recent milestone appeal or pace
     * appeal record.  If there is a "stmilestone" row, its deadline date is updated to the new deadline date, and if
     * not, a new "stmilestone" row is created with the new deadline date.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 8)
     * @param objective the objective (1 - 3)
     * @param msType    the milestone type (currently only MA are supported)
     * @return the number of days of extension added; 0 if the extension has already been used, -1 if this student does
     *         not exist or does not have accommodation extensions available (if this number is greater than 100, then
     *         the value mod 100 is the number of extension days granted, and the value divided by 100 is the number of
     *         extension days they had available, but which would have gone past the end of the term.  In this
     *         situation, the student should get a message that we were not able to add the full accommodation, and that
     *         they should contact the instructional team to discuss options if they need more time.
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 8, or the objective is less than 1
     *                                  or greater than 3
     * @throws SQLException             if there is an error accessing the database
     */
    public static int applyStandardAccommodationExtension(final Cache cache, final String stuId, final String paceTrack,
                                                          final int pace, final int index, final int unit,
                                                          final int objective, final String msType)
            throws IllegalArgumentException, SQLException {

        // Make sure the student is eligible, and find out the number of days they get
        final int days = daysAvailableStandardAccommodationExtension(cache, stuId, paceTrack, pace, index, unit,
                objective, msType);
        int added = 0;

        if (days > 0) {
            final TermRec active = cache.getSystemData().getActiveTerm();

            if (active == null) {
                Log.warning("Failed to query for active term.");
                added = -1;
            } else {
                final TermKey activeKey = active.term;
                final ResolvedStandardMilestones milestones = resolveStandardMilestones(cache, activeKey, stuId,
                        paceTrack, pace, index);

                if ("MA".equals(msType)) {
                    final LocalDate origDate = milestones.dates()[unit - 1][objective - 1];
                    added = buildStandardExtensionData(cache, activeKey, stuId, paceTrack, pace, index, unit, objective,
                            msType, RawMilestoneAppeal.APPEAL_TYPE_ACC, origDate, days);
                }
            }
        } else {
            Log.warning("Request to apply standard accommodation extension when none is available.");
            added = days;
        }

        return added;
    }

    /**
     * Applies a free extension for a student in a standards-based course, based on a request by that student in the
     * website (as opposed to an interview with the student through the normal appeal process). This inserts a milestone
     * appeal record, with its new deadline date calculated from the most recent milestone appeal or pace appeal record.
     * If there is a "stmilestone" row, its deadline date is updated to the new deadline date, and if not, a new
     * "stmilestone" row is created with the new deadline date.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index within the pace
     * @param unit      the unit (1 - 8)
     * @param objective the objective (1 - 3)
     * @param msType    the milestone type (RE or FE; F1 milestones are not appealed separately)
     * @return the number of days of extension added; 0 if the extension has already been used, -1 if this student does
     *         not exist or does not have accommodation extensions available (note this number may be less than the
     *         number of free extension days configured if the full extension would go past the end of the term)
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    public static int applyStandardFreeExtension(final Cache cache, final String stuId, final String paceTrack,
                                                 final int pace, final int index, final int unit, final int objective,
                                                 final String msType) throws IllegalArgumentException, SQLException {

        // Make sure the student is eligible, and find out the number of days they get
        final int days = daysAvailableStandardFreeExtension(cache, stuId, paceTrack, pace, index, unit, objective,
                msType);
        int added = 0;

        if (days > 0) {
            final TermRec active = cache.getSystemData().getActiveTerm();

            if (active == null) {
                Log.warning("Failed to query for active term.");
                added = -1;
            } else {
                final TermKey activeKey = active.term;
                final ResolvedStandardMilestones milestones = resolveStandardMilestones(cache, activeKey, stuId,
                        paceTrack, pace, index);

                if ("MA".equals(msType)) {
                    final LocalDate origDate = milestones.dates()[unit - 1][objective - 1];
                    added = buildStandardExtensionData(cache, activeKey, stuId, paceTrack, pace, index, unit, objective,
                            msType, RawMilestoneAppeal.APPEAL_TYPE_REQ, origDate, days);
                }
            }
        } else {
            Log.warning("Request to apply standard free extension when none is available.");
            added = days;
        }

        return added;
    }

    /**
     * Builds the data to denote an extension in a standards-based course.
     *
     * @param cache     the data cache
     * @param termKey   the term key
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index
     * @param unit      the unit
     * @param objective the objective
     * @param msType    the milestone type
     * @param current   the current deadline date
     * @param days      the number of days of extension to give
     * @return the number of days of extension added; 0 if the extension has already been used, -1 if this student does
     *         not exist or does not have accommodation extensions available (if this number is greater than 100, then
     *         the value mod 100 is the number of extension days granted, and the value divided by 100 is the number of
     *         extension days they had available, but which would have gone past the end of the term.  In this
     *         situation, the student should get a message that we were not able to add the full accommodation, and that
     *         they should contact the instructional team to discuss options if they need more time.
     * @throws IllegalArgumentException if the cache, student ID, pace track, or milestone type is null, the pace is
     *                                  less than 1 or greater than 5, or the index is less than 1 or greater than the
     *                                  pace, the unit is less than 1 or greater than 5
     * @throws SQLException             if there is an error accessing the database
     */
    private static int buildStandardExtensionData(final Cache cache, final TermKey termKey, final String stuId,
                                                  final String paceTrack, final int pace, final int index,
                                                  final int unit, final int objective, final String msType,
                                                  final String appealType, final LocalDate current, final int days)
            throws SQLException {

        int added;

        final LocalDateTime now = LocalDateTime.now();
        final Integer paceObj = Integer.valueOf(pace);
        final Integer msNbr = Integer.valueOf(pace * 1000 + index * 100 + unit * 10 + objective);

        final LocalDate newDeadline = TermCalendarLogic.nextOpenDay(cache, current, days);
        if (newDeadline == null) {
            // Not enough days before the end of term
            added = days - 1;
            LocalDate shortExtension = TermCalendarLogic.nextOpenDay(cache, current, added);
            while (added > 0 && shortExtension == null) {
                --added;
                shortExtension = TermCalendarLogic.nextOpenDay(cache, current, added);
            }
            if (added > 0) {
                final String comment = "Only able to add " + added + " days before end of term, student was allowed "
                                       + days;

                final RawMilestoneAppeal appealRecord = new RawMilestoneAppeal(termKey, stuId, now, appealType, paceObj,
                        paceTrack, msNbr, msType, current, shortExtension, null, "Requested extension via website",
                        comment, "websites");

                if (!RawMilestoneAppealLogic.INSTANCE.insert(cache, appealRecord)) {
                    Log.warning("Failed to insert MILESTONE_APPEAL record for requested extension");
                }

                if (!buildStuStdMilestone(cache, stuId, paceTrack, pace, index, unit, objective, msType,
                        shortExtension)) {
                    Log.warning("Failed to update STMILESTONE with new deadline");
                }
            }
            added += 100 * days;
        } else {
            added = days;

            final RawMilestoneAppeal appealRecord = new RawMilestoneAppeal(termKey, stuId, now, appealType, paceObj,
                    paceTrack, msNbr, msType, current, newDeadline, null, "Requested extension via website",
                    CoreConstants.EMPTY, "websites");

            if (!RawMilestoneAppealLogic.INSTANCE.insert(cache, appealRecord)) {
                Log.warning("Failed to insert MILESTONE_APPEAL record for requested extension");
            }

            if (!buildStuStdMilestone(cache, stuId, paceTrack, pace, index, unit, objective, msType, newDeadline)) {
                Log.warning("Failed to update STMILESTONE with new deadline");
            }
        }

        return added;
    }

    /**
     * Builds the STU_STD_MILESTONE record by either updating an existing record in place or adding a new record.
     *
     * @param cache       the data cache
     * @param stuId       the student ID
     * @param paceTrack   the pace track
     * @param pace        the pace
     * @param index       the course index
     * @param unit        the unit
     * @param objective   the objective
     * @param msType      the milestone type
     * @param newDeadline the new deadline date
     * @return true if data wazS successfully updated; false if not
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean buildStuStdMilestone(final Cache cache, final String stuId, final String paceTrack,
                                                final int pace, final int index, final int unit, final int objective,
                                                final String msType, final LocalDate newDeadline) throws SQLException {

        final StudentStandardMilestoneLogic logic = StudentStandardMilestoneLogic.get(cache);
        final Integer paceObj = Integer.valueOf(pace);
        final Integer indexObj = Integer.valueOf(index);
        final Integer unitObj = Integer.valueOf(unit);
        final Integer objectiveObj = Integer.valueOf(objective);

        final List<StudentStandardMilestoneRec> all = logic.queryByStuPaceTrackPaceIndex(cache, stuId, paceTrack,
                paceObj, indexObj);
        StudentStandardMilestoneRec existing = null;
        for (final StudentStandardMilestoneRec test : all) {
            if (Objects.equals(test.unit, unitObj) && Objects.equals(test.objective, objectiveObj)
                && test.objective.intValue() == objective && test.msType.equals(msType)) {
                existing = test;
                break;
            }
        }

        final boolean result;
        if (existing == null) {
            final StudentStandardMilestoneRec add = new StudentStandardMilestoneRec(stuId, paceTrack, paceObj,
                    indexObj, unitObj, objectiveObj, msType, newDeadline);
            result = logic.insert(cache, add);
        } else {
            result = logic.updateDate(cache, existing, newDeadline);
            existing.msDate = newDeadline;
        }

        return result;
    }

    /**
     * A container for resolved milestones for a single legacy course.
     */
    public record ResolvedLegacyMilestones(LocalDate re1, LocalDate re2, LocalDate re3, LocalDate re4, LocalDate fe,
                                           LocalDate f1, Integer numF1Tries) {
    }

    /**
     * A container for resolved milestones for a single standards-based course.
     */
    public record ResolvedStandardMilestones(LocalDate[][] dates) {
    }
}
