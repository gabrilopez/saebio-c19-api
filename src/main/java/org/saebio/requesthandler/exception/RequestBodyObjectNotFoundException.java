package org.saebio.requesthandler.exception;

import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;

public class RequestBodyObjectNotFoundException extends AbstractRequestException {
    public RequestBodyObjectNotFoundException() {
        super(new Answer("Request body object not found", HttpStatus.BadRequest()));
    }
}
