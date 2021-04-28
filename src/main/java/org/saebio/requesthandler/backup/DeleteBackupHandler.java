package org.saebio.requesthandler.backup;

import org.saebio.api.Answer;
import org.saebio.api.HttpStatus;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupApiConstants;
import org.saebio.backup.BackupService;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler.exception.InvalidRequestBodyObjectException;
import org.saebio.requesthandler.exception.RequestBodyObjectNotFoundException;
import org.saebio.utils.BackupModel;

import java.util.Map;

public class DeleteBackupHandler extends AbstractRequestHandler<Backup> {

    public DeleteBackupHandler(BackupModel backupModel) {
        super(Backup.class, backupModel);
    }

    @Override
    protected Answer processImpl(Backup value, Map<String, String> queryParams) throws AbstractRequestException {
        if (value == null || !value.isValid()) {
            throw new InvalidRequestBodyObjectException();
        }

        if (!backupModel.backupExists(value)) {
            throw new RequestBodyObjectNotFoundException();
        }

        if (backupModel.removeBackup(value)) {
            return new Answer(BackupApiConstants.SUCCESSFULLY_REMOVED_BACKUP, HttpStatus.OK());
        }

        return new Answer(BackupApiConstants.ERROR_REMOVING_BACKUP, HttpStatus.InternalError());
    }
}
