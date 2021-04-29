package org.saebio.requesthandler._backup;

import org.saebio.api._utils.Answer;
import org.saebio.api._utils.UnparsedRequestBody;
import org.saebio.api._utils.HttpStatus;
import org.saebio.api._utils._answers.InternalErrorAnswer;
import org.saebio.requesthandler.AbstractRequestHandler;
import org.saebio.utils.BackupModel;

import java.util.Map;

public class CreateBackupHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public CreateBackupHandler(BackupModel backupModel) {
        super(UnparsedRequestBody.class, backupModel);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) {
        boolean success = backupModel.createBackupHandler();
        if (success) {
            return new Answer("Backup created", HttpStatus.Created);
        } else {
            return new InternalErrorAnswer();
        }
    }
}
