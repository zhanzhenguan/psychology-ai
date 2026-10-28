package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.ConsultationMessagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 咨询消息 Mapper：对应 consultation_message 表，只做数据访问，业务逻辑放在应用层
 */
@Mapper
public interface ConsultationMessageMapper extends BaseMapper<ConsultationMessagePO> {

    /**
     * 按会话ID查询消息列表（仅未删除，按创建时间升序）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ConsultationMessagePO> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询会话内最新一条消息（仅未删除，按创建时间、ID 倒序取第一条）
     *
     * @param sessionId 会话ID
     * @return 最新消息，无消息时返回 null
     */
    ConsultationMessagePO selectLatestBySessionId(@Param("sessionId") Long sessionId);
}
