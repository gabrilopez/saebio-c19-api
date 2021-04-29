package org.saebio.api._utils._answers;

import org.saebio.api._utils.Answer;
import org.saebio.api._utils.HttpStatus;

public class InternalErrorAnswer extends Answer {
    public InternalErrorAnswer(String message) {
        super(message, HttpStatus.InternalError);
    }

    public InternalErrorAnswer() {
        super("Internal error", HttpStatus.InternalError);
    }
}
