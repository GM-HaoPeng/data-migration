package fun.datamigration.file.service.impl;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import fun.datamigration.file.entity.MigrationFile;
import fun.datamigration.file.mapper.MigrationFileMapper;
import fun.datamigration.file.service.FileMigrationService;
import fun.datamigration.migration.config.entity.MigrationConfigVO;
import fun.datamigration.migration.config.service.MigrationConfigService;
import fun.datamigration.migration.constant.FieldDataType;
import fun.datamigration.migration.constant.MigrationStatus;
import fun.datamigration.migration.datalog.entity.MigrationDataLog;
import fun.datamigration.migration.datalog.service.MigrationDataLogService;
import fun.datamigration.migration.tablelog.entity.MigrationTableLog;
import fun.datamigration.migration.tablelog.service.MigrationTableLogService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.get.RemoteFileInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileMigrationServiceImpl implements FileMigrationService {
    private MigrationFileMapper migrationFileMapper;

    private MigrationConfigService migrationConfigService;

    private FileStorageService fileStorageService;

    private ThreadPoolTaskExecutor dataMigrationExecutor;

    private MigrationDataLogService migrationDataLogService;

    private MigrationTableLogService migrationTableLogService;


    @Getter
    @Setter
    @Value("${migration.mode}")
    private String mode;

    @Getter
    @Setter
    @Value("${migration.batch-size:1000}")
    private Integer batchSize;

    @Getter
    @Setter
    @Value("${migration.retry-count:3}")
    private Integer maxRetries = 3;

    @Getter
    @Setter
    @Value("${migration.buffer-size:8192}")
    private int bufferSize;

    @Override
    public void fileMigration(String originPlatForm, String destinationPlatform) {
        List<MigrationFile> allMigrationFiles = getAllMigrationFiles();
        if ("test".equals(mode)) {
            if (batchSize == null) {
                batchSize = 1000;
            }
            allMigrationFiles = allMigrationFiles.stream().limit(batchSize).collect(Collectors.toList());
        }
        log.info("待迁移文件总数：{}", allMigrationFiles.size());
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("开始迁移文件");
        // 分批提交任务
        for (int i = 0; i < allMigrationFiles.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, allMigrationFiles.size());
            List<MigrationFile> batchFiles = allMigrationFiles.subList(i, endIndex);
            // 处理当前批次
            processBatch(batchFiles, originPlatForm, destinationPlatform);
            log.info("已完成第 {} 到第 {} 个文件的迁移", i + 1, endIndex);
        }
        stopWatch.stop();
        // 修改后代码
        long millis = stopWatch.getLastTaskTimeMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        if (minutes > 0) {
            log.info("文件迁移完成，耗时：{}分钟{}秒", minutes, seconds);
        } else {
            log.info("文件迁移完成，耗时：{}秒", seconds);
        }
    }

    /**
     * 分批处理文件迁移
     *
     * @param batchFiles          批次文件列表
     * @param originPlatForm      源平台
     * @param destinationPlatform 目标平台
     */
    private void processBatch(List<MigrationFile> batchFiles, String originPlatForm, String destinationPlatform) {
        log.info("开始处理批次，包含 {} 个文件", batchFiles.size());

        List<Future<MigrationFile>> futures = new ArrayList<>();
        for (MigrationFile migrationFile : batchFiles) {
            // 提交任务并获取 Future
            Future<MigrationFile> future = dataMigrationExecutor.submit(() ->
                    migrateSingleFile(migrationFile, originPlatForm, destinationPlatform));
            futures.add(future);
        }

        // 保存批次日志
        saveLog(futures);
        log.info("批次处理完成，共处理 {} 个文件", batchFiles.size());
    }

    private void saveLog(List<Future<MigrationFile>> futures) {
        List<MigrationTableLog> migrationTableLogs = new ArrayList<>();
        Map<String, String> dataSourceTableColumnNameMap = new HashMap<>();
        List<MigrationDataLog> migrationDataLogs = new ArrayList<>();
        for (Future<MigrationFile> future : futures) {
            try {
                // 获取任务结果，设置超时时间（可根据实际调整）
                MigrationFile fileInfo = future.get();
                if (fileInfo != null) {
                    // 构造迁移日志
                    MigrationDataLog dataLog = new MigrationDataLog();
                    dataLog.setData(fileInfo.getFilePath());
                    dataLog.setStatus(fileInfo.getStatus());
                    dataLog.setResult(fileInfo.getResult());
                    dataLog.setConfigId(fileInfo.getConfigId());
                    dataLog.setCreateTime(new Date());
                    migrationDataLogs.add(dataLog);
                    if (!dataSourceTableColumnNameMap.containsKey(fileInfo.getDataSource() + fileInfo.getTableName() + fileInfo.getColumnName())) {
                        MigrationTableLog migrationTableLog = new MigrationTableLog();
                        migrationTableLog.setTableName(fileInfo.getTableName());
                        migrationTableLog.setDataSource(fileInfo.getDataSource());
                        migrationTableLog.setColumnName(fileInfo.getColumnName());
                        migrationTableLog.setCreateTime(new Date());
                        migrationTableLogs.add(migrationTableLog);
                        dataSourceTableColumnNameMap.put(fileInfo.getDataSource() + fileInfo.getTableName() + fileInfo.getColumnName(), fileInfo.getColumnName());
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("文件迁移任务执行失败：{}", e.getMessage());
            }
        }
        migrationDataLogService.saveOrUpdateBatch(migrationDataLogs);
        migrationTableLogService.saveOrUpdateBatch(migrationTableLogs);
    }

    public MigrationFile migrateSingleFile(MigrationFile migrationFile, String originPlatForm, String destinationPlatform) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                String filePath = migrationFile.getFilePath();
                String realPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

                FileInfo fileInfo = new FileInfo();
                fileInfo.setPlatform(originPlatForm); // 使用配置中定义的平台名称
                fileInfo.setBasePath("");
                fileInfo.setPath(realPath);
                fileInfo.setFilename(fileName);

                FileInfo newFileInfo = new FileInfo();
                BeanUtils.copyProperties(fileInfo, newFileInfo);
                newFileInfo.setPlatform(destinationPlatform);
                boolean exists = fileStorageService.exists(newFileInfo);
                if (exists) {
                    migrationFile.setStatus(MigrationStatus.EXISTS);
                    migrationFile.setResult(MigrationStatus.EXISTS.getDesc());
                    log.info("文件已存在，无需重复上传：{}", fileName);
                    return migrationFile;
                }
                // 下载文件
                RemoteFileInfo remoteFileInfo = fileStorageService.getFile(fileInfo);
                if (remoteFileInfo == null) {
                    migrationFile.setStatus(MigrationStatus.NOT_EXISTS);
                    migrationFile.setResult(MigrationStatus.NOT_EXISTS.getDesc());
                    return migrationFile;
                }
                Long fileAllSize = remoteFileInfo.getSize();
                byte[] bytes = fileStorageService.download(fileInfo)
                        .setProgressListener(progressSize -> {
                            // 只在进度有显著变化时记录日志（例如每10%记录一次）
                            long fileSize = fileAllSize;
                            if (fileSize > 0) {
                                long progressPercent = (progressSize * 100) / fileSize;
                                if (progressPercent % 10 == 0) { // 每10%记录一次
                                    log.info("{} 已下载 {}%", fileName, progressPercent);
                                }
                            }
                        })
                        .bytes();

                // 上传文件
                fileStorageService.of(new ByteArrayInputStream(bytes))
                        .setPlatform(destinationPlatform)
                        .setPath(fileInfo.getPath())
                        .setSaveFilename(fileInfo.getFilename())
                        .setProgressListener(progressSize -> {
                            // 只在进度有显著变化时记录日志（例如每10%记录一次）
                            long fileSize = fileAllSize;
                            if (fileSize > 0) {
                                long progressPercent = (progressSize * 100) / fileSize;
                                if (progressPercent % 10 == 0) { // 每10%记录一次
                                    log.info("{} 已上传 {}%", fileName, progressPercent);
                                }
                            }
                        })
                        .upload();
                migrationFile.setStatus(MigrationStatus.SUCCESS);
                migrationFile.setResult(MigrationStatus.SUCCESS.getDesc());
                return migrationFile;
            } catch (Exception e) {
                if (i == maxRetries - 1) {
                    // 最后一次重试仍然失败
                    migrationFile.setStatus(MigrationStatus.FAILED);
                    migrationFile.setResult("重试" + maxRetries + "次后仍然失败: " + e.getMessage());
                    return migrationFile;
                }
                log.warn("文件迁移失败，正在进行第{}次重试: {}", i + 1, e.getMessage());
                try {
                    Thread.sleep(1000L * (i + 1)); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return migrationFile;
    }


    @Override
    public List<MigrationFile> getAllMigrationFiles() {
        List<MigrationConfigVO> allNeedMigrationConfigs = migrationConfigService.getAllNeedMigrationConfigs(FieldDataType.FILE);
        List<MigrationFile> allMigrationFiles = new ArrayList<>();
        for (MigrationConfigVO needMigrationConfig : allNeedMigrationConfigs) {
            // 手动设置数据源
            String dataSource = needMigrationConfig.getDataSource();
            DynamicDataSourceContextHolder.push(dataSource);
            try {
                List<MigrationFile> migrationFiles = migrationFileMapper.getMigrationFiles(needMigrationConfig);
                allMigrationFiles.addAll(migrationFiles);
            } finally {
                DynamicDataSourceContextHolder.poll();
            }
        }
        return allMigrationFiles;
    }

    @Autowired
    public void setMigrationFileMapper(MigrationFileMapper migrationFileMapper) {
        this.migrationFileMapper = migrationFileMapper;
    }

    @Autowired
    public void setMigrationConfigService(MigrationConfigService migrationConfigService) {
        this.migrationConfigService = migrationConfigService;
    }

    @Autowired
    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Autowired
    public void setDataMigrationExecutor(ThreadPoolTaskExecutor dataMigrationExecutor) {
        this.dataMigrationExecutor = dataMigrationExecutor;
    }

    @Autowired
    public void setMigrationDataLogService(MigrationDataLogService migrationDataLogService) {
        this.migrationDataLogService = migrationDataLogService;
    }

    @Autowired
    public void setMigrationTableLogService(MigrationTableLogService migrationTableLogService) {
        this.migrationTableLogService = migrationTableLogService;
    }
}
