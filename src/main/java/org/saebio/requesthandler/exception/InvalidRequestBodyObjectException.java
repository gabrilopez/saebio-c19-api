package org.saebio.requesthandler.exception;

import org.saebio.api._utils._answers.BadRequestAnswer;

public class InvalidRequestBodyObjectException extends AbstractRequestException {
    public InvalidRequestBodyObjectException() {
        super(new BadRequestAnswer("Request could not be parsed to object"));
    }
}
