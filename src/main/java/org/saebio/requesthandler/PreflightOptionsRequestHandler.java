package org.saebio.requesthandler;

import org.saebio.api._utils.Answer;
import org.saebio.api._utils._answers.SuccessAnswer;

import java.util.Map;

public class PreflightOptionsRequestHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public PreflightOptionsRequestHandler() {
        super(UnparsedRequestBody.class);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) {
        return new SuccessAnswer("OK");
    }
}
