package dev.mathops.dbjobs.report.analytics.irdata;

/**
 * A container for the data about a single term for a student.
 */
record TermRecord(
        String pid,           // CSU ID
        Integer termSeq,      // Count of the term for the student (1 is for first fall and 2 for first spring)
        String termCalc,      // Count of the term by number and term season ("01st Fall" is for first fall)
        String masterTerm,    // Term associated with longitudinal time-varying data like TERM_SEQ and TERM_CALC
        boolean censusFlag,   // (1/yes) if the student is enrolled at Census of  MASTER_TERM
        boolean deceased,     // (1/yes) if the student is deceased in  MASTER_TERM
        String censusCollege, // Primary major college as of Census for the selected MASTER_TERM
        String censusDept,    // Primary major department as of Census for the selected MASTER_TERM
        String censusProgram, // Primary major program/concentration as of Census for the selected MASTER_TERM
        String censusClass,   // Class level for MASTER_TERM based on earned credits (11=Fr 21=So 31=Jr 41=Sr)
        boolean eotFlag,      // (1/yes) if the student is enrolled at end-of-term (EOT) of MASTER_TERM
        Float eotTermGpa,     // CSU term GPA at the end of MASTER_TERM
        Float eotCsuGpa,      // CSU cumulative GPA at the end of MASTER_TERM
        Boolean eotProbation, // (1/yes) if on academic probation as of the end of MASTER_TERM
        Boolean persisted,    // (1/yes) if enrolled at Census/EOT in MASTER_TERM or graduated in/before MASTER_TERM
        Boolean graduated) {  // (1/yes) if graduated in/before MASTER_TERM
}
