package dev.mathops.assessment.example;

import dev.mathops.assessment.document.template.DocColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * The data representing an example.
 */
public class ExampleDoc {

    String title = null;
    final List<String> objectives;
    String promptHeading = null;
    DocColumn promptContent = null;
    final List<String> hintButtons;
    final List<DocColumn> hintContents;
    String walkthroughHeading = null;
    DocColumn walkthroughContent = null;
    String solutionHeading = null;
    DocColumn solutionContent = null;

    ExampleDoc() {
        this.objectives = new ArrayList<>(3);
        this.hintButtons = new ArrayList<>(3);
        this.hintContents = new ArrayList<>(3);
    }
}
