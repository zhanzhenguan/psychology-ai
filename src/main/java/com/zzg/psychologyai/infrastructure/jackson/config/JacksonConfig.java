package com.zzg.psychologyai.infrastructure.jackson.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author: zzg
 * @date: 2026年10月16日  23:32
 * @version: 1.0
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {

        return builder -> {
            // 日期格式
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // 时间格式
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            // 日期时间格式
            DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            builder.serializers(
                    new LocalDateSerializer(dateFmt),
                    new LocalTimeSerializer(timeFmt),
                    new LocalDateTimeSerializer(dateTimeFmt)
            );
            builder.deserializers(
                    new LocalDateDeserializer(dateFmt),
                    new LocalTimeDeserializer(timeFmt),
                    new LocalDateTimeDeserializer(dateTimeFmt)
            );
            // 全局 null 不输出，保持 JSON 干净
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        };
    }
}
