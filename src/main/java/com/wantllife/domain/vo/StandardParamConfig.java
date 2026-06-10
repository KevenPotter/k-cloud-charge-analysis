package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 参数配置
 *
 * @author KevenPotter
 * @date 2026-06-10 09:53:41
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardParamConfig {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** BMS单体最高允许充电电压 */
    private BigDecimal maxAllowVoltage;
    /** BMS最高允许充电电流 */
    private BigDecimal maxChargeCurrent;
    /** BMS蓄电池标称总能量 */
    private BigDecimal ratedEnergy;
    /** BMS最高允许充电总电压 */
    private BigDecimal maxTotalVoltage;
    /** BMS最高允许温度 */
    private Integer maxTemperature;
    /** SOC */
    private BigDecimal soc;
    /** BMS当前总电压 */
    private BigDecimal currentTotalVoltage;
    /** 电桩最高输出电压 */
    private BigDecimal maxOutVoltage;
    /** 电桩最低输出电压 */
    private BigDecimal minOutVoltage;
    /** 电桩最大输出电流 */
    private BigDecimal maxOutCurrent;
    /** 电桩最小输出电流 */
    private BigDecimal minOutCurrent;
}
