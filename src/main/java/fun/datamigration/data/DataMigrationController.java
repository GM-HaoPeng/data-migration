package fun.datamigration.data;

import fun.datamigration.data.service.DataMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class DataMigrationController {
    private DataMigrationService dataMigrationService;

    @RequestMapping("/migration")
    public void dataMigration() {
        dataMigrationService.dataMigration();
    }

    @Autowired
    public void setDataMigrationService(DataMigrationService dataMigrationService) {
        this.dataMigrationService = dataMigrationService;
    }
}
