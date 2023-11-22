module mathops_session {
    exports dev.mathops.session;
    exports dev.mathops.session.scramsha256;
    exports dev.mathops.session.txn.messages;
    exports dev.mathops.session.txn;
    exports dev.mathops.session.txn.handlers;
    exports dev.mathops.session.sitelogic.mathplan;
    exports dev.mathops.session.login;
    exports dev.mathops.session.sitelogic.mathplan.data;
    exports dev.mathops.session.sitelogic.servlet;
    exports dev.mathops.session.sitelogic;
    exports dev.mathops.session.sitelogic.data;
    exports dev.mathops.session.sitelogic.bogus;
    requires java.desktop;
    requires mathops_core;
    requires mathops_db;
    requires mathops_font;
    requires mathops_assessment;
    requires java.sql;
}