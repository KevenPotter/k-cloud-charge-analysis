package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 充电桩主动申请并充充电
 *
 * @author KevenPotter
 * @date 2026-06-11 15:42:49
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardApplyParallelCharging {

    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** 启动方式 */
    private Integer startupMode;
    /** 是否需要密码(0.不需要 1.需要) */
    private Integer needPassword;
    /** 账号或物理卡号 */
    private String accountOrCardNo;
    /** 输入密码 */
    private String password;
    /** 车辆识别码 */
    private String vin;
    /** 主辅枪标记 */
    private Integer priAndSecFlag;
    /** 并充序号 */
    private String parallelNo;
}
