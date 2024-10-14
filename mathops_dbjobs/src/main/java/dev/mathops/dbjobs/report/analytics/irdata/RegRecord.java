package dev.mathops.dbjobs.report.analytics.irdata;

/**
 * A container for the data about a single course registration.
 */
record RegRecord(
        String pid,             // CSU ID
        Integer termSeq,        // Count of the term for the student (1 is for first fall and 2 for first spring)
        String course,          // Course for student in MASTER_TERM; null if student not enrolled in MASTER_TERM
        String section,         // Section of course
        String instructionType, // Instruction type of the course
        String college,         // College of the course
        String collegeCode,     // College code of the course
        String dept,            // Department of the course
        String deptCode,        // Department code of the course
        String gradeGroup,      // Course grade group; null if student enrolled at Census but not EOT (Univ. Wd.)
        Float gradePoints,      // Course grade points; null if student enrolled at Census but not EOT (Univ. Wd.)
        String grade) {         // Course grade; null if student enrolled at Census but not EOT (Univ. Wd.)
}