package com.zzg.psychologyai.infrastructure.mongo.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * 聊天记忆 MongoDB 文档。
 * <p>
 * 参照 {@code InMemoryChatMemoryRepository} 的 Map<String, List<Message>> 模型——
 * 每个 conversationId 对应一条文档，整个消息列表作为 JSON 字符串覆盖写入。
 * 集合名 {@code chat_messages} 与 Repository 常量 COLLECTION 保持一致。
 *
 * @author zzg
 */

@Document("chat_messages")
public record ChatMessages (
        @Id
        String conversationId,
        List<Message> messages,      // ← 直接存 Message，Converter 自动转
        Instant lastUpdatedAt
){

    public ChatMessages(String conversationId, List<Message> messages) {
        this(conversationId, messages, Instant.now());
    }

}
