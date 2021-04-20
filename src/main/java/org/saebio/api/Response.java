package org.saebio.api;

import com.google.gson.JsonElement;

public class Response {
    private String message;
    private JsonElement data;

    public Response(String message) {
        this.message = message;
    }

    public Response(String message, JsonElement data) {
        this.message = message;
        this.data = data;
    }

    public Response(JsonElement data) {
        this.data = data;
    }
}
