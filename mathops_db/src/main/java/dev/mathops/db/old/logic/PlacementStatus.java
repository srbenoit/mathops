package dev.mathops.db.old.logic;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A data class containing the status of the math placement tool.
 */
public final class PlacementStatus {

    /**
     * The exam IDs of all available locally-proctored versions of the tool (includes departmental and university
     * testing centers, including Student Disability Center and remote "Zoom-based" proctoring by department staff).
     */
    public final Set<String> availableLocalProctoredIds;

    /**
     * The exam IDs of all available online proctored versions of the tool (a version for each proctoring service
     * currently supported).
     */
    public final Set<String> availableOnlineProctoredIds;

    /** The exam IDs of all available unproctored versions of the tool. */
    public final Set<String> availableUnproctoredIds;

    /**
     * Flag indicating whether the student is allowed (based only on application term) to use the math placement
     * tool without proctoring (regardless of whether they are currently eligible or not, or whether they have used an
     * unproctored attempt).
     */
    public boolean allowedToUseUnproctored;

    /**
     * If {@code allowedToUseUnproctored} is true, this will hold a short phrase indicating why unproctored access is
     * allowed for the student, suitable for use in a sentence like "Because [PHRASE], you may complete the placement
     * tool one time without proctoring".
     *
     * <p>
     * Examples of potential phrases are could be "you are an incoming first-year student" or "you are registered
     * through CSU Online".
     */
    public String whyUnproctoredAllowed;

    /**
     * If {@code allowedToUseUnproctored} is true but there are no unproctored versions available, this will hold a
     * short sentence indicating why unproctored access is not currently available.
     *
     * <p>
     * Examples of potential reasons could be "You have used your unproctored attempt on the Math Placement Tool." or
     * "You may not access the unproctored Math Placement Tool while attending an on-campus orientation.", or "You will
     * be eligible to complete the Math Placement Tool without proctoring starting [Date]."
     */
    public String whyUnproctoredUnavailable;

    /** Flag indicating student has attempted math placement. */
    public boolean placementAttempted;

    /** The number of attempts used. */
    public int attemptsUsed;

    /** The number of attempts remaining. */
    public int attemptsRemaining;

    /** Flag indicating the student has used their unproctored attempt. */
    public boolean unproctoredUsed;

    /**
     * The reason this format of the tool is not available ({@code null} if {@code availableLocalProctoredIds} contains
     * at least one entry; populated if {@code availableLocalProctoredIds} is empty).
     */
    public String whyProctoredUnavailable;

    /**
     * A short form of the reason for unavailability of locally proctored placement tool, for use in a GUI to show why a
     * button is not enabled.
     */
    public String shortWhyProctoredUnavailable;

    /** Date ranges when the unproctored placement tool is available. */
    public DateRangeGroups unproctoredDateRanges;

    /** The list of courses the student has placed out of. */
    public final SortedSet<String> placedOutOf;

    /** The list of courses the student is cleared to register for based on placement. */
    public final SortedSet<String> clearedFor;

    /** The list of courses the student has earned credit for based on placement. */
    public final SortedSet<String> earnedCreditFor;

    /**
     * Constructs a new {@code PlacementStatus}.
     */
    public PlacementStatus() {

        super();
        this.availableLocalProctoredIds = new LinkedHashSet<>(5);
        this.availableOnlineProctoredIds = new LinkedHashSet<>(5);
        this.availableUnproctoredIds = new LinkedHashSet<>(5);
        this.placedOutOf = new TreeSet<>();
        this.clearedFor = new TreeSet<>();
        this.earnedCreditFor = new TreeSet<>();
    }

    /**
     * Sets the reason the proctored placement tool is not currently available. This could be one of the following (the
     * long string followed by the short string).
     * <ul>
     * <li>"Unable to look up placement record." / "Lookup failed"
     * <li>"You have no attempts remaining on the Math Placement Tool." / "No attempts remain"
     * <li>"There is a hold on your account." / "Student has Hold"
     * </ul>
     *
     * @param theReason      the reason the tool is not available ({@code null} if
     *                       {@code availableLocalProctoredIds} contains at least one entry; populated if
     *                       {@code availableLocalProctoredIds} is empty)
     * @param theShortReason the short form of the reason, for use in GUIs
     */
    void setWhyProctoredUnavailable(final String theReason, final String theShortReason) {

        this.whyProctoredUnavailable = theReason;
        this.shortWhyProctoredUnavailable = theShortReason;
    }
}
