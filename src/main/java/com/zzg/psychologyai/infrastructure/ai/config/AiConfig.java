package com.zzg.psychologyai.infrastructure.ai.config;

import com.zzg.psychologyai.infrastructure.ai.repository.MongoChatMemoryRepository;
import com.zzg.psychologyai.infrastructure.ai.tool.EmotionAnalysisTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 集成配置（基础设施层）：底层模型由 spring.ai.openai.* 决定
 * （OpenAI 兼容协议，可对接 DeepSeek/通义等端点）
 */
@Configuration
public class AiConfig {

    /**
     * 聊天记忆：使用 MongoDB 持久化，替代内存实现。
     * 重启不丢上下文，多实例部署时各节点共享同一会话历史。
     */
    @Bean
    public ChatMemory chatMemory(MongoChatMemoryRepository mongoChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(mongoChatMemoryRepository)
                .maxMessages(30)
                .build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 EmotionAnalysisTool emotionAnalysisTool,
                                 ChatMemory chatMemory) {
        return ChatClient.builder(openAiChatModel)
                .defaultTools(emotionAnalysisTool)
                .defaultAdvisors()
                .defaultSystem("你是一个专业的心理疏导师，温和耐心，善于倾听，能够提供专业的心理支持和建议。回答客观、有同理心，不做医疗诊断，必要时建议寻求专业心理援助。")
                .build();
    }
}
