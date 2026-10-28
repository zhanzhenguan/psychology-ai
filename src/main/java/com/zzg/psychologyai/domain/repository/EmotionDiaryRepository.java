package com.zzg.psychologyai.domain.repository;

import com.zzg.psychologyai.domain.model.EmotionDiary;

import java.util.Optional;

/**
 * 情绪日记仓储端口
 */
public interface EmotionDiaryRepository {

    EmotionDiary save(EmotionDiary diary);

    Optional<EmotionDiary> findById(Long id);

    void deleteById(Long id);
}
