package org.saebio.requesthandler.exception;

import org.saebio.api._utils._answers.BadRequestAnswer;

public class RequestBodyObjectNotFoundException extends AbstractRequestException {
    public RequestBodyObjectNotFoundException() {
        super(new BadRequestAnswer("Request body object not found"));
    }
}
