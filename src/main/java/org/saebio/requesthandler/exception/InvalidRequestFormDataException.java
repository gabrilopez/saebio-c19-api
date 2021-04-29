package org.saebio.requesthandler.exception;

import org.saebio.api._utils._answers.BadRequestAnswer;

public class InvalidRequestFormDataException extends AbstractRequestException {
    public InvalidRequestFormDataException() {
        super (new BadRequestAnswer("Request form data could not be parsed to object"));
    }
}
