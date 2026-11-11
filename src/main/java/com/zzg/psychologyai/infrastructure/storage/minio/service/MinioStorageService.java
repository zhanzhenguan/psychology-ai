package com.zzg.psychologyai.infrastructure.storage.minio.service;

import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.infrastructure.storage.minio.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO 对象存储服务（基础设施层）。
 * <p>
 * 负责桶初始化、文件上传与预览地址拼接；与本地磁盘上传链路（{@code SysFileInfoAppService#upload}）
 * 相互独立，供 {@code uploadMinio} 流程调用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    /** 对象 key 日期分层目录格式：yyyy-MM-dd（桶内日期文件夹） */
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ISO_LOCAL_DATE;

    /** 落库相对路径前缀，与本地磁盘上传的 /uploads 口径一致；桶名亦为 uploads */
    private static final String RELATIVE_PREFIX = "/uploads";

    private final MinioClient minioClient;
    private final MinioProperties properties;

    /**
     * 生成桶内对象 key：日期文件夹 + UUID 去重，
     * 如 {@code 2026-10-19/90f4d76b60bd4d53a1bb5c752dd3e3bc.jpg}（bucket=uploads）
     *
     * @param suffix 文件名后缀（含点，无后缀传空串）
     * @return 对象 key（不含桶名，不带前导斜杠）
     */
    public String buildObjectKey(String suffix) {
        String dateDir = LocalDate.now().format(DATE_DIR);
        String storedName = UUID.randomUUID().toString().replace("-", "") + suffix;
        return dateDir + "/" + storedName;
    }

    /**
     * 将对象 key 转为落库相对路径，与本地磁盘上传口径一致：
     * {@code /uploads/yyyy-MM-dd/uuid.suffix}
     *
     * @param objectKey 对象 key（如 {@code 2026-10-19/xxx.jpg}）
     * @return 落库相对路径（带前导斜杠）
     */
    public String toRelativePath(String objectKey) {
        return RELATIVE_PREFIX + "/" + objectKey;
    }

    /**
     * 上传文件到 MinIO；桶不存在时幂等创建。
     * <p>
     * 先保证上传成功，再由应用层落库，避免产生只有记录、没有对象的脏数据。
     *
     * @param file     上传文件
     * @param objectKey 对象 key（不含桶名）
     * @throws BizException 上传失败时抛出业务异常
     */
    public void upload(MultipartFile file, String objectKey) {
        String bucket = properties.getBucket();
        try {
            ensureBucket(bucket);
            try (InputStream in = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .stream(in, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
            }
            log.info("MinIO 上传成功：bucket={}, objectKey={}, size={}", bucket, objectKey, file.getSize());
        } catch (IOException e) {
            log.error("读取上传文件流失败：objectKey={}", objectKey, e);
            throw new BizException(ResultCode.FAIL, "文件读取失败");
        } catch (Exception e) {
            log.error("上传 MinIO 失败：bucket={}, objectKey={}", bucket, objectKey, e);
            throw new BizException(ResultCode.FAIL, "文件上传到对象存储失败");
        }
    }

    /**
     * 拼接文件预览地址：{baseUrl}/{bucket}/{objectKey}。
     * baseUrl 优先取 {@code preview-url}，缺省回退 {@code endpoint}，并去除尾部斜杠。
     *
     * @param objectKey 对象 key
     * @return 可直接访问的预览 URL
     */
    public String getPreviewUrl(String objectKey) {
        String baseUrl = properties.getPreviewUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = properties.getEndpoint();
        }
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return baseUrl + "/" + properties.getBucket() + "/" + objectKey;
    }

    /**
     * 桶不存在则创建（幂等）。
     *
     * @param bucket 桶名
     * @throws Exception MinIO 调用异常
     */
    private void ensureBucket(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("MinIO 桶已创建：{}", bucket);
        }
    }
}
