package org.saebio.requesthandler;

import org.saebio.api.Answer;

import java.util.Map;

public interface RequestHandler<V> {
    Answer process(V value, Map<String, String> urlParams);
}
