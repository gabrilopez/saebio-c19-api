package org.saebio.api;

public class HttpStatus {
    public static int OK() {
        return 200;
    }

    public static int Created() {
        return 201;
    }

    public static int BadRequest() {
        return 400;
    }

    public static int Unauthorized() {
        return 401;
    }

    public static int NotFound() {
        return 404;
    }

    public static int InternalError() {
        return 500;
    }
}
