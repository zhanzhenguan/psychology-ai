package com.zzg.psychologyai.application;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzg.psychologyai.application.command.StartSessionCommand;
import com.zzg.psychologyai.application.dto.EmotionAnalyzeDTO;
import com.zzg.psychologyai.application.dto.SessionMessageDTO;
import com.zzg.psychologyai.application.view.ChatMessageView;
import com.zzg.psychologyai.application.view.ChatSessionView;
import com.zzg.psychologyai.common.enums.PromptManager;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.common.result.PageResult;
import com.zzg.psychologyai.domain.enums.MessageTypeEnum;
import com.zzg.psychologyai.domain.enums.SenderTypeEnum;
import com.zzg.psychologyai.domain.model.ConsultationMessage;
import com.zzg.psychologyai.domain.model.ConsultationSession;
import com.zzg.psychologyai.domain.repository.ConsultationMessageRepository;
import com.zzg.psychologyai.domain.repository.ConsultationSessionRepository;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationSessionMapper;
import com.zzg.psychologyai.infrastructure.persistence.read.ConsultationSessionDO;
import com.zzg.psychologyai.infrastructure.redis.RedisKeys;
import com.zzg.psychologyai.infrastructure.security.CurrentUserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 聊天会话应用服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationMessageAppService {

    /** ChatMemory 会话ID前缀，与前端会话ID隔离 */
    private static final String CONVERSATION_ID_PREFIX = "conversation_";

    /** 落库的 AI 模型标识 */
    private static final String AI_MODEL = "open-ai";

    /** 流式接口单用户最大并发数 */
    private static final long MAX_CONCURRENT_STREAMS = 3;

    /** 流式接口频率限制：窗口内最大请求数 */
    private static final long MAX_REQUESTS_PER_WINDOW = 20;

    /** 频率限制窗口时长（秒） */
    private static final long RATE_LIMIT_WINDOW_SECONDS = 60;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    private final ConsultationSessionRepository consultationSessionRepository;

    private final ConsultationMessageRepository consultationMessageRepository;

    /** 读侧（CQRS Query）：会话列表摘要直查 Mapper，不走领域仓储 */
    private final ConsultationSessionMapper consultationSessionMapper;

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;


    @Transactional(rollbackFor = Exception.class)
    public SessionMessageDTO start(StartSessionCommand command) {
        Long userId = CurrentUserUtils.getCurrentUserId();
        if(userId == null){
            throw new BizException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        ConsultationSession session = new ConsultationSession();
        // 归属用户必须写入 userId；此前误写为 setId(userId)，会导致主键被显式写成用户ID
        session.setUserId(userId);
        if(StrUtil.isBlank(command.title())){
            session.setSessionTitle("摆渡AI-"+ DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
        } else {
            session.setSessionTitle(command.title());
        }
        session.setStartedAt(LocalDateTime.now());
        session.setLastEmotionUpdatedAt(LocalDateTime.now());
        ConsultationSession saved = consultationSessionRepository.save(session);

        ConsultationMessage message = new ConsultationMessage();
        message.setSessionId(saved.getId());
        message.setSenderType(SenderTypeEnum.USER);
        message.setMessageType(MessageTypeEnum.TEXT);
        message.setContent(command.initialMessage());
        message.setCreateTime(LocalDateTime.now());
        consultationMessageRepository.save(message);

        return new SessionMessageDTO(saved.getId(), command.initialMessage(), userId);
    }


    /**
     * 分页查询当前登录用户的会话列表摘要。
     * <p>
     * 返回字段：sessionTitle、startedAt、userNickname、lastMessageContent、
     * lastMessageTime（最后一条消息时间）、messageCount（消息总条数）、
     * durationMinutes（startedAt 与最后一条消息时间之间的分钟数）。
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 分页结果 {records, total, size, current, pages}
     */
    public PageResult<ChatSessionView> page(long page, long size) {
        // 会话列表只查本人数据，userId 必须来自登录上下文而非请求参数（防越权）
        Long userId = CurrentUserUtils.getCurrentUserId();
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        QueryWrapper wrapper = new QueryWrapper<>();
        wrapper.eq("s.user_id", userId);
        wrapper.orderByDesc("m.last_message_time");
        wrapper.orderByDesc("s.id");
        Page<ConsultationSessionDO> result = consultationSessionMapper.selectSessionPage(new Page<>(page, size), wrapper);
        List<ChatSessionView> records = result.getRecords().stream().map(ChatSessionView::from).toList();
        return PageResult.of(records,result.getTotal(),result.getSize(),result.getCurrent(),result.getPages());
    }


    @Transactional(rollbackFor = Exception.class)
    public void delete(Long sessionId) {
        // 越权防护：会话必须存在且属于当前登录用户，否则按存在性/归属分别报 404/403
        ConsultationSession session = consultationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "会话不存在"));
        Long currentUserId = CurrentUserUtils.getCurrentUserId();
        if (!ObjectUtil.equals(session.getUserId(), currentUserId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权操作该会话");
        }
        consultationSessionRepository.deleteById(sessionId);
        consultationMessageRepository.deleteBySessionId(sessionId);
    }


    public List<ChatMessageView> messages(Long sessionId) {
        // 越权防护：先校验会话归属，再返回消息列表，防止读取他人会话内容
        ConsultationSession session = consultationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "会话不存在"));
        Long currentUserId = CurrentUserUtils.getCurrentUserId();
        if (!ObjectUtil.equals(session.getUserId(), currentUserId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        return consultationSessionRepository.findMessagesBySessionId(sessionId).stream()
                .map(ChatMessageView::from).toList();
    }

    /**
     * AI 流式心理咨询对话。
     * <p>
     * 注意：本方法<b>不能</b>加 {@code @Transactional}——方法返回 Flux 后方法栈即结束，
     * 事务在首个分片产出前就已提交，而落库动作发生在后续 Reactor 回调线程（事务上下文已不存在）；
     * 单条 insert 由 MyBatis 自动提交保证原子性。
     * <p>
     * userId 必须由 Controller 在 Web 请求线程取出后显式传入：
     * Reactor/Netty 回调线程读不到 {@code SecurityContextHolder} 的 ThreadLocal。
     *
     * @param userId      当前登录用户ID（请求线程解析）
     * @param sessionId   会话ID（前端直接传数值型主键）
     * @param userMessage 用户本轮消息
     * @return AI 回复文本分片流
     */
    public Flux<String> streamPsychologyChat(Long userId, Long sessionId, String userMessage) {

        return Flux.create(sink -> {
            // 0. 限流：先检查频率窗口，再检查并发占用，通过后立即占用并发槽位
            checkRateLimit(userId);
            String concurrentKey = RedisKeys.chatStreamLimit(userId) + ":concurrent";
            if (!tryAcquireConcurrent(concurrentKey)) {
                sink.error(new BizException(ResultCode.TOO_MANY_REQUESTS, "当前对话请求过多，请稍后再试"));
                return;
            }

            // 1. 会话存在性 + 归属校验，防止越权访问他人会话（IDOR）
            Optional<ConsultationSession> sessionOpt = consultationSessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                releaseConcurrent(concurrentKey);
                sink.error(new BizException(ResultCode.NOT_FOUND, "会话不存在"));
                return;
            }
            if (!ObjectUtil.equals(sessionOpt.get().getUserId(), userId)) {
                releaseConcurrent(concurrentKey);
                sink.error(new BizException(ResultCode.FORBIDDEN, "无权访问该会话"));
                return;
            }

            // 2. 初始消息去重：start 接口已落库过首条用户消息，内容一致时不再重复插入
            Optional<ConsultationMessage> latest = consultationMessageRepository.findLatestBySessionId(sessionId);
            boolean initialMessageAlreadySaved = latest
                    .map(m -> Objects.equals(m.getSenderType(), SenderTypeEnum.USER.getValue())
                            && userMessage.equals(m.getContent()))
                    .orElse(false);
            if (!initialMessageAlreadySaved) {
                consultationMessageRepository.saveUserMessage(sessionId, userMessage, null);
            }

            //如何有聊天过 重新写入会话信息，更新会话开始时间

            // 3. 组装多轮上下文：系统提示词 + ChatMemory 历史 + 本轮用户消息。
            //    ChatClient 未注册 MessageChatMemoryAdvisor，CONVERSATION_ID 参数无人消费，
            //    因此历史记忆必须手动取回，用户/AI 消息手动登记。
            String conversationId = CONVERSATION_ID_PREFIX + sessionId;
            List<Message> promptMessages = new ArrayList<>();
            promptMessages.add(new SystemMessage(PromptManager.PSYCHOLOGICAL_SUPPORT_SYSTEM_PROMPT));
            promptMessages.addAll(chatMemory.get(conversationId));
            promptMessages.add(new UserMessage(userMessage));
            chatMemory.add(conversationId, List.of(new UserMessage(userMessage)));

            // 4. 订阅 AI 流式回复
            StringBuilder aiFullAnswer = new StringBuilder();
            Disposable upstream = chatClient.prompt(new Prompt(promptMessages))
                    .toolContext(Map.of("sessionId", sessionId, "userId", userId))
                    .stream()
                    .content()
                    .doOnNext(fragment -> {
                        // 累计完整回复，同时逐分片推给前端
                        aiFullAnswer.append(fragment);
                        sink.next(fragment);
                    })
                    .doOnComplete(() -> {
                        try {
                            String answer = aiFullAnswer.toString();
                            // AI 回复以 sender_type=2 落库，模型名写入 ai_model（此前错存为用户消息/情绪标签）
                            consultationMessageRepository.saveAssistantMessage(sessionId, answer, AI_MODEL);
                            chatMemory.add(conversationId, List.of(new AssistantMessage(answer)));
                        } catch (Exception e) {
                            // 落库失败只记录日志，不能让异常阻断 SSE 正常收尾，否则前端会一直挂起
                            log.error("AI流式回复落库失败, sessionId={}", sessionId, e);
                        } finally {
                            releaseConcurrent(concurrentKey);
                            sink.complete();
                        }
                    })
                    .doOnError(error -> {
                        log.error("AI流式对话异常, sessionId={}", sessionId, error);
                        releaseConcurrent(concurrentKey);
                        sink.error(error);
                    })
                    .subscribe();

            // 5. 客户端断连/下游取消时同步取消上游 AI 请求，避免模型侧空转计费、
            //    以及向已取消的 sink 继续发射
            sink.onCancel(() -> {
                upstream.dispose();
                releaseConcurrent(concurrentKey);
            });
            sink.onDispose(() -> {
                upstream.dispose();
                releaseConcurrent(concurrentKey);
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * 频率限制：固定窗口计数，窗口内超过阈值则抛 429
     */
    private void checkRateLimit(Long userId) {
        String key = RedisKeys.chatStreamLimit(userId) + ":rate";
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(RATE_LIMIT_WINDOW_SECONDS));
        }
        if (count != null && count > MAX_REQUESTS_PER_WINDOW) {
            throw new BizException(ResultCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }
    }

    /**
     * 尝试占用并发槽位：当前并发数未达上限时 +1 并返回 true
     */
    private boolean tryAcquireConcurrent(String key) {
        Long current = stringRedisTemplate.opsForValue().increment(key);
        if (current != null && current == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(300));
        }
        if (current != null && current > MAX_CONCURRENT_STREAMS) {
            stringRedisTemplate.opsForValue().decrement(key);
            return false;
        }
        return true;
    }

    /**
     * 释放并发槽位：流结束/取消/异常时调用，避免槽位泄漏
     */
    private void releaseConcurrent(String key) {
        stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 查询会话最新一次情绪分析结果。
     * <p>
     * 越权防护：必须校验会话归属，防止读取他人会话的情绪分析数据。
     *
     * @param sessionId 会话ID（可带 "session_" 前缀）
     * @return 情绪分析结果；会话不存在或解析失败时返回 empty
     */
    public Optional<EmotionAnalyzeDTO> emotion(String sessionId) {
        Long id = Long.valueOf(sessionId.replace("session_", ""));
        ConsultationSession session = consultationSessionRepository.findById(id)
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "会话不存在"));
        Long currentUserId = CurrentUserUtils.getCurrentUserId();
        if (!ObjectUtil.equals(session.getUserId(), currentUserId)) {
            throw new BizException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        String lastEmotionAnalysis = session.getLastEmotionAnalysis();
        if (StrUtil.isBlank(lastEmotionAnalysis)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(lastEmotionAnalysis, EmotionAnalyzeDTO.class));
        } catch (JsonProcessingException e) {
            log.error("解析情绪标签失败, sessionId={},lastEmotionAnalysis={}", sessionId, lastEmotionAnalysis, e);
            return Optional.empty();
        }
    }
}
