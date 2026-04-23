package fun.datamigration.migration.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum MigrationStatus {
    FAILED(0, "迁移失败"),
    SUCCESS(1, "迁移成功"),
    EXISTS(2, "已存在，无需重复上传"),
    NOT_EXISTS(3, "文件在源平台不存在"),
    ;
    @EnumValue
    private final int code;
    private final String desc;
    MigrationStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static MigrationStatus getByCode(int code) {
        for (MigrationStatus value : values()) {
            if (code == value.code) {
                return value;
            }
        }
        return null;
    }
}
