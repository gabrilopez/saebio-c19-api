package org.saebio.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static spark.Spark.*;

public class ApiRestService {
    private static DateTimeFormatter birthDateFormatter = DateTimeFormatter
            .ofPattern("dd.MM.yyyy")
            .withLocale(Locale.ENGLISH);
    private static DateTimeFormatter registryDateFormatter = DateTimeFormatter
            .ofPattern("[d-M-yy][dd-MM-yy][dd-M-yy][d-MM-yy]");
            /*
    private static DateTimeFormatter registryDateFormatter = DateTimeFormatter
            .ofPattern("d-M-yy")
            .withLocale(Locale.ENGLISH);*/
    private static Map<String, Sample> cache = new HashMap<>();

    public static void main(String[] args) {
        // Filter after each request
        after((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "POST");
            res.type("application/json");
            try {
                Response response = new Gson().fromJson(res.body(), Response.class);
                if (response != null) res.status(response.getStatus());
            } catch (JsonParseException e) {
                // Do something
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
                    cache.clear();
                    JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());
                    return new Gson().toJson(new Response(HttpStatus.OK(), "Successfully changed database to backup " + backup.getName(), jsonElement));
                }
            } else {
                return new Gson().toJson(new Response(HttpStatus.BadRequest(), "Backup file not found"));
            }
            return new Gson().toJson(new Response(HttpStatus.BadRequest(), "Failed to replace database with existing backup. Try again later"));
        });

        // TODO: REMOVE?
        post("/force-backup", (req, res) ->  {
            SampleService sampleService = new SampleService();
            boolean success = sampleService.vacuumInto();
            if (success) {
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
                                        .lines();

            int size = 0;
            int errorCount = 0;
            for (String line : (Iterable<String>) stream::iterator) {
                Sample sample = handleSampleLine(line);
                if (sample == null) {
                    errorCount++;
                    System.out.println("Please check row: " + line);
                } else {
                    errorCount += sampleService.addSample(sample) ? 0 : 1;
                }
                size++;
            }
            stream.close();
            int added = size - errorCount;
            if (added > 0) sampleService.vacuumInto();

            Map<String, String> response = new HashMap<>();
            response.put("size", String.valueOf(size));
            response.put("added", String.valueOf(added));
            response.put("errors", String.valueOf(errorCount));

            return new Gson()
                    .toJsonTree(new Response(HttpStatus.OK(), new Gson().toJsonTree(response)));

            // int errorCount = 0;

            /*
            String[] line;
            while ((line = reader.readNext()) != null) {
                String temp = Arrays.toString(line);
                temp = temp.substring(1, temp.length() - 1);
                String[] data = temp.split(";");
                Sample sample = createSampleFromLine(data);
                if (sample == null) {
                    errorCount++;
                    System.out.println("Please check row: " + temp);
                } else {
                    errorCount += (sampleService.addSample(sample) == HttpStatus.OK()) ? 0 : 1;
                }
            }

            String message = "[FINISHED]\nRead " + reader.getLinesRead() + "\tError count: " + errorCount;
            System.out.println(message);
            System.out.println("FINAL CACHE SIZE:" + cache.size());

            return new Gson()
                    .toJsonTree(new Response(HttpStatus.OK(), message));

             */
        });
    }

    private static Sample handleSampleLine(String line) {
        String[] data = line.split(";");
        return createSampleFromLine(data);
    }

    /** MOVER A SAMPLE SERVICE? */
    private static Sample createSampleFromLine(String[] line) {
        // Length 8 mínima por ahora porque los campos resultadoTMA, sexo, edad, procedencia y motivo
        // pueden no estar seteados
        if (line.length < 9) return null;
        Sample sample = new Sample();
        try {
            sample.setRegistryDate(LocalDate.parse(line[0].split(" ")[0].replace('/', '-'), registryDateFormatter));
            sample.setPatientName(line[1]);
            sample.setPatientSurname(line[2]);
            sample.setBirthDate(LocalDate.parse(line[3], birthDateFormatter));
            sample.setNHC(line[4]);
            sample.setPetition(Integer.parseInt(line[5]));
            sample.setService(line[6]);
            sample.setCriteria(line[7]);
            if (!line[8].trim().isEmpty()) sample.setResultPCR(line[8]);

            // El hospital doctor negrín está trabajando en implementar estos campos
            if (line.length > 9 && !line[9].trim().isEmpty()) sample.setResultTMA(line[9]);
            if (line.length > 10) sample.setSex(!line[10].trim().isEmpty() ? line[10] : null);
            if (line.length > 11) sample.setAge(isNumeric(line[11]) ? Integer.valueOf(line[11]) : null);
            if (line.length > 12) sample.setOrigin(!line[12].trim().isEmpty() ? line[12] : null);
            if (line.length > 13) sample.setReason(!line[13].trim().isEmpty() ? line[13] : null);
            if (line.length > 14) sample.setVariant(!line[14].trim().isEmpty() ? line[14] : null);
            if (line.length > 15) sample.setLineage(!line[15].trim().isEmpty() ? line[15] : null);
            sample.setEpisode(getSampleEpisodeNumber(sample));
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        updateSampleCache(sample);
        return sample;
    }

    public static int getSampleEpisodeNumber(Sample newSample) {
        // Old sample will always be the first sample from current episode
        String NHC = newSample.getNHC();
        Sample oldSample = cache.getOrDefault(NHC, null);

        if (oldSample == null) {
            SampleService sampleService = new SampleService();
            oldSample = sampleService.getFirstSampleFromCurrentEpisode(NHC);
            if (oldSample == null) return 1;
            // Save old sample found in database to cache
            cache.put(NHC, oldSample);
        }
        return newSample.belongToSameEpisode(oldSample) ? oldSample.getEpisode() : oldSample.getEpisode() + 1;
    }

    private static void updateSampleCache(Sample newSample) {
        String NHC = newSample.getNHC();
        Sample oldSample = cache.getOrDefault(NHC, null);
        if (oldSample == null || oldSample.getEpisode() < newSample.getEpisode()) {
            cache.put(NHC, newSample);
        }
    }

    private static boolean isNumeric(String s) {
        return s.length() > 0 && s.chars().allMatch(Character::isDigit);
    }
}
