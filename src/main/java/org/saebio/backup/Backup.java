package org.saebio.backup;

import org.apache.commons.io.FileUtils;
import org.saebio.sample.SampleService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class Backup {
    private transient final File backupFile;
    String createdAt;
    String name;
    String size;
    int rows;
    Boolean selected;

    public Backup(File file) {
        this.backupFile = file;
        this.name = file.getName();
        this.selected = this.name.equals(SampleService.getDatabaseFileName());
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            this.size = FileUtils.byteCountToDisplaySize(basicFileAttributes.size());
            this.createdAt = basicFileAttributes.creationTime().toString();
            int separatorSpaceIndex = name.indexOf(' ');
            if (separatorSpaceIndex > 0) {
                String rows = this.name.substring(0, separatorSpaceIndex);
                if (isNumeric(rows)) this.rows = Integer.parseInt(rows);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public File getBackupFile() {
        return backupFile;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        final Backup backup = (Backup) obj;
        return (backup.name.equals((this.name))
                && backup.size.equals(this.size)
                && backup.createdAt.equals(this.createdAt)
                && backup.selected.equals(this.selected));
    }

    private static boolean isNumeric(String s) {
        return s.length() > 0 && s.chars().allMatch(Character::isDigit);
    }
}
