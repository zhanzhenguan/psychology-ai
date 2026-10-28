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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

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

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    private final ConsultationSessionRepository consultationSessionRepository;

    private final ConsultationMessageRepository consultationMessageRepository;

    /** 读侧（CQRS Query）：会话列表摘要直查 Mapper，不走领域仓储 */
    private final ConsultationSessionMapper consultationSessionMapper;

    private final ObjectMapper objectMapper;


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
    public PageResult<ChatSessionView> page(long page, long size, long pageNum, long pageSize) {
        QueryWrapper wrapper = new QueryWrapper<>();
        // 会话列表只查本人数据，userId 必须来自登录上下文而非请求参数（防越权）
        if (pageNum > 0 && pageSize > 0) {
            Long userId = CurrentUserUtils.getCurrentUserId();
            if (userId == null) {
                throw new BizException(ResultCode.UNAUTHORIZED, "用户未登录");
            }
            page = pageNum;
            size = pageSize;
            wrapper.eq(userId != null, "s.user_id", userId);
        }
        wrapper.orderByDesc("m.last_message_time");
        wrapper.orderByDesc("s.id");
        Page<ConsultationSessionDO> result = consultationSessionMapper.selectSessionPage(new Page<>(page, size), wrapper);
        List<ChatSessionView> records = result.getRecords().stream().map(ChatSessionView::from).toList();
        return PageResult.of(records,result.getTotal(),result.getSize(),result.getCurrent(),result.getPages());
    }


    @Transactional(rollbackFor = Exception.class)
    public void delete(Long sessionId) {
        consultationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "会话不存在"));
        consultationSessionRepository.deleteById(sessionId);
        consultationMessageRepository.deleteBySessionId(sessionId);
    }


    public List<ChatMessageView> messages(Long sessionId) {
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
            // 1. 会话存在性 + 归属校验，防止越权访问他人会话（IDOR）
            Optional<ConsultationSession> sessionOpt = consultationSessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                sink.error(new BizException(ResultCode.NOT_FOUND, "会话不存在"));
                return;
            }
            if (!ObjectUtil.equals(sessionOpt.get().getUserId(), userId)) {
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
                            sink.complete();
                        }
                    })
                    .doOnError(error -> {
                        log.error("AI流式对话异常, sessionId={}", sessionId, error);
                        sink.error(error);
                    })
                    .subscribe();

            // 5. 客户端断连/下游取消时同步取消上游 AI 请求，避免模型侧空转计费、
            //    以及向已取消的 sink 继续发射
            sink.onCancel(upstream::dispose);
            sink.onDispose(upstream::dispose);
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /**
     * 分析会话内所有消息，返回情绪标签
     *
     * @param sessionId
     * @return
     */
    public Optional<EmotionAnalyzeDTO> emotion(String sessionId) {
        Long id = Long.valueOf(sessionId.replace("session_", ""));
        Optional<ConsultationSession> sessionOpt = consultationSessionRepository.findById(id);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }
        ConsultationSession session = sessionOpt.get();
        String lastEmotionAnalysis = session.getLastEmotionAnalysis();
        try {
            EmotionAnalyzeDTO dto = objectMapper.readValue(lastEmotionAnalysis, EmotionAnalyzeDTO.class);
            return Optional.ofNullable(dto);
        } catch (JsonProcessingException e) {
            log.error("解析情绪标签失败, sessionId={},lastEmotionAnalysis={}", sessionId, lastEmotionAnalysis, e);
        }
        return Optional.empty();
    }
}
