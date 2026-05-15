package com.wantllife.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 计费模型
 *
 * @author KevenPotter
 * @date 2026-04-22 17:13:20
 */
@Data
@Accessors(chain = true)
public class StandardBillingModel {

    /*主键编号*/
    private Long modeId;
    /*策略编号*/
    private Long strategyId;
    /*时段类型*/
    private Integer timeSlotType;
    /*时段名称*/
    private String timeSlotName;
    /*开始时间*/
    private String startTime;
    /*结束时间*/
    private String endTime;
    /*基础电费*/
    private BigDecimal electricityFee;
    /*服务费用*/
    private BigDecimal serviceFee;
    /*成本费用*/
    private BigDecimal costFee;
}
