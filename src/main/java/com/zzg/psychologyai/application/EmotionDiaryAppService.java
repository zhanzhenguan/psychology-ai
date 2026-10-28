package com.zzg.psychologyai.application;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzg.psychologyai.application.command.CreateEmotionDiaryCommand;
import com.zzg.psychologyai.application.view.EmotionDiaryView;
import com.zzg.psychologyai.common.result.PageResult;
import com.zzg.psychologyai.common.enums.ResultCode;
import com.zzg.psychologyai.common.exception.BizException;
import com.zzg.psychologyai.domain.enums.AnalysisStatusEnum;
import com.zzg.psychologyai.domain.model.EmotionDiary;
import com.zzg.psychologyai.domain.repository.EmotionDiaryRepository;
import com.zzg.psychologyai.infrastructure.persistence.mapper.EmotionDiaryMapper;
import com.zzg.psychologyai.infrastructure.persistence.mapper.UserMapper;
import com.zzg.psychologyai.infrastructure.persistence.read.EmotionDiaryDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * 情绪日记应用服务实现
 */
@Service
@RequiredArgsConstructor
public class EmotionDiaryAppService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EmotionDiaryRepository emotionDiaryRepository;
    private final EmotionDiaryMapper emotionDiaryMapper;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    public EmotionDiaryView create(CreateEmotionDiaryCommand command) {
        EmotionDiary diary = new EmotionDiary();
        diary.setUserId(command.userId());
        diary.setDiaryDate(command.diaryDate());
        diary.setMoodScore(command.moodScore());
        diary.setDominantEmotion(command.dominantEmotion());
        diary.setEmotionTriggers(command.emotionTriggers());
        diary.setDiaryContent(command.diaryContent());
        diary.setSleepQuality(command.sleepQuality());
        diary.setStressLevel(command.stressLevel());
        diary.setAnalysisStatus(AnalysisStatusEnum.PENDING);
        EmotionDiary save = emotionDiaryRepository.save(diary);
        return EmotionDiaryView.from(save);
    }

    public PageResult<EmotionDiaryDO> adminPage(long currentPage, long size, Long userId, String moodScoreRange) {
        QueryWrapper wrapper = new QueryWrapper<>();
        if(StrUtil.isNotBlank(moodScoreRange)){
            wrapper.between("mood_score", moodScoreRange.split("-")[0], moodScoreRange.split("-")[1]);
        }
        wrapper.eq(userId != null, "b.user_id", userId);
        wrapper.orderByDesc("a.diary_date");
        Page<EmotionDiaryDO> page = emotionDiaryMapper.adminPage(new Page(currentPage, size), wrapper);
        page.getRecords().forEach(record ->{
            record.setContentLength(record.getDiaryContent().length());
            record.setHasAiEmotionAnalysis(record.getAiAnalysisStatus().equals(AnalysisStatusEnum.COMPLETED));
            record.setCreatedAt(record.getCreateTime());
            record.setUpdatedAt(record.getUpdateTime());
        });
        return PageResult.of(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        emotionDiaryRepository.findById(id).orElseThrow(() -> new BizException(ResultCode.NOT_FOUND, "日记不存在"));
        emotionDiaryRepository.deleteById(id);
    }


}
