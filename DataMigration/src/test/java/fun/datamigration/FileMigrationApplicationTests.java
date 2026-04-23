package fun.datamigration;

import fun.datamigration.file.service.FileMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.File;

@SpringBootTest
@Slf4j
class FileMigrationApplicationTests {
    private static final String FAST_DFS = "fastdfs-1";
    private static final String OSS_PLATFORM = "amazon-s3-1";
    @Autowired
    private FileMigrationService fileMigrationService;
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    void contextLoads() {
    }

    @Test
    void testGetFile() {
        fileMigrationService.fileMigration(FAST_DFS, OSS_PLATFORM);
    }

    @Test
    void fileExist(){
        // 方法1：直接设置 platform
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform("fastdfs-1"); // 使用配置中定义的平台名称
        fileInfo.setBasePath("");
        fileInfo.setPath("group1/M00/08/44/");
        fileInfo.setFilename("wKgAzF2nCHaAMXSYAABnso6e5KY891.png");

        boolean exists = fileStorageService.exists(fileInfo);
        if (exists){
            fileStorageService.download(fileInfo).setProgressListener(progressSize ->
                    System.out.println("已下载：" + progressSize)
            ).file(new File("E:\\file_transfer\\" + fileInfo.getFilename()));
        }
        System.out.println(exists);
    }

    @Test
    void fileTransfer(){
        // 方法1：直接设置 platform
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform("fastdfs-1"); // 使用配置中定义的平台名称
        fileInfo.setBasePath("");
        fileInfo.setPath("group1/M00/AB/E2/");
        fileInfo.setFilename("wKgAzGiSmwOASjYdAAHE_fDdrYk972.pdf");
        String toPlatform = "amazon-s3-1";
//        String toPlatform = "amazon-s3-2";

        byte[] bytes = fileStorageService.download(fileInfo).bytes();
        if (bytes == null || bytes.length == 0){
            return;
        }

        FileInfo s3 = new FileInfo();
        s3.setPlatform(toPlatform);
        s3.setBasePath("");
        s3.setPath(fileInfo.getPath());
        s3.setFilename(fileInfo.getFilename());
        boolean exists = fileStorageService.exists(s3);
        if (exists){
            System.out.println("文件已存在");
        }

        // 2. 上传到七牛云（指定存储平台为七牛云）
        s3 = fileStorageService.of(new ByteArrayInputStream(bytes))
                .setPlatform(toPlatform) // 对应配置中的七牛云平台标识
                .setPath(fileInfo.getPath()) // 七牛云存储路径
                .setSaveFilename(fileInfo.getFilename())
                .upload();
        System.out.println(s3);

    }

    @Test
    void fileTransfer2(){
    }


}
