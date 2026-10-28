package com.zzg.psychologyai.interfaces.response;

import com.zzg.psychologyai.domain.model.ConsultationSession;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: zzg
 * @date: 2026年10月16日  14:53
 * @version: 1.0
 */
@Data
@Builder
public class NewSessionResponse implements Serializable {

    private Long expiryTime;
    private String initialMessage;
    private Integer messageCount;
    private String sessionId;
    private Long startTime;
    /**
     * ACTIVE: 活跃会话
     * TEMP: 临时会话
     */
    private String status;
    private String userHash;


    public static NewSessionResponse of(Long sessionId, String initialMessage, Long userHash) {
        return NewSessionResponse.builder()
                .expiryTime(System.currentTimeMillis()+86400000)//24小时
                .initialMessage(initialMessage)
                .messageCount(1)
                .sessionId("session_"+sessionId)
                .startTime(System.currentTimeMillis())
                .status("ACTIVE")
                .userHash(userHash.toString())
                .build();
    }

}
