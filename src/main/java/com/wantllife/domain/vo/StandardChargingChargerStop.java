package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 充电阶段充电机中止
 *
 * @author KevenPotter
 * @date 2026-06-11 09:55:48
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardChargingChargerStop {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** 充电机中止充电原因 */
    private String chargerStopReason;
    /** 充电机中止充电故障原因 */
    private String chargerStopFailure;
    /** 充电机中止充电错误原因 */
    private String chargerStopErrorReason;
}
