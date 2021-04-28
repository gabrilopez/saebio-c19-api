package org.saebio.requesthandler.backup;

import org.saebio.api.Answer;
import org.saebio.api.UnparsedRequestBody;
import org.saebio.api.HttpStatus;
import org.saebio.backup.Backup;
import org.saebio.backup.BackupApiConstants;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.utils.BackupModel;

import java.util.Collection;
import java.util.Map;

public class CreateBackupHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public CreateBackupHandler(BackupModel backupModel) {
        super(UnparsedRequestBody.class, backupModel);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) {
        boolean success = backupModel.createBackupHandler();
        if (success) {
            return new Answer(BackupApiConstants.SUCCESSFULLY_GENERATED_BACKUP, HttpStatus.Created());
        } else {
            return new Answer(BackupApiConstants.ERROR_GENERATING_BACKUP, HttpStatus.InternalError());
        }
    }
}
