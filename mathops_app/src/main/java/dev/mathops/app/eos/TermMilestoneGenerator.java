package dev.mathops.app.eos;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawlogic.RawCampusCalendarLogic;
import dev.mathops.db.old.rawlogic.RawSemesterCalendarLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class generates milestone records for a term based on the term weeks in the 'semester_calendar' table and a list
 * of holidays stored in the 'campus_calendar' table.
 */
public enum TermMilestoneGenerator {
    ;

    /** A pace track. */
    private static final String A = "A";

    /** A pace track. */
    private static final String B = "B";

    /** A pace track. */
    private static final String C = "C";

    /**
     * Generates a list of milestones for the term.
     *
     * @param term      the term
     * @param termWeeks the list of term weeks
     * @param holidays  the 'holiday' calendar days within the term
     * @return the list of generated milestones
     */
    private static List<RawMilestone> generateMilestones(final TermRec term,
                                                         final Iterable<RawSemesterCalendar> termWeeks,
                                                         final Collection<RawCampusCalendar> holidays) {

        final TermKey key = term.term;

        final List<RawMilestone> result;

        if (key.name == ETermName.FALL) {
            result = generateFallMilestones(term, termWeeks, holidays);
        } else if (key.name == ETermName.SPRING) {
            result = generateSpringMilestones(term, termWeeks, holidays);
        } else if (key.name == ETermName.SUMMER) {
            result = generateSummerMilestones(term, termWeeks, holidays);
        } else {
            Log.warning("Unsupported term: ", key);
            result = new ArrayList<>(0);
        }

        return result;
    }

    /**
     * Generates a list of milestones for a Fall term.
     *
     * @param term      the term
     * @param termWeeks the list of term weeks
     * @param holidays  the 'holiday' calendar days within the term
     * @return the list of generated milestones
     */
    private static List<RawMilestone> generateFallMilestones(final TermRec term,
                                                             final Iterable<RawSemesterCalendar> termWeeks,
                                                             final Collection<RawCampusCalendar> holidays) {

        final Map<Integer, Map<Integer, LocalDate>> daysOfWeeks = generateDaysOfWeeks(termWeeks, holidays);
        final Map<Integer, LocalDate> dates = numberDates(daysOfWeeks);

        // Sanity check

        final List<RawMilestone> list = new ArrayList<>(150);

        final int numDays = dates.size();
        if (numDays < 73 || numDays > 75) {
            Log.warning("Unreasonable number of days in a Fall term: " + numDays);
        } else {
            // Track 1A
            list.add(makeREMilestone(term, 1, A, 111, dates.get(Integer.valueOf(13))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(9))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(10))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(11))));

            list.add(makeREMilestone(term, 1, A, 112, dates.get(Integer.valueOf(26))));

            list.add(makeREMilestone(term, 1, A, 113, dates.get(Integer.valueOf(39))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(35))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(36))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(37))));

            list.add(makeREMilestone(term, 1, A, 114, dates.get(Integer.valueOf(52))));

            list.add(makeFEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(61))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(57))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(58))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(59))));

            list.add(makeF1Milestone(term, 1, A, 115, dates.get(Integer.valueOf(62)), 1));
            list.add(makeT1F1Milestone(term, 1, A, 115, dates.get(Integer.valueOf(62))));

            // Track 1B
            list.add(makeREMilestone(term, 1, B, 111, dates.get(Integer.valueOf(15))));
            list.add(makeT1R1Milestone(term, 1, B, 111, dates.get(Integer.valueOf(11))));
            list.add(makeT1R1Milestone(term, 1, B, 111, dates.get(Integer.valueOf(12))));
            list.add(makeT1R1Milestone(term, 1, B, 111, dates.get(Integer.valueOf(13))));

            list.add(makeREMilestone(term, 1, B, 112, dates.get(Integer.valueOf(28))));

            list.add(makeREMilestone(term, 1, B, 113, dates.get(Integer.valueOf(41))));
            list.add(makeT1R3Milestone(term, 1, B, 113, dates.get(Integer.valueOf(37))));
            list.add(makeT1R3Milestone(term, 1, B, 113, dates.get(Integer.valueOf(38))));
            list.add(makeT1R3Milestone(term, 1, B, 113, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 1, B, 114, dates.get(Integer.valueOf(54))));

            list.add(makeFEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(63))));
            list.add(makeT1FEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(59))));
            list.add(makeT1FEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(60))));
            list.add(makeT1FEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(61))));

            list.add(makeF1Milestone(term, 1, B, 115, dates.get(Integer.valueOf(64)), 1));
            list.add(makeT1F1Milestone(term, 1, B, 115, dates.get(Integer.valueOf(64))));

            // Track 1C ("Late Start" track)
            list.add(makeREMilestone(term, 1, C, 111, dates.get(Integer.valueOf(43))));
            list.add(makeT1R1Milestone(term, 1, C, 111, dates.get(Integer.valueOf(39))));
            list.add(makeT1R1Milestone(term, 1, C, 111, dates.get(Integer.valueOf(40))));
            list.add(makeT1R1Milestone(term, 1, C, 111, dates.get(Integer.valueOf(41))));

            list.add(makeREMilestone(term, 1, C, 112, dates.get(Integer.valueOf(50))));

            list.add(makeREMilestone(term, 1, C, 113, dates.get(Integer.valueOf(57))));
            list.add(makeT1R3Milestone(term, 1, C, 113, dates.get(Integer.valueOf(53))));
            list.add(makeT1R3Milestone(term, 1, C, 113, dates.get(Integer.valueOf(54))));
            list.add(makeT1R3Milestone(term, 1, C, 113, dates.get(Integer.valueOf(55))));

            list.add(makeREMilestone(term, 1, C, 114, dates.get(Integer.valueOf(64))));

            list.add(makeFEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(69))));
            list.add(makeT1FEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(65))));
            list.add(makeT1FEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(66))));
            list.add(makeT1FEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(67))));

            list.add(makeF1Milestone(term, 1, C, 115, dates.get(Integer.valueOf(70)), 1));
            list.add(makeT1F1Milestone(term, 1, C, 115, dates.get(Integer.valueOf(70))));

            // Track 2A
            list.add(makeREMilestone(term, 2, A, 211, dates.get(Integer.valueOf(10))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(6))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(7))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(8))));

            list.add(makeREMilestone(term, 2, A, 212, dates.get(Integer.valueOf(16))));

            list.add(makeREMilestone(term, 2, A, 213, dates.get(Integer.valueOf(22))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(18))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(19))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(20))));

            list.add(makeREMilestone(term, 2, A, 214, dates.get(Integer.valueOf(29))));

            list.add(makeFEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(34))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(30))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(31))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(32))));

            list.add(makeF1Milestone(term, 2, A, 215, dates.get(Integer.valueOf(35)), 1));
            list.add(makeT1F1Milestone(term, 2, A, 215, dates.get(Integer.valueOf(35))));

            list.add(makeREMilestone(term, 2, A, 221, dates.get(Integer.valueOf(41))));

            list.add(makeREMilestone(term, 2, A, 222, dates.get(Integer.valueOf(48))));

            list.add(makeREMilestone(term, 2, A, 223, dates.get(Integer.valueOf(55))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(51))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(52))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(53))));

            list.add(makeREMilestone(term, 2, A, 224, dates.get(Integer.valueOf(62))));

            list.add(makeFEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(67))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(63))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(64))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(65))));

            list.add(makeF1Milestone(term, 2, A, 225, dates.get(Integer.valueOf(68)), 1));
            list.add(makeT2F1Milestone(term, 2, A, 225, dates.get(Integer.valueOf(68))));

            // Track 2B
            list.add(makeREMilestone(term, 2, B, 211, dates.get(Integer.valueOf(12))));
            list.add(makeT1R1Milestone(term, 2, B, 211, dates.get(Integer.valueOf(8))));
            list.add(makeT1R1Milestone(term, 2, B, 211, dates.get(Integer.valueOf(9))));
            list.add(makeT1R1Milestone(term, 2, B, 211, dates.get(Integer.valueOf(10))));

            list.add(makeREMilestone(term, 2, B, 212, dates.get(Integer.valueOf(18))));

            list.add(makeREMilestone(term, 2, B, 213, dates.get(Integer.valueOf(24))));
            list.add(makeT1R3Milestone(term, 2, B, 213, dates.get(Integer.valueOf(20))));
            list.add(makeT1R3Milestone(term, 2, B, 213, dates.get(Integer.valueOf(21))));
            list.add(makeT1R3Milestone(term, 2, B, 213, dates.get(Integer.valueOf(22))));

            list.add(makeREMilestone(term, 2, B, 214, dates.get(Integer.valueOf(31))));

            list.add(makeFEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(36))));
            list.add(makeT1FEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(32))));
            list.add(makeT1FEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(33))));
            list.add(makeT1FEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(34))));

            list.add(makeF1Milestone(term, 2, B, 215, dates.get(Integer.valueOf(37)), 1));
            list.add(makeT1F1Milestone(term, 2, B, 215, dates.get(Integer.valueOf(37))));

            list.add(makeREMilestone(term, 2, B, 221, dates.get(Integer.valueOf(43))));

            list.add(makeREMilestone(term, 2, B, 222, dates.get(Integer.valueOf(50))));
            list.add(makeREMilestone(term, 2, B, 223, dates.get(Integer.valueOf(57))));
            list.add(makeT2R3Milestone(term, 2, B, 223, dates.get(Integer.valueOf(53))));
            list.add(makeT2R3Milestone(term, 2, B, 223, dates.get(Integer.valueOf(54))));
            list.add(makeT2R3Milestone(term, 2, B, 223, dates.get(Integer.valueOf(55))));

            list.add(makeREMilestone(term, 2, B, 224, dates.get(Integer.valueOf(64))));

            list.add(makeFEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(69))));
            list.add(makeT2FEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(65))));
            list.add(makeT2FEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(66))));
            list.add(makeT2FEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(67))));

            list.add(makeF1Milestone(term, 2, B, 225, dates.get(Integer.valueOf(70)), 1));
            list.add(makeT2F1Milestone(term, 2, B, 225, dates.get(Integer.valueOf(70))));

            // Track 3A
            list.add(makeREMilestone(term, 3, A, 311, dates.get(Integer.valueOf(9))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(5))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(6))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(7))));

            list.add(makeREMilestone(term, 3, A, 312, dates.get(Integer.valueOf(13))));

            list.add(makeREMilestone(term, 3, A, 313, dates.get(Integer.valueOf(17))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(14))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(15))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(16))));

            list.add(makeREMilestone(term, 3, A, 314, dates.get(Integer.valueOf(21))));

            list.add(makeFEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(25))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(22))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(23))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(24))));

            list.add(makeF1Milestone(term, 3, A, 315, dates.get(Integer.valueOf(26)), 1));
            list.add(makeT1F1Milestone(term, 3, A, 315, dates.get(Integer.valueOf(26))));

            list.add(makeREMilestone(term, 3, A, 321, dates.get(Integer.valueOf(29))));

            list.add(makeREMilestone(term, 3, A, 322, dates.get(Integer.valueOf(33))));

            list.add(makeREMilestone(term, 3, A, 323, dates.get(Integer.valueOf(37))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(33))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(34))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(35))));

            list.add(makeREMilestone(term, 3, A, 324, dates.get(Integer.valueOf(42))));

            list.add(makeFEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(46))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(43))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(44))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(45))));

            list.add(makeF1Milestone(term, 3, A, 325, dates.get(Integer.valueOf(47)), 1));
            list.add(makeT2F1Milestone(term, 3, A, 325, dates.get(Integer.valueOf(47))));

            list.add(makeREMilestone(term, 3, A, 331, dates.get(Integer.valueOf(51))));

            list.add(makeREMilestone(term, 3, A, 332, dates.get(Integer.valueOf(56))));

            list.add(makeREMilestone(term, 3, A, 333, dates.get(Integer.valueOf(61))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(57))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(58))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(59))));

            list.add(makeREMilestone(term, 3, A, 334, dates.get(Integer.valueOf(66))));

            list.add(makeFEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(70))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(67))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(68))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(69))));

            list.add(makeF1Milestone(term, 3, A, 335, dates.get(Integer.valueOf(71)), 1));
            list.add(makeT3F1Milestone(term, 3, A, 335, dates.get(Integer.valueOf(71))));

            // Track 4A
            list.add(makeREMilestone(term, 4, A, 411, dates.get(Integer.valueOf(8))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(4))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(5))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(6))));

            list.add(makeREMilestone(term, 4, A, 412, dates.get(Integer.valueOf(11))));

            list.add(makeREMilestone(term, 4, A, 413, dates.get(Integer.valueOf(14))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(11))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(12))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(13))));

            list.add(makeREMilestone(term, 4, A, 414, dates.get(Integer.valueOf(18))));

            list.add(makeFEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(21))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(18))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(19))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(20))));

            list.add(makeF1Milestone(term, 4, A, 415, dates.get(Integer.valueOf(22)), 1));
            list.add(makeT1F1Milestone(term, 4, A, 415, dates.get(Integer.valueOf(22))));

            list.add(makeREMilestone(term, 4, A, 421, dates.get(Integer.valueOf(24))));

            list.add(makeREMilestone(term, 4, A, 422, dates.get(Integer.valueOf(27))));

            list.add(makeREMilestone(term, 4, A, 423, dates.get(Integer.valueOf(31))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(28))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(29))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(30))));

            list.add(makeREMilestone(term, 4, A, 424, dates.get(Integer.valueOf(35))));

            list.add(makeFEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(38))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(35))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(36))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(37))));

            list.add(makeF1Milestone(term, 4, A, 425, dates.get(Integer.valueOf(39)), 1));
            list.add(makeT2F1Milestone(term, 4, A, 425, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 4, A, 431, dates.get(Integer.valueOf(41))));

            list.add(makeREMilestone(term, 4, A, 432, dates.get(Integer.valueOf(44))));

            list.add(makeREMilestone(term, 4, A, 433, dates.get(Integer.valueOf(48))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(45))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(46))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(47))));

            list.add(makeREMilestone(term, 4, A, 434, dates.get(Integer.valueOf(53))));

            list.add(makeFEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(55))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(52))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(53))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(54))));

            list.add(makeF1Milestone(term, 4, A, 435, dates.get(Integer.valueOf(56)), 1));
            list.add(makeT3F1Milestone(term, 4, A, 435, dates.get(Integer.valueOf(56))));

            list.add(makeREMilestone(term, 4, A, 441, dates.get(Integer.valueOf(58))));

            list.add(makeREMilestone(term, 4, A, 442, dates.get(Integer.valueOf(61))));

            list.add(makeREMilestone(term, 4, A, 443, dates.get(Integer.valueOf(65))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(62))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(63))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(64))));

            list.add(makeREMilestone(term, 4, A, 444, dates.get(Integer.valueOf(69))));

            list.add(makeFEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 2))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 5))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 4))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 3))));

            list.add(makeF1Milestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 1)), 1));
            list.add(makeT4F1Milestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 1))));

            // Track 5A
            list.add(makeREMilestone(term, 5, A, 511, dates.get(Integer.valueOf(7))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(3))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(4))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(5))));

            list.add(makeREMilestone(term, 5, A, 512, dates.get(Integer.valueOf(9))));

            list.add(makeREMilestone(term, 5, A, 513, dates.get(Integer.valueOf(12))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(9))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(10))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(11))));

            list.add(makeREMilestone(term, 5, A, 514, dates.get(Integer.valueOf(14))));

            list.add(makeFEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(17))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(14))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(15))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(16))));

            list.add(makeF1Milestone(term, 5, A, 515, dates.get(Integer.valueOf(18)), 1));
            list.add(makeT1F1Milestone(term, 5, A, 515, dates.get(Integer.valueOf(18))));

            list.add(makeREMilestone(term, 5, A, 521, dates.get(Integer.valueOf(20))));

            list.add(makeREMilestone(term, 5, A, 522, dates.get(Integer.valueOf(23))));

            list.add(makeREMilestone(term, 5, A, 523, dates.get(Integer.valueOf(26))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(23))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(24))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(25))));

            list.add(makeREMilestone(term, 5, A, 524, dates.get(Integer.valueOf(29))));

            list.add(makeFEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(31))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(28))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(29))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(30))));

            list.add(makeF1Milestone(term, 5, A, 525, dates.get(Integer.valueOf(32)), 1));
            list.add(makeT2F1Milestone(term, 5, A, 525, dates.get(Integer.valueOf(32))));

            list.add(makeREMilestone(term, 5, A, 531, dates.get(Integer.valueOf(34))));

            list.add(makeREMilestone(term, 5, A, 532, dates.get(Integer.valueOf(37))));

            list.add(makeREMilestone(term, 5, A, 533, dates.get(Integer.valueOf(40))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(37))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(38))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 5, A, 534, dates.get(Integer.valueOf(43))));

            list.add(makeFEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(45))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(42))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(43))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(44))));

            list.add(makeF1Milestone(term, 5, A, 535, dates.get(Integer.valueOf(46)), 1));
            list.add(makeT3F1Milestone(term, 5, A, 535, dates.get(Integer.valueOf(46))));

            list.add(makeREMilestone(term, 5, A, 541, dates.get(Integer.valueOf(48))));

            list.add(makeREMilestone(term, 5, A, 542, dates.get(Integer.valueOf(51))));

            list.add(makeREMilestone(term, 5, A, 543, dates.get(Integer.valueOf(54))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(51))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(52))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(53))));

            list.add(makeREMilestone(term, 5, A, 544, dates.get(Integer.valueOf(57))));

            list.add(makeFEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(59))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(56))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(57))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(58))));

            list.add(makeF1Milestone(term, 5, A, 545, dates.get(Integer.valueOf(60)), 1));
            list.add(makeT4F1Milestone(term, 5, A, 545, dates.get(Integer.valueOf(60))));

            list.add(makeREMilestone(term, 5, A, 551, dates.get(Integer.valueOf(62))));

            list.add(makeREMilestone(term, 5, A, 552, dates.get(Integer.valueOf(65))));

            list.add(makeREMilestone(term, 5, A, 553, dates.get(Integer.valueOf(68))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(65))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(66))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(67))));

            list.add(makeREMilestone(term, 5, A, 554, dates.get(Integer.valueOf(numDays - 3))));

            list.add(makeFEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 1))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 4))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 3))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 2))));

            list.add(makeF1Milestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays)), 1));
            list.add(makeT5F1Milestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays))));
        }

        return list;
    }

    /**
     * Generates a list of milestones for a Spring term.
     *
     * @param term      the term
     * @param termWeeks the list of term weeks
     * @param holidays  the 'holiday' calendar days within the term
     * @return the list of generated milestones
     */
    private static List<RawMilestone> generateSpringMilestones(final TermRec term,
                                                               final Iterable<RawSemesterCalendar> termWeeks,
                                                               final Collection<RawCampusCalendar> holidays) {

        final Map<Integer, Map<Integer, LocalDate>> daysOfWeeks = generateDaysOfWeeks(termWeeks, holidays);
        final Map<Integer, LocalDate> dates = numberDates(daysOfWeeks);

        // Sanity check

        final List<RawMilestone> list = new ArrayList<>(150);

        final int numDays = dates.size();
        if (numDays < 73 || numDays > 75) {
            Log.warning("Unreasonable number of days in a Spring term: " + numDays);
        } else {
            // Track 1A
            list.add(makeREMilestone(term, 1, A, 111, dates.get(Integer.valueOf(13))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(9))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(10))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(11))));

            list.add(makeREMilestone(term, 1, A, 112, dates.get(Integer.valueOf(26))));

            list.add(makeREMilestone(term, 1, A, 113, dates.get(Integer.valueOf(37))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(33))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(34))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(35))));

            list.add(makeREMilestone(term, 1, A, 114, dates.get(Integer.valueOf(50))));

            list.add(makeFEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(60))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(56))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(57))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(58))));

            list.add(makeF1Milestone(term, 1, A, 115, dates.get(Integer.valueOf(61)), 1));
            list.add(makeT1F1Milestone(term, 1, A, 115, dates.get(Integer.valueOf(61))));

            // Track 1B
            list.add(makeREMilestone(term, 1, B, 111, dates.get(Integer.valueOf(15))));
            list.add(makeT1R1Milestone(term, 1, B, 111, dates.get(Integer.valueOf(11))));
            list.add(makeT1R1Milestone(term, 1, B, 111, dates.get(Integer.valueOf(12))));
            list.add(makeT1R1Milestone(term, 1, B, 111, dates.get(Integer.valueOf(13))));

            list.add(makeREMilestone(term, 1, B, 112, dates.get(Integer.valueOf(28))));

            list.add(makeREMilestone(term, 1, B, 113, dates.get(Integer.valueOf(39))));
            list.add(makeT1R3Milestone(term, 1, B, 113, dates.get(Integer.valueOf(35))));
            list.add(makeT1R3Milestone(term, 1, B, 113, dates.get(Integer.valueOf(36))));
            list.add(makeT1R3Milestone(term, 1, B, 113, dates.get(Integer.valueOf(37))));

            list.add(makeREMilestone(term, 1, B, 114, dates.get(Integer.valueOf(52))));

            list.add(makeFEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(62))));
            list.add(makeT1FEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(58))));
            list.add(makeT1FEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(59))));
            list.add(makeT1FEMilestone(term, 1, B, 115, dates.get(Integer.valueOf(60))));

            list.add(makeF1Milestone(term, 1, B, 115, dates.get(Integer.valueOf(63)), 1));
            list.add(makeT1F1Milestone(term, 1, B, 115, dates.get(Integer.valueOf(63))));

            // Track 1C ("Late Start" track)
            list.add(makeREMilestone(term, 1, C, 111, dates.get(Integer.valueOf(44))));
            list.add(makeT1R1Milestone(term, 1, C, 111, dates.get(Integer.valueOf(40))));
            list.add(makeT1R1Milestone(term, 1, C, 111, dates.get(Integer.valueOf(41))));
            list.add(makeT1R1Milestone(term, 1, C, 111, dates.get(Integer.valueOf(42))));

            list.add(makeREMilestone(term, 1, C, 112, dates.get(Integer.valueOf(51))));

            list.add(makeREMilestone(term, 1, C, 113, dates.get(Integer.valueOf(58))));
            list.add(makeT1R3Milestone(term, 1, C, 113, dates.get(Integer.valueOf(54))));
            list.add(makeT1R3Milestone(term, 1, C, 113, dates.get(Integer.valueOf(55))));
            list.add(makeT1R3Milestone(term, 1, C, 113, dates.get(Integer.valueOf(56))));

            list.add(makeREMilestone(term, 1, C, 114, dates.get(Integer.valueOf(65))));

            list.add(makeFEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(69))));
            list.add(makeT1FEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(65))));
            list.add(makeT1FEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(66))));
            list.add(makeT1FEMilestone(term, 1, C, 115, dates.get(Integer.valueOf(67))));

            list.add(makeF1Milestone(term, 1, C, 115, dates.get(Integer.valueOf(70)), 1));
            list.add(makeT1F1Milestone(term, 1, C, 115, dates.get(Integer.valueOf(70))));

            // Track 2A
            list.add(makeREMilestone(term, 2, A, 211, dates.get(Integer.valueOf(9))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(5))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(6))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(7))));

            list.add(makeREMilestone(term, 2, A, 212, dates.get(Integer.valueOf(16))));

            list.add(makeREMilestone(term, 2, A, 213, dates.get(Integer.valueOf(23))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(19))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(20))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(21))));

            list.add(makeREMilestone(term, 2, A, 214, dates.get(Integer.valueOf(30))));

            list.add(makeFEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(34))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(30))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(31))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(32))));

            list.add(makeF1Milestone(term, 2, A, 215, dates.get(Integer.valueOf(35)), 1));
            list.add(makeT1F1Milestone(term, 2, A, 215, dates.get(Integer.valueOf(35))));

            list.add(makeREMilestone(term, 2, A, 221, dates.get(Integer.valueOf(42))));

            list.add(makeREMilestone(term, 2, A, 222, dates.get(Integer.valueOf(49))));

            list.add(makeREMilestone(term, 2, A, 223, dates.get(Integer.valueOf(56))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(52))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(53))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(54))));

            list.add(makeREMilestone(term, 2, A, 224, dates.get(Integer.valueOf(63))));

            list.add(makeFEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(67))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(63))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(64))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(65))));

            list.add(makeF1Milestone(term, 2, A, 225, dates.get(Integer.valueOf(68)), 1));
            list.add(makeT2F1Milestone(term, 2, A, 225, dates.get(Integer.valueOf(68))));

            // Track 2B
            list.add(makeREMilestone(term, 2, B, 211, dates.get(Integer.valueOf(11))));
            list.add(makeT1R1Milestone(term, 2, B, 211, dates.get(Integer.valueOf(7))));
            list.add(makeT1R1Milestone(term, 2, B, 211, dates.get(Integer.valueOf(8))));
            list.add(makeT1R1Milestone(term, 2, B, 211, dates.get(Integer.valueOf(9))));

            list.add(makeREMilestone(term, 2, B, 212, dates.get(Integer.valueOf(18))));

            list.add(makeREMilestone(term, 2, B, 213, dates.get(Integer.valueOf(25))));
            list.add(makeT1R3Milestone(term, 2, B, 213, dates.get(Integer.valueOf(21))));
            list.add(makeT1R3Milestone(term, 2, B, 213, dates.get(Integer.valueOf(22))));
            list.add(makeT1R3Milestone(term, 2, B, 213, dates.get(Integer.valueOf(23))));

            list.add(makeREMilestone(term, 2, B, 214, dates.get(Integer.valueOf(32))));

            list.add(makeFEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(36))));
            list.add(makeT1FEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(32))));
            list.add(makeT1FEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(33))));
            list.add(makeT1FEMilestone(term, 2, B, 215, dates.get(Integer.valueOf(34))));

            list.add(makeF1Milestone(term, 2, B, 215, dates.get(Integer.valueOf(37)), 1));
            list.add(makeT1F1Milestone(term, 2, B, 215, dates.get(Integer.valueOf(37))));

            list.add(makeREMilestone(term, 2, B, 221, dates.get(Integer.valueOf(44))));

            list.add(makeREMilestone(term, 2, B, 222, dates.get(Integer.valueOf(51))));

            list.add(makeREMilestone(term, 2, B, 223, dates.get(Integer.valueOf(58))));
            list.add(makeT2R3Milestone(term, 2, B, 223, dates.get(Integer.valueOf(54))));
            list.add(makeT2R3Milestone(term, 2, B, 223, dates.get(Integer.valueOf(55))));
            list.add(makeT2R3Milestone(term, 2, B, 223, dates.get(Integer.valueOf(56))));

            list.add(makeREMilestone(term, 2, B, 224, dates.get(Integer.valueOf(65))));

            list.add(makeFEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(69))));
            list.add(makeT2FEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(65))));
            list.add(makeT2FEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(66))));
            list.add(makeT2FEMilestone(term, 2, B, 225, dates.get(Integer.valueOf(67))));

            list.add(makeF1Milestone(term, 2, B, 225, dates.get(Integer.valueOf(70)), 1));
            list.add(makeT2F1Milestone(term, 2, B, 225, dates.get(Integer.valueOf(70))));

            // Track 3A
            list.add(makeREMilestone(term, 3, A, 311, dates.get(Integer.valueOf(8))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(4))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(5))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(6))));

            list.add(makeREMilestone(term, 3, A, 312, dates.get(Integer.valueOf(12))));

            list.add(makeREMilestone(term, 3, A, 313, dates.get(Integer.valueOf(17))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(14))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(15))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(16))));

            list.add(makeREMilestone(term, 3, A, 314, dates.get(Integer.valueOf(21))));

            list.add(makeFEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(24))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(21))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(22))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(23))));

            list.add(makeF1Milestone(term, 3, A, 315, dates.get(Integer.valueOf(25)), 1));
            list.add(makeT1F1Milestone(term, 3, A, 315, dates.get(Integer.valueOf(25))));

            list.add(makeREMilestone(term, 3, A, 321, dates.get(Integer.valueOf(29))));

            list.add(makeREMilestone(term, 3, A, 322, dates.get(Integer.valueOf(33))));

            list.add(makeREMilestone(term, 3, A, 323, dates.get(Integer.valueOf(38))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(34))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(35))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(36))));

            list.add(makeREMilestone(term, 3, A, 324, dates.get(Integer.valueOf(43))));

            list.add(makeFEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(47))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(44))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(45))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(46))));

            list.add(makeF1Milestone(term, 3, A, 325, dates.get(Integer.valueOf(48)), 1));
            list.add(makeT2F1Milestone(term, 3, A, 325, dates.get(Integer.valueOf(48))));

            list.add(makeREMilestone(term, 3, A, 331, dates.get(Integer.valueOf(52))));

            list.add(makeREMilestone(term, 3, A, 332, dates.get(Integer.valueOf(57))));

            list.add(makeREMilestone(term, 3, A, 333, dates.get(Integer.valueOf(61))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(57))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(58))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(59))));

            list.add(makeREMilestone(term, 3, A, 334, dates.get(Integer.valueOf(66))));

            list.add(makeFEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(70))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(67))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(68))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(69))));

            list.add(makeF1Milestone(term, 3, A, 335, dates.get(Integer.valueOf(71)), 1));
            list.add(makeT3F1Milestone(term, 3, A, 335, dates.get(Integer.valueOf(71))));

            // Track 4A
            list.add(makeREMilestone(term, 4, A, 411, dates.get(Integer.valueOf(7))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(3))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(4))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(5))));

            list.add(makeREMilestone(term, 4, A, 412, dates.get(Integer.valueOf(10))));

            list.add(makeREMilestone(term, 4, A, 413, dates.get(Integer.valueOf(14))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(11))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(12))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(13))));

            list.add(makeREMilestone(term, 4, A, 414, dates.get(Integer.valueOf(18))));

            list.add(makeFEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(21))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(18))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(19))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(20))));

            list.add(makeF1Milestone(term, 4, A, 415, dates.get(Integer.valueOf(22)), 1));
            list.add(makeT1F1Milestone(term, 4, A, 415, dates.get(Integer.valueOf(22))));

            list.add(makeREMilestone(term, 4, A, 421, dates.get(Integer.valueOf(24))));

            list.add(makeREMilestone(term, 4, A, 422, dates.get(Integer.valueOf(27))));

            list.add(makeREMilestone(term, 4, A, 423, dates.get(Integer.valueOf(31))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(28))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(29))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(30))));

            list.add(makeREMilestone(term, 4, A, 424, dates.get(Integer.valueOf(35))));

            list.add(makeFEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(38))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(35))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(36))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(37))));

            list.add(makeF1Milestone(term, 4, A, 425, dates.get(Integer.valueOf(39)), 1));
            list.add(makeT2F1Milestone(term, 4, A, 425, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 4, A, 431, dates.get(Integer.valueOf(41))));

            list.add(makeREMilestone(term, 4, A, 432, dates.get(Integer.valueOf(44))));

            list.add(makeREMilestone(term, 4, A, 433, dates.get(Integer.valueOf(48))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(45))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(46))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(47))));

            list.add(makeREMilestone(term, 4, A, 434, dates.get(Integer.valueOf(52))));

            list.add(makeFEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(55))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(52))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(53))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(54))));

            list.add(makeF1Milestone(term, 4, A, 435, dates.get(Integer.valueOf(56)), 1));
            list.add(makeT3F1Milestone(term, 4, A, 435, dates.get(Integer.valueOf(56))));

            list.add(makeREMilestone(term, 4, A, 441, dates.get(Integer.valueOf(58))));

            list.add(makeREMilestone(term, 4, A, 442, dates.get(Integer.valueOf(61))));

            list.add(makeREMilestone(term, 4, A, 443, dates.get(Integer.valueOf(65))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(62))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(63))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(64))));

            list.add(makeREMilestone(term, 4, A, 444, dates.get(Integer.valueOf(69))));

            list.add(makeFEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 2))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 5))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 4))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 3))));

            list.add(makeF1Milestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 1)), 1));
            list.add(makeT4F1Milestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 1))));

            // Track 5A
            list.add(makeREMilestone(term, 5, A, 511, dates.get(Integer.valueOf(6))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(2))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(3))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(4))));

            list.add(makeREMilestone(term, 5, A, 512, dates.get(Integer.valueOf(9))));

            list.add(makeREMilestone(term, 5, A, 513, dates.get(Integer.valueOf(12))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(9))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(10))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(11))));

            list.add(makeREMilestone(term, 5, A, 514, dates.get(Integer.valueOf(15))));

            list.add(makeFEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(17))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(14))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(15))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(16))));

            list.add(makeF1Milestone(term, 5, A, 515, dates.get(Integer.valueOf(18)), 1));
            list.add(makeT1F1Milestone(term, 5, A, 515, dates.get(Integer.valueOf(18))));

            list.add(makeREMilestone(term, 5, A, 521, dates.get(Integer.valueOf(20))));

            list.add(makeREMilestone(term, 5, A, 522, dates.get(Integer.valueOf(23))));

            list.add(makeREMilestone(term, 5, A, 523, dates.get(Integer.valueOf(26))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(23))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(24))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(25))));

            list.add(makeREMilestone(term, 5, A, 524, dates.get(Integer.valueOf(29))));

            list.add(makeFEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(31))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(28))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(29))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(30))));

            list.add(makeF1Milestone(term, 5, A, 525, dates.get(Integer.valueOf(32)), 1));
            list.add(makeT2F1Milestone(term, 5, A, 525, dates.get(Integer.valueOf(32))));

            list.add(makeREMilestone(term, 5, A, 531, dates.get(Integer.valueOf(34))));

            list.add(makeREMilestone(term, 5, A, 532, dates.get(Integer.valueOf(37))));

            list.add(makeREMilestone(term, 5, A, 533, dates.get(Integer.valueOf(40))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(37))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(38))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 5, A, 534, dates.get(Integer.valueOf(43))));

            list.add(makeFEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(45))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(42))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(43))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(44))));

            list.add(makeF1Milestone(term, 5, A, 535, dates.get(Integer.valueOf(46)), 1));
            list.add(makeT3F1Milestone(term, 5, A, 535, dates.get(Integer.valueOf(46))));

            list.add(makeREMilestone(term, 5, A, 541, dates.get(Integer.valueOf(48))));

            list.add(makeREMilestone(term, 5, A, 542, dates.get(Integer.valueOf(51))));

            list.add(makeREMilestone(term, 5, A, 543, dates.get(Integer.valueOf(54))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(51))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(52))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(53))));

            list.add(makeREMilestone(term, 5, A, 544, dates.get(Integer.valueOf(57))));

            list.add(makeFEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(59))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(56))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(57))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(58))));

            list.add(makeF1Milestone(term, 5, A, 545, dates.get(Integer.valueOf(60)), 1));
            list.add(makeT4F1Milestone(term, 5, A, 545, dates.get(Integer.valueOf(60))));

            list.add(makeREMilestone(term, 5, A, 551, dates.get(Integer.valueOf(62))));

            list.add(makeREMilestone(term, 5, A, 552, dates.get(Integer.valueOf(65))));

            list.add(makeREMilestone(term, 5, A, 553, dates.get(Integer.valueOf(68))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(65))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(66))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(67))));

            list.add(makeREMilestone(term, 5, A, 554, dates.get(Integer.valueOf(numDays - 3))));

            list.add(makeFEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 1))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 4))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 3))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 2))));

            list.add(makeF1Milestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays)), 1));
            list.add(makeT5F1Milestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays))));
        }

        return list;
    }

    /**
     * Generates a list of milestones for a Summer term.
     *
     * @param term      the term
     * @param termWeeks the list of term weeks
     * @param holidays  the 'holiday' calendar days within the term
     * @return the list of generated milestones
     */
    private static List<RawMilestone> generateSummerMilestones(final TermRec term,
                                                               final Iterable<RawSemesterCalendar> termWeeks,
                                                               final Collection<RawCampusCalendar> holidays) {

        final Map<Integer, Map<Integer, LocalDate>> daysOfWeeks = generateDaysOfWeeks(termWeeks, holidays);
        final Map<Integer, LocalDate> dates = numberDates(daysOfWeeks);

        // for (Map.Entry<Integer, LocalDate> entry : dates.entrySet()) {
        // Log.info("Day ", entry.getKey(), ": ",
        // TemporalUtils.FMT_MDY.format(entry.getValue()));
        // }

        // Sanity check

        final List<RawMilestone> list = new ArrayList<>(150);

        final int numDays = dates.size();
        if (numDays < 58 || numDays > 60) {
            Log.warning("Unreasonable number of days in a Summer term: " + numDays);
        } else {
            // Track 1A
            list.add(makeREMilestone(term, 1, A, 111, dates.get(Integer.valueOf(28))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(24))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(25))));
            list.add(makeT1R1Milestone(term, 1, A, 111, dates.get(Integer.valueOf(26))));

            list.add(makeREMilestone(term, 1, A, 112, dates.get(Integer.valueOf(33))));

            list.add(makeREMilestone(term, 1, A, 113, dates.get(Integer.valueOf(41))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(37))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(38))));
            list.add(makeT1R3Milestone(term, 1, A, 113, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 1, A, 114, dates.get(Integer.valueOf(47))));

            list.add(makeFEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(52))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(48))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(49))));
            list.add(makeT1FEMilestone(term, 1, A, 115, dates.get(Integer.valueOf(50))));

            list.add(makeF1Milestone(term, 1, A, 115, dates.get(Integer.valueOf(53)), 1));
            list.add(makeT1F1Milestone(term, 1, A, 115, dates.get(Integer.valueOf(53))));

            // Track 2A
            list.add(makeREMilestone(term, 2, A, 211, dates.get(Integer.valueOf(26))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(22))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(23))));
            list.add(makeT1R1Milestone(term, 2, A, 211, dates.get(Integer.valueOf(24))));

            list.add(makeREMilestone(term, 2, A, 212, dates.get(Integer.valueOf(29))));

            list.add(makeREMilestone(term, 2, A, 213, dates.get(Integer.valueOf(32))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(29))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(30))));
            list.add(makeT1R3Milestone(term, 2, A, 213, dates.get(Integer.valueOf(31))));

            list.add(makeREMilestone(term, 2, A, 214, dates.get(Integer.valueOf(35))));

            list.add(makeFEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(38))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(35))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(36))));
            list.add(makeT1FEMilestone(term, 2, A, 215, dates.get(Integer.valueOf(37))));

            list.add(makeF1Milestone(term, 2, A, 215, dates.get(Integer.valueOf(39)), 1));
            list.add(makeT1F1Milestone(term, 2, A, 215, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 2, A, 221, dates.get(Integer.valueOf(42))));

            list.add(makeREMilestone(term, 2, A, 222, dates.get(Integer.valueOf(45))));

            list.add(makeREMilestone(term, 2, A, 223, dates.get(Integer.valueOf(48))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(45))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(46))));
            list.add(makeT2R3Milestone(term, 2, A, 223, dates.get(Integer.valueOf(47))));

            list.add(makeREMilestone(term, 2, A, 224, dates.get(Integer.valueOf(51))));

            list.add(makeFEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(54))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(51))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(52))));
            list.add(makeT2FEMilestone(term, 2, A, 225, dates.get(Integer.valueOf(53))));

            list.add(makeF1Milestone(term, 2, A, 225, dates.get(Integer.valueOf(55)), 1));
            list.add(makeT2F1Milestone(term, 2, A, 225, dates.get(Integer.valueOf(55))));

            // Track 3A
            list.add(makeREMilestone(term, 3, A, 311, dates.get(Integer.valueOf(25))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(21))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(22))));
            list.add(makeT1R1Milestone(term, 3, A, 311, dates.get(Integer.valueOf(23))));

            list.add(makeREMilestone(term, 3, A, 312, dates.get(Integer.valueOf(27))));

            list.add(makeREMilestone(term, 3, A, 313, dates.get(Integer.valueOf(29))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(26))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(27))));
            list.add(makeT1R3Milestone(term, 3, A, 313, dates.get(Integer.valueOf(28))));

            list.add(makeREMilestone(term, 3, A, 314, dates.get(Integer.valueOf(31))));

            list.add(makeFEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(33))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(30))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(31))));
            list.add(makeT1FEMilestone(term, 3, A, 315, dates.get(Integer.valueOf(32))));

            list.add(makeF1Milestone(term, 3, A, 315, dates.get(Integer.valueOf(34)), 1));
            list.add(makeT1F1Milestone(term, 3, A, 315, dates.get(Integer.valueOf(34))));

            list.add(makeREMilestone(term, 3, A, 321, dates.get(Integer.valueOf(36))));

            list.add(makeREMilestone(term, 3, A, 322, dates.get(Integer.valueOf(38))));

            list.add(makeREMilestone(term, 3, A, 323, dates.get(Integer.valueOf(40))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(37))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(38))));
            list.add(makeT2R3Milestone(term, 3, A, 323, dates.get(Integer.valueOf(39))));

            list.add(makeREMilestone(term, 3, A, 324, dates.get(Integer.valueOf(42))));

            list.add(makeFEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(44))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(41))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(42))));
            list.add(makeT2FEMilestone(term, 3, A, 325, dates.get(Integer.valueOf(43))));

            list.add(makeF1Milestone(term, 3, A, 325, dates.get(Integer.valueOf(45)), 1));
            list.add(makeT2F1Milestone(term, 3, A, 325, dates.get(Integer.valueOf(45))));

            list.add(makeREMilestone(term, 3, A, 331, dates.get(Integer.valueOf(47))));

            list.add(makeREMilestone(term, 3, A, 332, dates.get(Integer.valueOf(49))));

            list.add(makeREMilestone(term, 3, A, 333, dates.get(Integer.valueOf(51))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(48))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(49))));
            list.add(makeT3R3Milestone(term, 3, A, 333, dates.get(Integer.valueOf(50))));

            list.add(makeREMilestone(term, 3, A, 334, dates.get(Integer.valueOf(53))));

            list.add(makeFEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(55))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(52))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(53))));
            list.add(makeT3FEMilestone(term, 3, A, 335, dates.get(Integer.valueOf(54))));

            list.add(makeF1Milestone(term, 3, A, 335, dates.get(Integer.valueOf(56)), 1));
            list.add(makeT3F1Milestone(term, 3, A, 335, dates.get(Integer.valueOf(56))));

            // Track 4A
            list.add(makeREMilestone(term, 4, A, 411, dates.get(Integer.valueOf(24))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(20))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(21))));
            list.add(makeT1R1Milestone(term, 4, A, 411, dates.get(Integer.valueOf(22))));

            list.add(makeREMilestone(term, 4, A, 412, dates.get(Integer.valueOf(26))));

            list.add(makeREMilestone(term, 4, A, 413, dates.get(Integer.valueOf(28))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(25))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(26))));
            list.add(makeT1R3Milestone(term, 4, A, 413, dates.get(Integer.valueOf(27))));

            list.add(makeREMilestone(term, 4, A, 414, dates.get(Integer.valueOf(29))));

            list.add(makeFEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(30))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(27))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(28))));
            list.add(makeT1FEMilestone(term, 4, A, 415, dates.get(Integer.valueOf(29))));

            list.add(makeF1Milestone(term, 4, A, 415, dates.get(Integer.valueOf(31)), 1));
            list.add(makeT1F1Milestone(term, 4, A, 415, dates.get(Integer.valueOf(31))));

            list.add(makeREMilestone(term, 4, A, 421, dates.get(Integer.valueOf(32))));

            list.add(makeREMilestone(term, 4, A, 422, dates.get(Integer.valueOf(34))));

            list.add(makeREMilestone(term, 4, A, 423, dates.get(Integer.valueOf(36))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(33))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(34))));
            list.add(makeT2R3Milestone(term, 4, A, 423, dates.get(Integer.valueOf(35))));

            list.add(makeREMilestone(term, 4, A, 424, dates.get(Integer.valueOf(38))));

            list.add(makeFEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(39))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(36))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(37))));
            list.add(makeT2FEMilestone(term, 4, A, 425, dates.get(Integer.valueOf(38))));

            list.add(makeF1Milestone(term, 4, A, 425, dates.get(Integer.valueOf(40)), 1));
            list.add(makeT2F1Milestone(term, 4, A, 425, dates.get(Integer.valueOf(40))));

            list.add(makeREMilestone(term, 4, A, 431, dates.get(Integer.valueOf(41))));

            list.add(makeREMilestone(term, 4, A, 432, dates.get(Integer.valueOf(43))));

            list.add(makeREMilestone(term, 4, A, 433, dates.get(Integer.valueOf(45))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(42))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(43))));
            list.add(makeT3R3Milestone(term, 4, A, 433, dates.get(Integer.valueOf(44))));

            list.add(makeREMilestone(term, 4, A, 434, dates.get(Integer.valueOf(47))));

            list.add(makeFEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(48))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(45))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(46))));
            list.add(makeT3FEMilestone(term, 4, A, 435, dates.get(Integer.valueOf(47))));

            list.add(makeF1Milestone(term, 4, A, 435, dates.get(Integer.valueOf(49)), 1));
            list.add(makeT3F1Milestone(term, 4, A, 435, dates.get(Integer.valueOf(49))));

            list.add(makeREMilestone(term, 4, A, 441, dates.get(Integer.valueOf(50))));

            list.add(makeREMilestone(term, 4, A, 442, dates.get(Integer.valueOf(52))));

            list.add(makeREMilestone(term, 4, A, 443, dates.get(Integer.valueOf(54))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(51))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(52))));
            list.add(makeT4R3Milestone(term, 4, A, 443, dates.get(Integer.valueOf(53))));

            list.add(makeREMilestone(term, 4, A, 444, dates.get(Integer.valueOf(56))));

            list.add(makeFEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 1))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 4))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 3))));
            list.add(makeT4FEMilestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays - 2))));

            list.add(makeF1Milestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays)), 1));
            list.add(makeT4F1Milestone(term, 4, A, 445, dates.get(Integer.valueOf(numDays))));

            // Track 5A
            list.add(makeREMilestone(term, 5, A, 511, dates.get(Integer.valueOf(24))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(20))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(21))));
            list.add(makeT1R1Milestone(term, 5, A, 511, dates.get(Integer.valueOf(22))));

            list.add(makeREMilestone(term, 5, A, 512, dates.get(Integer.valueOf(26))));

            list.add(makeREMilestone(term, 5, A, 513, dates.get(Integer.valueOf(27))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(24))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(25))));
            list.add(makeT1R3Milestone(term, 5, A, 513, dates.get(Integer.valueOf(26))));

            list.add(makeREMilestone(term, 5, A, 514, dates.get(Integer.valueOf(28))));

            list.add(makeFEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(29))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(26))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(27))));
            list.add(makeT1FEMilestone(term, 5, A, 515, dates.get(Integer.valueOf(28))));

            list.add(makeF1Milestone(term, 5, A, 515, dates.get(Integer.valueOf(30)), 1));
            list.add(makeT1F1Milestone(term, 5, A, 515, dates.get(Integer.valueOf(30))));

            list.add(makeREMilestone(term, 5, A, 521, dates.get(Integer.valueOf(31))));

            list.add(makeREMilestone(term, 5, A, 522, dates.get(Integer.valueOf(33))));

            list.add(makeREMilestone(term, 5, A, 523, dates.get(Integer.valueOf(34))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(31))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(32))));
            list.add(makeT2R3Milestone(term, 5, A, 523, dates.get(Integer.valueOf(33))));

            list.add(makeREMilestone(term, 5, A, 524, dates.get(Integer.valueOf(35))));

            list.add(makeFEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(36))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(33))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(34))));
            list.add(makeT2FEMilestone(term, 5, A, 525, dates.get(Integer.valueOf(35))));

            list.add(makeF1Milestone(term, 5, A, 525, dates.get(Integer.valueOf(37)), 1));
            list.add(makeT2F1Milestone(term, 5, A, 525, dates.get(Integer.valueOf(37))));

            list.add(makeREMilestone(term, 5, A, 531, dates.get(Integer.valueOf(38))));

            list.add(makeREMilestone(term, 5, A, 532, dates.get(Integer.valueOf(40))));

            list.add(makeREMilestone(term, 5, A, 533, dates.get(Integer.valueOf(41))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(38))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(39))));
            list.add(makeT3R3Milestone(term, 5, A, 533, dates.get(Integer.valueOf(40))));

            list.add(makeREMilestone(term, 5, A, 534, dates.get(Integer.valueOf(42))));

            list.add(makeFEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(43))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(40))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(41))));
            list.add(makeT3FEMilestone(term, 5, A, 535, dates.get(Integer.valueOf(42))));

            list.add(makeF1Milestone(term, 5, A, 535, dates.get(Integer.valueOf(44)), 1));
            list.add(makeT3F1Milestone(term, 5, A, 535, dates.get(Integer.valueOf(44))));

            list.add(makeREMilestone(term, 5, A, 541, dates.get(Integer.valueOf(45))));

            list.add(makeREMilestone(term, 5, A, 542, dates.get(Integer.valueOf(47))));

            list.add(makeREMilestone(term, 5, A, 543, dates.get(Integer.valueOf(48))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(45))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(46))));
            list.add(makeT4R3Milestone(term, 5, A, 543, dates.get(Integer.valueOf(47))));

            list.add(makeREMilestone(term, 5, A, 544, dates.get(Integer.valueOf(49))));

            list.add(makeFEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(50))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(47))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(48))));
            list.add(makeT4FEMilestone(term, 5, A, 545, dates.get(Integer.valueOf(49))));

            list.add(makeF1Milestone(term, 5, A, 545, dates.get(Integer.valueOf(51)), 1));
            list.add(makeT4F1Milestone(term, 5, A, 545, dates.get(Integer.valueOf(51))));

            list.add(makeREMilestone(term, 5, A, 551, dates.get(Integer.valueOf(52))));

            list.add(makeREMilestone(term, 5, A, 552, dates.get(Integer.valueOf(54))));

            list.add(makeREMilestone(term, 5, A, 553, dates.get(Integer.valueOf(55))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(52))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(53))));
            list.add(makeT5R3Milestone(term, 5, A, 553, dates.get(Integer.valueOf(54))));

            list.add(makeREMilestone(term, 5, A, 554, dates.get(Integer.valueOf(56))));

            list.add(makeFEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 1))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 4))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 3))));
            list.add(makeT5FEMilestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays - 2))));

            list.add(makeF1Milestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays)), 1));
            list.add(makeT5F1Milestone(term, 5, A, 555, dates.get(Integer.valueOf(numDays))));
        }

        return list;
    }

    /**
     * Assembles the "days of week" for each week, by creating a map from week number to a map from day of week to its
     * date.
     *
     * <p>
     * This method assumes that each term week may only contain at-most one of each weekday that is not marked as a
     * holiday.
     *
     * @param termWeeks the list of weeks defined in the term (semester_calendar table)
     * @param holidays  the list of holidays in the term
     * @return the map from week number to map from day of week (1-5) to its date
     */
    private static Map<Integer, Map<Integer, LocalDate>> generateDaysOfWeeks(
            final Iterable<RawSemesterCalendar> termWeeks, final Collection<RawCampusCalendar> holidays) {

        final Collection<LocalDate> holidayDates = new ArrayList<>(holidays.size());
        for (final RawCampusCalendar cal : holidays) {
            holidayDates.add(cal.campusDt);
        }

        final Map<Integer, Map<Integer, LocalDate>> daysOfWeeks = new TreeMap<>();

        final java.util.Calendar cal = java.util.Calendar.getInstance();

        int maxWeek = -1;
        for (final RawSemesterCalendar week : termWeeks) {
            maxWeek = Math.max(maxWeek, week.weekNbr.intValue());
        }

        for (final RawSemesterCalendar week : termWeeks) {
            final Integer weekNum = week.weekNbr;
            if (weekNum.intValue() == 0 || weekNum.intValue() == maxWeek) {
                continue;
            }

            final Map<Integer, LocalDate> weekMap = new TreeMap<>();
            daysOfWeeks.put(weekNum, weekMap);

            final LocalDate start = week.startDt;
            final LocalDate end = week.endDt;

            LocalDate cur = start;
            do {
                if (!holidayDates.contains(cur)) {
                    cal.set(cur.getYear(), cur.getMonthValue() - 1, cur.getDayOfMonth());
                    final int day = cal.get(java.util.Calendar.DAY_OF_WEEK);

                    if (day == java.util.Calendar.MONDAY) {
                        weekMap.put(Integer.valueOf(1), cur);
                    } else if (day == java.util.Calendar.TUESDAY) {
                        weekMap.put(Integer.valueOf(2), cur);
                    } else if (day == java.util.Calendar.WEDNESDAY) {
                        weekMap.put(Integer.valueOf(3), cur);
                    } else if (day == java.util.Calendar.THURSDAY) {
                        weekMap.put(Integer.valueOf(4), cur);
                    } else if (day == java.util.Calendar.FRIDAY) {
                        weekMap.put(Integer.valueOf(5), cur);
                    }
                }

                cur = cur.plusDays(1L);
            } while (!cur.isAfter(end));
        }

        return daysOfWeeks;
    }

    /**
     * Given a map from week number to a map from day (1-5) to date, generates map from day of the term (1-N) to the
     * date.
     *
     * @param daysOfWeeks the days of weeks map
     * @return the numbered dates map
     */
    private static Map<Integer, LocalDate>
    numberDates(final Map<Integer, ? extends Map<Integer, LocalDate>> daysOfWeeks) {

        final Map<Integer, LocalDate> result = new TreeMap<>();

        int dayOfTerm = 1;

        for (final Map<Integer, LocalDate> weekMap : daysOfWeeks.values()) {
            for (final LocalDate date : weekMap.values()) {
                result.put(Integer.valueOf(dayOfTerm), date);
                ++dayOfTerm;
            }
        }

        return result;
    }

    /**
     * Creates a {@code RawMilestone} record for a unit review exam.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeREMilestone(final TermRec term, final int pace, final String track,
                                                final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                "RE", date, null);
    }

    /**
     * Creates a {@code RawMilestone} record for a final exam.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeFEMilestone(final TermRec term, final int pace,
                                                final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                "FE", date, null);
    }

    /**
     * Creates a {@code RawMilestone} record for a final exam last try.
     *
     * @param term     the term
     * @param pace     the pace
     * @param track    the track
     * @param msNbr    the milestone number
     * @param date     the date
     * @param numTries the number of tries
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeF1Milestone(final TermRec term, final int pace,
                                                final String track, final int msNbr, final LocalDate date,
                                                final int numTries) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                "F1", date, Integer.valueOf(numTries));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Review exam 1 of first course" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT1R1Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_1R1, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Review exam 3 of course 1" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT1R3Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_1R3, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam of course 1" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT1FEMilestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_1FE, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam last try of course 1" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT1F1Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_1F1, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Review exam 3 of course 2" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT2R3Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_2R3, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam of course 2" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT2FEMilestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_2FE, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam last try of course 2" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT2F1Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_2F1, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Review exam 3 of course 3" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT3R3Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_3R3, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam of course 3" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT3FEMilestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_3FE, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam last try of course 3" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT3F1Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_3F1, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Review exam 3 of course 4" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT4R3Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_4R3, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam of course 4" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT4FEMilestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_4FE, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam last try of course 4" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT4F1Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_4F1, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Review exam 3 of course 5" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT5R3Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_5R3, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam of course 5" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT5FEMilestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_5FE, date, Integer.valueOf(0));
    }

    /**
     * Creates a {@code RawMilestone} record for a "Final exam last try of course 5" touch point.
     *
     * @param term  the term
     * @param pace  the pace
     * @param track the track
     * @param msNbr the milestone number
     * @param date  the date
     * @return the {@code RawMilestone} record
     */
    private static RawMilestone makeT5F1Milestone(final TermRec term, final int pace,
                                                  final String track, final int msNbr, final LocalDate date) {

        return new RawMilestone(term.term, Integer.valueOf(pace), track, Integer.valueOf(msNbr),
                RawMilestone.TOUCHPOINT_5F1, date, Integer.valueOf(0));
    }

    /**
     * Executes the job.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        final DbProfile dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);

        final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final TermRec active = TermLogic.get(cache).queryActive(cache);

                final List<RawSemesterCalendar> weeks =
                        RawSemesterCalendarLogic.INSTANCE.queryAll(cache);
                final List<RawCampusCalendar> calendarDays =
                        RawCampusCalendarLogic.INSTANCE.queryAll(cache);

                final List<RawMilestone> milestones =
                        generateMilestones(active, weeks, calendarDays);
                Collections.sort(milestones);

                String last = CoreConstants.EMPTY;
                for (final RawMilestone ms : milestones) {
                    final String str = ms.msNbr.toString();

                    if (!last.equals(str)) {
                        Log.fine(CoreConstants.EMPTY);
                        last = str;
                    }
                    Log.fine("Pace ", ms.pace, " Track ", ms.paceTrack, ", Milestone ", str, ", Type ",
                            ms.msType, ", ", TemporalUtils.FMT_WMDY.format(ms.msDate));
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}
