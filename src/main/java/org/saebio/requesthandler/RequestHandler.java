package org.saebio.requesthandler;

import org.saebio.api.Answer;
import org.saebio.requesthandler.exception.AbstractRequestException;

import java.util.Map;

public interface RequestHandler<V> {
    Answer process(V value, Map<String, String> urlParams) throws AbstractRequestException;
}
