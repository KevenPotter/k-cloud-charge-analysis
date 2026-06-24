package com.wantllife.simulator.manager;

import com.wantllife.simulator.client.TcpClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_PROJECT_NAME;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_TIP_ICON;

/**
 * TCP连接管理器
 * 全局管理所有模拟设备连接状态
 *
 * @author KevenPotter
 * @date 2026-05-26 13:52:33
 */
@Slf4j
public class TcpConnectionManager {

    /** 设备连接缓存:key=设备编号,value=TcpClient实例.使用线程安全的ConcurrentHashMap保证高并发下正常运行 */
    private static final Map<String, TcpClient> CLIENT_MAP = new ConcurrentHashMap<>(16);

    /**
     * 私有构造函数，禁止实例化
     *
     * @author KevenPotter
     * @date 2026-05-27 16:01:35
     */
    private TcpConnectionManager() {
    }

    /**
     * 注册设备TCP客户端
     *
     * @param deviceId 设备编号
     * @param client   TCP客户端实例
     * @author KevenPotter
     * @date 2026-05-27 16:02:31
     */
    public static void register(String deviceId, TcpClient client) {
        CLIENT_MAP.put(deviceId, client);
        log.info("{} {} {} device is registered. Online count: {}", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, CLIENT_MAP.size());
    }

    /**
     * 移除设备TCP客户端(断开连接时调用)
     *
     * @param deviceId 设备编号
     * @param client   TCP客户端实例
     * @author KevenPotter
     * @date 2026-05-27 16:02:34
     */
    public static void remove(String deviceId, TcpClient client) {
        CLIENT_MAP.remove(deviceId);
        log.info("{} {} {} device is removed. Online count: {}", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, CLIENT_MAP.size());
    }

    /**
     * 根据设备编号获取对应的TCP客户端
     *
     * @param deviceId 设备编号
     * @return 对应的TCP客户端，不存在则返回null
     * @author KevenPotter
     * @date 2026-05-27 16:03:18
     */
    public static TcpClient getClient(String deviceId) {
        return CLIENT_MAP.get(deviceId);
    }

    /**
     * 获取当前在线设备数量
     *
     * @return 在线设备总数
     * @author KevenPotter
     * @date 2026-05-27 16:03:55
     */
    public static int getOnlineCount() {
        return CLIENT_MAP.size();
    }

    /**
     * 关闭所有TCP连接并清空缓存
     * <p>
     * 常用于服务停止、模拟器关闭时统一释放资源
     *
     * @author KevenPotter
     * @date 2026-05-27 16:04:21
     */
    public static void closeAll() {
        log.info("{} {} close all connections. Total count: {}", SIM_TIP_ICON, SIM_PROJECT_NAME, CLIENT_MAP.size());
        CLIENT_MAP.forEach((k, v) -> {
            try {
                v.stop();
            } catch (Exception e) {
                log.error("{} {} {} device shutdown exception", SIM_TIP_ICON, SIM_PROJECT_NAME, k);
            }
        });
        CLIENT_MAP.clear();
    }
}
