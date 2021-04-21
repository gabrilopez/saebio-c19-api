package org.saebio.api;

import com.google.gson.JsonElement;

public class Answer {
    private String message;
    private JsonElement data;
    private int status;

    public Answer(String message) {
        this.message = message;
    }

    public Answer(String message, JsonElement data) {
        this.message = message;
        this.data = data;
    }

    public Answer(JsonElement data) {
        this.data = data;
    }

    public Answer (String message, int status) {
        this.message = message;
        this.status = status;
    }

    public Answer(String message, JsonElement data, int status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public JsonElement getData() {
        return data;
    }
}
