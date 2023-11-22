package dev.mathops.app.webstart;

import dev.mathops.core.log.Log;
import dev.mathops.core.parser.HexEncoder;
import dev.mathops.core.parser.xml.IElement;

import java.time.format.DateTimeParseException;

/**
 * A descriptor of a single file that makes up an application.
 */
final class FileDescriptor {

    /** The filename. */
    public final String name;

    /** The file size, in bytes. */
    public final long size;

    /** The SHA-256 hash. */
    private final byte[] sha256;

    /**
     * Constructs a new {@code FileDescriptor}.
     *
     * @param theName   the name
     * @param theSize   the size
     * @param theSha256 the SHA-256 hash
     */
    private FileDescriptor(final String theName, final long theSize, final byte[] theSha256) {

        this.name = theName;
        this.size = theSize;
        this.sha256 = theSha256.clone();
    }

    /**
     * Attempts to extract attributes and child elements to construct a file descriptor.
     *
     * @param elem the 'file' element
     * @return the parsed {@code FileDescriptor}; {@code null} if data could not be parsed
     */
    public static FileDescriptor extract(final IElement elem) {

        FileDescriptor result;

        final String name = elem.getStringAttr("name");
        final String size = elem.getStringAttr("size");
        final String sha256 = elem.getStringAttr("sha256");

        if (name == null) {
            Log.warning("Missing 'name' attribute on <file> element");
            result = null;
        } else if (size == null) {
            Log.warning("Missing 'size' attribute on <file> element");
            result = null;
        } else if (sha256 == null) {
            Log.warning("Missing 'sha256' attribute on <file> element");
            result = null;
        } else {
            try {
                final long sz = Long.parseLong(size);
                if (sz <= 0L) {
                    Log.warning("Invalid 'size' attribute on <file> element");
                    result = null;
                } else if (sha256.length() != 64) {
                    Log.warning("Invalid 'sha256' attribute on <file> element");
                    final byte[] hash = new byte[0];
                    result = new FileDescriptor(name, sz, hash);
                } else {
                    try {
                        final byte[] hash = HexEncoder.decode(sha256);
                        result = new FileDescriptor(name, sz, hash);
                    } catch (final IllegalArgumentException ex) {
                        Log.warning("Invalid 'sha256' attribute on <file> element",
                                ex);
                        result = null;
                    }
                }
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid 'size' attribute on <file> element", ex);
                result = null;
            }
        }

        return result;
    }

    /**
     * Gets the SHA-256 hash.
     *
     * @return the hash
     */
    public byte[] getSHA256() {

        return this.sha256.clone();
    }

    /**
     * Generates a hash code for the descriptor.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.name.hashCode() + Long.hashCode(this.size) + this.sha256.hashCode();
    }

    /**
     * Tests whether this object is equal to another.
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final FileDescriptor fd) {
            equal = fd.name.equals(this.name) && fd.size == this.size && fd.sha256.equals(this.sha256);
        } else {
            equal = false;
        }

        return equal;
    }
}
