package org.saebio.backup;

import org.apache.commons.io.FileUtils;
import org.saebio.utils.BackupModel;
import org.saebio.utils.SqliteModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

public class BackupService implements BackupModel {
    private String backupsRoute;
    private SqliteModel sqliteModel;
    // debe llegar el path!!! no el archivo que se pasa por consola !!!!
    public BackupService(String backupsRoute, SqliteModel sqliteModel) {
        this.backupsRoute = backupsRoute;
        this.sqliteModel = sqliteModel;
    }

    public Collection<Backup> getBackups() {
        String route = SqliteModel.getDatabaseRoute();
        File directory = new File(route);
        String[] suffixFileFilter = new String[] {"db"};
        Collection<File> fileList = FileUtils.listFiles(directory, suffixFileFilter, true);
        Collection<Backup> backupList = fileList.stream()
                .map(Backup::new)
                .collect(Collectors.toList());

        // Set current .db file number of rows
        if (sqliteModel.testConnection()) {
            int currentDatabaseNumberOfRows = sqliteModel.getRowCount();
            backupList.forEach(backup -> {
                if (backup.getSelected()) backup.setRows(currentDatabaseNumberOfRows);
            });
        }
        return sort(backupList);
    }

    // prev: changeDatabaseToBackup
    public boolean restoreBackup(Backup backup) {
        SqliteModel.closeConnection();
        // Path from = new File(SampleService.getDatabaseRoute() + "backups/" + backup.getName()).toPath();
        Path from = new File(backupsRoute + backup.getName()).toPath();
        // Path to = new File(SampleService.getDatabaseRoute() + SampleService.getDatabaseFileName()).toPath();
        Path to = new File(SqliteModel.getDatabaseRoute() + SqliteModel.getDatabaseFileName()).toPath();
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBackup(Backup backup) {
        if (backup.selected) return false;
        File backupFile = new File(backupsRoute + backup.getName());
        return FileUtils.deleteQuietly(backupFile);
    }

    public boolean backupExists(Backup backup) {
        Collection<Backup> backups = getBackups();
        return (backups.stream().anyMatch(b -> b.equals(backup)));
    }

    public boolean removeOldestBackup() {
        Collection<Backup> backups = getBackups();
        return removeBackup((Backup) backups.toArray()[backups.size() - 1]);
    }

    @Override
    public boolean createBackupHandler() {
        boolean createdBackup = createBackup();
        if (createdBackup && maximumNumberOfBackupsSurpassed()) removeOldestBackup();
        return createdBackup;
    }

    private boolean createBackup() {
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH.mm.ss").format(new Date());
        String route = backupsRoute + sqliteModel.getRowCount() + " " + timeStamp + ".db";
        return sqliteModel.vacuumInto(route);
    }

    private Collection<Backup> sort(Collection<Backup> backupList) {
        return backupList.stream().sorted((backup1, backup2) -> {
            if (backup1.selected) return -1;
            if (backup2.selected) return 1;
            Instant instant1 = Instant.parse(backup1.createdAt);
            Instant instant2 = Instant.parse(backup2.createdAt);
            return instant2.compareTo(instant1);
        }).collect(Collectors.toList());
    }

    private boolean maximumNumberOfBackupsSurpassed() {
        return this.getBackups().size() > 14;
    }
}
