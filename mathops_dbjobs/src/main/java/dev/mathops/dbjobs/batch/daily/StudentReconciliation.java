package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A utility class that accepts a list of (authoritative) {@code Student} records and reconciles it with the current
 * contents of the database, updating or inserting records as needed.
 */
enum StudentReconciliation {
    ;

    /** A commonly used string. */
    private static final String STUDENT = "  Student ";

    /** A commonly used string. */
    private static final String TO = " to ";

    /** A commonly used string. */
    private static final String TICK_TO_TICK = "' to '";

    /** A commonly used string. */
    private static final String TICK = "'";

    /**
     * Compares the existing student records to a list of new student records. For any new records not in the existing
     * collection, a new student record is created. For all new records that exist already, the existing record is
     * tested for changes and updated if needed.
     *
     * @param cache            the data cache
     * @param existingStudents a map from student ID to existing student data record
     * @param newStudents      a map from student ID to new student data record
     * @param report           a list of strings to which to add report output lines
     * @param debug            if true, actions will be printed rather than performed on the database
     * @throws SQLException if there is an error accessing the database
     */
    static void reconcile(final Cache cache, final Map<? super String, RawStudent> existingStudents,
                          final Map<String, RawStudent> newStudents, final Collection<? super String> report,
                          final boolean debug) throws SQLException {

        int numToAdd = 0;
        int numExisting = 0;
        for (final RawStudent rec : newStudents.values()) {
            if (existingStudents.containsKey(rec.stuId)) {
                ++numExisting;
            } else {
                ++numToAdd;
            }
        }

        report.add("  " + numExisting + " existing student records will be checked for update.");
        report.add("  " + numToAdd + " new student records will be added.");

        int insertCount = 0;
        for (final RawStudent rec : newStudents.values()) {
            if ("888888888".equals(rec.stuId)) {
                continue;
            }

            final RawStudent existing = existingStudents.get(rec.stuId);

            if (existing == null) {
                // Record should be inserted
                report.add("  Inserting " + rec.getScreenName() + " (" + rec.stuId + ")");

                if (debug) {
                    Log.fine("Student to insert: ", rec.stuId);
                    Log.fine(" PIDM ", rec.pidm);
                    Log.fine(" Name: ", rec.firstName, CoreConstants.SPC, rec.lastName);
                    Log.fine(" Screen Name: ", rec.getScreenName());
                    Log.fine(" App Term: ", rec.aplnTerm);
                    Log.fine(" Level: ", rec.clazz);
                    Log.fine(" College: ", rec.college);
                    Log.fine(" Department: ", rec.dept);
                    Log.fine(" Program: ", rec.programCode);
                    Log.fine(" Minor: ", rec.minor);
                    Log.fine(" Grad term: ", rec.estGraduation);
                    Log.fine(" Transfer: ", rec.trCredits);
                    Log.fine(" HS Code: ", rec.hsCode);
                    Log.fine(" HS GPA: ", rec.hsGpa);
                    Log.fine(" HS Rank: ", rec.hsClassRank);
                    Log.fine(" HS Size: ", rec.hsSizeClass);
                    Log.fine(" ACT: ", rec.actScore);
                    Log.fine(" SAT: ", rec.satScore);
                    Log.fine(" AP: ", rec.apScore);
                    Log.fine(" Residency: ", rec.resident);
                    Log.fine(" Birth Date: ", rec.birthdate);
                    Log.fine(" Gender: ", rec.gender);
                    Log.fine(" Campus: ", rec.campus);
                    Log.fine(" Email: ", rec.stuEmail);
                    Log.fine(" Adviser: ", rec.adviserEmail);
                    Log.fine(" Admit Type: ", rec.admitType);
                    Log.fine(" Course Order:", rec.orderEnforce);
                    Log.fine(" When Created:", rec.createDt);
                } else {
                    RawStudentLogic.insert(cache, rec);
                }

                ++insertCount;
                existingStudents.put(rec.stuId, rec);
            }
        }

        int updateCount = 0;
        for (final RawStudent rec : newStudents.values()) {
            if ("888888888".equals(rec.stuId)) {
                continue;
            }

            // Normalize GPA
            if (rec.hsGpa != null) {
                int len = rec.hsGpa.length();
                while (len > 1 && rec.hsGpa.charAt(len - 1) == '0') {
                    rec.hsGpa = rec.hsGpa.substring(0, len - 1);
                    --len;
                }

                if (len > 1 && rec.hsGpa.charAt(len - 1) == '.') {
                    rec.hsGpa = rec.hsGpa.substring(0, len - 1);
                    --len;
                }
            }

            final RawStudent existing = existingStudents.get(rec.stuId);
            if (existing != null) {
                boolean updated;

                // NOTE: The fields below come from registrations - if the student has no active
                // registrations in the term where the query is run, these will be null, but
                // we don't want nulls in these cases to stomp real data that already exists
                // in the STUDENT table. So we replace nulls with current data if found.
                // rec.getClassLevel()
                // rec.getCollege()
                // rec.getDepartment()
                // rec.getProgramCode()
                // rec.getAnticGradTerm()
                // rec.getResidency()
                // rec.getCampus()

                boolean updateName = false;

                if (rec.firstName != null
                        && !Objects.equals(rec.firstName, existing.firstName)) {
                    report.add(STUDENT + existing.stuId + " first name: update from '"
                            + existing.firstName + TICK_TO_TICK + rec.firstName + TICK);
                    updateName = true;
                }

                if (rec.lastName != null
                        && !Objects.equals(rec.lastName, existing.lastName)) {
                    report.add(STUDENT + existing.stuId + " last name:, update from '"
                            + existing.lastName + TICK_TO_TICK + rec.lastName + TICK);
                    updateName = true;
                }

                if (rec.prefName != null
                        && !Objects.equals(rec.prefName, existing.prefName)) {
                    report.add(STUDENT + existing.stuId + " pref name:, update from '"
                            + existing.prefName + TICK_TO_TICK + rec.prefName + TICK);
                    updateName = true;
                }

                if (rec.middleInitial != null && !Objects.equals(rec.middleInitial, existing.middleInitial)) {
                    report.add(STUDENT + existing.stuId + " middle initial:, update from '"
                            + existing.middleInitial + TICK_TO_TICK + rec.middleInitial + TICK);
                    updateName = true;
                }

                updated = updateName;
                if (updateName && !debug) {
                    RawStudentLogic.updateName(cache, existing.stuId,
                            rec.lastName == null ? existing.lastName : rec.lastName,
                            rec.firstName == null ? existing.firstName : rec.firstName,
                            rec.prefName == null ? existing.prefName : rec.prefName,
                            rec.middleInitial == null ? existing.middleInitial : rec.middleInitial);
                }

                if (rec.birthdate != null
                        && !Objects.equals(rec.birthdate, existing.birthdate)) {
                    report.add(STUDENT + existing.stuId + " birth day: update from "
                            + existing.birthdate + TO + rec.birthdate);
                    if (!debug) {
                        RawStudentLogic.updateBirthDate(cache, existing.stuId, rec.birthdate);
                    }
                    updated = true;
                }

                if (!Objects.equals(rec.gender, existing.gender)) {
                    report.add(STUDENT + existing.stuId + " gender: update from " + existing.gender
                            + TO + rec.gender);
                    if (!debug) {
                        RawStudentLogic.updateGender(cache, existing.stuId, rec.gender);
                    }
                    updated = true;
                }

                if (rec.clazz != null
                        && !Objects.equals(rec.clazz, existing.clazz)) {
                    report.add(STUDENT + existing.stuId + " class level: update from "
                            + existing.clazz + TO + rec.clazz);
                    if (!debug) {
                        RawStudentLogic.updateClassLevel(cache, existing.stuId, rec.clazz);
                    }
                    updated = true;
                }

                boolean updateProgram = false;

                if (rec.college != null
                        && !Objects.equals(rec.college, existing.college)) {
                    report.add(STUDENT + existing.stuId + " college: update from "
                            + existing.college + TO + rec.college);
                    updateProgram = true;
                }

                if (rec.dept != null
                        && !Objects.equals(rec.dept, existing.dept)) {
                    report.add(STUDENT + existing.stuId + " department: update from "
                            + existing.dept + TO + rec.dept);
                    updateProgram = true;
                }

                if (rec.programCode != null
                        && !Objects.equals(rec.programCode, existing.programCode)) {
                    report.add(STUDENT + existing.stuId + " program code: update from "
                            + existing.programCode + TO + rec.programCode);
                    updateProgram = true;
                }

                updated = updated || updateProgram;
                if (updateProgram && !debug) {
                    RawStudentLogic.updateProgram(cache, existing.stuId, rec.college, rec.dept,
                            rec.programCode, existing.minor);
                }

                if (rec.trCredits != null
                        && !Objects.equals(rec.trCredits, existing.trCredits)) {
                    report.add(STUDENT + existing.stuId + " transfer credits: update from "
                            + existing.trCredits + TO + rec.trCredits);
                    if (!debug) {
                        RawStudentLogic.updateNumTransferCredits(cache, existing.stuId,
                                rec.trCredits);
                    }
                    updated = true;
                }

                boolean updateHighSchool = false;

                if (!Objects.equals(rec.hsCode, existing.hsCode)) {
                    report.add(STUDENT + existing.stuId + " high school code: update from "
                            + existing.hsCode + TO + rec.hsCode);
                    updateHighSchool = true;
                }

                if (!Objects.equals(rec.hsGpa, existing.hsGpa)) {
                    report.add(STUDENT + existing.stuId + " high school GPA: update from "
                            + existing.hsGpa + TO + rec.hsGpa);
                    updateHighSchool = true;
                }

                if (!Objects.equals(rec.hsClassRank, existing.hsClassRank)) {
                    report.add(STUDENT + existing.stuId + " high school class rank: update from "
                            + existing.hsClassRank + TO + rec.hsClassRank);
                    updateHighSchool = true;
                }

                if (!Objects.equals(rec.hsSizeClass, existing.hsSizeClass)) {
                    report.add(STUDENT + existing.stuId + " high school class size: update from "
                            + existing.hsSizeClass + TO + rec.hsSizeClass);
                    updateHighSchool = true;
                }

                updated = updated || updateHighSchool;
                if (updateHighSchool && !debug) {
                    RawStudentLogic.updateHighSchool(cache, existing.stuId, rec.hsCode, rec.hsGpa,
                            rec.hsClassRank, rec.hsSizeClass);
                }

                boolean updateTestScores = false;

                if (!Objects.equals(rec.actScore, existing.actScore)) {
                    report.add(STUDENT + existing.stuId + " ACT: update from " + existing.actScore
                            + TO + rec.actScore);
                    updateTestScores = true;
                }

                if (!Objects.equals(rec.satScore, existing.satScore)) {
                    report.add(STUDENT + existing.stuId + " SAT: update from " + existing.satScore
                            + TO + rec.satScore);
                    updateTestScores = true;
                }

                if (!Objects.equals(rec.apScore, existing.apScore)) {
                    report.add(STUDENT + existing.stuId + " AP: update from " + existing.apScore
                            + TO + rec.apScore);
                    updateTestScores = true;
                }

                updated = updated || updateTestScores;
                if (updateTestScores && !debug) {
                    RawStudentLogic.updateTestScores(cache, existing.stuId, rec.actScore,
                            rec.satScore, rec.apScore);
                }

                // It does not seem like the resident from ODS is the right code.

                if ((rec.resident != null
                        && !Objects.equals(rec.resident, existing.resident))
                        && (existing.resident != null
                        && existing.resident.length() < rec.resident.length())) {

                    report.add(STUDENT + existing.stuId + " resident: update from "
                            + existing.resident + TO + rec.resident);
                    if (!debug) {
                        RawStudentLogic.updateResidency(cache, existing.stuId, rec.resident);
                    }
                    updated = true;
                }

                if (!Objects.equals(rec.pidm, existing.pidm)) {
                    report.add(STUDENT + existing.stuId + " Internal ID: update from "
                            + existing.pidm + TO + rec.pidm);
                    if (!debug) {
                        RawStudentLogic.updateInternalId(cache, existing.stuId, rec.pidm);
                    }
                    updated = true;
                }

                if (rec.aplnTerm != null
                        && !Objects.equals(rec.aplnTerm, existing.aplnTerm)) {
                    report.add(STUDENT + existing.stuId + " application term: update from "
                            + existing.aplnTerm + TO + rec.aplnTerm);
                    if (!debug) {
                        RawStudentLogic.updateApplicationTerm(cache, existing.stuId, rec.aplnTerm);
                    }
                    updated = true;
                }

                if (rec.estGraduation != null && !Objects.equals(rec.estGraduation, existing.estGraduation)) {
                    report.add(STUDENT + existing.stuId + " graduation term: update from "
                            + existing.estGraduation + TO + rec.estGraduation);
                    if (!debug) {
                        RawStudentLogic.updateAnticGradTerm(cache, existing.stuId,
                                rec.estGraduation);
                    }
                    updated = true;
                }

                if (rec.campus != null
                        && !Objects.equals(rec.campus, existing.campus)) {
                    report.add(STUDENT + existing.stuId + " campus: update from " + existing.campus
                            + TO + rec.campus);
                    if (!debug) {
                        RawStudentLogic.updateCampus(cache, existing.stuId, rec.campus);
                    }
                    updated = true;
                }

                if (updated) {
                    ++updateCount;
                }
            }
        }

        report.add("  Inserted " + insertCount + " records, updated " + updateCount + " records.");
    }
}
