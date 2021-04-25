package org.saebio.requesthandler.exception;

import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;

public class InvalidRequestFormDataException extends AbstractRequestException {
    public InvalidRequestFormDataException() {
        super(new Answer("Request form data could not be parsed", HttpStatus.BadRequest()));
    }
}
