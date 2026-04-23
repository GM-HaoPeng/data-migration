# 数据迁移工具 v1.0
## 表说明
1. migration_configuration 表存放的是需要迁移数据的表名，字段，及所属数据源，以下简称配置表
2. migration_table_log 表存放的是数据源、表、列的迁移记录，以下简称表迁移记录
3. migration_data_log 表存放的是详细的数据迁移记录，主要包含迁移的数据id或附件路径、状态等
## 执行逻辑说明
1. 工具根据配置表中的数据源、表、列信息，生成数据查询脚本。生成的sql 使用 union all 将所有结果合并，合并后的字段名称为映射列的名称，因目前实现中接收对象为实体，采用filePath字段接收，因此文件类的映射字段统一配置为 file_path；如迁移  a 表中 up_pic 和 down_pic 两个字段，最终生成的数据查询sql为

   ```sql
   select up_pic as file_path
   where up_pic is not null and up_pic != ''
   union all select down_pic as file_path 
   where down_pic is not null and down_pic != ''
   ```

2. 同时根据 表迁移记录过滤已迁移的表和列

3. 最后将文件下载然后上传至目标平台
## 目前只做了文件迁移
### 功能说明
示例：将fastdfs的附件数据迁移至天翼云
涉及到多个数据源的多个表的多个字段迁移
1. 在 application.yml 中配置 master数据源，以及 flyway 数据源，用以初始化数据库表，修改部分如下。

   ```yml
           master:
             url: jdbc:mysql://localhost:3306/migration_log_test?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
             driver-class-name: com.mysql.cj.jdbc.Driver
             username: root
             password: root
   ```

   ```yml
     flyway:
       url: jdbc:mysql://localhost:3306/migration_log_test?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
       user: root
       password: root
   ```

2. 配置存储平台属性 参考 x-file-storage 官网，示例如下：

   ```yml
       fastdfs:
         - platform: fastdfs-1 # 存储平台标识
           enable-storage: true  # 启用存储
           run-mod: COVER #运行模式，默认 COVER（覆盖模式），强制用 FastDFS 返回的路径及文件名覆盖 FileInfo 中的 path 及 filename。
           tracker-server: # Tracker Server 配置
             server-addr: ?? # Tracker Server 地址（IP:PORT），多个用英文逗号隔开
           domain: ?? # 访问域名，注意“/”结尾，例如：https://file.abc.com/
           base-path: "" # 基础路径，强烈建议留空，详情查看兼容性说明章节
       amazon-s3: # Amazon S3 天翼云也在此配置
         - platform: amazon-s3-1 # 存储平台标识
           enable-storage: true  # 启用存储
           access-key: ??
           secret-key: ??
           region:  # 与 end-point 参数至少填一个
           end-point: ?? # 与 region 参数至少填一个
           bucket-name: bucket-xxx #桶名称
           domain: https://bucket-xxx.??/ # 访问域名，注意“/”结尾，例如：https://abcd.s3.ap-east-1.amazonaws.com/
           base-path:  # 基础路径        
   ```

   其中 end-point 要填写不带桶的域名，domain填写带桶的路径

3. 查看迁移的数据平台配置，如果不正确，进行修改

   ```yml
   migration:
    file-migration:
     origin-platform: fastdfs-1 # 源平台
     destination-platform: amazon-s3-1 # 目标平台
   ```

4. 在配置表中配置表，字段，映射字段(使用file_path)，数据类型（附件为file）数据源，#目前根据任务描述，内置了以下表的配置：

   | 表名                       | 字段                                                         | 数据源     |
   | -------------------------- | ------------------------------------------------------------ |---------|
   | artificial_collect_picture | FILE_PATH                                                    | source1 |
   | event_process_pic          | UP_PIC_PATH，DOWN_PIC_PATH                                   | source1 |
   | finish_event_pic           | BEFOER_DOWN_FILE，BEFOER_UP_FILE，AFTER_DOWN_FILE，AFTER_UP_FILE | source1 |
   | upload_media               | path                                                         | source2 |
   |                            |                                                              |         |
   |                            |                                                              |         |
   |                            |                                                              |         |

5. 在 application.yml 中配置 在配置表中用到的数据源，如在 步骤4 中部分数据来自其他数据源，则新增相应的数据源配置

   ```yml
           source1:
             url: jdbc:mysql://ip:port/database?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false
             driver-class-name: com.mysql.cj.jdbc.Driver
             username: ??
             password: ??
           source2:
             url: jdbc:mysql://ip:port/database/gaoguantong?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false
             username: ??
             password: ??
   ```

6. 使用以下命令运行即可开始数据迁移，迁移完成会自动停止。

   ```cmd
    jar -jar DataMigration.jar 
   ```

   
## 特别说明
根据表迁移记录过滤已迁移的表和列后，仍会判断文件是否存在，如果存在则跳过，因此某个表的列想重新迁移，请先删除表迁移记录表中该表该列的记录即可。

