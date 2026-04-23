package fun.datamigration.file.service;

import fun.datamigration.file.entity.MigrationFile;

import java.util.List;

public interface FileMigrationService {
    void fileMigration(String originPlatForm, String destinationPlatform);
    List<MigrationFile> getAllMigrationFiles();
}
