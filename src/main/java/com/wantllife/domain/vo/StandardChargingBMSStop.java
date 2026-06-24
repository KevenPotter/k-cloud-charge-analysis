package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 充电阶段BMS中止
 *
 * @author KevenPotter
 * @date 2026-06-10 11:50:18
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardChargingBMSStop {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** BMS中止充电原因 */
    private String bmsStopReason;
    /** BMS中止充电故障原因 */
    private String bmsStopFailure;
    /** BMS中止充电错误原因 */
    private String bmsStopErrorReason;
}
