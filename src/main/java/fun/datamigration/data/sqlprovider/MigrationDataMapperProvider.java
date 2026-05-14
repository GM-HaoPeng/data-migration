package fun.datamigration.data.sqlprovider;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import fun.datamigration.data.entity.MigrationDataConfig;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class MigrationDataMapperProvider {

    public String selectSourceRows(MigrationDataConfig config) {
        validateSourceConfig(config);
        StringJoiner columns = new StringJoiner(", ");
        List<String> sourceColumns = config.getSourceColumnNames();
        List<String> targetColumns = config.getTargetColumnNames();
        for (int i = 0; i < sourceColumns.size(); i++) {
            columns.add(sourceColumns.get(i) + " as `" + targetColumns.get(i) + "`");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(columns)
                .append(" from ").append(config.getSourceTableName());
        if (StringUtils.isNotBlank(config.getWhereClause())) {
            sql.append(" where ").append(config.getWhereClause());
        }
        sql.append(" order by ").append(config.getSourcePrimaryKey())
                .append(" limit ").append(config.getBatchSize())
                .append(" offset ").append(config.getOffset());
        return sql.toString();
    }

    public String insertTargetRows(MigrationDataConfig config) {
        validateTargetConfig(config);
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        for (String targetColumn : config.getTargetColumnNames()) {
            columns.add("`" + targetColumn + "`");
            values.add("#{row." + targetColumn + "}");
        }
        return "<script>insert ignore into " + config.getTargetTableName() + " (" + columns + ") values "
                + "<foreach collection=\"rows\" item=\"row\" separator=\",\">"
                + "(" + values + ")"
                + "</foreach></script>";
    }

    public String insertTargetRowsFromParams(Map<String, Object> params) {
        return insertTargetRows((MigrationDataConfig) params.get("config"));
    }

    private void validateSourceConfig(MigrationDataConfig config) {
        if (config == null || StringUtils.isBlank(config.getSourceTableName())
                || StringUtils.isBlank(config.getSourcePrimaryKey())
                || config.getSourceColumnNames() == null || config.getSourceColumnNames().isEmpty()
                || config.getTargetColumnNames() == null
                || config.getSourceColumnNames().size() != config.getTargetColumnNames().size()
                || config.getOffset() == null || config.getBatchSize() == null) {
            throw new RuntimeException("错误的数据迁移查询配置");
        }
    }

    private void validateTargetConfig(MigrationDataConfig config) {
        if (config == null || StringUtils.isBlank(config.getTargetTableName())
                || config.getTargetColumnNames() == null || config.getTargetColumnNames().isEmpty()) {
            throw new RuntimeException("错误的数据迁移写入配置");
        }
    }
}
