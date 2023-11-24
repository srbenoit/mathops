package dev.mathops.web.front;

import dev.mathops.core.CoreConstants;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilities that help extract information from HTTP Servlet requests.
 */
/* default */  enum ServletUtils {
    ;

    /**
     * Gets the host from a request, removing any trailing "dev" or "test" from the leading component (so
     * "foodev.bar.baz" and "footest.bar.baz" each return "foo.bar.baz" as the host).
     *
     * @param req the servlet request
     * @return the host
     */
    static String getHost(final ServletRequest req) {

        final String server = req.getServerName();
        final String host;

        final int firstDot = server.indexOf('.');
        if (firstDot < 4) {
            host = server;
        } else {
            final String name = server.substring(0, firstDot);
            if (name.endsWith("dev")) {
                host = name.substring(0, name.length() - 3) + server.substring(firstDot);
            } else if (name.endsWith("test")) {
                host = name.substring(0, name.length() - 4) + server.substring(firstDot);
            } else {
                host = server;
            }
        }

        return host;
    }

    /**
     * Gets the servlet path from a request, converting a {@code null} value to the empty string. This returns the whole
     * path (the servlet path + the path info part).
     * <p>
     * Handlers should be registered on either the empty string or a path with a leading "/" and no trailing "/". The
     * reason for this behavior is that we want a handler to receive the same relative paths when registered on "" as it
     * would get if registered on "/foo".
     *
     * <pre>
     * http://www.example.com      --> ""     --> "" + ""
     * http://www.example.com/     --> "/"    --> "" + "/"
     * http://www.example.com/a    --> "/a"   --> "" + "/a"
     * http://www.example.com/a/   --> "/a/"  --> "" + "/a/"
     * http://www.example.com/a/b  --> "/a/b" --> "" + "/a/b"
     *
     * http://www.example.com/foo      --> "/foo"     --> "/foo" + ""
     * http://www.example.com/foo/     --> "/foo/"    --> "/foo" + "/"
     * http://www.example.com/foo/a    --> "/foo/a"   --> "/foo" + "/a"
     * http://www.example.com/foo/a/   --> "/foo/a/"  --> "/foo" + "/a/"
     * http://www.example.com/foo/a/b  --> "/foo/a/b" --> "/foo" + "/a/b"
     * </pre>
     *
     * @param req the servlet request
     * @return the path
     */
    static String getPath(final HttpServletRequest req) {

        final String sPath = req.getServletPath();
        final String iPath = req.getPathInfo();

        final String path = sPath == null ? iPath : iPath == null ? sPath : sPath + iPath;

        return path == null ? CoreConstants.EMPTY : path;
    }
}
