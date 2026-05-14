package fun.datamigration.migration;

import fun.datamigration.data.service.DataMigrationService;
import fun.datamigration.file.service.FileMigrationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MigrationExecutor implements ApplicationRunner {
    private FileMigrationService fileMigrationService;
    private DataMigrationService dataMigrationService;

    @Resource
    private ApplicationContext applicationContext;

    @Getter
    @Setter
    @Value("${migration.task-type:file}")
    private String taskType;

    @Getter
    @Setter
    @Value("${migration.file-migration.origin-platform}")
    private String originPlatform;

    @Getter
    @Setter
    @Value("${migration.file-migration.destination-platform}")
    private String destinationPlatform;

    @Override
    public void run(ApplicationArguments args) {
        try {
            if ("file".equals(taskType) || "all".equals(taskType)) {
                fileMigrationService.fileMigration(originPlatform, destinationPlatform);
            }
            if ("data".equals(taskType) || "all".equals(taskType)) {
                dataMigrationService.dataMigration();
            }
        } finally {
            System.out.println("准备退出应用...");
        }
        int exitCode = SpringApplication.exit(applicationContext);
        System.exit(exitCode);
    }

    @Autowired
    public void setFileMigrationService(FileMigrationService fileMigrationService) {
        this.fileMigrationService = fileMigrationService;
    }

    @Autowired
    public void setDataMigrationService(DataMigrationService dataMigrationService) {
        this.dataMigrationService = dataMigrationService;
    }
}
