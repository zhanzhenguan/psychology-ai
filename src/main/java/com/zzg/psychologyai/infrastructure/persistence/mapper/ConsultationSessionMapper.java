package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationSessionPO;
import com.zzg.psychologyai.infrastructure.persistence.read.ConsultationSessionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 咨询会话 Mapper：对应 consultation_session 表，只做数据访问，业务逻辑放在应用层
 */
@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSessionPO> {

    /**
     * 分页查询会话列表摘要（含用户昵称、消息条数、最后消息、会话时长）。
     * 由 MyBatis-Plus 分页插件自动追加 LIMIT 并回填 total。
     *
     * @param page   分页参数
     * @param wrapper 查询条件
     * @return 分页结果，records 为查询投影
     */
    Page<ConsultationSessionDO> selectSessionPage(Page<ConsultationSessionDO> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);
}
