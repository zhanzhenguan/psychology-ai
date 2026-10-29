package com.zzg.psychologyai.infrastructure.mongo.config;

import org.bson.Document;
import org.springframework.ai.chat.messages.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: zzg
 * @date: 2026年10月29日  16:03
 * @version: 1.0
 */

@Component
@ReadingConverter
public class MessageReadConverter implements Converter<Document, Message> {


    @Override
    public Message convert(Document source) {
        String type = source.getString("type");
        String textContent = source.getString("textContent");
        Map<String, Object> metadata = (Map<String, Object>) source.get("metadata");
        return switch (type) {
            case "user" -> UserMessage.builder().text(textContent).metadata(metadata).build();
            case "assistant" -> AssistantMessage.builder().content(textContent).properties(metadata).build();
            case "system" -> SystemMessage.builder().text(textContent).metadata(metadata).build();
            case "tool" -> ToolResponseMessage.builder().metadata(metadata).build();
            default -> throw new IllegalArgumentException("Unknown message type: " + type);
        };
    }
}
