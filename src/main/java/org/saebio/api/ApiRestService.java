package org.saebio.api;

import com.google.gson.Gson;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupService;
import org.saebio.sample.Sample;
import org.saebio.sample.SampleService;
import org.saebio.utils.JsonTransformer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import static spark.Spark.*;

public class ApiRestService {
    public static void main(String[] args) {
        // Filter after each request
        after((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "POST");
            res.type("application/json");
            System.gc();
        });

        get("/backups", (req, res) -> {
            Collection<Backup> backups = BackupService.getBackups();
            SampleService sampleService = new SampleService();
            if (sampleService.tryConnection()) {
                int currentDatabaseNumberOfRows = sampleService.getRowCount();
                backups.forEach(backup -> {
                    if (backup.getSelected()) backup.setRows(currentDatabaseNumberOfRows);
                });
            }
            JsonElement jsonElement = new Gson().toJsonTree(backups);
            res.status(HttpStatus.OK());
            return new Response(backups.size() + " files found", jsonElement);
        }, new JsonTransformer());

        post("/change-database-to-backup", (req, res) -> {
            Backup backup;
            try {
                backup = new Gson().fromJson(req.body(), Backup.class);
            } catch (JsonParseException e) {
                res.status(HttpStatus.BadRequest());
                return new Response("Object is not a backup");
            }

            if (BackupService.backupExists(backup)) {
                if (BackupService.changeDatabaseToBackup(backup)) {
                    SampleService.clearCache();
                    JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());

                    res.status(HttpStatus.OK());
                    return new Response("Successfully changed database to backup " + backup.getName(), jsonElement);
                }
            } else {
                res.status(HttpStatus.BadRequest());
                return new Response("Backup file not found");
            }

            res.status(HttpStatus.BadRequest());
            return new Response("Failed to replace database with existing backup. Try again later");
        }, new JsonTransformer());


        post("/force-backup", (req, res) ->  {
            SampleService sampleService = new SampleService();
            boolean success = sampleService.vacuumInto();
            if (success) {
                // If backups > 14, remove oldest backup
                Collection<Backup> backups = BackupService.getBackups();
                if (backups.size() > 14) BackupService.removeOldestBackup();

                JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());
                String message = "Successfully generated backup";
                res.status(HttpStatus.Created());
                return new Response(message, jsonElement);
            } else {
                res.status(HttpStatus.InternalError());
                return new Response("Error generating backup");
            }
        }, new JsonTransformer());

        post("/remove-backup", (req, res) -> {
            Backup backup;
            try {
                backup = new Gson().fromJson(req.body(), Backup.class);
                if (!BackupService.backupExists(backup)) {
                    res.status(HttpStatus.BadRequest());
                    return new Response("Backup does not exist");
                }
                boolean removeSuccess = BackupService.removeBackup(backup);
                if (removeSuccess) {
                    JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());
                    String message = "Successfully removed backup";

                    res.status(HttpStatus.OK());
                    return new Response(message, jsonElement);
                } else {
                    res.status(HttpStatus.InternalError());
                    return new Response("Backup could not be removed");
                }
            } catch (JsonParseException e) {
                res.status(HttpStatus.BadRequest());
                return new Response("Object is not a backup");
            }
        }, new JsonTransformer());

        post("/insert-data", (req, res) -> {
            MultipartConfigElement tmp = new MultipartConfigElement("/tmp");
            req.attribute("org.eclipse.jetty.multipartConfig", tmp);
            Part filePart = req.raw().getPart("file");

            SampleService sampleService = new SampleService();
            if (!sampleService.tryConnection()) {
                res.status(HttpStatus.InternalError());
                return new Response("Could not connect to database");
            }

            InputStream inputStream = filePart.getInputStream();
            Stream<String> stream = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                                        .lines().skip(1);

            int count = 0;
            List<Integer> errorLines = new ArrayList<>();
            for (String line : (Iterable<String>) stream::iterator) {
                Sample sample = SampleService.handleSampleLine(line);
                if (sample == null) {
                    errorLines.add(count + 1);
                } else {
                    if (!sampleService.addSample(sample)) errorLines.add(count + 1);
                }
                count++;
            }
            stream.close();
            int added = count - errorLines.size();
            if (added > 0) sampleService.vacuumInto();

            // If backups > 14, remove oldest backup
            Collection<Backup> backups = BackupService.getBackups();
            if (backups.size() > 14) BackupService.removeOldestBackup();

            Map<String, Object> response = new HashMap<>();
            response.put("size", count);
            response.put("added", added);
            response.put("errors", errorLines.size());
            response.put("errorLines", errorLines.toString());
            res.status(HttpStatus.OK());
            return new Response(new Gson().toJsonTree(response));
        }, new JsonTransformer());
    }
}
