package com.zzg.psychologyai.infrastructure.mongo.config;

import org.bson.Document;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: zzg
 * @date: 2026年10月29日  16:06
 * @version: 1.0
 */

@Component
@WritingConverter
public class MessageWriteConverter implements Converter<Message, Document> {

    @Override
    public Document convert(Message source) {
        Document doc = new Document();
        doc.put("type", source.getMessageType().getValue().toLowerCase());
        doc.put("textContent", source.getText());
        doc.put("metadata", source.getMetadata() != null ? source.getMetadata() : Map.<String, Object>of());
        return doc;
    }
}
