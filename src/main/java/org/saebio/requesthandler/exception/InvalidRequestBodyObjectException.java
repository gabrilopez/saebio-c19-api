package org.saebio.requesthandler.exception;

import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;

public class InvalidRequestBodyObjectException extends AbstractRequestException {
    public InvalidRequestBodyObjectException() {
        super(new Answer("Request could not be parsed to object", HttpStatus.BadRequest()));
    }
}
