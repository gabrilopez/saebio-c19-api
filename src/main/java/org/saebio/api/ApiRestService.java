package org.saebio.api;

import com.google.gson.Gson;

import org.saebio.api._utils.Answer;
import org.saebio.api._utils.InputArgumentsHandler;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupService;
import org.saebio.requesthandler.PreflightOptionsRequestHandler;
import org.saebio.requesthandler._backup.*;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler._sample.AddSamplesRequestHandler;
import org.saebio.utils.SqliteModel;

import static spark.Spark.*;

public class ApiRestService {

    public static void main(String[] args) {
        InputArgumentsHandler inputArgumentsHandler = new InputArgumentsHandler(args);

        SqliteModel sqliteModel = new SqliteModel(
                inputArgumentsHandler.getOption("database"),
                inputArgumentsHandler.getOption("user"),
                inputArgumentsHandler.getOption("password"));

        BackupService backupService = new BackupService(sqliteModel);

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

        get("/backups", new GetBackupsHandler(backupService));

        put("/backup/restore", new RestoreBackupHandler(backupService));

        // Handle CORS Preflight Options Request
        options("/*", new PreflightOptionsRequestHandler());

        post("/backup", new CreateBackupHandler(backupService));

        delete("/backup", new DeleteBackupHandler(backupService));

        post("/insert-data", new AddSamplesRequestHandler(sqliteModel, backupService));
    }
}
