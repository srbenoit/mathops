package dev.mathops.session.txn;

/**
 * The interface for the client end of a server connection, which encapsulates a bidirectional data stream.
 */
public interface IWebServiceClient {

    /**
     * Initialize the object.
     *
     * @return {@code true} if successful; {@code false} otherwise
     */
    boolean init();

    /**
     * Tests whether the connection is in a state where data may be sent to the server. This does not necessarily imply
     * an open socket or other persistent connection.
     *
     * @return {@code true} if the connection is ready to be used; {@code false} if not
     */
    boolean isOpen();

    /**
     * Closes the object.
     */
    void close();

    /**
     * Writes a block of data to the server.
     *
     * @param obj the data block to write
     * @return {@code true} if successful; {@code false} otherwise
     */
    boolean writeObject(String obj);

    /**
     * Read a block of data from the server.
     *
     * @param objName a friendly name for the object that is to be read, to allow logging of errors to better direct the
     *                developer to the problem
     * @return the object read, or {@code null} if an error occurred
     */
    char[] readObject(String objName);
}
