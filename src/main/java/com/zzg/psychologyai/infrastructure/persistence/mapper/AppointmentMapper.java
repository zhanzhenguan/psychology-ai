package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.AppointmentPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约 Mapper：对应 appointment 表，只做数据访问，业务逻辑放在应用层
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<AppointmentPO> {
}
