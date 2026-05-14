package fun.datamigration.migration.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import fun.datamigration.migration.config.entity.MigrationConfig;
import fun.datamigration.migration.config.entity.MigrationConfigVO;
import fun.datamigration.migration.config.mapper.MigrationConfigMapper;
import fun.datamigration.migration.constant.FieldDataType;
import fun.datamigration.migration.tablelog.service.MigrationTableLogService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MigrationConfigServiceImplTest {

    @Test
    void getAllNeedMigrationConfigsOnlyLoadsRequestedFieldDataType() {
        MigrationConfig fileConfig = config("file-1", "source1", "file_table", "FILE_PATH", "file_path", FieldDataType.FILE);
        MigrationConfig dataConfig = config("data-1", "source1", "user_old", "name", "username", FieldDataType.DATA);

        MigrationConfigMapper configMapper = mock(MigrationConfigMapper.class);
        when(configMapper.selectList(any())).thenAnswer(invocation -> {
            QueryWrapper<MigrationConfig> wrapper = invocation.getArgument(0);
            String sqlSegment = wrapper.getSqlSegment();
            if (sqlSegment != null && sqlSegment.contains("field_data_type")) {
                return Collections.singletonList(dataConfig);
            }
            return Arrays.asList(fileConfig, dataConfig);
        });

        MigrationTableLogService tableLogService = mock(MigrationTableLogService.class);
        when(tableLogService.list()).thenReturn(Collections.emptyList());

        MigrationConfigServiceImpl service = new MigrationConfigServiceImpl();
        service.setMigrationConfigMapper(configMapper);
        service.setMigrationTableLogService(tableLogService);

        List<MigrationConfigVO> configs = service.getAllNeedMigrationConfigs(FieldDataType.DATA);

        assertThat(configs).hasSize(1);
        assertThat(configs.get(0).getTableName()).isEqualTo("user_old");
        assertThat(configs.get(0).getColumnNames()).isEqualTo("name");
    }

    private static MigrationConfig config(String id, String dataSource, String tableName, String columnName,
                                          String mappingColumnName, FieldDataType type) {
        MigrationConfig config = new MigrationConfig();
        config.setId(id);
        config.setDataSource(dataSource);
        config.setTableName(tableName);
        config.setColumnName(columnName);
        config.setMappingColumnName(mappingColumnName);
        config.setFieldDataType(type.getCode());
        return config;
    }
}
