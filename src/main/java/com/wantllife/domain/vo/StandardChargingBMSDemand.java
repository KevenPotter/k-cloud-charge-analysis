package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 充电过程BMS需求与充电机输出
 *
 * @author KevenPotter
 * @date 2026-06-11 10:44:17
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardChargingBMSDemand {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** BMS电压需求 */
    private BigDecimal voltageDemand;
    /** BMS电流需求 */
    private BigDecimal currentDemand;
    /** BMS充电模式 */
    private Integer chargeMode;
    /** BMS充电电压测量值 */
    private BigDecimal voltageMeasure;
    /** BMS充电电流测量值 */
    private BigDecimal currentMeasure;
    /** BMS最高单体电压+组号 */
    private String maxVoltageGroup;
    /** 最高单体电压 */
    private BigDecimal maxVoltage;
    /** 最高电压所在组号 */
    private Integer maxVoltageGroupNo;
    /** BMS当前SOC */
    private Integer soc;
    /** BMS估算剩余充电时间 */
    private Integer remainingChargingTime;
    /** 电桩电压输出值 */
    private BigDecimal voltageOutput;
    /** 电桩电流输出值 */
    private BigDecimal currentOutput;
    /** 累计充电时间 */
    private Integer accumulatedChargingTime;
}
