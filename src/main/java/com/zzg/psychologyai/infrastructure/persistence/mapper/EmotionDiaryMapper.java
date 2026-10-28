package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzg.psychologyai.infrastructure.persistence.po.EmotionDiaryPO;
import com.zzg.psychologyai.infrastructure.persistence.read.EmotionDiaryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 情绪日记 Mapper
 */
@Mapper
public interface EmotionDiaryMapper extends BaseMapper<EmotionDiaryPO> {

    Page<EmotionDiaryDO> adminPage(Page<EmotionDiaryDO> objectPage, @Param(Constants.WRAPPER) QueryWrapper wrapper);


}
