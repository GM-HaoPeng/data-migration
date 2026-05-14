package fun.datamigration.data.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import fun.datamigration.data.entity.MigrationDataConfig;
import fun.datamigration.data.mapper.MigrationDataMapper;
import fun.datamigration.data.service.DataMigrationService;
import fun.datamigration.migration.config.entity.MigrationConfigVO;
import fun.datamigration.migration.config.service.MigrationConfigService;
import fun.datamigration.migration.constant.FieldDataType;
import fun.datamigration.migration.constant.MigrationStatus;
import fun.datamigration.migration.datalog.entity.MigrationDataLog;
import fun.datamigration.migration.datalog.service.MigrationDataLogService;
import fun.datamigration.migration.tablelog.entity.MigrationTableLog;
import fun.datamigration.migration.tablelog.service.MigrationTableLogService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataMigrationServiceImpl implements DataMigrationService {
    private MigrationConfigService migrationConfigService;
    private MigrationDataMapper migrationDataMapper;
    private MigrationDataLogService migrationDataLogService;
    private MigrationTableLogService migrationTableLogService;

    @Getter
    @Setter
    @Value("${migration.mode}")
    private String mode;

    @Getter
    @Setter
    @Value("${migration.batch-size:1000}")
    private Integer batchSize;

    @Override
    public void dataMigration() {
        List<MigrationConfigVO> configs = migrationConfigService.getAllNeedMigrationConfigs(FieldDataType.DATA);
        for (MigrationConfigVO configVO : configs) {
            migrateConfig(toDataConfig(configVO));
        }
    }

    private void migrateConfig(MigrationDataConfig config) {
        int offset = 0;
        boolean hasFailure = false;
        while (true) {
            config.setOffset(offset);
            config.setBatchSize(batchSize);
            List<Map<String, Object>> rows = selectSourceRows(config);
            if (rows.isEmpty()) {
                if (!hasFailure) {
                    saveTableLogs(config);
                }
                return;
            }
            hasFailure = migrateRows(config, rows) || hasFailure;
            log.info("已迁移 {} 到 {} 的第 {} 至 {} 条数据", config.getSourceTableName(),
                    config.getTargetTableName(), offset + 1, offset + rows.size());
            if ("test".equals(mode) || rows.size() < batchSize) {
                if (!hasFailure) {
                    saveTableLogs(config);
                }
                return;
            }
            offset += batchSize;
        }
    }

    private boolean migrateRows(MigrationDataConfig config, List<Map<String, Object>> rows) {
        try {
            insertTargetRows(config, rows);
            saveDataLogs(config, rows, MigrationStatus.SUCCESS, MigrationStatus.SUCCESS.getDesc());
            return false;
        } catch (Exception batchException) {
            log.warn("批量写入失败，切换为逐行写入。源表：{}，目标表：{}，原因：{}", config.getSourceTableName(),
                    config.getTargetTableName(), batchException.getMessage());
            boolean hasFailure = false;
            for (Map<String, Object> row : rows) {
                List<Map<String, Object>> singleRow = new ArrayList<>();
                singleRow.add(row);
                try {
                    insertTargetRows(config, singleRow);
                    saveDataLogs(config, singleRow, MigrationStatus.SUCCESS, MigrationStatus.SUCCESS.getDesc());
                } catch (Exception rowException) {
                    hasFailure = true;
                    saveDataLogs(config, singleRow, MigrationStatus.FAILED, rowException.getMessage());
                    log.error("单行数据迁移失败，源表：{}，目标表：{}，主键：{}，原因：{}", config.getSourceTableName(),
                            config.getTargetTableName(), row.get(config.getTargetPrimaryKey()), rowException.getMessage());
                }
            }
            return hasFailure;
        }
    }

    private List<Map<String, Object>> selectSourceRows(MigrationDataConfig config) {
        DynamicDataSourceContextHolder.push(config.getSourceDataSource());
        try {
            return migrationDataMapper.selectSourceRows(config);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    private void insertTargetRows(MigrationDataConfig config, List<Map<String, Object>> rows) {
        DynamicDataSourceContextHolder.push(config.getTargetDataSource());
        try {
            migrationDataMapper.insertTargetRows(config, rows);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    private void saveDataLogs(MigrationDataConfig config, List<Map<String, Object>> rows,
                              MigrationStatus status, String result) {
        List<MigrationDataLog> logs = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            MigrationDataLog log = new MigrationDataLog();
            log.setConfigId(config.getConfigId());
            log.setData(config.getSourceTableName() + "." + config.getSourcePrimaryKey() + "="
                    + row.get(config.getTargetPrimaryKey()));
            log.setStatus(status);
            log.setResult(result);
            log.setCreateTime(new Date());
            logs.add(log);
        }
        migrationDataLogService.saveOrUpdateBatch(logs);
    }

    private void saveTableLogs(MigrationDataConfig config) {
        List<MigrationTableLog> logs = new ArrayList<>();
        for (String sourceColumn : config.getSourceColumnNames()) {
            MigrationTableLog tableLog = new MigrationTableLog();
            tableLog.setDataSource(config.getSourceDataSource());
            tableLog.setTableName(config.getSourceTableName());
            tableLog.setColumnName(sourceColumn);
            tableLog.setCreateTime(new Date());
            logs.add(tableLog);
        }
        migrationTableLogService.saveOrUpdateBatch(logs);
    }

    private MigrationDataConfig toDataConfig(MigrationConfigVO configVO) {
        List<String> sourceColumns = splitColumns(configVO.getColumnNames());
        List<String> targetColumns = sourceColumns.stream()
                .map(sourceColumn -> configVO.getMappingColumnNames().get(sourceColumn))
                .collect(Collectors.toList());
        Map<String, String> mapping = new LinkedHashMap<>();
        for (int i = 0; i < sourceColumns.size(); i++) {
            mapping.put(sourceColumns.get(i), targetColumns.get(i));
        }

        MigrationDataConfig config = new MigrationDataConfig();
        config.setConfigId(configVO.getColumnIdMap().values().stream().findFirst().orElse(null));
        config.setSourceDataSource(configVO.getDataSource());
        config.setTargetDataSource(configVO.getTargetDataSource());
        config.setSourceTableName(configVO.getTableName());
        config.setTargetTableName(configVO.getTargetTableName());
        config.setSourcePrimaryKey(configVO.getSourcePrimaryKey());
        config.setTargetPrimaryKey(configVO.getTargetPrimaryKey());
        config.setWhereClause(configVO.getWhereClause());
        config.setSourceColumnNames(sourceColumns);
        config.setTargetColumnNames(targetColumns);
        config.setSourceToTargetColumnMap(mapping);
        return config;
    }

    private List<String> splitColumns(String columnNames) {
        return Arrays.stream(columnNames.split(","))
                .map(String::trim)
                .filter(column -> !column.isEmpty())
                .collect(Collectors.toList());
    }

    @Autowired
    public void setMigrationConfigService(MigrationConfigService migrationConfigService) {
        this.migrationConfigService = migrationConfigService;
    }

    @Autowired
    public void setMigrationDataMapper(MigrationDataMapper migrationDataMapper) {
        this.migrationDataMapper = migrationDataMapper;
    }

    @Autowired
    public void setMigrationDataLogService(MigrationDataLogService migrationDataLogService) {
        this.migrationDataLogService = migrationDataLogService;
    }

    @Autowired
    public void setMigrationTableLogService(MigrationTableLogService migrationTableLogService) {
        this.migrationTableLogService = migrationTableLogService;
    }
}
