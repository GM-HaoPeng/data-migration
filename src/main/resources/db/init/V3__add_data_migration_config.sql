alter table migration_configuration
    add column target_data_source varchar(64) default null comment '目标数据源',
    add column target_table_name varchar(256) default null comment '目标表名',
    add column source_primary_key varchar(64) default null comment '源表主键字段',
    add column target_primary_key varchar(64) default null comment '目标表主键字段',
    add column where_clause varchar(1024) default null comment '源表迁移过滤条件',
    add column enabled tinyint(1) default 1 comment '是否启用',
    add column migration_order int default 0 comment '迁移顺序';
