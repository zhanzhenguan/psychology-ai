package com.zzg.psychologyai.infrastructure.ai.repository;

import com.zzg.psychologyai.infrastructure.mongo.po.ChatMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MongoChatMemoryRepository 单元测试（纯 Mockito，不启动 Spring Context）。
 * 验证 Repository 的 CRUD 语义与参数校验逻辑。
 */
@ExtendWith(MockitoExtension.class)
class MongoChatMemoryRepositoryTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MongoChatMemoryRepository repository;

    @Test
    @DisplayName("saveAll：正常写入，调用 mongoTemplate.save 且 conversationId 正确")
    void saveAllNormal() {
        // Given
        List<Message> messages = List.of(
                new UserMessage("你好"),
                new AssistantMessage("你好！我是你的心理疏导伙伴")
        );
        ArgumentCaptor<ChatMessages> captor = ArgumentCaptor.forClass(ChatMessages.class);

        // When
        repository.saveAll("conv-001", messages);

        // Then
        verify(mongoTemplate, times(1)).save(captor.capture());
        ChatMessages saved = captor.getValue();
        assertEquals("conv-001", saved.conversationId());
        assertEquals(2, saved.messages().size());
        assertNotNull(saved.lastUpdatedAt());
    }

    @Test
    @DisplayName("saveAll：同一 conversationId 重复写入为覆盖写（对齐 InMemory 语义）")
    void saveAllOverwrite() {
        // Given
        List<Message> first = List.of(new UserMessage("第一条"));
        List<Message> second = List.of(
                new UserMessage("第一条"),
                new AssistantMessage("回复"),
                new UserMessage("第二条")
        );

        // When
        repository.saveAll("conv-001", first);
        repository.saveAll("conv-001", second);

        // Then：第二次调用覆盖第一次，最终保存的是 second
        verify(mongoTemplate, times(2)).save(any(ChatMessages.class));
    }

    @Test
    @DisplayName("saveAll：conversationId 为空字符串时抛 IllegalArgumentException")
    void saveAllWithEmptyConversationIdThrows() {
        List<Message> messages = List.of(new UserMessage("内容"));
        assertThrows(IllegalArgumentException.class, () -> repository.saveAll("", messages));
    }

    @Test
    @DisplayName("saveAll：messages 为 null 时抛 IllegalArgumentException")
    void saveAllWithNullMessagesThrows() {
        assertThrows(IllegalArgumentException.class, () -> repository.saveAll("conv-001", null));
    }

    @Test
    @DisplayName("saveAll：messages 含 null 元素时抛 IllegalArgumentException")
    void saveAllWithNullElementThrows() {
        List<Message> messagesWithNull = new java.util.ArrayList<>();
        messagesWithNull.add(new UserMessage("正常消息"));
        messagesWithNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> repository.saveAll("conv-001", messagesWithNull));
    }

    @Test
    @DisplayName("findByConversationId：正常返回，消息数量与类型正确")
    void findByConversationIdNormal() {
        // Given
        List<Message> expected = List.of(
                new UserMessage("你好"),
                new AssistantMessage("你好！")
        );
        ChatMessages doc = new ChatMessages("conv-001", expected, java.time.Instant.now());
        when(mongoTemplate.findById("conv-001", ChatMessages.class)).thenReturn(doc);

        // When
        List<Message> result = repository.findByConversationId("conv-001");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertInstanceOf(UserMessage.class, result.get(0));
        assertInstanceOf(AssistantMessage.class, result.get(1));
        assertEquals("你好", ((UserMessage) result.get(0)).getText());
    }

    @Test
    @DisplayName("findByConversationId：会话不存在时返回空列表，不抛异常")
    void findByConversationIdNotFound() {
        // Given
        when(mongoTemplate.findById("nonexistent", ChatMessages.class)).thenReturn(null);

        // When
        List<Message> result = repository.findByConversationId("nonexistent");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByConversationId：conversationId 为空字符串时抛 IllegalArgumentException")
    void findByConversationIdEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> repository.findByConversationId(""));
    }

    @Test
    @DisplayName("findConversationIds：返回所有会话 ID 列表")
    void findConversationIdsReturnsAll() {
        // Given
        when(mongoTemplate.findDistinct(any(Query.class), eq("conversationId"), eq(ChatMessages.class), eq(String.class)))
                .thenReturn(List.of("conv-001", "conv-002", "conv-003"));

        // When
        List<String> ids = repository.findConversationIds();

        // Then
        assertNotNull(ids);
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(List.of("conv-001", "conv-002", "conv-003")));
    }

    @Test
    @DisplayName("findConversationIds：无数据时返回空列表")
    void findConversationIdsEmpty() {
        // Given
        when(mongoTemplate.findDistinct(any(Query.class), eq("conversationId"), eq(ChatMessages.class), eq(String.class)))
                .thenReturn(List.of());

        // When
        List<String> ids = repository.findConversationIds();

        // Then
        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    @Test
    @DisplayName("deleteByConversationId：正常删除，调用 mongoTemplate.remove")
    void deleteByConversationIdNormal() {
        // Given
        when(mongoTemplate.remove(any(Query.class), eq(ChatMessages.class)))
                .thenReturn(null);

        // When
        repository.deleteByConversationId("conv-001");

        // Then
        verify(mongoTemplate, times(1)).remove(any(Query.class), eq(ChatMessages.class));
    }

    @Test
    @DisplayName("deleteByConversationId：conversationId 为空字符串时抛 IllegalArgumentException")
    void deleteByConversationIdEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> repository.deleteByConversationId(""));
    }

}
