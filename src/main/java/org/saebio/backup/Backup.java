package org.saebio.backup;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class Backup {
    String createdAt;
    String name;
    String size;

    public Backup(File file) {
        this.name = file.getName();
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            this.size = FileUtils.byteCountToDisplaySize(basicFileAttributes.size());
            this.createdAt = basicFileAttributes.creationTime().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
