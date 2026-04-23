package fun.datamigration.migration.datalog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.datamigration.migration.datalog.entity.MigrationDataLog;
import fun.datamigration.migration.datalog.mapper.MigrationDataLogMapper;
import fun.datamigration.migration.datalog.service.MigrationDataLogService;
import org.springframework.stereotype.Service;

@Service
public class MigrationDataLogServiceImpl extends ServiceImpl<MigrationDataLogMapper, MigrationDataLog> implements MigrationDataLogService {

}
