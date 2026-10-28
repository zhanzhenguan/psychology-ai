package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.SysFileInfoPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统文件 Mapper
 */
@Mapper
public interface SysFileInfoMapper extends BaseMapper<SysFileInfoPO> {
}
