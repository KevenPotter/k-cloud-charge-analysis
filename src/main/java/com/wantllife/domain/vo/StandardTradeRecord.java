package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易记录
 *
 * @author KevenPotter
 * @date 2026-06-12 13:27:09
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardTradeRecord {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** 开始时间 */
    private Date startTime;
    /** 结束时间 */
    private Date endTime;

    /** 尖单价 */
    private BigDecimal sharpUnitPrice;
    /** 尖电量 */
    private BigDecimal sharpElectricity;
    /** 计损尖电量 */
    private BigDecimal sharpLossElectricity;
    /** 尖金额 */
    private BigDecimal sharpAmount;

    /** 峰单价 */
    private BigDecimal peakUnitPrice;
    /** 峰电量 */
    private BigDecimal peakElectricity;
    /** 计损峰电量 */
    private BigDecimal peakLossElectricity;
    /** 峰金额 */
    private BigDecimal peakAmount;

    /** 平单价 */
    private BigDecimal flatUnitPrice;
    /** 平电量 */
    private BigDecimal flatElectricity;
    /** 计损平电量 */
    private BigDecimal flatLossElectricity;
    /** 平金额 */
    private BigDecimal flatAmount;

    /** 谷单价 */
    private BigDecimal valleyUnitPrice;
    /** 谷电量 */
    private BigDecimal valleyElectricity;
    /** 计损谷电量 */
    private BigDecimal valleyLossElectricity;
    /** 谷金额 */
    private BigDecimal valleyAmount;

    /** 电表总起值 */
    private BigDecimal electricityStart;
    /** 电表总止值 */
    private BigDecimal electricityEnd;
    /** 总电量 */
    private BigDecimal totalElectricity;
    /** 计损总电量 */
    private BigDecimal totalLossElectricity;

    /** 消费金额 */
    private BigDecimal totalAmount;
    /** 电动汽车唯一标识 */
    private String vinCode;
    /** 交易标识 */
    private Integer tradeIdentifier;
    /** 交易日期、时间 */
    private Date tradeTime;
    /** 停止原因 */
    private Integer stopReason;
    /** 物理卡号 */
    private String physicalCardNo;
}
