CREATE TABLE `migration_configuration` (
                                           `id` varchar(64) NOT NULL COMMENT '主键',
                                           `table_name` varchar(256) DEFAULT NULL COMMENT '表名',
                                           `column_name` varchar(64) DEFAULT NULL COMMENT '列名',
                                           `mapping_column_name` varchar(64) DEFAULT NULL COMMENT '映射列名',
                                           `field_data_type` varchar(64) DEFAULT NULL COMMENT '字段数据类型',
                                           `data_source` varchar(64) DEFAULT NULL COMMENT '数据源',
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='迁移配置表';

CREATE TABLE `migration_data_log` (
                                      `id` varchar(64) NOT NULL COMMENT '主键',
                                      `data` text COMMENT '数据',
                                      `status` tinyint(4) DEFAULT '1' COMMENT '状态( 0 失败 1成功)',
                                      `result` text DEFAULT NULL COMMENT '更新结果',
                                      `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                      `config_id` varchar(64) DEFAULT NULL COMMENT '配置id',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='迁移数据日志';

CREATE TABLE `migration_table_log` (
                                       `id` varchar(64) NOT NULL COMMENT '主键',
                                       `table_name` varchar(64) DEFAULT NULL COMMENT '表名',
                                       `column_name` varchar(64) DEFAULT NULL COMMENT '列名',
                                       `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                       `data_source` varchar(64) DEFAULT NULL COMMENT '数据源',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='迁移表字段日志';



