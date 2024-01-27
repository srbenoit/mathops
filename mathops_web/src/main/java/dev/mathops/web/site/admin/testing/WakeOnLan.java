package dev.mathops.web.site.admin.testing;

import dev.mathops.commons.log.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Pattern;

/**
 * This class can send a wake-on-LAN "magic packet" to a specified hardware address to trigger it to power on.
 */
public enum WakeOnLan {
    ;

    /** The default port. */
    private static final int PORT = 9;

    /** A compiled regular expression pattern. */
    private static final Pattern PATTERN = Pattern.compile("(\\:|\\-)");

    /**
     * Main method to send a test packet.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        if (args.length != 2) {
            Log.fine("Usage: java WakeOnLan <broadcast-ip> <mac-address>");
            Log.fine("Example: java WakeOnLan 192.168.0.255 00:0D:61:08:22:4A");
            Log.fine("Example: java WakeOnLan 192.168.0.255 00-0D-61-08-22-4A");
            System.exit(1);
        }

        final String ipStr = args[0];
        final String macStr = args[1];

        try {
            final byte[] macBytes = getMacBytes(macStr);
            final int len = 6 + 16 * macBytes.length;
            final byte[] bytes = new byte[len];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < len; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            final InetAddress address = InetAddress.getByName(ipStr);
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }

            Log.info("Wake-on-LAN packet sent.");
        } catch (final IOException e) {
            Log.warning("Failed to send Wake-on-LAN packet", e);
            System.exit(1);
        }
    }

    /**
     * Decodes bytes from a MAC address string.
     *
     * @param macStr the address string
     * @return the bytes
     * @throws IllegalArgumentException if there is an error decoding a byte
     */
    private static byte[] getMacBytes(final CharSequence macStr) throws IllegalArgumentException {

        final byte[] bytes = new byte[6];
        final String[] hex = PATTERN.split(macStr);
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address", e);
        }
        return bytes;
    }

}
