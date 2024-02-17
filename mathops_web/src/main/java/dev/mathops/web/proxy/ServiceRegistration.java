package dev.mathops.web.proxy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A registration of one or more services under a path.  There may be multiple "active" services, and zero or more
 * "inactive" services.
 */
final class ServiceRegistration {

    /** The path under which this service (or list of services) is registered. */
    private String path;

    /**
     * Constructs a new {@code ServiceRegistration}.
     *
     * @param thePath the path under which this service (or list of services) is registered
     */
    ServiceRegistration(final String thePath) {

        this.path = thePath;
    }

    /**
     * Gets the path under which this service (or list of services) is registered.
     *
     * @return the path
     */
    public String getPath() {

        return this.path;
    }

    /**
     * Serves a request using a registered service.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException      if there is an error writing the response
     * @throws ServletException if there is an exception processing the request
     */
    void serve(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {

    }

    /**
     * Generates the string representation of the registration.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "ServiceRegistration{}";
    }
}
