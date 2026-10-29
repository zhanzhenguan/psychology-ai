package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.zzg.psychologyai.domain.model.EmotionDiary;
import com.zzg.psychologyai.domain.repository.EmotionDiaryRepository;
import com.zzg.psychologyai.infrastructure.persistence.converter.EmotionDiaryConverter;
import com.zzg.psychologyai.infrastructure.persistence.mapper.EmotionDiaryMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.EmotionDiaryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 情绪日记仓储实现
 */
@Repository
@RequiredArgsConstructor
public class EmotionDiaryRepositoryImpl implements EmotionDiaryRepository {

    private final EmotionDiaryMapper emotionDiaryMapper;
    private final EmotionDiaryConverter emotionDiaryConverter;

    @Override
    public EmotionDiary save(EmotionDiary diary) {
        EmotionDiaryPO po = emotionDiaryConverter.toPO(diary);
        if (diary.getId() == null) {
            emotionDiaryMapper.insert(po);
            diary.setId(po.getId());
        } else {
            emotionDiaryMapper.updateById(po);
        }
        return diary;
    }

    @Override
    public Optional<EmotionDiary> findById(Long id) {
        EmotionDiaryPO po = emotionDiaryMapper.selectById(id);
        return po == null ? Optional.empty() : Optional.of(emotionDiaryConverter.toDomain(po));
    }

    @Override
    public void deleteById(Long id) {
        emotionDiaryMapper.deleteById(id);
    }
}
