package org.saebio.requesthandler.sample;

import com.google.gson.Gson;
import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;
import org.saebio.api.UnparsedRequestBody;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupService;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler.exception.InvalidRequestFormDataException;
import org.saebio.sample.Sample;
import org.saebio.sample.SampleApiConstants;
import org.saebio.sample.SampleService;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class AddSamplesRequestHandler extends AbstractRequestHandler<UnparsedRequestBody> {
    public AddSamplesRequestHandler() {
        super(UnparsedRequestBody.class);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) throws AbstractRequestException {
        Part filePart = this.requestParts.get("file");
        InputStream inputStream = null;
        try {
            inputStream = filePart.getInputStream();
        } catch (IOException | NullPointerException e) {
            throw new InvalidRequestFormDataException();
        }

        SampleService sampleService = new SampleService();
        if (!sampleService.tryConnection()) {
            // res.status(HttpStatus.InternalError());
            return new Answer(SampleApiConstants.ERROR_CONNECTING_TO_DATABASE, HttpStatus.InternalError());
        }


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
        // res.status(HttpStatus.OK());
        return new Answer("Success!", new Gson().toJsonTree(response), HttpStatus.OK());
    }
}
