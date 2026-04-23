package fun.datamigration.migration.datalog.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import fun.datamigration.migration.constant.MigrationStatus;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@TableName("migration_data_log")
public class MigrationDataLog implements Serializable {
    private static final long serialVersionUID = 2706317093915250520L;
    @TableId
    private String id;
    private String data;
    private MigrationStatus status;
    private Date createTime;
    private String result;
    private String configId;
}
