package org.saebio.requesthandler._backup;

import org.saebio.api._utils.Answer;
import org.saebio.api._utils._answers.InternalErrorAnswer;
import org.saebio.api._utils._answers.SuccessAnswer;
import org.saebio.backup.Backup;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.requesthandler.exception.AbstractRequestException;
import org.saebio.requesthandler.exception.InvalidRequestBodyObjectException;
import org.saebio.requesthandler.exception.RequestBodyObjectNotFoundException;
import org.saebio.sample.SampleService;
import org.saebio.utils.BackupModel;

import java.util.Map;

public class RestoreBackupHandler extends AbstractRequestHandler<Backup> {
    public RestoreBackupHandler(BackupModel backupModel) {
        super(Backup.class, backupModel);
    }


    @Override
    protected Answer processImpl(Backup value, Map<String, String> queryParams) throws AbstractRequestException {
        if (value == null || !value.isValid()) {
            throw new InvalidRequestBodyObjectException();
        }

        if (backupModel.backupExists(value)) {
            if (backupModel.restoreBackup(value)) {
                SampleService.clearCache();
                return new SuccessAnswer("Successfully restored backup " + value.getName());
            }
        } else {
            throw new RequestBodyObjectNotFoundException();
        }

        return new InternalErrorAnswer();
    }
}
