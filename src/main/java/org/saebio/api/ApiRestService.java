package org.saebio.api;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import org.saebio.sample.Sample;
import org.saebio.sample.SampleService;
import spark.Filter;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static spark.Spark.*;

public class ApiRestService {
    private static DateTimeFormatter birthDateFormatter = DateTimeFormatter
            .ofPattern("dd.MM.yyyy")
            .withLocale(Locale.ENGLISH);
    private static Map<String, Sample> cache = new HashMap<>();

    public static void main(String[] args) {
        // Filter after each request
        after((Filter) (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "POST");
            res.type("application/json");
        });

        post("/insert-data", (req, res)-> {
            String body = req.body();
            CSVReader reader = new CSVReader(new StringReader(body));

            int cont = 0;
            int errorCount = 0;
            SampleService sampleService = new SampleService();
            if (!sampleService.tryConnection()) {
                String gson = new Gson().toJson(new Response(HttpStatus.InternalError(), "Could not connect to database"));
                System.out.println(gson);
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
                if (cont == 16374) break;   // TODO: BORRAR!
                cont++;
            }

            String message = "[FINISHED]\nRead " + reader.getLinesRead() + "\tError count: " + errorCount;
            System.out.println(message);
            System.out.println("FINAL CACHE SIZE:" + cache.size());
            System.gc();

            return new Gson()
                    .toJsonTree(new Response(HttpStatus.OK(), message));
        });
    }

    private static Sample createSampleFromLine(String[] line) {
        if (line.length < 16) return null;
        Sample sample = new Sample();
        try {
            sample.setPetition(Integer.parseInt(line[0]));
            sample.setRegistryDate(LocalDate.parse(line[1]));
            sample.setHospital(line[2]);
            sample.setHospitalService(line[3]);
            sample.setDestination(line[4]);
            sample.setPrescriptor(line[5]);
            sample.setNHC(line[6]);
            sample.setPatient(line[7]);
            sample.setSex(line[8]);
            sample.setAge(Integer.parseInt(line[9]));
            sample.setBirthDate(LocalDate.parse(line[10], birthDateFormatter));
            sample.setMonth(Integer.parseInt(line[11]));
            sample.setYear(Integer.parseInt(line[12]));
            sample.setType(line[13]);
            sample.setResult(line[14]);
            sample.setEpisode(getSampleEpisodeNumber(sample));
        } catch(Exception e) {
            return null;
        }
        cache.put(sample.getNHC(), sample);
        return sample;
    }

    private static int getSampleEpisodeNumber(Sample newSample) {
        String NHC = newSample.getNHC();
        Sample oldSample;
        if (cache.containsKey(NHC)) {
            oldSample = cache.get(NHC);
        } else {
            SampleService sampleService = new SampleService();
            oldSample = sampleService.getLastSampleForNHC(NHC);
            if (oldSample == null) return 1;
        }
        return newSample.belongToSameEpisode(oldSample) ? oldSample.getEpisode() : oldSample.getEpisode() + 1;
    }
}
