package com.wantllife.constant;

/**
 * 模拟器常量
 *
 * @author KevenPotter
 * @date 2026-05-26 13:51:42
 */
public final class SimulatorConstants {

    private SimulatorConstants() {
    }

    /*TCP连接断开后,自动重连间隔时间(单位：秒)*/
    public static final int RECONNECT_INTERVAL_SECOND = 5;
    /*Socket读取数据超时时间(单位：毫秒).超时后不会抛出异常，继续循环监听*/
    public static final int SO_TIMEOUT_MILLISECOND = 60000;
    /*多设备模拟启动时，设备之间的启动间隔时间(单位：毫秒).防止瞬间并发连接导致服务器端口风暴*/
    public static final long MULTI_DEVICE_START_INTERVAL = 500;
}
