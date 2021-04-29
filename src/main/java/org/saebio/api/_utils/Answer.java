package org.saebio.api._utils;

import com.google.gson.JsonElement;

public class Answer {
    private String message;
    private JsonElement data;
    private final transient int status;

    public Answer(JsonElement data, int status) {
        this.data = data;
        this.status = status;
    }

    public Answer (String message, int status) {
        this.message = message;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
