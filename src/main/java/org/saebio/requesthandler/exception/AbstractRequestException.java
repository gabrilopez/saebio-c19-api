package org.saebio.requesthandler.exception;

import org.saebio.api._utils.Answer;

public abstract class AbstractRequestException extends Exception {
    private final Answer answer;

    public AbstractRequestException(Answer answer) {
        this.answer = answer;
    }

    public Answer getAnswer() {
        return answer;
    }
}
