/**
 * The front controller, which uses parameters passed in from the servlet context to create the {@code Installation},
 * and then routes requests. When servicing a request, the installation is stored in the ThreadLocal of the thread
 * processing the requests.
 *
 * <p>
 * Requests for any URL beginning with "/www/" are served directly by the front-end HTTP server.
 *
 * <p>
 * All other requests are routed to a mid-controller that hosts websites.
 *
 * <p>
 * The front controller does not enforce any security restrictions - downstream servlets should be prepared to deal with
 * unsecured requests (typically by redirecting to a secured port).
 *
 * <p>
 * This package also provides classes to support the creation of mid-controllers to further direct requests forwarded by
 * the front controller.
 */
package dev.mathops.web.front;
