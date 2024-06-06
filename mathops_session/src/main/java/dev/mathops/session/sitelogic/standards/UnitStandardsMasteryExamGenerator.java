package dev.mathops.session.sitelogic.standards;

import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawlogic.RawCustdLogic;
import dev.mathops.db.old.rawlogic.RawStdLogic;
import dev.mathops.db.old.rawlogic.RawStstdLogic;
import dev.mathops.db.old.rawrecord.RawCustd;
import dev.mathops.db.old.rawrecord.RawStd;
import dev.mathops.db.old.rawrecord.RawStstd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that can generate a realized unit mastery assessment for a student in a course. The assessment will include
 * standards from the selected unit and any preceding units that have not yet been mastered.
 */
public enum UnitStandardsMasteryExamGenerator {
    ;

    /**
     * Attempts to build a mastery exam for a student in a course and unit.
     *
     * @param cache    the data cache
     * @param stuId    the student ID
     * @param courseId the course ID
     * @param unit     the unit number
     * @return the constructed exam, which may have no items if the student is not eligible for the unit exam or has
     *         already mastered all standards
     */
    public static StandardsMasteryExamContainer buildAssessment(final Cache cache,
                                                                final String stuId, final String courseId,
                                                                final int unit) {

        // Fetch all "custd" records for the course and units <= the specified unit, and build
        // a map keyed on standard ID of these. At the same time, create a list of standard IDs
        // sorted by the order
        final List<RawCustd> courseStandards = RawCustdLogic.queryByCourse(cache, courseId);
        final Map<String, RawCustd> courseStandardsMap = new HashMap<>(courseStandards.size());
        final Collection<String> orderedStandardIds = new ArrayList<>(courseStandards.size());

        for (final RawCustd row : courseStandards) {
            if (row.unit.intValue() <= unit) {
                courseStandardsMap.put(row.stdId, row);
                orderedStandardIds.add(row.stdId);
            }
        }

        // Query all "ststd" records and organize all that apply the course into a map keyed on
        // standard ID
        final List<RawStstd> studentStandards = RawStstdLogic.queryByStudent(cache, stuId);
        final Map<String, RawStstd> studentStandardMap = new HashMap<>(studentStandards.size());

        for (final RawStstd row : studentStandards) {
            if (orderedStandardIds.contains(row.stdId)) {
                studentStandardMap.put(row.stdId, row);
            }
        }

        // Query all "std" records and create a map that has all standards indicated above, keyed
        // on standard ID
        final List<RawStd> standards = RawStdLogic.INSTANCE.queryAll(cache);
        final Map<String, RawStd> standardsMap = new HashMap<>(standards.size());

        for (final RawStd row : standards) {
            if (orderedStandardIds.contains(row.stdId)) {
                standardsMap.put(row.stdId, row);
            }
        }

        final StandardsMasteryExam exam = new StandardsMasteryExam();

        // Sweep through all standards needed, and add all the student has not mastered to the
        // exam.
        int numGroups = 0;
        for (final String standardId : orderedStandardIds) {
            final RawStstd ststd = studentStandardMap.get(standardId);
            if (ststd == null) {
                // Not yet started - add all item groups to the assessment
                final RawCustd custd = courseStandardsMap.get(standardId);
                final RawStd std = standardsMap.get(standardId);
                final int masteryGroups = std.masteryGroups.intValue();

                for (int group = 1; group < 32; ++group) {
                    final int bitFlag = 0x01 << (group - 1);
                    if (bitFlag > masteryGroups) {
                        break;
                    }
                    if ((bitFlag & masteryGroups) == bitFlag) {
                        // Group is part of standard, and not yet attempted
                        exam.addItemGroup(custd.unit, std.stdId, group);
                        ++numGroups;
                    }
                }
            } else if (ststd.whenMastered == null) {
                // Not yet completely mastered - add unmastered item groups to the exam...

                final RawCustd custd = courseStandardsMap.get(standardId);
                final RawStd std = standardsMap.get(standardId);
                final int masteryGroups = std.masteryGroups.intValue();
                final int masteredGroups = ststd.masteredGroups.intValue();

                for (int group = 1; group < 32; ++group) {
                    final int bitFlag = 0x01 << (group - 1);
                    if (bitFlag > masteryGroups) {
                        break;
                    }
                    if ((bitFlag & masteryGroups) == bitFlag && (bitFlag & masteredGroups) == 0) {
                        // Group is part of standard, but not yet mastered
                        exam.addItemGroup(custd.unit, std.stdId, group);
                        ++numGroups;
                    }
                }
            }
        }

        final StandardsMasteryExamContainer result;

        if (numGroups == 0) {
            // Student has already mastered the entire set of applicable standards!
            result = new StandardsMasteryExamContainer(
                    EStandardsMasteryExamResult.NO_ITEM_GROUPS_NEED_MASTERY, null);
        } else {
            // Realize the exam, generating random items from each needed group.
            final EStandardsMasteryExamResult status = exam.realize(cache);
            result = new StandardsMasteryExamContainer(status, exam);
        }

        return result;
    }
}
