package com.wantllife.simulator.fake;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备充电状态(每个设备独立实例)
 * 用于真实时间模拟：累计时间、剩余时间、电量、金额等自动递增
 *
 * @author KevenPotter
 */
@Data
@Accessors(chain = true)
public class DeviceChargingState {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*交易流水号*/
    private String tradeNo;
    /*是否正在充电中*/
    private boolean charging = false;
    /*充电开始时间*/
    private LocalDateTime chargeStartTime;
    /*总充电时间(固定120分钟,后续可改)*/
    public static final int TOTAL_CHARGE_MINUTES = 120;
    /*累计充电分钟数*/
    private int accumulatedMinutes = 0;
    /*剩余充电分钟数*/
    private int remainingMinutes = TOTAL_CHARGE_MINUTES;
    /*累计充电度数*/
    private BigDecimal chargingDegree = BigDecimal.ZERO;
    /*已充金额*/
    private BigDecimal chargedAmount = BigDecimal.ZERO;
}
