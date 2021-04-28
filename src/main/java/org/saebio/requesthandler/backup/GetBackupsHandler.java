package org.saebio.requesthandler.backup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.saebio.api.Answer;
import org.saebio.api.UnparsedRequestBody;
import org.saebio.api.HttpStatus;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupApiConstants;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.utils.BackupModel;

import java.util.Collection;
import java.util.Map;

public class GetBackupsHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public GetBackupsHandler(BackupModel backupModel) {
        super(UnparsedRequestBody.class, backupModel);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) {
        Collection<Backup> backups = backupModel.getBackups();
        JsonElement jsonElement = new Gson().toJsonTree(backups);
        return new Answer(backups.size() + " " + BackupApiConstants.FILES_FOUND, jsonElement, HttpStatus.OK());
    }
}
