package com.wantllife.simulator.process;

import com.wantllife.simulator.req.SAALoginReq;
import com.wantllife.simulator.state.DeviceState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_PROJECT_NAME;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_TIP_ICON;

/**
 * 设备状态持有管理器
 *
 * @author KevenPotter
 * @date 2026-07-03 16:52:03
 */
@Slf4j
@Getter
public class DeviceStateHolder {

    /** 当前绑定设备编号 */
    private String deviceId;
    /** 设备状态机(独立枚举) */
    private DeviceState currentState;
    /** 登录请求对象(用于心跳) */
    @Setter
    private SAALoginReq loginReq;
    /** 平台下发的计费模型编码 */
    @Setter
    private Integer platformBillingModeId;
    /** 实时监测数据[0x13指令]是否已经发送过初始化 */
    private boolean initRealTimeSent;

    /** 构造初始化默认状态 */
    public DeviceStateHolder() {
        this.resetAllState();
    }

    /**
     * 绑定当前设备编号,由外部初始化设备信息时调用
     *
     * @param deviceId 设备唯一编号
     * @author KevenPotter
     * @date 2026-07-03 17:23:32
     */
    public void bindDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * 全局重置所有状态,TCP重连、设备复位时调用
     * 恢复初始等待登录状态,清空缓存对象与标记位
     *
     * @author KevenPotter
     * @date 2026-07-03 16:54:15
     */
    public void resetAllState() {
        this.currentState = DeviceState.WAIT_LOGIN;
        this.loginReq = null;
        this.platformBillingModeId = null;
        this.initRealTimeSent = false;
    }

    /**
     * 判断当前状态是否等于目标状态
     *
     * @param targetState 待校验目标状态
     * @return 匹配返回true, 否则false
     * @author KevenPotter
     * @date 2026-07-03 16:54:30
     */
    public boolean isCurrentState(DeviceState targetState) {
        return this.currentState == targetState;
    }

    /**
     * 切换设备状态
     *
     * @param newState 要跳转的新状态
     * @author KevenPotter
     * @date 2026-07-03 16:55:19
     */
    public void switchState(DeviceState newState) {
        if (this.currentState != newState) {
            log.info("{} {} {} Device Status Change: {} → {}", SIM_TIP_ICON, SIM_PROJECT_NAME, this.deviceId, this.currentState, newState);
            this.currentState = newState;
        }
    }

    /**
     * 设置初始化实时报文上报标记
     *
     * @param flag 标记值
     * @author KevenPotter
     * @date 2026-07-03 17:00:00
     */
    public void markInitRealTimeSent(boolean flag) {
        this.initRealTimeSent = flag;
    }
}
