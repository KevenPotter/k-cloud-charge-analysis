package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 地锁数据上送
 *
 * @author KevenPotter
 * @date 2026-06-12 16:35:12
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardGroundLockData {

    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** 车位锁状态 */
    private Integer lockStatus;
    /** 车位状态 */
    private Integer parkingStatus;
    /** 地锁电量状态 */
    private Integer batteryStatus;
    /** 报警状态 */
    private Integer alarmStatus;
    /** 预留位 */
    private String reserved;
}
