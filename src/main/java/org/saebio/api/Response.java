package org.saebio.api;

import com.google.gson.JsonElement;

public class Response {
    private int status;
    private String message;
    private JsonElement data;

    public Response(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(int status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getStatus() {
        return status;
    }
}
