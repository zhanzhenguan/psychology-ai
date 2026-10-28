package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.AiAnalysisTaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI分析任务 Mapper：对应 ai_analysis_task 表，只做数据访问，业务逻辑放在应用层
 */
@Mapper
public interface AiAnalysisTaskMapper extends BaseMapper<AiAnalysisTaskPO> {
}
