package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 实时监控
 *
 * @author KevenPotter
 * @date 2026-06-01 17:14:15
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class StandardRealTimeMonitor {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*状态*/
    private Integer status;
    /*枪是否归位*/
    private Integer haveReturn;
    /*是否插枪*/
    private Integer haveInsert;
    /*输出电压*/
    private BigDecimal voltage;
    /*输出电流*/
    private BigDecimal current;
    /*枪线温度*/
    private Integer temperature;
    /*枪线编码*/
    private String gunCode;
    /*SOC*/
    private Integer soc;
    /*电池组最高温度*/
    private Integer highestTemperature;
    /*累计充电时间*/
    private Integer accumulatedChargingTime;
    /*剩余时间*/
    private Integer remainingChargingTime;
    /*充电度数*/
    private BigDecimal chargingDegree;
    /*计损充电度数*/
    private BigDecimal calculatedChargingDegree;
    /*已充金额*/
    private BigDecimal chargedAmount;
    /*硬件故障*/
    private String hardwareFailure;
}
