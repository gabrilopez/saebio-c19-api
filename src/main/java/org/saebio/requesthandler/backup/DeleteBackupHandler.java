package org.saebio.requesthandler.backup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupApiConstants;
import org.saebio.backup.BackupService;
import org.saebio.requesthandler.AbstractRequestHandler;

import java.util.Map;

public class DeleteBackupHandler extends AbstractRequestHandler<Backup> {

    public DeleteBackupHandler() {
        super(Backup.class);
    }

    @Override
    protected Answer processImpl(Backup value, Map<String, String> queryParams) {
        if (!BackupService.backupExists(value)) {
            return new Answer(BackupApiConstants.ERROR_BACKUP_DOES_NOT_EXIST, HttpStatus.BadRequest());
        }

        if (BackupService.removeBackup(value)) {
            JsonElement jsonElement = new Gson().toJsonTree(BackupService.getBackups());
            return new Answer(BackupApiConstants.SUCCESSFULLY_REMOVED_BACKUP, jsonElement, HttpStatus.OK());
        }

        return new Answer(BackupApiConstants.ERROR_REMOVING_BACKUP, HttpStatus.InternalError());
    }
}
