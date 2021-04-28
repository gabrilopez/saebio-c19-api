package org.saebio.requesthandler.sample;

import com.google.gson.Gson;
import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;
import org.saebio.api.UnparsedRequestBody;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler.exception.InvalidRequestFormDataException;
import org.saebio.sample.Sample;
import org.saebio.sample.SampleApiConstants;
import org.saebio.sample.SampleService;
import org.saebio.utils.BackupModel;
import org.saebio.utils.DatabaseModel;

import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class AddSamplesRequestHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public AddSamplesRequestHandler(DatabaseModel databaseModel, BackupModel backupModel) {
        super(UnparsedRequestBody.class, databaseModel, backupModel);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) throws AbstractRequestException {
        Part filePart = this.requestParts.get("file");
        InputStream inputStream;

        try {
            inputStream = filePart.getInputStream();
        } catch (IOException | NullPointerException e) {
            throw new InvalidRequestFormDataException();
        }

        if (!databaseModel.testConnection()) {
            return new Answer(SampleApiConstants.ERROR_CONNECTING_TO_DATABASE, HttpStatus.InternalError());
        }

        Stream<String> stream = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().skip(1);

        int count = 0;
        int errorCount = 0;
        HashMap<String, String> errorLines = new HashMap<>();
        SampleService sampleService = new SampleService(databaseModel);
        for (String line : (Iterable<String>) stream::iterator) {
            Sample sample = sampleService.handleSampleLine(line);
            if (sample == null) {
                errorLines.put("rowFormatError", errorLines.getOrDefault("rowFormatError", "") + (count + 1) + ", ");
            } else {
                DatabaseModel.InsertStatus status = databaseModel.addSample(sample);
                switch(status) {
                    case SAMPLE_ALREADY_EXISTS:
                        errorCount++;
                        errorLines.put("alreadyExistingSamples", errorLines.getOrDefault("alreadyExistingSamples", "") + (count + 1) + ", ");
                        break;
                    case SAMPLE_INSERT_ERROR:
                        errorCount++;
                        errorLines.put("insertError", errorLines.getOrDefault("insertError", "") + (count + 1) + ", ");
                        break;
                    default:
                        break;
                }
            }
            count++;
        }
        stream.close();

        int added = count - errorCount;
        if (added > 0) backupModel.createBackupHandler();

        Map<String, Object> response = new HashMap<>();
        response.put("size", count);
        response.put("added", added);
        response.put("errors", errorCount);
        response.put("errorLines", errorLines);
        return new Answer("Success!", new Gson().toJsonTree(response), HttpStatus.OK());
    }
}
