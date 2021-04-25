package org.saebio.requesthandler;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.saebio.api.Answer;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler.exception.InvalidRequestBodyObjectException;
import org.saebio.requesthandler.exception.InvalidRequestFormDataException;
import org.saebio.utils.Model;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.*;

public abstract class AbstractRequestHandler<V> implements RequestHandler<V>, Route {

    private final Class<V> valueClass;
    protected Model model;
    protected Map<String, Part> requestParts = new HashMap<>();

    public AbstractRequestHandler(Class<V> valueClass, Model model){
        this.valueClass = valueClass;
        this.model = model;
    }

    public AbstractRequestHandler(Class<V> valueClass) {
        this.valueClass = valueClass;
    }

    public final Answer process(V value, Map<String, String> queryParams) throws AbstractRequestException {
        return processImpl(value, queryParams);
    }

    protected abstract Answer processImpl(V value, Map<String, String> queryParams) throws AbstractRequestException;

    private boolean requestHasFormData(Request request) {
        String contentType = request.contentType();
        return contentType != null && contentType.contains("multipart/form-data");
    }

    private void extractRequestFormData(Request request) throws InvalidRequestFormDataException {
        try {
            MultipartConfigElement tmp = new MultipartConfigElement("/tmp");
            request.attribute("org.eclipse.jetty.multipartConfig", tmp);
            Collection<Part> parts = request.raw().getParts();
            for (Part part : parts) {
                this.requestParts.put(part.getName(), part);
            }
        } catch (IOException | ServletException e) {
            e.printStackTrace();
            throw new InvalidRequestFormDataException();
        }
    }

    private V parseRequestBodyToObject(String body) throws InvalidRequestBodyObjectException {
        try {
            return new Gson().fromJson(body, valueClass);
        } catch (JsonParseException e) {
            throw new InvalidRequestBodyObjectException();
        }
    }

    private boolean requestHasJsonBody(Request request) {
        String contentType = request.contentType();
        return contentType != null && contentType.contains("application/json");
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        V value = null;
        if (requestHasJsonBody(request)) {
            value = parseRequestBodyToObject(response.body());
        }

        if (requestHasFormData(request)) extractRequestFormData(request);

        Map<String, String> queryParams = new HashMap<>();
        Answer answer = process(value, queryParams);

        response.body(new Gson().toJson(answer));
        response.status(answer.getStatus());
        return answer;

        /*

        try {
            value = new Gson().fromJson(request.body(), valueClass);
            if (requestHasFormData(request)) extractRequestFormData(request);
        } catch(JsonParseException e) {
            throw new InvalidRequestBodyException();
        } catch(IOException | ServletException e) {
            return new Answer("Request multipart form data could not be parsed", HttpStatus.BadRequest());
        }*/
    }
}
