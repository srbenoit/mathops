package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Queries all MILESTONE records for the current term and checks that all expected milestone records exist, that there
 * are no duplicate rows, and that dates are in a sensible order and all fall within the current term.
 */
public enum MilestoneSanityCheck {
    ;

    /**
     * Runs the report, writing the result to a file.
     */
    private static void runReport() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.REPORT_PATH);
        final Cache cache = new Cache(profile);

        final SystemData systemData = cache.getSystemData();

        try {
            final TermRec active = systemData.getActiveTerm();

            final Collection<String> report = new ArrayList<>(100);
            final List<RawMilestone> milestones = systemData.getMilestones(active.term);

            runTests(milestones, active, report);

            for (final String s : report) {
                Log.fine(s);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Runs sanity checks of milestone data.
     *
     * @param milestones the list of all milestones in the active term
     * @param active     the active term
     * @param report     the list to which to append report lines
     */
    private static void runTests(final List<RawMilestone> milestones, final TermRec active,
                                 final Collection<? super String> report) {

        report.add("Queried " + milestones.size() + " MILESTONE records for the active term");

        report.add(CoreConstants.EMPTY);
        report.add("TEST 1: Checking that dates all lie within the active term:");
        final LocalDate start = active.startDate;
        final LocalDate end = active.endDate;
        boolean goodDates = true;
        for (final RawMilestone test : milestones) {
            final LocalDate date = test.msDate;
            if (date.isBefore(start)) {
                report.add("    ERROR: Milestone falls before start of term:");
                report.add("           Pace " + test.pace + " track " + test.paceTrack + " number "
                           + test.msNbr + " type " + test.msType);
                goodDates = false;
            } else if (date.isAfter(end)) {
                report.add("    ERROR: Milestone falls after end of term:");
                report.add("           Pace " + test.pace + " track " + test.paceTrack + " number "
                           + test.msNbr + " type " + test.msType);
                goodDates = false;
            }
        }
        if (goodDates) {
            report.add("    All milestone dates fall within the active term.");
        }

        report.add(CoreConstants.EMPTY);
        report.add("TEST 2: Checking that all expected milestones exist:");

        // Sort milestones into tracks in proper date order
        final Collection<RawMilestone> track1A = new ArrayList<>(32);
        gatherUsers(milestones, 1, "A", track1A, report);
        gatherMilestonesForCourse(milestones, 1, 1, "A", track1A, report);

        final Collection<RawMilestone> track1B = new ArrayList<>(32);
        gatherUsers(milestones, 1, "B", track1B, report);
        gatherMilestonesForCourse(milestones, 1, 1, "B", track1B, report);

        final Collection<RawMilestone> track1C = new ArrayList<>(32);
        gatherUsers(milestones, 1, "C", track1C, report);
        gatherMilestonesForCourse(milestones, 1, 1, "C", track1C, report);

        final Collection<RawMilestone> track2A = new ArrayList<>(63);
        gatherUsers(milestones, 2, "A", track2A, report);
        gatherMilestonesForCourse(milestones, 2, 1, "A", track2A, report);
        gatherMilestonesForCourse(milestones, 2, 2, "A", track2A, report);

        final Collection<RawMilestone> track2B = new ArrayList<>(63);
        gatherUsers(milestones, 2, "B", track2B, report);
        gatherMilestonesForCourse(milestones, 2, 1, "B", track2B, report);
        gatherMilestonesForCourse(milestones, 2, 2, "B", track2B, report);

        final Collection<RawMilestone> track2C = new ArrayList<>(63);
        gatherUsers(milestones, 2, "C", track2C, report);
        gatherMilestonesForCourse(milestones, 2, 1, "C", track2C, report);
        gatherMilestonesForCourse(milestones, 2, 2, "C", track2C, report);

        final Collection<RawMilestone> track3A = new ArrayList<>(94);
        gatherUsers(milestones, 3, "A", track3A, report);
        gatherMilestonesForCourse(milestones, 3, 1, "A", track3A, report);
        gatherMilestonesForCourse(milestones, 3, 2, "A", track3A, report);
        gatherMilestonesForCourse(milestones, 3, 3, "A", track3A, report);

        final Collection<RawMilestone> track4A = new ArrayList<>(125);
        gatherUsers(milestones, 4, "A", track4A, report);
        gatherMilestonesForCourse(milestones, 4, 1, "A", track4A, report);
        gatherMilestonesForCourse(milestones, 4, 2, "A", track4A, report);
        gatherMilestonesForCourse(milestones, 4, 3, "A", track4A, report);
        gatherMilestonesForCourse(milestones, 4, 4, "A", track4A, report);

        final Collection<RawMilestone> track5A = new ArrayList<>(156);
        gatherUsers(milestones, 5, "A", track5A, report);
        gatherMilestonesForCourse(milestones, 5, 1, "A", track5A, report);
        gatherMilestonesForCourse(milestones, 5, 2, "A", track5A, report);
        gatherMilestonesForCourse(milestones, 5, 3, "A", track5A, report);
        gatherMilestonesForCourse(milestones, 5, 4, "A", track5A, report);
        gatherMilestonesForCourse(milestones, 5, 5, "A", track5A, report);

        if (track1A.size() == 32
            && track1B.size() == 32
            && track1C.size() == 32
            && track2A.size() == 63
            && track2B.size() == 63
            && track2C.size() == 63
            && track3A.size() == 94
            && track4A.size() == 125
            && track5A.size() == 156) {

            report.add("    All expected milestones were found.");
        }

        if (milestones.isEmpty()) {
            report.add("    No extra milestone records were found.");
        } else {
            report.add("    ERROR: Extra milestone records were found:");
            for (final RawMilestone extra : milestones) {
                report.add("           Pace " + extra.pace + " track " + extra.paceTrack
                           + " number " + extra.msNbr + " type " + extra.msType);
            }
        }

        report.add(CoreConstants.EMPTY);
        report.add("TEST 3: Checking that dates within each track are in proper order:");
        final boolean goodSequence = checkSequence(track1A, report)
                                     && checkSequence(track1B, report)
                                     && checkSequence(track1C, report)
                                     && checkSequence(track2A, report)
                                     && checkSequence(track2B, report)
                                     && checkSequence(track2C, report)
                                     && checkSequence(track3A, report)
                                     && checkSequence(track4A, report)
                                     && checkSequence(track5A, report);
        if (goodSequence) {
            report.add("    All milestone sequences are sensible.");
        }
    }

    /**
     * Attempts to gather the milestone for the User's Exam for a specified pace/track and adds it to a target list,
     * removing it from a source list when found.
     *
     * @param source the source list of milestone records
     * @param pace   the pace
     * @param track  the pace track
     * @param target the target list to which to add milestone if found
     * @param report a report to which to log when a milestone is not found
     */
    private static void gatherUsers(final Iterable<RawMilestone> source, final int pace,
                                    final String track, final Collection<? super RawMilestone> target,
                                    final Collection<? super String> report) {

        final int number = pace * 100;

        final RawMilestone us = scanForMilestone(source, pace, track, number, "US", report);
        if (us != null) {
            target.add(us);
        }
    }

    /**
     * Attempts to gather all expected milestones for a specified pace/index/track and add them to a target list,
     * removing them from a source list as it goes.
     *
     * @param source the source list of milestone records
     * @param pace   the pace
     * @param index  the course index
     * @param track  the pace track
     * @param target the target list to which to add milestones found
     * @param report a report to which to log when a milestone is not found
     */
    private static void gatherMilestonesForCourse(final Iterable<RawMilestone> source, final int pace,
                                                  final int index, final String track,
                                                  final Collection<? super RawMilestone> target,
                                                  final Collection<? super String> report) {

        final int srNumber = pace * 100 + index * 10;
        final int[] unitNumbers = {srNumber + 1, srNumber + 2, srNumber + 3, srNumber + 4};
        final int finNumber = srNumber + 5;

        final RawMilestone sr = scanForMilestone(source, pace, track, srNumber, "SR", report);
        if (sr != null) {
            target.add(sr);
        }
        for (int i = 0; i < 4; ++i) {
            final int number = unitNumbers[i];

            final RawMilestone h11 = scanForMilestone(source, pace, track, number, "H1", report);
            if (h11 != null) {
                target.add(h11);
            }
            final RawMilestone h12 = scanForMilestone(source, pace, track, number, "H2", report);
            if (h12 != null) {
                target.add(h12);
            }
            final RawMilestone h13 = scanForMilestone(source, pace, track, number, "H3", report);
            if (h13 != null) {
                target.add(h13);
            }
            final RawMilestone h14 = scanForMilestone(source, pace, track, number, "H4", report);
            if (h14 != null) {
                target.add(h14);
            }
            final RawMilestone h15 = scanForMilestone(source, pace, track, number, "H5", report);
            if (h15 != null) {
                target.add(h15);
            }
            final RawMilestone re = scanForMilestone(source, pace, track, number, "RE", report);
            if (re != null) {
                target.add(re);
            }
            final RawMilestone ue = scanForMilestone(source, pace, track, number, "UE", report);
            if (ue != null) {
                target.add(ue);
            }
        }
        final RawMilestone fe = scanForMilestone(source, pace, track, finNumber, "FE", report);
        if (fe != null) {
            target.add(fe);
        }
        final RawMilestone f1 = scanForMilestone(source, pace, track, finNumber, "F1", report);
        if (f1 != null) {
            target.add(f1);
        }
    }

    /**
     * Scans for a particular milestone, and if found, removes it from the list of milestones and returns the removed
     * record.
     *
     * @param milestones the list of milestones to search
     * @param pace       the pace
     * @param track      the pace track
     * @param number     the milestone number
     * @param type       the milestone type
     * @param report     a report to which to log when a milestone is not found
     * @return the resulting record, if found, null if not
     */
    private static RawMilestone scanForMilestone(final Iterable<RawMilestone> milestones,
                                                 final int pace, final String track, final int number,
                                                 final String type, final Collection<? super String> report) {

        RawMilestone result = null;

        final Iterator<RawMilestone> iter = milestones.iterator();
        while (iter.hasNext()) {
            final RawMilestone test = iter.next();
            if (pace == test.pace.intValue() && track.equals(test.paceTrack)
                && number == test.msNbr.intValue() && type.equals(test.msType)) {
                result = test;
                iter.remove();
                break;
            }
        }

        if (result == null) {
            report.add("    ERROR: Milestone " + number + " record not found for pace " + pace + " track " + track
                       + " type " + type);
        }

        return result;
    }

    /**
     * Checks whether a list of milestones forms a non-descending sequence of dates - consecutive list entries can have
     * the same date, but a later entry cannot have an earlier date.
     *
     * @param milestones the list of milestones to check
     * @param report     a report to which to log when a milestone is not found
     * @return true if no errors were reported
     */
    private static boolean checkSequence(final Iterable<RawMilestone> milestones,
                                         final Collection<? super String> report) {

        boolean good = true;

        RawMilestone prior = null;
        for (final RawMilestone test : milestones) {
            if (prior == null) {
                prior = test;
            } else if (test.msDate.isBefore(prior.msDate)) {
                report.add("    ERROR: Milestone " + test.msNbr + " for pace " + test.pace
                           + " track " + test.paceTrack + " type " + test.msType
                           + " has date that precedes milestone " + prior.msNbr + " type " + prior.msType);

                good = false;
            }
        }

        return good;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        runReport();
    }
}
