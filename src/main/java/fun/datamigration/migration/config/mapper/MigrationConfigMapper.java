package fun.datamigration.migration.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.datamigration.migration.config.entity.MigrationConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MigrationConfigMapper extends BaseMapper<MigrationConfig> {
}
