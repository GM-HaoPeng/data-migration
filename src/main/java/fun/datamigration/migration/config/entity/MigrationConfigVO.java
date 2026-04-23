package fun.datamigration.migration.config.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class MigrationConfigVO implements Serializable {
    private static final long serialVersionUID = 1519653336755194117L;
    private String tableName;
    private String columnNames;
    private String dataSource;
    private Map<String, String> mappingColumnNames;
    private Map<String , String> columnIdMap;
}
