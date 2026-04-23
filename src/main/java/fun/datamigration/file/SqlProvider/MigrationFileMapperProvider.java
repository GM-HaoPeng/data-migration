package fun.datamigration.file.SqlProvider;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import fun.datamigration.migration.config.entity.MigrationConfigVO;

import java.util.Map;
import java.util.StringJoiner;

public class MigrationFileMapperProvider {
    public String getMigrationFiles(MigrationConfigVO migrationConfigVO) {
        if (migrationConfigVO == null || StringUtils.isBlank(migrationConfigVO.getTableName())
                || StringUtils.isBlank(migrationConfigVO.getDataSource())
                || StringUtils.isBlank(migrationConfigVO.getColumnNames())) {
            throw new RuntimeException("错误的配置");
        }
        String tableName = migrationConfigVO.getTableName();
        String columnNames = migrationConfigVO.getColumnNames();
        String[] fieldList = columnNames.split(",");
        StringJoiner sqlJoiner = new StringJoiner(" union all ");
        Map<String, String> mappingColumnNames = migrationConfigVO.getMappingColumnNames();
        Map<String, String> columnIdMap = migrationConfigVO.getColumnIdMap();
        // 循环拼接每个字段的查询语句
        for (String field : fieldList) {
            // 跳过空字段
            if (field == null || field.trim().isEmpty()) {
                continue;
            }

            // 构建单条查询语句
            StringBuilder singleSql = new StringBuilder();
            singleSql.append("select ")
                    .append(field);
            if (mappingColumnNames != null && mappingColumnNames.containsKey(field)) {
                singleSql.append(" as ")
                        .append(mappingColumnNames.get(field)).append(", ");
            }
            singleSql.append("'").append(migrationConfigVO.getDataSource()).append("'").append(" as data_source")
                    .append(",'").append(columnIdMap.get(field)).append("'").append(" as config_id,")
                    .append("'").append(migrationConfigVO.getTableName()).append("' as table_name,")
                    .append("'").append(field).append("' as column_name")
                    .append(" ")
                    .append("from ")
                    .append(tableName)
                    .append(" ")
                    .append("where ")
                    .append(field)
                    .append(" is not null ").append("and ")
                    .append( field).append(" != '' "); // 注释包含表名

            sqlJoiner.add(singleSql.toString());
        }
        return sqlJoiner.toString();
    }
}
