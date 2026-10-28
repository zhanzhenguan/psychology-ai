package com.zzg.psychologyai.interfaces.controller;

import cn.hutool.json.JSONUtil;
import com.zzg.psychologyai.application.ConsultationMessageAppService;
import com.zzg.psychologyai.application.command.StartSessionCommand;
import com.zzg.psychologyai.application.dto.SessionMessageDTO;
import com.zzg.psychologyai.application.view.ChatMessageView;
import com.zzg.psychologyai.application.view.ChatSessionView;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.common.result.PageResult;
import com.zzg.psychologyai.common.result.Result;
import com.zzg.psychologyai.infrastructure.persistence.mapper.ConsultationSessionMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationSessionPO;
import com.zzg.psychologyai.infrastructure.security.CurrentUserUtils;
import com.zzg.psychologyai.interfaces.request.ChatSession;
import com.zzg.psychologyai.interfaces.response.NewSessionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 心理聊天会话接口
 */
@RestController
@RequestMapping("/psychological-chat")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ConsultationMessageAppService chatSessionAppService;
    private final ConsultationSessionMapper chatSessionMapper;

    @PostMapping("/session/start")
    public Result<NewSessionResponse> start(@RequestBody ChatSession.StartSessionRequest request) {
        SessionMessageDTO start = chatSessionAppService.start(new StartSessionCommand(request.initialMessage(), request.sessionTitle()));
        return Result.success(NewSessionResponse.of(start.sessionId(), start.initialMessage(), start.userHash()));
    }

    /**
     * 分页查询当前登录用户的会话列表。
     * <p>
     * 路径：GET /api/psychological-chat/sessions
     *pageNum
     * 1
     * pageSize
     * 10
     * @param page 页码（默认 1）
     * @param size 每页条数（默认 10）
     * @return 分页结果：records 含 sessionTitle/startedAt/userNickname/
     *         lastMessageContent/lastMessageTime/messageCount/durationMinutes
     */
    @GetMapping("/sessions")
    public Result<PageResult<ChatSessionView>> page(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size,
                                                    @RequestParam(defaultValue = "0") long pageNum,
                                                    @RequestParam(defaultValue = "0") long pageSize) {
        PageResult<ChatSessionView> pageResult = chatSessionAppService.page(page, size, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> delete(@PathVariable Long sessionId) {
        chatSessionAppService.delete(sessionId);
        return Result.success();
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessageView>> messages(@PathVariable Long sessionId) {
        return Result.success(chatSessionAppService.messages(sessionId));
    }

    /**
     * 会话情绪分析结果：查 consultation_session.last_emotion_analysis（JSON）
     */
    @GetMapping("/session/{sessionId}/emotion")
    public Result<Map<String, Object>> emotions(@PathVariable String sessionId) {
        Long id = Long.valueOf(sessionId.replace("session_", ""));
        ConsultationSessionPO po = chatSessionMapper.selectById(id);
        if (po == null) {
            return Result.success(Map.of());
        }
        Map<String, Object> data = Map.of(
                "sessionId", sessionId,
                "lastEmotionAnalysis", po.getLastEmotionAnalysis() == null ? "" : po.getLastEmotionAnalysis(),
                "lastEmotionUpdatedAt", po.getLastEmotionUpdatedAt() == null ? "" : po.getLastEmotionUpdatedAt().toString()
        );
        return Result.success(data);
    }


    /**
     * 流式心理聊天会话（SSE）。
     * todo 目前问题 继续聊天时，会话自动关闭
     * @param request 请求体：sessionId 会话ID、initialMessage 用户消息（JSON）
     * @return SSE 事件流
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@Valid @RequestBody ChatSession.ChatStreamRequest request) {
        // 必须在 Web 请求线程内读取登录用户：进入 Reactor 回调线程后 SecurityContext 的 ThreadLocal 已失效
        Long currentUserId = CurrentUserUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Flux.just(buildErrorEvent(Result.fail(ResultCode.UNAUTHORIZED)));
        }
        Long sessionId = Long.valueOf(request.sessionId().replace("session_", ""));
        return chatSessionAppService.streamPsychologyChat(currentUserId, sessionId, request.userMessage())
                .map(fragment -> ServerSentEvent.<String>builder()
                        .event("message")
                        .data(JSONUtil.toJsonStr(Result.success(Map.of("content", fragment, "type", "normal"))))
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data("{}")
                        .build()))
                // 响应式流的异常不会经过 @RestControllerAdvice，这里统一转成 error 事件，避免直接断连
                .onErrorResume(this::buildErrorEventFlux)
                // 每个分片延迟 50ms，形成打字机输出效果
                .delayElements(Duration.ofMillis(50));
    }

    /**
     * 将流内异常映射为单个 error SSE 事件：业务异常透出业务码与文案，其余异常统一兜底
     */
    private Flux<ServerSentEvent<String>> buildErrorEventFlux(Throwable error) {
        Result<Void> result = error instanceof BizException biz
                ? Result.fail(biz.getCode(), biz.getMessage())
                : Result.fail(ResultCode.ERROR);
        return Flux.just(buildErrorEvent(result));
    }

    /**
     * 构造 error 事件
     */
    private ServerSentEvent<String> buildErrorEvent(Result<?> result) {
        return ServerSentEvent.<String>builder()
                .event("error")
                .data(JSONUtil.toJsonStr(result))
                .build();
    }


}
