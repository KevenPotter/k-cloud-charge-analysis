package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 充电过程BMS信息
 *
 * @author KevenPotter
 * @date 2026-06-11 11:37:51
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardChargingBMSInfo {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** 最高单体电压所在编号 */
    private Integer maxVoltageNo;
    /** 最高动力蓄电池温度 */
    private Integer maxBatteryTemperature;
    /** 最高温度检测点编号 */
    private Integer maxTemperatureCheckNo;
    /** 最低蓄电池温度 */
    private Integer minBatteryTemperature;
    /** 最低温度检测点编号 */
    private Integer minTemperatureCheckNo;
    /** 状态位原始值 */
    private String statusBits;
    /** 单体电压过高/过低描述 */
    private String voltageStatusDesc;
    /** SOC过高/过低描述 */
    private String socStatusDesc;
    /** 充电过流描述 */
    private String chargeOverCurrentDesc;
    /** 电池温度过高描述 */
    private String batteryTemperatureOverDesc;
    /** 绝缘状态描述 */
    private String insulationStatusDesc;
    /** 连接器连接状态描述 */
    private String connectorStatusDesc;
    /** 充电禁止描述 */
    private String chargeForbidDesc;
    /** 预留位描述 */
    private String reservedDesc;
}
