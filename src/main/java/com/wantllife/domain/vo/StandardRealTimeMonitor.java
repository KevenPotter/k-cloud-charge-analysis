package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.wantllife.util.StringUtil.generateSerial;

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

    /**
     * 构建初始对象
     *
     * @param deviceId 设备编号
     * @param gunNo    枪号
     * @author KevenPotter
     * @date 2026-06-01 17:23:24
     */
    public StandardRealTimeMonitor(String deviceId, Integer gunNo) {
        this.tradeNo = generateSerial(deviceId, gunNo);
        this.deviceId = deviceId;
        this.gunNo = gunNo;
        this.status = 2;
        this.haveReturn = 2;
        this.haveInsert = 1;
        this.voltage = BigDecimal.valueOf(235.6D);
        this.current = BigDecimal.valueOf(1.0D);
        this.temperature = 0;
        this.gunCode = "0000000000000000";
        this.soc = 0;
        this.highestTemperature = -50;
        this.accumulatedChargingTime = 0;
        this.remainingChargingTime = 0;
        this.chargingDegree = BigDecimal.valueOf(0.0000D);
        this.calculatedChargingDegree = BigDecimal.valueOf(0.0000D);
        this.chargedAmount = BigDecimal.valueOf(0.0000D);
        this.hardwareFailure = "00";
    }

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
