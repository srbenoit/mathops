package dev.mathops.app.canvas;

import dev.mathops.commons.parser.json.JSONObject;

import java.util.List;

/**
 * The result of an API call.
 */
public final class ApiResult {

    /** The parsed JSON response. */
    public final JSONObject response;

    /** The parsed JSON array response. */
    public final List<JSONObject> arrayResponse;

    /** The error message of the request failed. */
    public final String error;

    /**
     * Constructs a {@code ApiResult} with a parsed response.
     *
     * @param theResponse the parsed response
     */
    ApiResult(final JSONObject theResponse) {

        this.response = theResponse;
        this.arrayResponse = null;
        this.error = null;
    }

    /**
     * Constructs a {@code ApiResult} with a parsed array response.
     *
     * @param theResponse the parsed array response
     */
    ApiResult(final List<JSONObject> theResponse) {

        this.response = null;
        this.arrayResponse = theResponse;
        this.error = null;
    }

    /**
     * Constructs a {@code ApiResult} with an error message
     *
     * @param theError the error message
     */
    ApiResult(final String theError) {

        this.response = null;
        this.arrayResponse = null;
        this.error = theError;
    }
}
