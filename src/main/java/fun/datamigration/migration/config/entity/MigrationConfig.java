package fun.datamigration.migration.config.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@TableName("migration_configuration")
@ToString
public class MigrationConfig implements Serializable {
    private static final long serialVersionUID = 1962321484894798549L;
    @TableId
    private String id;
    private String tableName;
    private String columnName;
    private String mappingColumnName;
    private String dataSource;
    private String fieldDataType;
    private String targetDataSource;
    private String targetTableName;
    private String sourcePrimaryKey;
    private String targetPrimaryKey;
    private String whereClause;
    private Integer enabled;
    private Integer migrationOrder;
}
