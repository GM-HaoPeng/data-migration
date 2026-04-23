package fun.datamigration.file.entity;

import fun.datamigration.migration.constant.MigrationStatus;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class MigrationFile implements Serializable {
    private static final long serialVersionUID = -2370205274928707505L;
    private String filePath;
    private String dataSource;
    private String tableName;
    private String columnName;
    private String configId;
    private MigrationStatus status;
    private String result;
}
