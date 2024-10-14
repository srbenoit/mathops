package dev.mathops.dbjobs.report.analytics.irdata;

/**
 * A container for the data about a student that does not change between lines in the data file.
 */
record StudentCohortRecord(
        String pid,               // CSU ID
        String fallTerm,          // For summer starts, the fall term they matriculate to (SM16 start has FA16 here)
        String term,              // Cohort term a student begins at CSU
        String applyType,         // Application type to CSU (all are "New" here)
        String fullTime,          // Enrolled full-time (1/yes) or part-time (0/no) for their first fall term
        String collegeName,       // Primary major college in first term
        String deptName,          // Primary major department in first term
        String programDesc,       // Primary major program/concentration in first term
        boolean female,           // Flag for female (1/yes) in cohort term
        boolean rm,               // Flag for Racially Minoritized status (1/yes) in cohort term
        boolean firstGen,         // Flag for first generation (1/yes)
        boolean pell,             // Flag for Pell status (1/yes) in cohort term
        boolean resident,         // Flag for CO resident (1/yes) in cohort term
        Float hsGpa,              // High school GPA
        boolean srsFlag,          // Student's SRS status (1/yes) in first fall
        boolean struggledMathHs,  // Student is SRS because they struggled with math in HS; not used consistently
        boolean lowMathTestScore, // Student is SRS because of low math test scores; used often before test optional
        boolean dfGradeMath,      // Student is SRS because they received D/F grades in HS math; only in FA23 cohort
        boolean mathFlags,        // Student is SRS because of any math flags identified above
        boolean lowGpa) {         // Student is SRS because of a low HS GPA
}
