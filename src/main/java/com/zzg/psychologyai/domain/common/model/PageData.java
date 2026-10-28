package com.zzg.psychologyai.domain.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 领域分页数据：仓储接口的返回类型，避免领域层依赖 MyBatis-Plus 的分页对象
 *
 * @param <E> 领域对象类型
 */
@Data
@AllArgsConstructor
public class PageData<E> implements Serializable {

    /**
     * 当前页领域对象
     */
    private List<E> records;

    /**
     * 总记录数
     */
    private long total;
}
