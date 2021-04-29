package org.saebio.api._utils._answers;

import org.saebio.api._utils.Answer;
import org.saebio.api._utils.HttpStatus;

public class BadRequestAnswer extends Answer {
    public BadRequestAnswer(String message) {
        super(message, HttpStatus.BadRequest);
    }
}
