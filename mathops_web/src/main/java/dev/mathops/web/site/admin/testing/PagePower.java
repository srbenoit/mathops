package dev.mathops.web.site.admin.testing;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.HexEncoder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawTestingCenterLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawTestingCenter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Page that allows the user to power stations on and off.
 */
enum PagePower {
    ;

    /**
     * Handles a GET request for the page.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = TestingPage.startTestingPage(cache, site, session);

        TestingPage.emitNavBlock(ETestingTopic.POWER_ON_OFF, htm);

        final List<RawTestingCenter> centers = RawTestingCenterLogic.INSTANCE.queryAll(cache);
        final List<RawClientPc> allStations = RawClientPcLogic.INSTANCE.queryAll(cache);

        Collections.sort(centers);
        Collections.sort(allStations);

        for (final RawTestingCenter center : centers) {
            final String tc = center.testingCenterId;

            if ("1".equals(tc) || "4".equals(tc)) {

                // Count number of machines whose MAC address are known
                boolean found = false;
                for (final RawClientPc pc : allStations) {
                    if (pc.testingCenterId.equals(center.testingCenterId)
                            && pc.macAddress != null) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    htm.sH(3).add(center.tcName).eH(3);

                    for (final RawClientPc pc : allStations) {
                        if (pc.macAddress == null) {
                            continue;
                        }
                        if (pc.testingCenterId.equals(center.testingCenterId)) {

                            if (RawClientPc.POWER_TURNING_ON.equals(pc.powerStatus)) {
                                htm.addln(
                                        "<form class='indent' method='POST' action='power.html'>");
                                htm.add("Station ", pc.stationNbr,
                                        " (currently powering up) &nbsp; ");
                                htm.addln("<input type='hidden' name='action' value='ON'/>");
                                htm.addln("<input type='hidden' name='pc' value='",
                                        pc.computerId, "'/>");
                                htm.addln("<input style='display:inline;' type='submit' ",
                                        "value='Turn on station'/>");
                            } else if (RawClientPc.POWER_REPORTING_ON.equals(pc.powerStatus)) {

                                if (pc.currentStuId == null) {
                                    htm.addln(
                                            "<form class='indent' method='POST' action='power.html'>");
                                    htm.add("Station ", pc.stationNbr,
                                            " (currently ON and not in use) &nbsp; ");
                                    htm.addln("<input type='hidden' name='action' value='OFF'/>");
                                    htm.addln("<input type='hidden' name='pc' value='",
                                            pc.computerId, "'/>");
                                    htm.addln("<input style='display:inline;' type='submit' ",
                                            "value='Turn off station'/>");
                                } else {
                                    htm.addln("<form class='indent'>");
                                    htm.add("Station ", pc.stationNbr,
                                            " (currently ON and IN USE)");
                                }
                            } else {
                                htm.addln(
                                        "<form class='indent' method='POST' action='power.html'>");
                                htm.add("Station ", pc.stationNbr, " (currently OFF) &nbsp; ");
                                htm.addln("<input type='hidden' name='action' value='ON'/>");
                                htm.addln("<input type='hidden' name='pc' value='",
                                        pc.computerId, "'/>");
                                htm.addln("<input style='display:inline;' type='submit' ",
                                        "value='Turn on station'/>");
                            }
                            htm.addln("</form>");
                        }
                    }
                    htm.div("vgap");
                }
            }

        }

        TestingPage.endTestingPage(cache, htm, site, req, resp);
    }

    /**
     * Handles a POST request for the page.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String action = req.getParameter("action");
        final String pc = req.getParameter("pc");

        if (action != null && pc != null) {
            final RawClientPc station = RawClientPcLogic.query(cache, pc);

            if (station != null && station.macAddress != null) {
                if ("ON".equals(action)) {
                    if (!RawClientPc.POWER_REPORTING_ON.equals(station.powerStatus)) {
                        turnOnStation(cache, station);
                    }
                } else if ("OFF".equals(action)
                        && (RawClientPc.POWER_REPORTING_ON.equals(station.powerStatus)
                        && station.currentStuId == null)) {
                    turnOffStation(cache, station);
                }
            }
        }

        doGet(cache, site, req, resp, session);
    }

    /**
     * Attempts to turn on a station by sending a "Wake on LAN" magic packet to the MAC address associated with the
     * station. This will work only of the station has "Wake on LAN" enabled in its BIOS, its MAC address is correct,
     * and it is installed on the same physical subnet as the server.
     *
     * @param cache   the data cache
     * @param station the station to attempt to turn on
     * @throws SQLException if there is an error accessing the database
     */
    private static void turnOnStation(final Cache cache, final RawClientPc station)
            throws SQLException {

        final String ipStr = "192.168.1.255";
        final String macStr = station.macAddress;

        if (macStr != null && macStr.length() == 12) {
            final byte[] macBytes = HexEncoder.decode(macStr);

            try {
                final int len = 6 + 16 * macBytes.length;
                final byte[] bytes = new byte[len];
                for (int i = 0; i < 6; i++) {
                    bytes[i] = (byte) 0xff;
                }
                for (int i = 6; i < len; i += macBytes.length) {
                    System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
                }

                final InetAddress address = InetAddress.getByName(ipStr);
                final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
                try (final DatagramSocket socket = new DatagramSocket()) {
                    socket.send(packet);
                }

                Log.info("Wake-on-LAN packet sent.");

                RawClientPcLogic.updatePowerStatus(cache, station.computerId, RawClientPc.POWER_TURNING_ON);
            } catch (final IOException ex) {
                Log.warning("Failed to send Wake-on-LAN packet:", ex);
            }
        }
    }

    /**
     * Attempts to turn off a station by executing an operating system script vis SSH on the station.
     *
     * @param cache   the data cache
     * @param station the station to attempt to turn off
     * @throws SQLException if there is an error accessing the database
     */
    private static void turnOffStation(final Cache cache, final RawClientPc station)
            throws SQLException {

        final String address = "192.168.1." + station.stationNbr;

        final String[] cmd = {"/opt/zircon/cfg/shutdown_station.sh", address};

        try {
            final Process p = Runtime.getRuntime().exec(cmd);

            RawClientPcLogic.updatePowerStatus(cache, station.computerId, RawClientPc.POWER_OFF);
        } catch (final IOException ex) {
            Log.warning("Failed to execute shell script", ex);
        }
    }

//    /**
//     * Starts a small navigation button.
//     *
//     * @param htm      the {@code HtmlBuilder} to which to append
//     * @param selected true if button is currently selected
//     * @param label    the button label
//     * @param url      the URL to which to link
//     * @param query    optional query string (not including leading '?')
//     */
//    static void navButtonSmall(final HtmlBuilder htm, final boolean selected, final String label,
//                               final String url, final String query) {
//
//        htm.add("<button");
//        if (selected) {
//            htm.add(" class='nav8 selected'");
//        } else {
//            htm.add(" class='nav8'");
//        }
//        htm.add(" onclick='pick(\"", url);
//        if (query != null) {
//            htm.add('?').add(query);
//        }
//        htm.add("\");'>", label, "</button>");
//    }
}
