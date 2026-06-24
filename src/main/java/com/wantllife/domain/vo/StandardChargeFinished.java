package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 充电结束
 *
 * @author KevenPotter
 * @date 2026-06-10 09:53:41
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardChargeFinished {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** BMC中止荷电状态SOC */
    private Integer stopSoc;
    /** BMS动力蓄电池单体最低电压 */
    private BigDecimal minVoltage;
    /** BMS动力蓄电池单体最高电压 */
    private BigDecimal maxVoltage;
    /** BMS动力蓄电池最低温度 */
    private Integer minTemperature;
    /** BMS动力蓄电池最高温度 */
    private Integer maxTemperature;
    /** 电桩累计充电时间 */
    private Integer chargeTime;
    /** 电桩输出能量 */
    private BigDecimal outputEnergy;
    /** 电桩充电机编号 */
    private String chargeNo;
}
