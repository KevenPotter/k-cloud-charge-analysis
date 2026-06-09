package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 充电握手
 *
 * @author KevenPotter
 * @date 2026-06-09 09:49:33
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardChargingHandshake {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** BMS通信协议版本号 */
    private String communicationProtocolVersion;
    /** 电池类型 */
    private Integer batteryType;
    /** 整车动力蓄电池系统额定容量 */
    private BigDecimal batteryRated;
    /** 整车动力蓄电池系统额定总电压 */
    private BigDecimal batteryTotalVoltage;
    /** 电池生产厂商名称 */
    private String batteryManufacturer;
    /** 电池组序号 */
    private String batterySerialNo;
    /** 电池组生产日期年 */
    private Integer batteryProductionYear;
    /** 电池组生产日期月 */
    private Integer batteryProductionMonth;
    /** 电池组生产日期日 */
    private Integer batteryProductionDay;
    /** 电池组充电次数 */
    private Integer batteryChargeCounts;
    /** 电池组产权标识 */
    private Integer batteryPropertyIdentification;
    /** 预留位 */
    private String reserved;
    /** 车辆识别码 */
    private String vin;
    /** 软件版本号 */
    private String softwareVersion;
}
