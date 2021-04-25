package org.saebio.requesthandler.backup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.saebio.api.Answer;
import org.saebio.api.UnparsedRequestBody;
import org.saebio.api.HttpStatus;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupApiConstants;
import org.saebio.backup.BackupService;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.sample.SampleService;

import java.util.Collection;
import java.util.Map;

public class GetBackupsHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public GetBackupsHandler() {
        super(UnparsedRequestBody.class);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) {
        Collection<Backup> backups = BackupService.getBackups();
        SampleService sampleService = new SampleService(); // este sería mi model
        if (sampleService.tryConnection()) {
            int currentDatabaseNumberOfRows = sampleService.getRowCount();
            backups.forEach(backup -> {
                if (backup.getSelected()) backup.setRows(currentDatabaseNumberOfRows); // refactor del nombre getSelected
            });
        }
        JsonElement jsonElement = new Gson().toJsonTree(backups);
        return new Answer(backups.size() + " " + BackupApiConstants.FILES_FOUND, jsonElement, HttpStatus.OK());
    }
}
