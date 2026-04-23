package fun.datamigration.migration.tablelog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.datamigration.migration.tablelog.entity.MigrationTableLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MigrationTableLogMapper extends BaseMapper<MigrationTableLog> {
}
