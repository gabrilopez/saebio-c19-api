package org.saebio.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupService;
import org.saebio.sample.Sample;
import org.saebio.sample.SampleService;

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
            try {
                Response response = new Gson().fromJson(res.body(), Response.class);
                if (response != null) res.status(response.getStatus());
            } catch (JsonSyntaxException ignored) {
            }
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
            return new Gson().toJsonTree(new Response(HttpStatus.OK(), backups.size() + " files found", jsonElement));
        });

        post("/change-database-to-backup", (req, res) -> {
            Backup backup;
            try {
                backup = new Gson().fromJson(req.body(), Backup.class);
            } catch (JsonParseException e) {
                return new Gson().toJson(new Response(HttpStatus.BadRequest(), "Object is not a backup"));
            }

            if (BackupService.backupExists(backup)) {
                if (BackupService.changeDatabaseToBackup(backup)) {
                    SampleService.clearCache();
                    JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());
                    return new Gson().toJson(new Response(HttpStatus.OK(), "Successfully changed database to backup " + backup.getName(), jsonElement));
                }
            } else {
                return new Gson().toJson(new Response(HttpStatus.BadRequest(), "Backup file not found"));
            }
            return new Gson().toJson(new Response(HttpStatus.BadRequest(), "Failed to replace database with existing backup. Try again later"));
        });


        post("/force-backup", (req, res) ->  {
            SampleService sampleService = new SampleService();
            boolean success = sampleService.vacuumInto();
            if (success) {
                // If backups > 14, remove oldest backup
                Collection<Backup> backups = BackupService.getBackups();
                if (backups.size() > 14) BackupService.removeOldestBackup();

                JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());
                String message = "Successfully generated backup";
                return new Gson().toJsonTree(new Response(HttpStatus.OK(), message, jsonElement));
            } else {
                return new Gson().toJson(new Response(HttpStatus.InternalError(), "Error generating backup"));
            }
        });

        post("/remove-backup", (req, res) -> {
            Backup backup;
            try {
                backup = new Gson().fromJson(req.body(), Backup.class);
                if (!BackupService.backupExists(backup)) return new Gson().toJson(new Response(HttpStatus.BadRequest(), "Backup does not exist"));
                boolean removeSuccess = BackupService.removeBackup(backup);
                if (removeSuccess) {
                    JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());
                    String message = "Successfully generated backup";
                    return new Gson().toJsonTree(new Response(HttpStatus.OK(), message, jsonElement));
                } else {
                    return new Gson().toJson(new Response(HttpStatus.InternalError(), "Backup could not be removed"));
                }
            } catch (JsonParseException e) {
                return new Gson().toJson(new Response(HttpStatus.BadRequest(), "Object is not a backup"));
            }
        });

        post("/insert-data", (req, res) -> {
            MultipartConfigElement tmp = new MultipartConfigElement("/tmp");
            req.attribute("org.eclipse.jetty.multipartConfig", tmp);
            Part filePart = req.raw().getPart("file");

            SampleService sampleService = new SampleService();
            if (!sampleService.tryConnection()) {
                res.status(HttpStatus.InternalError());
                return new Gson()
                        .toJson(new Response(HttpStatus.InternalError(), "Could not connect to database"));
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
            return new Gson()
                    .toJsonTree(new Response(HttpStatus.OK(), new Gson().toJsonTree(response)));
        });
    }
}
