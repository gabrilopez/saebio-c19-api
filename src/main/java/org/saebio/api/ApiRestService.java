package org.saebio.api;

import com.google.gson.Gson;

import org.saebio.requesthandler.PreflightOptionsRequestHandler;
import org.saebio.requesthandler.backup.*;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler.sample.AddSamplesRequestHandler;

import static spark.Spark.*;

public class ApiRestService {

    public static void main(String[] args) {
        // Add CORS headers before each request
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Accept");
            response.type("application/json");
        });

        after((req, res) -> System.gc());

        exception(AbstractRequestException.class, (e, request, response) -> {
            Answer answer = e.getAnswer();
            response.body(new Gson().toJson(answer));
            response.status(answer.getStatus());
        });

        get("/backups", new GetBackupsHandler());

        put("/backup/restore", new RestoreBackupHandler());

        // Handle CORS Preflight Options Request
        options("/*", new PreflightOptionsRequestHandler());

        post("/backup", new CreateBackupHandler());

        delete("/backup", new DeleteBackupHandler());

        post("/insert-data", new AddSamplesRequestHandler());
    }
}
