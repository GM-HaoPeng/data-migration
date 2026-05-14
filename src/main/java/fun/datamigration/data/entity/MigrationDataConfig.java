package fun.datamigration.data.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class MigrationDataConfig implements Serializable {
    private static final long serialVersionUID = -7287245459622220243L;
    private String configId;
    private String sourceDataSource;
    private String targetDataSource;
    private String sourceTableName;
    private String targetTableName;
    private String sourcePrimaryKey;
    private String targetPrimaryKey;
    private String whereClause;
    private List<String> sourceColumnNames;
    private List<String> targetColumnNames;
    private Map<String, String> sourceToTargetColumnMap;
    private Integer offset;
    private Integer batchSize;
}
