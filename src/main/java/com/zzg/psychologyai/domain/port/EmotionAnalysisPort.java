package com.zzg.psychologyai.domain.port;

import com.zzg.psychologyai.domain.model.EmotionDiary;

import java.util.Optional;

/**
 * 情绪分析防腐端口：应用层只声明“对情绪日记做 AI 分析”的能力，
 * 不依赖具体大模型客户端，由基础设施层适配实现
 */
public interface EmotionAnalysisPort {

    /**
     * 分析情绪日记
     *
     * @param diary 情绪日记聚合
     * @return 结构化分析结果 JSON；分析失败或结果无法解析时返回 empty
     */
    Optional<String> analyze(EmotionDiary diary);
}
