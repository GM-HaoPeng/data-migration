package fun.datamigration.data.sqlprovider;

import fun.datamigration.data.entity.MigrationDataConfig;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationDataMapperProviderTest {

    @Test
    void buildsPagedSourceQueryWithFieldMappingsAndPrimaryKey() {
        MigrationDataConfig config = new MigrationDataConfig();
        config.setSourceTableName("user_old");
        config.setSourcePrimaryKey("id");
        config.setSourceColumnNames(Arrays.asList("id", "name", "mobile"));
        config.setTargetColumnNames(Arrays.asList("id", "username", "phone"));
        config.setWhereClause("deleted = 0");
        config.setOffset(20);
        config.setBatchSize(10);

        String sql = new MigrationDataMapperProvider().selectSourceRows(config);

        assertThat(sql).isEqualTo("select id as `id`, name as `username`, mobile as `phone` "
                + "from user_old where deleted = 0 order by id limit 10 offset 20");
    }

    @Test
    void buildsInsertSqlForTargetColumns() {
        MigrationDataConfig config = new MigrationDataConfig();
        config.setTargetTableName("user_new");
        config.setTargetColumnNames(Arrays.asList("id", "username", "phone"));

        String sql = new MigrationDataMapperProvider().insertTargetRows(config);

        assertThat(sql).isEqualTo("<script>insert ignore into user_new (`id`, `username`, `phone`) values "
                + "<foreach collection=\"rows\" item=\"row\" separator=\",\">"
                + "(#{row.id}, #{row.username}, #{row.phone})"
                + "</foreach></script>");
    }
}
