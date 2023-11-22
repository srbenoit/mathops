package dev.mathops.app.placement.survey;

import dev.mathops.app.simplewizard.Wizard;
import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import javax.swing.JFrame;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to construct and display the survey wizard with no data.
 */
public enum Survey {
    ;

    /**
     * Static method to build and display the survey wizard. The response list is 19 strings, some of which may be
     * null.
     *
     * @param frame     The frame that will own the survey window.
     * @param questions An array of Strings containing the question identifiers.
     * @param answers   An array of Strings containing the student's current answers, if any, and which is to be
     *                  populated with the student's new answers.
     * @param examTitle the exam title
     * @return One of the return codes defined in the {@code Wizard} class: FINISH_RETURN_CODE if the survey was
     *         completed. CANCEL_RETURN_CODE if the survey was canceled. ERROR_RETURN_CODE if an error occurred.
     */
    public static int doSurvey(final JFrame frame, final String[] questions, final String[] answers,
                               final String examTitle) {

        // Assemble a hashtable from the Q & A data.
        final Map<String, String> hash = new HashMap<>(10);

        final int numQ = questions.length;
        final int numA = answers.length;

        for (int i = 0; (i < numQ) && (i < numA); ++i) {
            if ((questions[i] != null) && (answers[i] != null)) {
                hash.put(questions[i], answers[i]);
            }
        }

        // Build a wizard to present the survey.
        final Wizard wizard = new Wizard(frame, true);
        wizard.getDialog().setTitle("Student Information");

        // Install the 6 panels in the wizard.
        final WizardPanelDescriptor descriptor1 = new SurveyPanel1Descriptor(examTitle);
        wizard.registerWizardPanel(SurveyPanel1Descriptor.IDENTIFIER, descriptor1);
        final WizardPanelDescriptor descriptor2 = new SurveyPanel2Descriptor();
        wizard.registerWizardPanel(SurveyPanel2Descriptor.IDENTIFIER, descriptor2);
        final WizardPanelDescriptor descriptor3 = new SurveyPanel3Descriptor();
        wizard.registerWizardPanel(SurveyPanel3Descriptor.IDENTIFIER, descriptor3);
        final WizardPanelDescriptor descriptor4 = new SurveyPanel4Descriptor();
        wizard.registerWizardPanel(SurveyPanel4Descriptor.IDENTIFIER, descriptor4);
        final WizardPanelDescriptor descriptor5 = new SurveyPanel5Descriptor();
        wizard.registerWizardPanel(SurveyPanel5Descriptor.IDENTIFIER, descriptor5);
        final WizardPanelDescriptor descriptor6 = new SurveyPanel6Descriptor();
        wizard.registerWizardPanel(SurveyPanel6Descriptor.IDENTIFIER, descriptor6);
        wizard.setCurrentPanel(SurveyPanel1Descriptor.IDENTIFIER);

        final SurveyPanel2 panel2 = (SurveyPanel2) descriptor2.getPanelComponent();
        final SurveyPanel3 panel3 = (SurveyPanel3) descriptor3.getPanelComponent();
        final SurveyPanel4 panel4 = (SurveyPanel4) descriptor4.getPanelComponent();
        final SurveyPanel5 panel5 = (SurveyPanel5) descriptor5.getPanelComponent();
        final SurveyPanel6 panel6 = (SurveyPanel6) descriptor6.getPanelComponent();

        // Populate the dialog with the current values in the responses array
        // (which will be the student's prior answers)
        String answer = hash.get("Time spent preparing");

        if (answer != null) {
            panel2.setRadioButtonSelected(answer);
        }

        answer = hash.get("Resources used to prepare");

        if (answer != null) {

            try {
                final int intanswer = Long.valueOf(answer).intValue();

                if ((intanswer & 8) == 8) {
                    panel2.setCheckBoxSelected(0);
                }

                if ((intanswer & 4) == 4) {
                    panel2.setCheckBoxSelected(1);
                }

                if ((intanswer & 2) == 2) {
                    panel2.setCheckBoxSelected(2);
                }

                if ((intanswer & 1) == 1) {
                    panel2.setCheckBoxSelected(3);
                }
            } catch (final NumberFormatException e) { /* Empty */
            }
        }

        answer = hash.get("Time since last math course");

        if (answer != null) {
            panel3.setQ1RadioButtonSelected(answer);
        }

        answer = hash.get("Grade typically earned in math");

        if (answer != null) {
            panel3.setQ2RadioButtonSelected(answer);
        }

        answer = hash.get("HS Freshman Course 1");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("No math");
        }

        if (answer != null) {
            panel4.setQ1CheckBoxSelected(answer);
        }

        answer = hash.get("HS Freshman Course 2");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Other");
        }

        if (answer != null) {
            panel4.setQ1CheckBoxSelected(answer);
        }

        answer = hash.get("HS Sophomore Course 1");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("C Other");
        }

        if (answer != null) {
            panel4.setQ2CheckBoxSelected(answer);
        }

        answer = hash.get("HS Sophomore Course 2");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("C Math for Liberal Arts");
        }

        if (answer != null) {
            panel4.setQ2CheckBoxSelected(answer);
        }

        answer = hash.get("HS Junior Course 1");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Algebra I");
        }

        if (answer != null) {
            panel5.setQ1CheckBoxSelected(answer);
        }

        answer = hash.get("HS Junior Course 2");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Integrated Math I");
        }

        if (answer != null) {
            panel5.setQ1CheckBoxSelected(answer);
        }

        answer = hash.get("HS Senior Course 1");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Geometry");
        }

        if (answer != null) {
            panel5.setQ2CheckBoxSelected(answer);
        }

        answer = hash.get("HS Senior Course 2");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Integrated Math II");
        }

        if (answer != null) {
            panel5.setQ2CheckBoxSelected(answer);
        }

        answer = hash.get("HS Senior Course 3");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("C Elementry Algebra");
        }

        if (answer == null) {
            answer = hash.get("C Elementary Algebra");
        }

        if (answer != null) {
            panel5.setQ2CheckBoxSelected(answer);
        }

        answer = hash.get("C Course 1");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Algebra II");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        answer = hash.get("C Course 2");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Integrated Math III");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        answer = hash.get("C Course 3");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("C Intermediate Algebra");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        answer = hash.get("C Course 4");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("C College Algebra");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        answer = hash.get("C Course 5");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Pre-calculus");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        answer = hash.get("C Course 6");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("C Pre-calculus");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        answer = hash.get("C Course 7");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("HS Calculus");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        answer = hash.get("C Course 8");

        // FIXME: Remove this once database has been updated.
        if (answer == null) {
            answer = hash.get("C Calculus");
        }

        if (answer != null) {
            panel6.setCheckBoxSelected(answer);
        }

        // Present the wizard (modal).
        final int ret = wizard.showModalDialog();

        // Clear pre-existing answers
        Arrays.fill(answers, null);

        // Accumulate the results if it was finished.
        if (ret == Wizard.FINISH_RETURN_CODE) {

            // Time spent preparing
            answers[0] = panel2.getRadioButtonSelected();

            // Resources used to prepare
            final int intanswer = (panel2.isCheckBoxSelected(0) ? 8 : 0)
                    + (panel2.isCheckBoxSelected(1) ? 4 : 0) + (panel2.isCheckBoxSelected(2) ? 2 : 0)
                    + (panel2.isCheckBoxSelected(3) ? 1 : 0);
            answers[1] = Integer.toString(intanswer);

            // Time since last math course
            answers[2] = panel3.getQ1RadioButtonSelected();

            // Typical grade earned in math
            answers[3] = panel3.getQ2RadioButtonSelected();

            // Courses taken freshman year
            int which = 4;
            for (int i = 0; (i < 9) && (which < 6); ++i) {
                if (panel4.isQ1CheckBoxSelected(i)) {
                    answers[which] = panel4.getQ1CheckBoxCommand(i);
                    which++;
                }
            }

            // Courses taken sophomore year
            which = 6;
            for (int i = 0; (i < 9) && (which < 8); ++i) {
                if (panel4.isQ2CheckBoxSelected(i)) {
                    answers[which] = panel4.getQ2CheckBoxCommand(i);
                    which++;
                }
            }

            // Courses taken junior year
            which = 8;
            for (int i = 0; (i < 10) && (which < 10); ++i) {
                if (panel5.isQ1CheckBoxSelected(i)) {
                    answers[which] = panel5.getQ1CheckBoxCommand(i);
                    which++;
                }
            }

            // Courses taken senior year
            which = 10;
            for (int i = 0; (i < 10) && (which < 13); ++i) {
                if (panel5.isQ2CheckBoxSelected(i)) {
                    answers[which] = panel5.getQ2CheckBoxCommand(i);
                    which++;
                }
            }

            // Courses taken since graduation
            which = 13;
            for (int i = 0; i < 7; ++i) {
                if (panel6.isCheckBoxSelected(i)) {
                    answers[which] = panel6.getCheckBoxCommand(i);
                    which++;
                }
            }
        }

        return ret;
    }
}
