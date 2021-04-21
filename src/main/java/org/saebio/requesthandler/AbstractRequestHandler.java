package org.saebio.requesthandler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;
import org.saebio.utils.Model;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRequestHandler<V> implements RequestHandler<V>, Route {

    private final Class<V> valueClass;
    protected Model model;

    public AbstractRequestHandler(Class<V> valueClass, Model model){
        this.valueClass = valueClass;
        this.model = model;
    }

    public AbstractRequestHandler(Class<V> valueClass) {
        this.valueClass = valueClass;
    }

    public final Answer process(V value, Map<String, String> queryParams) {
        return processImpl(value, queryParams);
    }

    protected abstract Answer processImpl(V value, Map<String, String> queryParams);

    @Override
    public Object handle(Request request, Response response) throws Exception {
        V value = null;
        try {
            value = new Gson().fromJson(request.body(), valueClass);
        } catch(JsonParseException e) {
            return new Answer("Request could not be parsed to object", new JsonObject(), HttpStatus.BadRequest());
        }

        Map<String, String> queryParams = new HashMap<>();
        Answer answer = process(value, queryParams);

        response.status(answer.getStatus());
        response.body(answer.getData().toString());
        return answer.getData();
    }
}
