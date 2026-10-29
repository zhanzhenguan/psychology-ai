package com.zzg.psychologyai.infrastructure.ai.repository;

import com.zzg.psychologyai.infrastructure.mongo.po.ChatMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zzg
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoChatMemoryRepository implements ChatMemoryRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<String> findConversationIds() {
        List<String> conversationId = mongoTemplate.findDistinct(new Query(), "conversationId", ChatMessages.class, String.class);
        return conversationId == null || conversationId.isEmpty() ? List.of() : conversationId;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        ChatMessages doc = mongoTemplate.findById(conversationId, ChatMessages.class);
        return doc != null && doc.messages() != null ? new ArrayList<>(doc.messages()) : List.of();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        ChatMessages chatMessages = new ChatMessages(conversationId, messages);
        mongoTemplate.save(chatMessages);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Query query = Query.query(Criteria.where("conversationId").is(conversationId));
        mongoTemplate.remove(query, ChatMessages.class);
    }

}
