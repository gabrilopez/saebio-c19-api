package org.saebio.requesthandler;

import org.saebio.api.Answer;
import org.saebio.api.UnparsedRequestBody;
import org.saebio.api.HttpStatus;
import org.saebio.requesthandler.AbstractRequestHandler;

import java.util.Map;

public class PreflightOptionsRequestHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public PreflightOptionsRequestHandler() {
        super(UnparsedRequestBody.class);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) {
        return new Answer("OK", HttpStatus.OK());
    }
}
