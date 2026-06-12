package com.wantllife.simulator.state;

/**
 * 设备状态机
 * 用于控制设备初始化流程:登录 → 计费验证 → 计费请求 → 正常运行
 *
 * @author KevenPotter
 * @date 2026-05-29 10:34:15
 */
public enum DeviceState {

    /**
     * 待登录(循环发送0x01充电桩登录认证)
     */
    WAIT_LOGIN,

    /**
     * 待计费模型验证(循环发送0x05计费模型验证请求)
     */
    WAIT_BILLING_VALID,

    /**
     * 待充电桩计费模型(循环发送0x09充电桩计费模型请求)
     */
    WAIT_BILLING_MODEL,

    /**
     * 就绪状态（循环发送0X03充电桩心跳包）
     */
    READY
}
