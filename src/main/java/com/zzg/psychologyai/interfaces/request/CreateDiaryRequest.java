package com.zzg.psychologyai.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 创建情绪日记请求（userId 由后端从登录态注入，前端不传）
 */
public record CreateDiaryRequest(
        @NotNull(message = "日记日期不能为空")
        LocalDate diaryDate,

        @NotNull(message = "情绪评分不能为空")
        Integer moodScore,

        @NotBlank(message = "主导情绪不能为空")
        String dominantEmotion,

        @NotBlank(message = "情绪触发因素不能为空")
        String emotionTriggers,

        @NotBlank(message = "日记内容不能为空")
        String diaryContent,

        @NotNull(message = "睡眠质量不能为空")
        Integer sleepQuality,

        @NotNull(message = "压力水平不能为空")
        Integer stressLevel
) implements Serializable {


}
