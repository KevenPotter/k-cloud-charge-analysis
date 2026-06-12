package com.wantllife.util;

import com.wantllife.simulator.client.TcpClient;
import com.wantllife.simulator.manager.TcpConnectionManager;
import com.wantllife.simulator.process.SimDevMsgProcessor;

/**
 * 模拟器工具类
 *
 * @author KevenPotter
 * @date 2026-06-12 11:19:35
 */
public class SimulatorUtil {

    // 私有构造，禁止实例化
    private SimulatorUtil() {
    }

    /**
     * 模拟器虚拟设备上行发送报文对外统一入口
     *
     * @param deviceId 模拟器设备编号
     * @param command  模拟指令
     * @return true:发送成功 false:失败
     * @author KevenPotter
     * @date 2026-06-12 11:21:07
     */
    public static boolean simulatorSendMsg(String deviceId, byte[] command) {
        TcpClient tcpClient = TcpConnectionManager.getClient(deviceId);
        if (tcpClient == null) {
            return false;
        }
        try {
            SimDevMsgProcessor processor = tcpClient.getSimDevMsgProcessor();
            processor.sendMessage(command);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
