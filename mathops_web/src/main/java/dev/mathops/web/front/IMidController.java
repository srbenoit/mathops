package dev.mathops.web.front;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An interface for mid-controllers to which the front controller can forward requests. The mid-controller will be
 * responsible for construction of the servlet handler request and passing the request to the appropriate servlet
 * handler.
 */
public interface IMidController {

    /**
     * Services a request. The connection is known to be insecure (HTTP) at this point.
     *
     * @param req         the request
     * @param resp        the response
     * @param requestPath the complete request path
     * @throws IOException      if there is an error writing the response
     * @throws ServletException if there is an exception processing the request
     */
    void serviceInsecure(HttpServletRequest req, HttpServletResponse resp, String requestPath)
            throws IOException, ServletException;

    /**
     * Services a request. The connection is known to be secure (HTTPS) at this point.
     *
     * @param req         the request
     * @param resp        the response
     * @param requestPath the complete request path
     * @throws IOException      if there is an error writing the response
     * @throws ServletException if there is an exception processing the request
     */
    void serviceSecure(HttpServletRequest req, HttpServletResponse resp, String requestPath)
            throws IOException, ServletException;
}
