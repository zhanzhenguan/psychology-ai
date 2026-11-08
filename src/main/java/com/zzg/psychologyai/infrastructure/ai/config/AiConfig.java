package com.zzg.psychologyai.infrastructure.ai.config;

import com.zzg.psychologyai.infrastructure.ai.repository.MongoChatMemoryRepository;
import com.zzg.psychologyai.infrastructure.ai.tool.EmotionAnalysisTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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

    /**
     * 对话用 ChatClient：注册情绪分析工具，供咨询对话在模型主动调用时把分析结果入库。
     * 标记 {@link Primary}，按类型注入时默认取本 Bean。
     */
    @Bean
    @Primary
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 EmotionAnalysisTool emotionAnalysisTool) {
        return ChatClient.builder(openAiChatModel)
                .defaultTools(emotionAnalysisTool)
                .defaultAdvisors()
                .defaultSystem("你是一个专业的心理疏导师，温和耐心，善于倾听，能够提供专业的心理支持和建议。回答客观、有同理心，不做医疗诊断，必要时建议寻求专业心理援助。")
                .build();
    }

    /**
     * 结构化分析用 ChatClient：不注册任何工具。
     * <p>
     * 情绪日记分析只依赖 prompt 约束模型输出 JSON，若复用对话用 ChatClient，
     * 模型可能误调 {@code mood_analysis_now} 工具，而该工具依赖 ToolContext
     * （本调用链无会话上下文），会抛“ToolContext is required by the method as an argument”。
     */
    @Bean
    public ChatClient analysisChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}
