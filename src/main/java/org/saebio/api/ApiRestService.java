package org.saebio.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.saebio.backup.Backup;
import org.saebio.sample.Sample;
import org.saebio.sample.SampleService;
import spark.Filter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
        after((Filter) (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "POST");
            res.type("application/json");
            System.gc();
        });

        post("/insert-data", (req, res) -> {
            String body = req.body();
            CSVReader reader = new CSVReader(new StringReader(body));

            int errorCount = 0;
            SampleService sampleService = new SampleService();
            if (!sampleService.tryConnection()) {
                res.status(HttpStatus.InternalError());
                return new Gson()
                        .toJson(new Response(HttpStatus.InternalError(), "Could not connect to database"));
            }

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
        });

        get("/backups", (req, res) -> {
            Collection<Backup> backups = getBackups().stream()
                                            .map(Backup::new)
                                            .collect(Collectors.toList());
            JsonElement jsonElement = new Gson().toJsonTree(backups);
            return new Gson().toJsonTree(new Response(HttpStatus.OK(), backups.size() + " files found", jsonElement));
        });
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
            sample.setEpisode(getSampleEpisodeNumber(sample));
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        updateSampleCache(sample);
        return sample;
    }

    private static int getSampleEpisodeNumber(Sample newSample) {
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

    private static void createBackup(String fileName) {
        String databaseRoute = SampleService.getDatabaseRoute();
        File source = new File (databaseRoute + SampleService.getDatabaseFileName());
        File destination = new File (databaseRoute + fileName + ".db");
        try {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Collection<File> getBackups() {
        String route = SampleService.getDatabaseRoute();
        File directory = new File(route);
        String[] suffixFileFilter = new String[] {"db"};
        return FileUtils.listFiles(directory, suffixFileFilter, true);
    }
}
