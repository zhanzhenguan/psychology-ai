package com.zzg.psychologyai.interfaces.assembler;

import com.zzg.psychologyai.application.command.CreateEmotionDiaryCommand;
import com.zzg.psychologyai.infrastructure.security.CurrentUserUtils;
import com.zzg.psychologyai.interfaces.request.CreateDiaryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author: zzg
 * @date: 2026年10月16日  12:05
 * @version: 1.0
 */

public final class EmotionDiaryAssembler {

    /**
     * 注册请求 → 注册命令
     *
     * @param request 注册请求体
     * @return 注册命令
     */
    public static CreateEmotionDiaryCommand toCommand(CreateDiaryRequest request){
        return new CreateEmotionDiaryCommand(
                CurrentUserUtils.getCurrentUserId(),
                request.diaryDate(),
                request.moodScore(),
                request.dominantEmotion(),
                request.emotionTriggers(),
                request.diaryContent(),
                request.sleepQuality(),
                request.stressLevel()
        );
    }



}
