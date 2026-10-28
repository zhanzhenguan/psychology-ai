package com.zzg.psychologyai.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zzg.psychologyai.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约持久化对象：对应 docs/ai-agent.sql 的 appointment 表。
 * <p>
 * 审计字段继承自 BaseEntity；表中 date、time 为 varchar 存储的日期/时段字符串，故用 String 承载
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("appointment")
public class AppointmentPO extends BaseEntity {

    /**
     * 主键（自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预约人姓名
     */
    private String username;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 就诊科室
     */
    private String department;

    /**
     * 预约日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 预约时段（如 09:00-09:30）
     */
    private String time;

    /**
     * 医生姓名
     */
    private String doctorName;
}
