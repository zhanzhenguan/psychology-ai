package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper：只做数据访问，业务逻辑一律放在应用层
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    UserPO selectByUsername(@Param("username") String username);

}
