package fun.datamigration.migration.config.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.datamigration.migration.config.entity.MigrationConfig;
import fun.datamigration.migration.config.entity.MigrationConfigVO;
import fun.datamigration.migration.constant.FieldDataType;

import java.util.List;

public interface MigrationConfigService extends IService<MigrationConfig> {
    List<MigrationConfigVO> getAllNeedMigrationConfigs(FieldDataType fieldDataType);
}
