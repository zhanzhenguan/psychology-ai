package com.zzg.psychologyai.infrastructure.persistence.repository;

import com.zzg.psychologyai.domain.model.EmotionDiary;
import com.zzg.psychologyai.domain.repository.EmotionDiaryRepository;
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



    @Override
    public EmotionDiary save(EmotionDiary diary) {
        EmotionDiaryPO po = toPO(diary);
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
        return po == null ? Optional.empty() : Optional.of(toDomain(po));
    }

    @Override
    public void deleteById(Long id) {
        emotionDiaryMapper.deleteById(id);
    }

    private EmotionDiaryPO toPO(EmotionDiary d) {
        EmotionDiaryPO po = new EmotionDiaryPO();
        po.setId(d.getId());
        po.setUserId(d.getUserId());
        po.setDiaryDate(d.getDiaryDate());
        po.setMoodScore(d.getMoodScore());
        po.setDominantEmotion(d.getDominantEmotion());
        po.setEmotionTriggers(d.getEmotionTriggers());
        po.setDiaryContent(d.getDiaryContent());
        po.setSleepQuality(d.getSleepQuality());
        po.setStressLevel(d.getStressLevel());
        return po;
    }

    private EmotionDiary toDomain(EmotionDiaryPO po) {
        EmotionDiary d = new EmotionDiary();
        d.setId(po.getId());
        d.setUserId(po.getUserId());
        d.setDiaryDate(po.getDiaryDate());
        d.setMoodScore(po.getMoodScore());
        d.setDominantEmotion(po.getDominantEmotion());
        d.setEmotionTriggers(po.getEmotionTriggers());
        d.setDiaryContent(po.getDiaryContent());
        d.setSleepQuality(po.getSleepQuality());
        d.setStressLevel(po.getStressLevel());
        return d;
    }
}
