package fun.datamigration.migration.tablelog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.datamigration.migration.tablelog.entity.MigrationTableLog;
import fun.datamigration.migration.tablelog.mapper.MigrationTableLogMapper;
import fun.datamigration.migration.tablelog.service.MigrationTableLogService;
import org.springframework.stereotype.Service;

@Service
public class MigrationTableLogServiceImpl extends ServiceImpl<MigrationTableLogMapper, MigrationTableLog> implements MigrationTableLogService {
}
