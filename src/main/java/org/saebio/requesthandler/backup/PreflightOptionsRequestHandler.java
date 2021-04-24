package org.saebio.requesthandler.backup;

import org.saebio.api.Answer;
import org.saebio.api.EmptyRequestBody;
import org.saebio.api.HttpStatus;
import org.saebio.requesthandler.AbstractRequestHandler;

import java.util.Map;

public class PreflightOptionsRequestHandler extends AbstractRequestHandler<EmptyRequestBody> {

    public PreflightOptionsRequestHandler() {
        super(EmptyRequestBody.class);
    }

    @Override
    protected Answer processImpl(EmptyRequestBody value, Map<String, String> queryParams) {
        return new Answer("OK", HttpStatus.OK());
    }
}
