package fun.datamigration.file.controller;

import fun.datamigration.file.service.FileMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
public class FileMigrationController {
    private static final String FAST_DFS = "fastdfs-1";
    private static final String OSS_PLATFORM = "amazon-s3-1";
    private FileMigrationService fileMigrationService;
    @RequestMapping("/migration")
    public void fileMigration()
    {
        fileMigrationService.fileMigration(FAST_DFS, OSS_PLATFORM);
    }
    @Autowired
    public void setFileMigrationService(FileMigrationService fileMigrationService) {
        this.fileMigrationService = fileMigrationService;
    }
}
