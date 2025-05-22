package dev.mathops.dbjobs.report;

import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Server;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Generates a report that shows the active database configuration and all database objects present in all available
 * databases.
 */
final class DatabaseObjects implements Runnable {

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT = //
            DateTimeFormatter.ofPattern("yyyy'_'MM'_'dd'-'hh'_'mm'_'ss", Locale.US);

    /**
     * Private constructor to prevent direct instantiation.
     */
    private DatabaseObjects() {

        // No action
    }

    /**
     * Runs the report.
     */
    @Override
    public void run() {

        final HtmlBuilder report = new HtmlBuilder(10000);
        final HtmlBuilder errors = new HtmlBuilder(1000);
        errors.addln();
        errors.addln("*** ERRORS ***");
        errors.addln();

        final boolean hasErrors = generateReport(report, errors);

        final LocalDateTime now = LocalDateTime.now();
        final String dateStamp = DTTM_FMT.format(now);
        final File file = new File("/opt/zircon/reports/Database_Objects" + dateStamp + ".txt");
        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            final String reportStr = report.toString();
            fw.write(reportStr);
            if (hasErrors) {
                final String errorsStr = errors.toString();
                fw.write(errorsStr);
            }
            final String filePath = file.getAbsolutePath();
            Log.info("Report complete, written to: ", filePath);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates the report.
     *
     * @param report the {@code HtmlBuilder} to which to write lines of the report
     * @param errors an {@code HtmlBuilder} to which to write error messages
     * @return true if any errors were encountered; false if not
     */
    private static boolean generateReport(final HtmlBuilder report, final HtmlBuilder errors) {

        boolean hasErrors = false;
        final DatabaseConfig config = DatabaseConfig.getDefault();

        int longestLoginId = 0;
        for (final String loginId : config.getLoginIds()) {
            final int len = loginId.length();
            longestLoginId = Math.max(longestLoginId, len);
        }
        int longestDataId = 0;
        for (final String dataId : config.getDataIds()) {
            final int len = dataId.length();
            longestDataId = Math.max(longestDataId, len);
        }
        int longestSchemaName = 0;
        for (final ESchema schema : ESchema.values()) {
            final String schemaStr = schema.toString();
            final int len = schemaStr.length();
            longestSchemaName = Math.max(longestSchemaName, len);
        }
        int longestUseName = 0;
        for (final EDbUse use : EDbUse.values()) {
            final String useStr = use.toString();
            final int len = useStr.length();
            longestUseName = Math.max(longestUseName, len);
        }

        report.addln("Database Servers:");
        report.addln("-----------------");
        int serverIndex = 1;
        for (final Server server : config.getServers()) {
            report.addln();
            report.add(serverIndex);
            report.add(". ", server.host, ":");
            report.add(server.port);
            report.addln(" (", server.type, ")");

            report.addln("     Databases:");
            int databaseIndex = 1;
            for (final Database database : server.getDatabases()) {
                if (database.server != server) {
                    errors.addln("ERROR: ", server.type, " server at ", server.host, " has database ", database.id,
                            ", but that database has a different server.");
                    hasErrors = true;
                }

                report.add(databaseIndex > 9 ? "      " : "       ");
                report.add(databaseIndex);
                report.add(". ID: ", database.id);
                if (database.instance != null) {
                    report.add("  Instance: ", database.instance);
                }
                if (database.dba != null) {
                    report.add("  DBA: ", database.dba);
                }
                report.addln();

                report.addln("            Logins:");
                int loginIndex = 1;
                for (final Login login : database.getLogins()) {
                    final int len = login.id.length();
                    longestLoginId = Math.max(longestLoginId, len);

                    if (login.database != database) {
                        errors.addln("ERROR: Database ", database.id, " has login ", login.id,
                                ", but that login has a different database.");
                        hasErrors = true;
                    }
                    final Login test = config.getLogin(login.id);
                    if (test != login) {
                        errors.addln("ERROR: retrieving login ", login.id, " by ID did not return the correct object");
                        hasErrors = true;
                    }

                    report.add(loginIndex > 9 ? "              " : "              ");
                    report.add(loginIndex);
                    final String padded = ReportUtils.padOrTrimString(login.id, longestLoginId);
                    report.add(". ID: ", padded);
                    report.addln("  User: ", login.user);
                    ++loginIndex;
                }

                report.addln("            Data:");
                int dataIndex = 1;
                for (final Data data : database.getData()) {
                    final int len = data.id.length();
                    longestDataId = Math.max(longestDataId, len);

                    if (data.database != database) {
                        Log.warning("ERROR: Database ", database.id, " has data ", data.id,
                                ", but that data has a different database.");
                    }
                    final Data test = config.getData(data.id);
                    if (test != data) {
                        errors.addln("ERROR: retrieving data ", data.id, " by ID did not return the correct object");
                        hasErrors = true;
                    }

                    report.add(dataIndex > 9 ? "             " : "              ");
                    report.add(dataIndex);
                    final String padded = ReportUtils.padOrTrimString(data.id, longestLoginId);
                    report.add(". ID: ", padded);
                    final String schemaName = data.schema.toString();
                    final String padSchema = ReportUtils.padOrTrimString(schemaName, longestSchemaName);
                    report.add("  Schema: ", padSchema);
                    final String useName = data.use.toString();
                    final String padUse = ReportUtils.padOrTrimString(useName, longestUseName);
                    report.add("  Use: ", padUse);
                    if (data.prefix != null) {
                        report.add("  Prefix: ", data.prefix);
                    }
                    report.addln();
                    ++dataIndex;
                }

                ++databaseIndex;
            }
            ++serverIndex;
        }

        report.addln();
        report.addln();
        report.addln("Login Map:");
        report.addln("----------");
        for (final String loginId : config.getLoginIds()) {
            final Login login = config.getLogin(loginId);
            final String padded = ReportUtils.padOrTrimString(loginId, longestLoginId);
            report.addln("    ", padded, " --> [Login object with ID ", login.id, "]");
        }

        report.addln();
        report.addln();
        report.addln("Data Map:");
        report.addln("---------");
        for (final String dataId : config.getDataIds()) {
            final Data data = config.getData(dataId);
            final String padded = ReportUtils.padOrTrimString(dataId, longestDataId);
            report.addln("    ", padded, " --> [Data object with ID ", data.id, "]");
        }

        report.addln();
        report.addln();
        report.addln("Profiles:");
        report.addln("---------");
        int profileIndex = 1;
        for (final Profile profile : config.getProfiles()) {
            final Profile test = config.getProfile(profile.id);
            if (profile != test) {
                errors.addln("ERROR: Attempt to retrieve profile with ID ", profile.id,
                        " returned a different profile");
                hasErrors = true;
            }
            report.add(profileIndex > 9 ? "" : " ");
            report.add(profileIndex);
            report.addln(". ID: ", profile.id);

            report.addln("      Facets:");
            int facetIndex = 1;
            for (final Facet facet : profile.getFacets()) {
                final Login login = facet.login;
                final Data data = facet.data;
                report.add(facetIndex > 9 ? "       " : "        ");
                report.add(facetIndex);
                final String padLoginId = ReportUtils.padOrTrimString(login.id, longestLoginId);
                final String padDataId = ReportUtils.padOrTrimString(data.id, longestDataId);
                final String schemaStr = data.schema.toString();
                final String padSchema = ReportUtils.padOrTrimString(schemaStr, longestSchemaName);
                report.addln(".  Login ID: ", padLoginId, "  Data ID: ", padDataId, "  Schema: ", padSchema, "  Use: ",
                        data.use);
                ++facetIndex;
            }

            ++profileIndex;
        }

        int longestSite = 0;
        for (final String host : config.getWebHosts()) {
            for (final String site : config.getWebSites(host)) {
                final int len = site.length();
                longestSite = Math.max(longestSite, len);
            }
        }

        report.addln();
        report.addln();
        report.addln("Web Contexts:");
        report.addln("-------------");
        for (final String host : config.getWebHosts()) {
            report.addln();
            report.addln("Host: ", host);

            for (final String site : config.getWebSites(host)) {
                final Profile profile = config.getWebProfile(host, site);
                final String padded = ReportUtils.padOrTrimString(site, longestSite);
                report.addln("  Site: ", padded, "  using profile ", profile.id);
            }
        }

        int longestCode = 0;
        for (final String id : config.getCodeContextIds()) {
            final int len = id.length();
            longestCode = Math.max(longestCode, len);
        }

        report.addln();
        report.addln();
        report.addln("Code Contexts:");
        report.addln("--------------");
        for (final String id : config.getCodeContextIds()) {
            final Profile profile = config.getCodeProfile(id);
            final String padded = ReportUtils.padOrTrimString(id, longestCode);
            report.addln("Context: ", padded, "  using profile ", profile.id);
        }

        return hasErrors;
    }

    /**
     * Main method to run the report.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final Runnable obj = new DatabaseObjects();

        obj.run();
    }
}
