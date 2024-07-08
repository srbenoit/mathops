package dev.mathops.app.wigner;

import dev.mathops.commons.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Wigner, who observes the state via the boundary, and might, if so inclined, report results to the observer. This
 * class attempts to implement quantum erasure.
 */
public final class Wigner implements Runnable {

    /** The index. */
    private final int index;

    /** The value. */
    private final int value;

    /**
     * Constructs a new {@code Wigner}.
     *
     * @param theIndex the index
     * @param theValue the value
     */
    private Wigner(final int theIndex, final int theValue) {

        this.index = theIndex;
        this.value = theValue;
    }

    /**
     * Runs the process.
     */
    @Override
    public void run() {

        try {
            final SecureRandom rnd = SecureRandom.getInstanceStrong();

            for (;;) {
                final long delay = rnd.nextLong(2000);
                TimeUnit.MILLISECONDS.sleep(delay);
                pollBoundary(rnd);
            }
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
        } catch (final InterruptedException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Polls the random source, testing whether a particular digit has a particular value.
     */
    private void pollBoundary(final SecureRandom rnd) {

        try {
            final URI uri = new URI("http://localhost:8001/check?i=" + this.index + "&v=" + this.value);
            final URL url = uri.toURL();

            final URLConnection conn = url.openConnection();
            try (final InputStream in = conn.getInputStream();
                 final InputStreamReader reader = new InputStreamReader(in)) {

                final char[] buffer = new char[40];
                final StringBuilder builder = new StringBuilder(40);

                int count = reader.read(buffer);
                while (count > 0) {
                    builder.append(new String(buffer, 0, count));
                    count = reader.read(buffer);
                }

                final String fetched = builder.toString().trim();

                if ("Y".equals(fetched)) {

                    if (rnd.nextDouble() < 0.1) {
                        notifyObserver();
                    }
                }
            }
        } catch (final MalformedURLException ex) {
            Log.warning("Invalid URL.", ex);
        } catch (final URISyntaxException ex) {
            Log.warning("Invalid URI.", ex);
        } catch (final IOException ex) {
            Log.warning("Failed to poll boundary.", ex);
        }
    }

    /**
     * Polls the random source, testing whether a particular digit has a particular value.
     *
     * @return true if the random source reported that the entry with the specified index has the specified value
     */
    private boolean notifyObserver() {

        boolean result = false;

        try {
            final URI uri = new URI("http://localhost:8002/notify?i=" + this.index + "&v=" + this.value);
            final URL url = uri.toURL();
            url.openConnection();
        } catch (final MalformedURLException ex) {
            Log.warning("Invalid URL.", ex);
        } catch (final URISyntaxException ex) {
            Log.warning("Invalid URI.", ex);
        } catch (final IOException ex) {
            Log.warning("Failed to fetch random number data.", ex);
        }

        return result;
    }

    /**
     * Tests the class.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final Runnable digit0 = new Wigner(0, 8);
        final Runnable digit1 = new Wigner(1, 2);
        final Runnable digit2 = new Wigner(2, 3);
        final Runnable digit3 = new Wigner(3, 2);
        final Runnable digit4 = new Wigner(4, 5);
        final Runnable digit5 = new Wigner(5, 1);
        final Runnable digit6 = new Wigner(6, 2);
        final Runnable digit7 = new Wigner(7, 1);
        final Runnable digit8 = new Wigner(8, 3);
        final Runnable digit9 = new Wigner(9, 0);

        new Thread(digit0).run();
        new Thread(digit1).run();
        new Thread(digit2).run();
        new Thread(digit3).run();
        new Thread(digit4).run();
        new Thread(digit5).run();
        new Thread(digit6).run();
        new Thread(digit7).run();
        new Thread(digit8).run();
        new Thread(digit9).run();
    }
}
