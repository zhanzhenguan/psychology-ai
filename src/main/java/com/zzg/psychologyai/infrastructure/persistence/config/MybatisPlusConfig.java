package com.zzg.psychologyai.infrastructure.persistence.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 集成配置（基础设施层）：统一扫描基础设施层 Mapper，注册插件链
 */
@Configuration
@MapperScan("com.zzg.psychologyai.infrastructure.**.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 插件链，分页插件需置于最后
     *
     * @return 插件拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
