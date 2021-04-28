package org.saebio.sample;

import org.saebio.utils.DatabaseModel;
import org.saebio.utils.Utils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SampleService {
    private static final Map<String, Sample> cache = new HashMap<>();
    private final DatabaseModel databaseModel;

    public SampleService (DatabaseModel databaseModel) {
        this.databaseModel = databaseModel;
    }

    public Sample handleSampleLine(String line) {
        String[] data = line.split(";");
        return createSampleFromLine(data);
    }

    private Sample createSampleFromLine(String[] line) {
        // Length 8 mínima por ahora porque los campos resultadoTMA, sexo, edad, procedencia y motivo
        // pueden no estar seteados
        if (line.length < 9) return null;
        Sample sample = new Sample();
        try {
            sample.setRegistryDate(LocalDate.parse(line[0].split(" ")[0], Utils.dateTimeFormatter));
            sample.setPatientName(line[1]);
            sample.setPatientSurname(line[2]);
            sample.setBirthDate(LocalDate.parse(line[3], Utils.dateTimeFormatter));
            sample.setNHC(line[4]);
            sample.setPetition(Integer.parseInt(line[5]));
            sample.setService(line[6]);
            sample.setCriteria(line[7]);
            if (!line[8].trim().isEmpty()) sample.setResultPCR(line[8]);

            // El hospital doctor negrín está trabajando en implementar estos campos
            if (line.length > 9 && !line[9].trim().isEmpty()) sample.setResultTMA(line[9]);
            if (line.length > 10) sample.setSex(!line[10].trim().isEmpty() ? line[10] : null);
            if (line.length > 11) sample.setAge(Utils.isNumeric(line[11]) ? Integer.valueOf(line[11]) : null);
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

    private int getSampleEpisodeNumber(Sample newSample) {
        // Old sample will always be the first sample from current episode
        String NHC = newSample.getNHC();
        Sample oldSample = cache.getOrDefault(NHC, null);

        if (oldSample == null) {
            oldSample = this.databaseModel.getFirstSampleFromCurrentEpisode(NHC);
            if (oldSample == null) return 1;
            cache.put(NHC, oldSample);
        }
        return newSample.belongToSameEpisode(oldSample) ? oldSample.getEpisode() : oldSample.getEpisode() + 1;
    }

    private void updateSampleCache(Sample newSample) {
        String NHC = newSample.getNHC();
        Sample oldSample = cache.getOrDefault(NHC, null);
        if (oldSample == null || oldSample.getEpisode() < newSample.getEpisode()) {
            cache.put(NHC, newSample);
        }
    }

    public static void clearCache() {
        cache.clear();
    }
}