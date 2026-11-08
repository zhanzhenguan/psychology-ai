package com.zzg.psychologyai.infrastructure.ai.repository;

import com.zzg.psychologyai.infrastructure.mongo.po.ChatMessages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MongoChatMemoryRepository 真实 MongoDB 集成测试。
 * <p>
 * 需要真实 MongoDB 连接（application.yml 配置的地址），
 * 测试后自动清理 chat_messages 集合中的测试数据。
 * <p>
 * 运行方式：mvn test -Dtest=MongoChatMemoryRepositoryIntegrationTest -Dintegration=true
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "integration", matches = "true")
class MongoChatMemoryRepositoryIntegrationTest {

    private static final String TEST_CONVERSATION_ID = "integration-test-conv-001";
    private static final String TEST_CONVERSATION_ID_2 = "integration-test-conv-002";

    @Autowired
    private MongoChatMemoryRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void verifyMongoAvailable() {
        // 确认 MongoDB 连接可用，否则跳过
        mongoTemplate.executeCommand("{\"ping\": 1}");
    }

    @AfterEach
    void cleanUp() {
        // 测试结束后清理测试数据，避免污染真实集合
        mongoTemplate.remove(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id")
                                .in(TEST_CONVERSATION_ID, TEST_CONVERSATION_ID_2)),
                ChatMessages.class
        );
    }

    @Test
    @DisplayName("真实MongoDB：saveAll + findByConversationId 完整读写")
    void saveAndFind() {
        // Given
        List<Message> messages = List.of(
                new UserMessage("你好，我最近心情不太好"),
                new AssistantMessage("你好！我是你的心理疏导伙伴，可以告诉我发生了什么吗？我会陪着你的。")
        );

        // When
        repository.saveAll(TEST_CONVERSATION_ID, messages);

        // Then
        List<Message> found = repository.findByConversationId(TEST_CONVERSATION_ID);
        assertNotNull(found);
        assertEquals(2, found.size());
        assertInstanceOf(UserMessage.class, found.get(0));
        assertInstanceOf(AssistantMessage.class, found.get(1));
        assertEquals("你好，我最近心情不太好", ((UserMessage) found.get(0)).getText());
        assertEquals("你好！我是你的心理疏导伙伴，可以告诉我发生了什么吗？我会陪着你的。", ((AssistantMessage) found.get(1)).getText());
    }

    @Test
    @DisplayName("真实MongoDB：同一 conversationId 重复写入为覆盖写")
    void saveAllOverwrite() {
        // Given
        List<Message> first = List.of(new UserMessage("第一轮对话"));
        List<Message> second = List.of(
                new UserMessage("第一轮对话"),
                new AssistantMessage("第一轮回复"),
                new UserMessage("第二轮对话"),
                new AssistantMessage("第二轮回复")
        );

        // When：先写一条，再覆盖写四条
        repository.saveAll(TEST_CONVERSATION_ID, first);
        repository.saveAll(TEST_CONVERSATION_ID, second);

        // Then：最终读到的是第二次写入的内容
        List<Message> found = repository.findByConversationId(TEST_CONVERSATION_ID);
        assertNotNull(found);
        assertEquals(4, found.size());
        assertEquals("第二轮对话", ((UserMessage) found.get(2)).getText());
    }

    @Test
    @DisplayName("真实MongoDB：findConversationIds 返回已保存的会话ID")
    void findConversationIdsContainsSaved() {
        // Given
        repository.saveAll(TEST_CONVERSATION_ID_2, List.of(new UserMessage("测试会话")));

        // When
        List<String> ids = repository.findConversationIds();

        // Then
        assertNotNull(ids);
        assertTrue(ids.contains(TEST_CONVERSATION_ID_2));
    }

    @Test
    @DisplayName("真实MongoDB：deleteByConversationId 删除后查询为空")
    void deleteByConversationId() {
        // Given
        repository.saveAll(TEST_CONVERSATION_ID, List.of(new UserMessage("待删除的消息")));
        assertFalse(repository.findByConversationId(TEST_CONVERSATION_ID).isEmpty());

        // When
        repository.deleteByConversationId(TEST_CONVERSATION_ID);

        // Then
        List<Message> found = repository.findByConversationId(TEST_CONVERSATION_ID);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("真实MongoDB：多会话独立存储，互不影响")
    void multipleConversationsIsolated() {
        // Given
        repository.saveAll(TEST_CONVERSATION_ID, List.of(new UserMessage("会话1的消息")));
        repository.saveAll(TEST_CONVERSATION_ID_2, List.of(new UserMessage("会话2的消息"), new AssistantMessage("会话2的回复")));

        // When
        List<Message> conv1 = repository.findByConversationId(TEST_CONVERSATION_ID);
        List<Message> conv2 = repository.findByConversationId(TEST_CONVERSATION_ID_2);

        // Then
        assertEquals(1, conv1.size());
        assertEquals("会话1的消息", ((UserMessage) conv1.get(0)).getText());
        assertEquals(2, conv2.size());
        assertEquals("会话2的消息", ((UserMessage) conv2.get(0)).getText());
    }

}
