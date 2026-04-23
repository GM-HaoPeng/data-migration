package fun.datamigration.migration.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum FieldDataType {
    FILE("FILE", "文件"),
    DATA("DATA", "数据"),
    ;
    @EnumValue
    private final String code;
    private final String desc;
    FieldDataType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
