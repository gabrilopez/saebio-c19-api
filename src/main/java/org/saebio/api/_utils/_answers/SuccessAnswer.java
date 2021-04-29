package org.saebio.api._utils._answers;

import com.google.gson.JsonElement;
import org.saebio.api._utils.Answer;
import org.saebio.api._utils.HttpStatus;

public class SuccessAnswer extends Answer {
    public SuccessAnswer(String message) {
        super(message, HttpStatus.OK);
    }

    public SuccessAnswer(JsonElement jsonElement) {
        super(jsonElement, HttpStatus.OK);
    }
}
