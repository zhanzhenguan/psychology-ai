package com.zzg.psychologyai.interfaces.controller;

import com.zzg.psychologyai.application.EmotionDiaryAppService;
import com.zzg.psychologyai.application.command.CreateEmotionDiaryCommand;
import com.zzg.psychologyai.application.view.EmotionDiaryView;
import com.zzg.psychologyai.common.result.PageResult;
import com.zzg.psychologyai.common.result.Result;
import com.zzg.psychologyai.infrastructure.persistence.read.EmotionDiaryDO;
import com.zzg.psychologyai.interfaces.assembler.EmotionDiaryAssembler;
import com.zzg.psychologyai.interfaces.request.CreateDiaryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 情绪日记接口（对应 emotion_diary 表）
 */
@RestController
@RequestMapping("/emotion-diary")
@RequiredArgsConstructor
public class EmotionDiaryController {

    private final EmotionDiaryAppService emotionDiaryAppService;

    /**
     * 创建情绪日记
     * @param request
     * @return
     */
    @PostMapping
    public Result<EmotionDiaryView> create(@Valid @RequestBody CreateDiaryRequest request) {
        CreateEmotionDiaryCommand command = EmotionDiaryAssembler.toCommand(request);
        return Result.success(emotionDiaryAppService.create(command));
    }

    @GetMapping("/admin/page")
    public Result<PageResult<EmotionDiaryDO>> adminPage(
            @RequestParam(defaultValue = "1") long currentPage,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "moodScoreRange", required = false) String moodScoreRange) {
        PageResult<EmotionDiaryDO> pageResult = emotionDiaryAppService.adminPage(currentPage, size, userId, moodScoreRange);
        return Result.success(pageResult);
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        emotionDiaryAppService.delete(id);
        return Result.success();
    }
}
