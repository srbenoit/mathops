module mathops_assessment {
    exports dev.mathops.assessment;
    exports dev.mathops.assessment.exam;
    exports dev.mathops.assessment.problem.template;
    exports dev.mathops.assessment.document.template;
    exports dev.mathops.assessment.variable;
    exports dev.mathops.assessment.formula;
    exports dev.mathops.assessment.variable.edit;
    exports dev.mathops.assessment.formula.edit;
    exports dev.mathops.assessment.document;
    exports dev.mathops.assessment.problem;
    exports dev.mathops.assessment.htmlgen;
    requires java.desktop;
    requires mathops_core;
    requires mathops_db;
    requires mathops_font;
}