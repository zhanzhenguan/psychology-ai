package com.zzg.psychologyai.application.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: zzg
 * @date: 2026年10月17日  00:05
 * @version: 1.0
 */

public record SessionMessageDTO(
        Long sessionId,
        String initialMessage,
        Long userHash

) implements Serializable {
}
