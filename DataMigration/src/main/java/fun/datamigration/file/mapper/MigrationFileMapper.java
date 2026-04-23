package fun.datamigration.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.datamigration.file.SqlProvider.MigrationFileMapperProvider;
import fun.datamigration.file.entity.MigrationFile;
import fun.datamigration.migration.config.entity.MigrationConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface MigrationFileMapper extends BaseMapper<MigrationFile> {
    @SelectProvider(type = MigrationFileMapperProvider.class, method = "getMigrationFiles")
    List<MigrationFile> getMigrationFiles(MigrationConfigVO migrationConfigVO);
}
