package dev.mathops.db.old.logic.mathplan.data;

/**
 * Possible next steps for a student.
 */
public enum ENextStep {

    /**
     * 1A. "It is critical that you satisfy the prerequisites for your first semester course, so you can register to
     * take that course in the {APLN_TERM} semester (ideally during Orientation)."
     */
    MSG_1A_SINGULAR,

    /**
     * 1A. "It is critical that you satisfy the prerequisites for your first semester courses, so you can register to
     * take those courses in the {APLN_TERM} semester (ideally during Orientation)."
     */
    MSG_1A_PLURAL,

    /**
     * 1B. "A course critical to your program has prerequisites that are not yet satisfied. You should try to satisfy
     * these prerequisites as quickly as possible."
     */
    MSG_1B_SINGULAR,

    /**
     * 1B. "There are courses critical to your program whose prerequisites are not yet satisfied. You should try to
     * satisfy these prerequisites as quickly as possible."
     */
    MSG_1B_PLURAL,

    /**
     * 1C. "We recommend that you satisfy the prerequisites for your first semester course, so you can register to take
     * that course in {APLN_TERM} (ideally during Orientation), if space permits."
     */
    MSG_1C_SINGULAR,

    /**
     * 1C. "We recommend that you satisfy the prerequisites for your first semester courses, so you can register to take
     * those courses in {APLN_TERM} (ideally during Orientation), if space permits."
     */
    MSG_1C_PLURAL,

    /**
     * 1D. "A course that is recommended for your program has prerequisites that are not yet satisfied. You should try
     * to satisfy these prerequisites as quickly as possible."
     */
    MSG_1D_SINGULAR,

    /**
     * 1D. "There are courses that are recommended for your program whose prerequisites are not yet satisfied. You
     * should try to satisfy these prerequisites as quickly as possible."
     */
    MSG_1D_PLURAL,

    /**
     * 1E. "Satisfying the prerequisites of your first-semester course would allow you to register to take that course
     * in {APLN_TERM} (ideally during Orientation), if space permits."
     */
    MSG_1E_SINGULAR,

    /**
     * 1E. "Satisfying the prerequisites of your first-semester courses would allow you to register to take those
     * courses in {APLN_TERM} (ideally during Orientation), if space permits."
     */
    MSG_1E_PLURAL,

    /**
     * 1F. "A course in your program has prerequisites that are not yet satisfied. You should try to satisfy these
     * prerequisites as quickly as possible."
     */
    MSG_1F_SINGULAR,

    /**
     * 1F. "There are courses in your program whose prerequisites are not yet satisfied. You should try to satisfy these
     * prerequisites as quickly as possible."
     */
    MSG_1F_PLURAL,

    /**
     * 1G. "The fastest way to satisfy these prerequisites is with the Math Placement Exam. Review and practice
     * materials are provided to help you prepare for the placement exam."
     */
    MSG_1G,

    /**
     * 1H. "You have {0} attempt(s) remaining on the Math Placement Exam. You can use this (these) attempt(s) to try to
     * satisfy these prerequisites. Review and practice materials are provided to help you prepare for the placement
     * exam."
     */
    MSG_1H,

    /**
     * 1I. "If you do not satisfy all prerequisites for your first-semester course with the Math Placement Exam, you may
     * complete sections of the Precalculus Tutorial to satisfy these prerequisites."
     */
    MSG_1I,

    /**
     * 1J. "You may complete sections of the Precalculus Tutorial to satisfy these prerequisites."
     */
    MSG_1J,

    /**
     * 1K. "You also have {0} attempt(s) remaining on the Math Placement Exam. You can use this (these) attempt(s) to
     * try to satisfy these prerequisites. Review and practice materials are provided to help you prepare for the
     * placement exam."
     */
    MSG_1K,

    /**
     * 1L. "You can complete the Entry-Level Mathematics (ELM) Tutorial to allow you to register for MATH 117: College
     * Algebra in Context I, but this will NOT satisfy the prerequisites for your first-semester course."
     */
    MSG_1L_SINGULAR,

    /**
     * 1L. "You can complete the Entry-Level Mathematics (ELM) Tutorial to allow you to register for MATH 117: College
     * Algebra in Context I, but this will NOT satisfy the prerequisites for your first-semester courses."
     */
    MSG_1L_PLURAL,

    /**
     * 1M. "You should speak with your adviser about your critical first-semester mathematics course."
     */
    MSG_1M_SINGULAR,

    /**
     * 1M. "You should speak with your adviser about your critical first-semester mathematics courses."
     */
    MSG_1M_PLURAL,

    /**
     * 1N. "The fastest way to satisfy this (these) prerequisite(s) is through Math Placement Exam. Review and practice
     * materials are provided to help you succeed."
     */
    MSG_1N,

    /**
     * 1O. "You have {0} attempt(s) remaining on the Math Placement Exam. You can use this (these) attempt(s) to try to
     * satisfy these prerequisites. Review and practice materials are provided to help you prepare for the placement
     * exam."
     */
    MSG_1O,

    /**
     * 1P. "If you do not satisfy all prerequisites for your first-semester course with the Math Placement Exam, you may
     * complete sections of the Precalculus Tutorial to satisfy these prerequisites."
     */
    MSG_1P,

    /**
     * 1Q. "You may complete sections of the Precalculus Tutorial to satisfy these prerequisites."
     */
    MSG_1Q,

    /**
     * 1R. "You also have {0} attempt(s) remaining on the Math Placement Exam. You can use this (these) attempt(s) to
     * try to satisfy these prerequisites. Review and practice materials are provided to help you prepare for the
     * placement exam."
     */
    MSG_1R,

    /**
     * 1S. "You can complete the Entry-Level Mathematics (ELM) Tutorial to allow you to register for MATH 117: College
     * Algebra in Context I, but this will NOT satisfy the prerequisites for your first-semester course."
     */
    MSG_1S_SINGULAR,

    /**
     * 1S. "You can complete the Entry-Level Mathematics (ELM) Tutorial to allow you to register for MATH 117: College
     * Algebra in Context I, but this will NOT satisfy the prerequisites for your first-semester courses."
     */
    MSG_1S_PLURAL,

    //
    //
    //

    /**
     * 2A. "It is critical that you become eligible to register to take MATH 117: College Algebra in Context I in
     * {APLN_TERM} (ideally during Orientation)."
     */
    MSG_2A,

    /**
     * 2B. "We recommend that you try to become eligible to register to take MATH 117: College Algebra in Context I in
     * {APLN_TERM} (ideally during Orientation)."
     */
    MSG_2B,

    /**
     * 2C. "If you would like to take MATH 117: College Algebra in Context in {APLN_TERM}, you should try to become
     * eligible for that course in time to register for {APLN_TERM} courses (ideally before Orientation)."
     */
    MSG_2C,

    /**
     * 2D. "It is critical that you become eligible to register to take {0} in {APLN_TERM} (ideally during
     * Orientation)."
     */
    MSG_2D,

    /**
     * 2E. "We recommend that you try to become eligible to register to take {0} in {APLN_TERM} (ideally during
     * Orientation)."
     */
    MSG_2E,

    /**
     * 2F. "It is critical that you become eligible to register to take MATH 117: College Algebra in Context I as
     * quickly as possible."
     */
    MSG_2F,

    /**
     * 2G. "We recommend that you try to become eligible to register to take MATH 117: College Algebra in Context I as
     * quickly as possible."
     */
    MSG_2G,

    /**
     * 2H. "We recommend that you try to become eligible to register to take MATH 117: College Algebra in Context I as
     * quickly as possible."
     */
    MSG_2H,

    /**
     * 2I. "It is critical that you become eligible to register to take {0} as quickly as possible."
     */
    MSG_2I,

    /**
     * 2J. "We recommend that you try to become eligible to take {0} as quickly as possible."
     */
    MSG_2J,

    /**
     * 2K. "The fastest way to become eligible to take MATH 117 is with the Math Placement Exam. Review and practice
     * materials are provided to help you prepare for the placement exam."
     */
    MSG_2K,

    /**
     * 2L. "You only need to complete the Algebra section of the Math Placement Exam to become eligible for MATH 117,
     * but completing as much of the exam as you can gives you more options if you change majors."
     */
    MSG_2L,

    /**
     * 2M. "You have {0} attempt(s) remaining on the Math Placement Exam. You can use this (these) attempt(s) to try to
     * satisfy these prerequisites. Review and practice materials are provided to help you prepare for the placement
     * exam."
     */
    MSG_2M,

    /**
     * 2N. "You can also become eligible for MATH 117 by completing the Entry Level Mathematics (ELM) Tutorial."
     */
    MSG_2N,

    /**
     * 2O. "You can become eligible for MATH 117 by completing the Entry Level Mathematics (ELM) Tutorial."
     */
    MSG_2O,

    /**
     * 2P. "You also have {0} attempt(s) remaining on the Math Challenge Exam. You can use this (these) attempt(s) to
     * try to become eligible for MATH 117. Review and practice materials are provided to help you prepare for the
     * placement exam."
     */
    MSG_2P,

    /** 2Q. "You can become eligible for {0} by taking the Math Placement Exam." */
    MSG_2Q,

    /**
     * 2R. "You only need to complete the Algebra section of the Math Placement Exam to become eligible for this (these)
     * course(s), but completing as much of the exam as you can gives you more options in case you change majors."
     */
    MSG_2R,

    //
    //
    //

    /**
     * 3A. "[Check] No further action is needed."
     */
    MSG_3A,

    /**
     * 3B. "We recommend that you complete the required three credits of Core Curriculum mathematics within your first
     * thirty credit hours."
     */
    MSG_3B,

    /**
     * 3C. "If you decide to pursue a major that requires College Algebra or other Precalculus or Calculus courses, you
     * will need to take the Math Placement Exam or complete a tutorial to become eligible for those mathematics
     * courses."
     */
    MSG_3C,

    /**
     * 3D. "You have {0} attempt(s) remaining on the Math Placement Exam. By taking this exam in a proctored setting,
     * you can attempt(s) to try to earn credit in one or more required courses."
     */
    MSG_3D,

    /**
     * 3E. "You can place out of one or more of the courses required by your major with the Math Placement Exam. Review
     * and practice materials are provided to help you prepare for the placement exam."
     */
    MSG_3E,

    /**
     * 3F. "You have {0} attempt(s) remaining on the Math Placement Exam. You can use this (these) attempts to try to
     * place out of one or more required courses. Review and practice materials are provided to help you prepare for the
     * placement exam."
     */
    MSG_3F,

    /**
     * 3G. "You do not need to take a Math Placement Exam."
     */
    MSG_3G,

    /**
     * 3H. "You do not need to take a Math Placement Exam unless you plan to take a math course other than MATH 101,
     * MATH 105, STAT 100, STAT 201, or STAT 204."
     */
    MSG_3H,

    //
    //
    //

    /**
     * 4A. "[Check] No further action is needed."
     */
    MSG_4A,

    /**
     * 4B. "You do not need to take a Math Placement Exam."
     */
    MSG_4B,

    /**
     * 4C. "Your existing course credit satisfies the requirements of your selected major(s) of interest. Please verify
     * this when you speak with your adviser."
     */
    MSG_4C,

    //
    //
    //

    /** A. Action: [Tell me more about the Math Placement Exam]. */
    ACT_MATH_PLACEMENT_EXAM,

    /** B. Action: [Tell me more about the ELM Tutorial]. */
    ACT_ELM_TUTORIAL,

    /** C. Action: [Tell me more about the Precalculus Tutorial]. */
    ACT_PRECALCULUS_TUTORIAL,

    /** D. Action: [Contact my adviser]. */
    ACT_CONTACT_MY_ADVISER,
}
