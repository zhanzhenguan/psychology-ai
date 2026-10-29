package com.zzg.psychologyai.infrastructure.mongo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

/**
 * @author: zzg
 * @date: 2026年10月29日  16:10
 * @version: 1.0
 */
@Configuration
public class MongoConvertConfig {

    /**
     * 注册 Converter 到 MongoTemplate
     * @param reader
     * @param writer
     * @return
     */

    @Bean
    MongoCustomConversions mongoCustomConversions(MessageReadConverter reader, MessageWriteConverter writer) {
        return new MongoCustomConversions(List.of(reader, writer));
    }
}
