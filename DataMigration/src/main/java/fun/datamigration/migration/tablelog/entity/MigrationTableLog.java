package fun.datamigration.migration.tablelog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@TableName("migration_table_log")
public class MigrationTableLog implements Serializable {
    private static final long serialVersionUID = 7971362487319063662L;
    @TableId
    private String id;
    private String tableName;
    private String columnName;
    private Date createTime;
    private String dataSource;
}
