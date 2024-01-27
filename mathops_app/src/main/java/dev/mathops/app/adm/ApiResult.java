package dev.mathops.app.adm;

import dev.mathops.commons.parser.json.JSONObject;

import java.util.List;

/**
 * The result of an API call.
 */
final class ApiResult {

    /** The parsed JSON response. */
    private final JSONObject response;

    /** The parsed JSON array response. */
    private final List<JSONObject> arrayResponse;

    /** The error message of the request failed. */
    private final String error;

    /**
     * Constructs a {@code ApiCallResult} with a parsed response.
     *
     * @param theResponse the parsed response
     */
    ApiResult(final JSONObject theResponse) {

        this.response = theResponse;
        this.arrayResponse = null;
        this.error = null;
    }

    /**
     * Constructs a {@code ApiCallResult} with a parsed array response.
     *
     * @param theResponse the parsed array response
     */
    ApiResult(final List<JSONObject> theResponse) {

        this.response = null;
        this.arrayResponse = theResponse;
        this.error = null;
    }

    /**
     * Constructs a {@code ApiCallResult} with an error message
     *
     * @param theError the error message
     */
    ApiResult(final String theError) {

        this.response = null;
        this.arrayResponse = null;
        this.error = theError;
    }
}
