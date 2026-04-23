package fun.datamigration.migration.config.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.datamigration.migration.config.entity.MigrationConfig;
import fun.datamigration.migration.config.entity.MigrationConfigVO;
import fun.datamigration.migration.config.mapper.MigrationConfigMapper;
import fun.datamigration.migration.config.service.MigrationConfigService;
import fun.datamigration.migration.constant.FieldDataType;
import fun.datamigration.migration.tablelog.entity.MigrationTableLog;
import fun.datamigration.migration.tablelog.service.MigrationTableLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MigrationConfigServiceImpl extends ServiceImpl<MigrationConfigMapper, MigrationConfig> implements MigrationConfigService {

    private MigrationConfigMapper migrationConfigMapper;
    private MigrationTableLogService migrationTableLogService;

    @Override
    public List<MigrationConfigVO> getAllNeedMigrationConfigs(FieldDataType fieldDataType) {
        QueryWrapper<MigrationConfig> queryWrapper = new QueryWrapper<>();
        if (fieldDataType != null){
            queryWrapper.eq("field_data_type", fieldDataType.getCode());
        }
        List<MigrationConfig> migrationConfigs = migrationConfigMapper.selectList(new QueryWrapper<>());
        List<MigrationTableLog> allMigrationTableLogs = migrationTableLogService.list();
        return initNeedMigrationConfig(migrationConfigs, allMigrationTableLogs);
    }

    private List<MigrationConfigVO> initNeedMigrationConfig(List<MigrationConfig> migrationConfigs,
                                                            List<MigrationTableLog> allMigrationTableLogs){
        Map<String, List<MigrationConfig>> allConfigMap = migrationConfigs.stream().collect(Collectors.groupingBy(t -> t.getDataSource() + t.getTableName()));
        Map<String, List<MigrationTableLog>> allTableLogMap = allMigrationTableLogs.stream().collect(Collectors.groupingBy(t -> t.getDataSource() + t.getTableName()));
        Map<String, List<MigrationConfig>> needMigrationConfigMap = new HashMap<>();
        for (Map.Entry<String, List<MigrationConfig>> configMap : allConfigMap.entrySet()) {
            String key = configMap.getKey();
            List<MigrationConfig> configs = configMap.getValue();
            // 没有该表的迁移记录，直接全部加入
            if (!allTableLogMap.containsKey( key)){
                needMigrationConfigMap.put(key, configs);
                continue;
            }
            // 有该表的迁移记录，则取出未迁移的字段，因此如果想重新迁移某个字段的数据，删除该数据源下，该字段的迁移记录，即可重新迁移该字段的数据
            List<MigrationConfig> needMigrationConfigs = new ArrayList<>();
            List<MigrationTableLog> migrationTableLogs = allTableLogMap.get(key);
            List<String> allLogFields = migrationTableLogs.stream().map(MigrationTableLog::getColumnName).collect(Collectors.toList());
            for (MigrationConfig config : configs) {
                if (!allLogFields.contains(config.getColumnName())){
                    needMigrationConfigs.add(config);
                }
            }
            needMigrationConfigMap.put(key, needMigrationConfigs);
        }
        List<MigrationConfigVO> migrationConfigVOS = new ArrayList<>();
        for (Map.Entry<String, List<MigrationConfig>> configMap : needMigrationConfigMap.entrySet()) {
            List<MigrationConfig> configs = configMap.getValue();
            if (CollectionUtil.isEmpty(configs)){
                continue;
            }
            MigrationConfigVO migrationConfigVO = new MigrationConfigVO();
            migrationConfigVO.setTableName(configs.get(0).getTableName());
            migrationConfigVO.setColumnNames(configs.stream().map(MigrationConfig::getColumnName).collect(Collectors.joining(",")));
            migrationConfigVO.setDataSource(configs.get(0).getDataSource());
            migrationConfigVO.setMappingColumnNames(configs.stream().collect(Collectors.toMap(MigrationConfig::getColumnName, MigrationConfig::getMappingColumnName)));
            migrationConfigVO.setColumnIdMap(configs.stream().collect(Collectors.toMap(MigrationConfig::getColumnName, MigrationConfig::getId)));
            migrationConfigVOS.add(migrationConfigVO);
        }
        return migrationConfigVOS;
    }

    @Autowired
    public void setMigrationConfigMapper(MigrationConfigMapper migrationConfigMapper) {
        this.migrationConfigMapper = migrationConfigMapper;
    }

    @Autowired
    public void setMigrationTableLogService(MigrationTableLogService migrationTableLogService) {
        this.migrationTableLogService = migrationTableLogService;
    }
}
