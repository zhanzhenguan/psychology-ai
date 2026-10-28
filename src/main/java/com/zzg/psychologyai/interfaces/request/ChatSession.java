package com.zzg.psychologyai.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * @author: zzg
 * @date: 2026年10月16日  14:56
 * @version: 1.0
 */
public class ChatSession implements Serializable {


    /**
     * 开始会话请求
     * @param initialMessage 初始消息
     * @param sessionTitle 会话标题
     */
    public record StartSessionRequest(
            @NotBlank(message = "初始消息不能为空")
            String initialMessage,
            @NotBlank(message = "会话标题不能为空")
            String sessionTitle) implements Serializable {

    }


    public record ChatStreamRequest(
            // Long 类型必须用 @NotNull；@NotBlank 仅支持 CharSequence，误用会在校验时抛 UnexpectedTypeException
            @NotNull(message = "会话ID不能为空")
            String sessionId,
            @NotBlank(message = "用户消息不能为空")
            String userMessage

    ) implements Serializable {

    }


}
