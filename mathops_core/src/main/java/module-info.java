module mathops_core {
    requires transitive java.desktop;
    requires transitive java.logging;
    exports dev.mathops.core;
    exports dev.mathops.core.log;
    exports dev.mathops.core.builder;
    exports dev.mathops.core.res;
    exports dev.mathops.core.file;
    exports dev.mathops.core.parser;
    exports dev.mathops.core.parser.xml;
    exports dev.mathops.core.parser.json;
    exports dev.mathops.core.ui.layout;
    exports dev.mathops.core.ui;
    exports dev.mathops.core.unicode;
    exports dev.mathops.core.installation;
}