package com.zzg.psychologyai.infrastructure.storage.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置（基础设施层）。
 * <p>
 * 对应 application.yml 的 {@code minio.*} 配置，生产环境通过环境变量覆盖 endpoint 与凭据。
 * 客户端为懒连接：构造 Bean 阶段不建连，首次上传/查询时才真正连接 MinIO。
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** MinIO 服务端点，如 http://192.168.136.128:9000 */
    private String endpoint;

    /** Access Key（账号） */
    private String accessKey;

    /** Secret Key（密钥） */
    private String secretKey;

    /** 存储桶名称，上传前幂等创建 */
    private String bucket;

    /**
     * 对外预览地址前缀；留空则回退 {@link #endpoint}。
     * 经 Nginx 反向代理对外暴露时，用它覆盖成对外域名/端口。
     */
    private String previewUrl;
}
