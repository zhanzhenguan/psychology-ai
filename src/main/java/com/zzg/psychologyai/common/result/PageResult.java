package com.zzg.psychologyai.common.result;

import cn.hutool.core.util.PageUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 统一分页响应体：对齐目标后端 {records, total, size, current, pages}
 *
 * @param <T> 业务数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    /**
     * 每页条数上限，防止恶意大分页拖垮数据库
     */
    public static final long MAX_PAGE_SIZE = 100;

    /**
     * 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 每页条数 */
    private long size;

    /** 当前页码 */
    private long current;

    /** 总页数 */
    private long pages;




    public PageResult() {}


    public PageResult(List<T> records, long total, long size, long current, long pages) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
        this.pages = pages;
    }


    public PageResult(List<T> records) {
        this.records = records;
    }



    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        List<R> mapped = this.records == null ? List.of() : this.records.stream().<R>map(mapper).toList();
        return new PageResult<>(mapped, this.total, this.size, this.current, this.pages);
    }


    public void setPage(IPage<T> page){
        this.records = page.getRecords();
        this.total = page.getTotal();
        this.size = page.getSize();
        this.current = page.getCurrent();
        this.pages = page.getPages();
    }



    //-----------------------------------静态方法-----------------------------------------------------------

    /**
     * 分页构造方法（size 超过上限时自动截断）
     */
    public static <T> PageResult<T> of(List<T> records, long total, long size, long current) {
        long safeSize = Math.min(size, MAX_PAGE_SIZE);
        long pages = safeSize == 0 ? 0 : (total + safeSize - 1) / safeSize;
        return new PageResult<>(records, total, safeSize, current, pages);
    }


    /**
     * 分页构造方法
     * @param records
     * @param total
     * @param size
     * @param current
     * @param pages
     * @return
     * @param <T>
     */
    public static  <T> PageResult<T> of(List<T> records, long total, long size, long current,long pages) {
        return new PageResult<>(records, total, size, current, pages);
    }

    /**
     * 带限制的分页构造方法limitCount限制多少行，返回数据最大就到多少行，主要针对es 深度分页10000行限制
     * @param records
     * @param total
     * @param size
     * @param current
     * @param limit
     * @return
     * @param <T>
     */

    public static <T> PageResult<T> of(List<T> records, int total, int size, int current,int limit) {
        if(total > limit){
            total = limit;
        }
        long pages = size == 0 ? 0 : (total + size - 1) / size;
        return new PageResult<>(records, total, size, current, pages);
    }

    /**
     * 分页构造方法
     * @param page
     * @return
     * @param <T>
     */

    public static <T> PageResult<T> of(Page<T> page) {
        List<T> records = page.getRecords();
        long total = page.getTotal();
        long size = page.getSize();
        long current = page.getCurrent();
//        long pages = size == 0 ? 0 : (total + size - 1) / size;
        long pages = page.getPages();
        return new PageResult<>(records, total, size, current, pages);
    }


    public static  <T> PageResult<T> of() {
        List<T> records = new ArrayList<>();
        long total = 0;
        long size = 0;
        long current = 0;
        long pages = 0;
        return new PageResult<>(records, total, size, current, pages);
    }


    public static  <T> PageResult<T> of(List<T> records) {
        return new PageResult<>(records);
    }




}
