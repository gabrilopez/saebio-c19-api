package org.saebio.backup;

import org.apache.commons.io.FileUtils;
import org.saebio.sample.SampleService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.stream.Collectors;

public class BackupService {
    public static void createBackup(String fileName) {
        String databaseRoute = SampleService.getDatabaseRoute();
        File source = new File (databaseRoute + SampleService.getDatabaseFileName());
        File destination = new File (databaseRoute + fileName + ".db");
        try {
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Collection<Backup> getBackups() {
        String route = SampleService.getDatabaseRoute();
        File directory = new File(route);
        String[] suffixFileFilter = new String[] {"db"};
        Collection<File> fileList = FileUtils.listFiles(directory, suffixFileFilter, true);
        return fileList.stream()
                .map(Backup::new)
                .collect(Collectors.toList());
    }

    public static boolean changeDatabaseToBackup(Backup backup) {
        SampleService.closeConnection();
        Path from = new File(SampleService.getDatabaseRoute() + "backups/" + backup.getName()).toPath();
        Path to = new File(SampleService.getDatabaseRoute() + SampleService.getDatabaseFileName()).toPath();
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeBackup(Backup backup) {
        File backupFile = new File(SampleService.getDatabaseRoute() + SampleService.getBackupsRoute() + backup.getName());
        return FileUtils.deleteQuietly(backupFile);
    }

    public static boolean backupExists(Backup backup) {
        Collection<Backup> backups = getBackups();
        return (backups.stream().anyMatch(b -> b.equals(backup)));
    }
}
