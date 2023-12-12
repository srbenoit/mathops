package dev.mathops.app.placement.results;

import dev.mathops.app.simplewizard.Wizard;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.txn.messages.PlacementStatusReply;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A class to construct and display the survey wizard with no data.
 */
public enum Results {
    ;

    /**
     * Static method to build and display the survey wizard. The response list this method fills is variable length,
     * depending on the list of courses the student indicates he/she has taken. At most, this will require 19 strings to
     * hold. The response list is guaranteed to contain at least 4 strings.
     *
     * @param status the placement status reply from the server
     * @return one of the return codes defined in the {@code Wizard} class: FINISH_RETURN_CODE if the survey was
     *         completed; CANCEL_RETURN_CODE if the survey was canceled; ERROR_RETURN_CODE if an error occurred
     */
    public static int doResults(final PlacementStatusReply status) {

        final SortedSet<String> credit = new TreeSet<>();
        final SortedSet<String> placed = new TreeSet<>();
        final SortedSet<String> cleared = new TreeSet<>();

        // Generate lists of credit, placed and cleared courses
        placementStatus(status, credit, placed, cleared);

        // Build a wizard to present the survey.
        final Wizard wizard = new Wizard(true);
        wizard.getDialog().setTitle("Placement Results");

        // Install the 5 panels in the wizard.
        final ResultsPanel1Descriptor descriptor1 = new ResultsPanel1Descriptor();
        wizard.registerWizardPanel(ResultsPanel1Descriptor.IDENTIFIER, descriptor1);

        final ResultsPanel2Descriptor descriptor2 =
                new ResultsPanel2Descriptor(credit, placed, cleared);
        wizard.registerWizardPanel(ResultsPanel2Descriptor.IDENTIFIER, descriptor2);

        final ResultsPanel3Descriptor descriptor3 =
                new ResultsPanel3Descriptor(credit, placed, cleared);
        wizard.registerWizardPanel(ResultsPanel3Descriptor.IDENTIFIER, descriptor3);

        final ResultsPanel4Descriptor descriptor4 =
                new ResultsPanel4Descriptor(credit, placed, cleared);
        wizard.registerWizardPanel(ResultsPanel4Descriptor.IDENTIFIER, descriptor4);

        final ResultsPanel5Descriptor descriptor5 = new ResultsPanel5Descriptor();
        wizard.registerWizardPanel(ResultsPanel5Descriptor.IDENTIFIER, descriptor5);
        wizard.setCurrentPanel(ResultsPanel1Descriptor.IDENTIFIER);

        // Present the wizard (modal).
        return wizard.showModalDialog();
    }

    /**
     * Analyze the list of placement results, and populate a list of courses that the student has earned credit for, has
     * placed out of, and is cleared to take. The placed out of list will not contain items that are in the credit list,
     * and the cleared list will not contain items that are in either of the others, or are "lower" than any course in
     * the placed or credit lists. The cleared course list will include calculus courses if the placement or credit
     * results indicate the prerequisites have been net.
     *
     * @param status  the placement status items to analyze
     * @param credit  the list of courses for which credit has been earned
     * @param placed  the list of courses the student has placed out of
     * @param cleared the list of courses the student is cleared to enter
     */
    private static void placementStatus(final PlacementStatusReply status,
                                        final Collection<String> credit, final Collection<String> placed,
                                        final Collection<String> cleared) {

        boolean did120 = false;

        // Make sure there are no lingering entries
        credit.clear();
        placed.clear();
        cleared.clear();

        // March through the status entries, adding list entries. Using a Set
        // ensures no duplicates (adding a second time does nothing)
        // FIXME: Hardcodes
        final int numStatus = status.status.length;
        for (int i = 0; i < numStatus; ++i) {

            if (RawRecordConstants.M100C.equals(status.courses[i])) {
                cleared.add(RawRecordConstants.M117);
                cleared.add("M 101");
                cleared.add("M 105");
            } else if ("M 100M".equals(status.courses[i])) {
                cleared.add("M 101");
                cleared.add("M 105");
            } else if ("M 100A".equals(status.courses[i])) {
                cleared.add(RawRecordConstants.M117);
            }

            if (RawRecordConstants.M117.equals(status.courses[i])) {

                if (status.status[i] == 'C') {
                    credit.add(RawRecordConstants.M117);
                } else {
                    placed.add(RawRecordConstants.M117);
                }

                cleared.add(RawRecordConstants.M118);
                cleared.add("Recommend-Retake");
            } else if (RawRecordConstants.M118.equals(status.courses[i])) {

                if (status.status[i] == 'C') {
                    credit.add(RawRecordConstants.M117);
                    credit.add(RawRecordConstants.M118);
                } else {
                    placed.add(RawRecordConstants.M117);
                    placed.add(RawRecordConstants.M117);
                }

                cleared.add(RawRecordConstants.M117);
                cleared.add(RawRecordConstants.M125);
                cleared.add("Calculus");
                cleared.add("Recommend-Retake");
            } else if ("M 120".equals(status.courses[i])
                    || "M 120A".equals(status.courses[i])
                    || "M 121".equals(status.courses[i])) {

                if (!did120) {
                    did120 = true;

                    if (status.status[i] == 'C') {
                        credit.add(RawRecordConstants.M117);
                        credit.add(RawRecordConstants.M118);
                    } else {
                        placed.add(RawRecordConstants.M117);
                        placed.add(RawRecordConstants.M118);
                    }

                    cleared.add(RawRecordConstants.M124);
                    cleared.add(RawRecordConstants.M125);
                    cleared.add("Calculus");
                    cleared.add("Recommend-Retake");
                }
            } else if (RawRecordConstants.M124.equals(status.courses[i])) {

                if (status.status[i] == 'C') {
                    credit.add(RawRecordConstants.M124);
                } else {
                    placed.add(RawRecordConstants.M124);
                }

                cleared.add("Calculus");
                cleared.add("Recommend-Retake");
            } else if (RawRecordConstants.M125.equals(status.courses[i])) {

                if (status.status[i] == 'C') {
                    credit.add(RawRecordConstants.M125);
                } else {
                    placed.add(RawRecordConstants.M125);
                }

                cleared.add(RawRecordConstants.M126);
                cleared.add("Calculus");
                cleared.add("Recommend-Retake");
            } else if (RawRecordConstants.M126.equals(status.courses[i])) {

                if (status.status[i] == 'C') {
                    credit.add(RawRecordConstants.M125);
                    credit.add(RawRecordConstants.M126);
                } else {
                    placed.add(RawRecordConstants.M125);
                    placed.add(RawRecordConstants.M126);
                }

                cleared.add("Calculus");
                cleared.add("Recommend-Retake");
            }
        }

        // Now clear out placed items that are in credit, and cleared items
        // that are in either placed or credit.
        placed.removeAll(credit);
        cleared.removeAll(credit);
        cleared.removeAll(placed);
    }

    /**
     * Generate the display name from a course number.
     *
     * @param course the course number
     * @return the corresponding display name
     */
    static String courseNameLookup(final String course) {

        final String result;

        if ("M 105".equals(course)) {
            result = "  MATH 105: Patterns of Phenomena I";
        } else if ("M 101".equals(course)) {
            result = "  MATH 101: Math in the Social Sciences";
        } else if (RawRecordConstants.M126.equals(course)
                || RawRecordConstants.MATH126.equals(course)) {
            result = "  MATH 126: Analytic Trigonometry";
        } else if (RawRecordConstants.M125.equals(course)
                || RawRecordConstants.MATH125.equals(course)) {
            result = "  MATH 125: Numerical Trigonometry";
        } else if (RawRecordConstants.M124.equals(course)
                || RawRecordConstants.MATH124.equals(course)) {
            result = "  MATH 124: Logarithmic & Exponential Functions";
        } else if (RawRecordConstants.M118.equals(course)
                || RawRecordConstants.MATH118.equals(course)) {
            result = "  MATH 118: College Algebra in Context II";
        } else if (RawRecordConstants.M117.equals(course)
                || RawRecordConstants.MATH117.equals(course)) {
            result = "  MATH 117: College Algebra in Context I";
        } else {
            result = course;
        }

        return result;
    }
}
