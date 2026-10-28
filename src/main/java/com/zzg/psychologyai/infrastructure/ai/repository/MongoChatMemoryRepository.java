package com.zzg.psychologyai.infrastructure.ai.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB 版 ChatMemoryRepository：替代 InMemoryChatMemoryRepository，
 * 会话消息按 conversationId 持久化到 chat_memory 集合。
 * <p>
 * 序列化统一用 ObjectMapper（Spring AI Message 原生基于 Jackson 注解，多态自动识别），
 * messagesJson 字段存 JSON 字符串，便于在 MongoDB Compass / Shell 中直接阅读。
 *
 * @author zzg
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoChatMemoryRepository implements ChatMemoryRepository {

    private static final String COLLECTION = "chat_memory";
    private static final String FIELD_CONVERSATION_ID = "conversationId";
    private static final String FIELD_MESSAGES_JSON = "messagesJson";

    private static final TypeReference<List<Message>> MSG_LIST_TYPE = new TypeReference<>() {};

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> findConversationIds() {
        return mongoTemplate.findDistinct(
                new Query(),
                FIELD_CONVERSATION_ID,
                COLLECTION,
                String.class
        );
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Query query = Query.query(Criteria.where(FIELD_CONVERSATION_ID).is(conversationId));
        ChatMemoryDoc doc = mongoTemplate.findOne(query, ChatMemoryDoc.class, COLLECTION);
        if (doc == null || doc.messagesJson() == null || doc.messagesJson().isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(doc.messagesJson(), MSG_LIST_TYPE);
        } catch (Exception e) {
            log.error("反序列化会话消息失败, conversationId={}", conversationId, e);
            return List.of();
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String json;
        try {
            json = objectMapper.writeValueAsString(messages);
        } catch (Exception e) {
            log.error("序列化会话消息失败, conversationId={}", conversationId, e);
            return;
        }
        Query query = Query.query(Criteria.where(FIELD_CONVERSATION_ID).is(conversationId));
        Update update = new Update()
                .set(FIELD_CONVERSATION_ID, conversationId)
                .set(FIELD_MESSAGES_JSON, json);
        mongoTemplate.upsert(query, update, COLLECTION);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Query query = Query.query(Criteria.where(FIELD_CONVERSATION_ID).is(conversationId));
        mongoTemplate.remove(query, COLLECTION);
    }

    /**
     * MongoDB 文档结构（内部记录类）
     */
    public record ChatMemoryDoc(String conversationId, String messagesJson) {
    }
}
