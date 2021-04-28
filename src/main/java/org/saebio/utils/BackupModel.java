package org.saebio.utils;

import org.saebio.backup.Backup;

import java.util.Collection;

public interface BackupModel {
    Collection<Backup> getBackups();

    boolean restoreBackup(Backup backup);

    boolean removeBackup(Backup backup);

    boolean backupExists(Backup backup);

    boolean removeOldestBackup();

    boolean createBackupHandler();
}
