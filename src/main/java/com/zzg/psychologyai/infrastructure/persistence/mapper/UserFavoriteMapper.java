package com.zzg.psychologyai.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzg.psychologyai.infrastructure.persistence.po.UserFavoritePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户文章收藏 Mapper：对应 user_favorite 表，只做数据访问，业务逻辑放在应用层
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavoritePO> {
}
