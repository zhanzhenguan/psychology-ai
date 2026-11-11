package com.zzg.psychologyai.infrastructure.storage.minio.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置（基础设施层）。
 * <p>
 * {@link MinioClient} 线程安全，注册为单例；构造阶段仅保存连接参数、不发起网络连接，
 * 首次调用上传/查询接口时才建连，MinIO 未启动不影响应用启动。
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    /**
     * 构建 MinIO 客户端单例
     *
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
