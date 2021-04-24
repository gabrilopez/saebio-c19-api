package org.saebio.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupApiConstants;
import org.saebio.backup.BackupService;
import org.saebio.requesthandler.backup.CreateBackupHandler;
import org.saebio.requesthandler.backup.GetBackupsHandler;
import org.saebio.requesthandler.backup.DeleteBackupHandler;
import org.saebio.requesthandler.backup.PreflightOptionsRequestHandler;
import org.saebio.sample.Sample;
import org.saebio.sample.SampleApiConstants;
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
    private static final String accessControlAllowMethods = "GET, POST, DELETE, OPTIONS";
    private static final String accessControlallowHeaders = "Content-Type, Accept";


    public static void main(String[] args) {
        // Filter after each request
        after((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", accessControlAllowMethods);
            res.header("Access-Control-Allow-Headers", accessControlallowHeaders);
            res.type("application/json");
            System.gc();
        });

        get("/backups", new GetBackupsHandler());

        post("/change-database-to-backup", (req, res) -> {
            Backup backup;
            try {
                backup = new Gson().fromJson(req.body(), Backup.class);
            } catch (JsonParseException e) {
                res.status(HttpStatus.BadRequest());
                return new Answer(BackupApiConstants.ERROR_PROVIDED_OBJECT_IS_NOT_A_BACKUP);
            }

            if (BackupService.backupExists(backup)) {
                if (BackupService.changeDatabaseToBackup(backup)) {
                    SampleService.clearCache();
                    JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());

                    res.status(HttpStatus.OK());
                    return new Answer(BackupApiConstants.SUCCESSFULLY_CHANGED_DATABASE_TO_BACKUP + " " + backup.getName(), jsonElement);
                }
            } else {
                res.status(HttpStatus.BadRequest());
                return new Answer(BackupApiConstants.ERROR_BACKUP_FILE_NOT_FOUND);
            }

            res.status(HttpStatus.BadRequest());
            return new Answer(BackupApiConstants.ERROR_FAILED_REPLACE_DATABASE_WITH_BACKUP);
        }, new JsonTransformer());


        // Handle CORS Preflight Options Request Handler
        options("/*", new PreflightOptionsRequestHandler());

        post("/create-backup", new CreateBackupHandler());


        delete("/backup", new DeleteBackupHandler());

        post("/insert-data", (req, res) -> {
            MultipartConfigElement tmp = new MultipartConfigElement("/tmp");
            req.attribute("org.eclipse.jetty.multipartConfig", tmp);
            Part filePart = req.raw().getPart("file");

            SampleService sampleService = new SampleService();
            if (!sampleService.tryConnection()) {
                res.status(HttpStatus.InternalError());
                return new Answer(SampleApiConstants.ERROR_CONNECTING_TO_DATABASE);
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
            if (added > 0) sampleService.vacuumInto(); // aquí debería llamar a new createbackuphandler... lógica repetida

            // If backups > 14, remove oldest backup
            Collection<Backup> backups = BackupService.getBackups();
            if (backups.size() > 14) BackupService.removeOldestBackup();

            Map<String, Object> response = new HashMap<>();
            response.put("size", count);
            response.put("added", added);
            response.put("errors", errorLines.size());
            response.put("errorLines", errorLines.toString());
            res.status(HttpStatus.OK());
            return new Answer(new Gson().toJsonTree(response));
        }, new JsonTransformer());
    }
}
