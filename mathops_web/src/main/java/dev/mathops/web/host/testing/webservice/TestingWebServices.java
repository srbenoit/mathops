package dev.mathops.web.host.testing.webservice;

import dev.mathops.commons.HexEncoder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.schema.legacy.RawClientPc;
import dev.mathops.session.scramsha256.UserCredentials;

import jakarta.servlet.ServletRequest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.SQLException;

/**
 * Web services related to testing centers and testing stations.
 */
enum TestingWebServices {
    ;

    /**
     * A service that allows an authorized user to power on a testing station.
     *
     * <pre>
     * Parameters:
     *     token = SCRAM-SHA-256 token from handshake (used to validate credentials)
     *     computer-id = ID of testing station computer to turn on
     * </pre>
     *
     * @param cache       the data cache
     * @param credentials the user's credentials
     * @param request     the request
     * @return the reply ("OK" or "!" followed by an error message)
     */
    static String powerStationOn(final Cache cache, final UserCredentials credentials, final ServletRequest request) {

        String result;

        if ("ADM".equals(credentials.role)) {

            final String computerId = request.getParameter("computer-id");

            if (computerId == null) {
                result = "!No computer-id parameter provided.";
            } else {
                try {
                    final RawClientPc station = RawClientPcLogic.query(cache, computerId);

                    if (RawClientPc.POWER_REPORTING_ON.equals(station.powerStatus)) {
                        result = "!Station already reporting ON.";
                    } else {
                        result = turnOnStation(cache, station);
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    result = "!Failed to look up testing station.";
                }
            }
        } else {
            result = "!Not authorized";
        }

        return result;
    }

    /**
     * Attempts to turn on a station by sending a "Wake on LAN" magic packet to the MAC address associated with the
     * station. This will work only of the station has "Wake on LAN" enabled in its BIOS, its MAC address is correct,
     * and it is installed on the same physical subnet as the server.
     *
     * @param cache   the data cache
     * @param station the station to attempt to turn on
     * @return the reply ("OK" or "!" followed by an error message)
     * @throws SQLException if there is an error accessing the database
     */
    private static String turnOnStation(final Cache cache, final RawClientPc station) throws SQLException {

        String result;

        final String ipStr = "192.168.1.255";
        final String macStr = station.macAddress;

        if (macStr != null && macStr.length() == 12) {
            final byte[] macBytes = HexEncoder.decode(macStr);

            try {
                final int count = 6 + 16 * macBytes.length;
                final byte[] bytes = new byte[count];
                for (int i = 0; i < 6; i++) {
                    bytes[i] = (byte) 0xff;
                }
                for (int i = 6; i < count; i += macBytes.length) {
                    System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
                }

                final InetAddress address = InetAddress.getByName(ipStr);
                final DatagramPacket packet = new DatagramPacket(bytes, count, address, 9);
                try (final DatagramSocket socket = new DatagramSocket()) {
                    socket.send(packet);
                }

                RawClientPcLogic.updatePowerStatus(cache, station.computerId, RawClientPc.POWER_TURNING_ON);
                result = "OK";
            } catch (final IOException ex) {
                Log.warning(ex);
                result = "!Failed to send Wake-on-LAN packet";
            }
        } else {
            result = "!Invalid MAC address configured for testing station";
        }

        return result;
    }

    /**
     * A service that allows an authorized user to power off a testing station.
     *
     * <pre>
     * Parameters:
     *     token = SCRAM-SHA-256 token from handshake (used to validate credentials)
     *     computer-id = ID of testing station computer to turn off
     * </pre>
     *
     * @param cache       the data cache
     * @param credentials the user's credentials
     * @param request     the request
     * @return the reply ("OK" or "!" followed by an error message)
     */
    static String powerStationOff(final Cache cache, final UserCredentials credentials, final ServletRequest request) {

        String result;

        if ("ADM".equals(credentials.role)) {

            final String computerId = request.getParameter("computer-id");

            if (computerId == null) {
                result = "!No computer-id parameter provided.";
            } else {
                try {
                    final RawClientPc station = RawClientPcLogic.query(cache, computerId);

                    if (RawClientPc.POWER_REPORTING_ON.equals(station.powerStatus)) {
                        if (station.currentStuId == null) {
                            result = turnOffStation(cache, station);
                        } else {
                            result = "!Station is in use.";
                        }
                    } else {
                        result = "!Station is not reporting ON.";
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    result = "!Failed to look up testing station.";
                }
            }
        } else {
            result = "!Not authorized";
        }

        return result;
    }

    /**
     * Attempts to turn off a station by executing an operating system script vis SSH on the station.
     *
     * @param cache   the data cache
     * @param station the station to attempt to turn off
     * @return the reply ("OK" or "!" followed by an error message)
     * @throws SQLException if there is an error accessing the database
     */
    private static String turnOffStation(final Cache cache, final RawClientPc station) throws SQLException {

        String result;

        final String address = "online@192.168.1." + station.stationNbr;

        final String[] cmd = {"/usr/bin/ssh", "-i", "/imp/online/.ssh/ed25519", "-p", "2236", address,
                "sudo /usr/sbin/shutdown now"};

        try {
            final ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream();
            final Process proc = pb.start();
            try {
                proc.waitFor();
            } catch (final InterruptedException ex) {
                Log.warning("Interrupted", ex);
                Thread.currentThread().interrupt();
            }

            RawClientPcLogic.updatePowerStatus(cache, station.computerId, RawClientPc.POWER_OFF);
            RawClientPcLogic.updateLastPing(cache, station.computerId, null);
            result = "OK";
        } catch (final IOException ex) {
            Log.warning(ex);
            result = "!Failed to execute shell script";
        }

        return result;
    }
}
