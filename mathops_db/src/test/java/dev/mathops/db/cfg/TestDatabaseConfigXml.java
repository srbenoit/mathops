package dev.mathops.db.cfg;

import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.text.parser.ParsingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code DatabaseConfigXml} class.
 */
final class TestDatabaseConfigXml {

    /**
     * Constructs a new {@code TestDatabaseConfigXml}.
     */
    TestDatabaseConfigXml() {

        // No action
    }

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        DbConnection.registerDrivers();
    }

    /** Test case. */
    @Test
    @DisplayName("Default Config")
    void test0001() {

        DatabaseConfig config = null;

        final File source = DatabaseConfigXml.getDefaultFile();
        try {
            config = DatabaseConfigXml.load(source);
        } catch (final IOException | ParsingException ex) {
            Log.warning(ex);
        }

        assertNotNull(config, "Unable to load default configuration");

        final Login ifxPMath = config.getLogin("IFX.P.MATH");
        try (final Connection ignored = ifxPMath.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to IFX.P.MATH", ex);
        }

        final Login ifxDMath = config.getLogin("IFX.D.MATH");
        try (final Connection ignored = ifxDMath.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to IFX.D.MATH", ex);
        }

        final Login ifxTMath = config.getLogin("IFX.T.MATH");
        try (final Connection ignored = ifxTMath.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to IFX.T.MATH", ex);
        }

        final Login ifxPWeb = config.getLogin("IFX.P.WEB");
        try (final Connection ignored = ifxPWeb.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to IFX.P.WEB", ex);
        }

        final Login ifxDWeb = config.getLogin("IFX.D.WEB");
        try (final Connection ignored = ifxDWeb.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to IFX.D.WEB", ex);
        }

        final Login pgsMath = config.getLogin("PGS.MATH");
        try (final Connection ignored = pgsMath.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to PGS.MATH", ex);
        }

        final Login banPWeb = config.getLogin("BAN.P.WEB");
        try (final Connection ignored = banPWeb.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to BAN.P.WEB", ex);
        }

        final Login banTWeb = config.getLogin("BAN.T.WEB");
        try (final Connection ignored = banTWeb.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to BAN.T.WEB", ex);
        }

        final Login opsPWeb = config.getLogin("ODS.P.WEB");
        try (final Connection ignored = opsPWeb.openConnection()) {
        } catch (final SQLException ex) {
            fail("Failed to connect to ODS.P.WEB", ex);
        }
    }
}
