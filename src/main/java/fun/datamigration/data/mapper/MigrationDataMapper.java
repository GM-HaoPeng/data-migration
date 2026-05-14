package fun.datamigration.data.mapper;

import fun.datamigration.data.entity.MigrationDataConfig;
import fun.datamigration.data.sqlprovider.MigrationDataMapperProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

@Mapper
public interface MigrationDataMapper {
    @SelectProvider(type = MigrationDataMapperProvider.class, method = "selectSourceRows")
    List<Map<String, Object>> selectSourceRows(MigrationDataConfig config);

    @InsertProvider(type = MigrationDataMapperProvider.class, method = "insertTargetRowsFromParams")
    int insertTargetRows(@Param("config") MigrationDataConfig config, @Param("rows") List<Map<String, Object>> rows);
}
