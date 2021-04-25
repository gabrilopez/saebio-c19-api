package org.saebio.requesthandler.backup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupApiConstants;
import org.saebio.backup.BackupService;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler.exception.InvalidRequestBodyObjectException;
import org.saebio.requesthandler.exception.RequestBodyObjectNotFoundException;
import org.saebio.sample.SampleService;

import java.util.Map;

public class RestoreBackupHandler extends AbstractRequestHandler<Backup> {
    public RestoreBackupHandler() {
        super(Backup.class);
    }


    @Override
    protected Answer processImpl(Backup value, Map<String, String> queryParams) throws AbstractRequestException {
        if (!value.isValid()) {
            throw new InvalidRequestBodyObjectException();
        }

        if (BackupService.backupExists(value)) {
            if (BackupService.changeDatabaseToBackup(value)) {
                SampleService.clearCache();
                return new Answer(BackupApiConstants.SUCCESSFULLY_CHANGED_DATABASE_TO_BACKUP + " " + value.getName(), HttpStatus.OK());
            }
        } else {
            throw new RequestBodyObjectNotFoundException();
        }

        return new Answer(BackupApiConstants.ERROR_FAILED_REPLACE_DATABASE_WITH_BACKUP, HttpStatus.BadRequest());
    }
}
