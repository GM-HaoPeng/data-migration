# Database Data Migration

## Scope

The first version supports direct field mapping migration from a source table to a target table. It is designed for MySQL-to-MySQL migration and keeps the SQL builder isolated so later versions can add dialects, value transformers, joins, or dependency ordering.

## Configuration Semantics

`migration_configuration` keeps the original columns, with these meanings:

- `data_source`: source datasource name.
- `table_name`: source table name.
- `column_name`: source column name.
- `mapping_column_name`: target column name.
- `field_data_type`: use `DATA` or `data` for database rows, `FILE` or `file` for attachment files.

New columns added by `V3__add_data_migration_config.sql`:

- `target_data_source`: target datasource name.
- `target_table_name`: target table name.
- `source_primary_key`: source table primary key used for stable paging.
- `target_primary_key`: target table primary key after mapping.
- `where_clause`: optional source filter without the `where` keyword.
- `enabled`: `1` to enable, `0` to disable.
- `migration_order`: lower values run first.

## Example

```sql
insert into migration_configuration (
    id, data_source, table_name, column_name, mapping_column_name, field_data_type,
    target_data_source, target_table_name, source_primary_key, target_primary_key,
    where_clause, enabled, migration_order
) values
('2001', 'source1', 'user_old', 'id', 'id', 'DATA', 'master', 'user_new', 'id', 'id', 'deleted = 0', 1, 10),
('2002', 'source1', 'user_old', 'name', 'username', 'DATA', 'master', 'user_new', 'id', 'id', 'deleted = 0', 1, 10),
('2003', 'source1', 'user_old', 'mobile', 'phone', 'DATA', 'master', 'user_new', 'id', 'id', 'deleted = 0', 1, 10);
```

Run data migration by setting:

```yaml
migration:
  task-type: data
```

Use `file` for the existing file migration and `all` to run both.
