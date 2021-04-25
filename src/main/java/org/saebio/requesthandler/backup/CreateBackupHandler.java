package org.saebio.requesthandler.backup;

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

public class CreateBackupHandler extends AbstractRequestHandler<UnparsedRequestBody> {

    public CreateBackupHandler() {
        super(UnparsedRequestBody.class);
    }

    @Override
    protected Answer processImpl(UnparsedRequestBody value, Map<String, String> queryParams) {
        SampleService sampleService = new SampleService(); // sustituir por model
        boolean success = sampleService.vacuumInto();
        if (success) {
            // If backups > 14, remove oldest backup
            Collection<Backup> backups = BackupService.getBackups();
            if (backups.size() > 14) BackupService.removeOldestBackup();
            
            return new Answer(BackupApiConstants.SUCCESSFULLY_GENERATED_BACKUP, HttpStatus.Created());
        } else {
            return new Answer(BackupApiConstants.ERROR_GENERATING_BACKUP, HttpStatus.InternalError());
        }
    }
}
