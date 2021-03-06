package org.saebio.backup;

import org.apache.commons.io.FileUtils;
import org.saebio.sample.SampleService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class BackupService {
    public static Collection<Backup> getBackups() {
        String route = SampleService.getDatabaseRoute();
        File directory = new File(route);
        String[] suffixFileFilter = new String[] {"db"};
        Collection<File> fileList = FileUtils.listFiles(directory, suffixFileFilter, true);
        Collection<Backup> backupList = fileList.stream()
                .map(Backup::new)
                .collect(Collectors.toList());
        return sort(backupList);
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
        if (backup.selected) return false;
        File backupFile = new File(SampleService.getDatabaseRoute() + SampleService.getBackupsRoute() + backup.getName());
        return FileUtils.deleteQuietly(backupFile);
    }

    public static boolean backupExists(Backup backup) {
        Collection<Backup> backups = getBackups();
        return (backups.stream().anyMatch(b -> b.equals(backup)));
    }

    public static boolean removeOldestBackup() {
        Collection<Backup> backups = getBackups();
        return removeBackup((Backup) backups.toArray()[backups.size() - 1]);
    }

    private static Collection<Backup> sort(Collection<Backup> backupList) {
        return backupList.stream().sorted(new Comparator<Backup>() {
            @Override
            public int compare(Backup backup1, Backup backup2) {
                if (backup1.selected) return -1;
                if (backup2.selected) return 1;
                Instant instant1 = Instant.parse(backup1.createdAt);
                Instant instant2 = Instant.parse(backup2.createdAt);
                return instant2.compareTo(instant1);
            }
        }).collect(Collectors.toList());
    }
}
