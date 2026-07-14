package com.wantllife.simulator;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.SimulatorConfig;
import com.wantllife.constant.SimulatorConstants;
import com.wantllife.domain.vo.StandardDevice;
import com.wantllife.simulator.client.TcpClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_PROJECT_NAME;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_TIP_ICON;

/**
 * 云快充协议 多设备TCP模拟器
 * 功能:模拟多个充电桩设备，自动连接TCP服务器，自动登录，自动应答下行指令
 * 支持两种启动方式：
 * 1.Spring @Bean 初始化（项目启动自动运行）
 * 2.代码动态创建（Web接口前端传参控制）
 *
 * @author KevenPotter
 * @date 2026-05-26 13:39:02
 */
@Slf4j
public class DeviceSimulator {

    /*模拟器配置*/
    private final SimulatorConfig config;
    /*设备TCP客户端列表*/
    private final List<TcpClient> tcpClientList = new CopyOnWriteArrayList<>();
    /*运行状态*/
    @Getter
    private volatile boolean running = false;

    /**
     * 静态工厂方法
     *
     * @author KevenPotter
     * @date 2026-05-26 15:29:33
     */
    public static DeviceSimulator create(SimulatorConfig config) {
        return new DeviceSimulator(config);
    }

    /**
     * 构造函数-使用默认消息处理器
     *
     * @param config 模拟器配置
     * @author KevenPotter
     * @date 2026-05-27 16:04:39
     */
    public DeviceSimulator(SimulatorConfig config) {
        this.config = config;
    }

    /**
     * 启动模拟器
     * 异步后台运行，不阻塞主线程
     *
     * @author KevenPotter
     * @date 2026-05-27 16:05:54
     */
    public void start() {
        // 判断模拟器是否已启动，避免重复启动
        if (running) {
            log.info("{} {} Started, no need to repeat operation", SIM_TIP_ICON, SIM_PROJECT_NAME);
            return;
        }
        // 校验服务器配置是否合法
        if (config == null || StrUtil.isBlank(config.getServerIP()) || config.getServerPort() <= 0) {
            log.error("{} {} Startup failed: Server IP or Server Port is empty", SIM_TIP_ICON, SIM_PROJECT_NAME);
            return;
        }
        // 获取待模拟的设备列表
        List<StandardDevice> deviceList = config.getSimulatorDeviceList();
        if (deviceList == null || deviceList.isEmpty()) {
            log.error("{} {} Startup failed: Device list is empty", SIM_TIP_ICON, SIM_PROJECT_NAME);
            return;
        }
        // 将模拟器状态标记为运行中
        running = true;
        log.info("{} {} Starting → Server: {}:{}, device count: {}", SIM_TIP_ICON, SIM_PROJECT_NAME, config.getServerIP(), config.getServerPort(), deviceList.size());
        // 异步启动所有设备，不阻塞主线程
        ThreadUtil.execAsync(() -> {
            // 遍历所有设备，逐个创建TCP客户端并启动
            for (StandardDevice device : deviceList) {
                // 如果模拟器已停止，立即中断启动流程
                if (!running) break;
                String deviceId = device.getDeviceId();
                try {
                    // 为当前设备创建TCP客户端
                    TcpClient tcpClient = new TcpClient(
                            config.getServerIP(),
                            config.getServerPort(),
                            device
                    );
                    // 启动TCP客户端(建立连接、收发数据)
                    tcpClient.start();
                    // 将客户端加入列表统一管理
                    tcpClientList.add(tcpClient);
                    // 多设备启动间隔，避免瞬间并发过高
                    TimeUnit.MILLISECONDS.sleep(SimulatorConstants.MULTI_DEVICE_START_INTERVAL);
                } catch (Exception e) {
                    // 单个设备启动异常，不影响其他设备
                    log.error("{} {} {} device startup exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
                }
            }
        });
        log.info("{} {} Startup completed, automatic connection in the background", SIM_TIP_ICON, SIM_PROJECT_NAME);
    }

    /**
     * 停止模拟器
     * 关闭所有TCP连接
     *
     * @author KevenPotter
     * @date 2026-05-27 16:06:49
     */
    public void stop() {
        // 判断模拟器是否未运行，避免重复停止
        if (!running) {
            log.warn("{} {} Not running", SIM_TIP_ICON, SIM_PROJECT_NAME);
            return;
        }
        log.info("{} {} Pausing → device closed count: {}", SIM_TIP_ICON, SIM_PROJECT_NAME, tcpClientList.size());
        // 遍历所有TCP客户端，逐个关闭连接
        for (TcpClient client : tcpClientList) {
            try {
                // 停止单个设备的TCP连接
                client.stop();
            } catch (Exception e) {
                // 单个设备关闭异常，不影响其他设备
                log.error("{} {} {} device shutdown exception", SIM_TIP_ICON, SIM_PROJECT_NAME, client.getDeviceId(), e);
            }
        }
        // 清空客户端列表
        tcpClientList.clear();
        // 将模拟器状态标记为已停止
        running = false;
        log.info("[设备模拟器] 已完全停止");
        log.info("{} {} Completely stopped", SIM_TIP_ICON, SIM_PROJECT_NAME);
    }

    /**
     * 动态新增单台模拟器设备,自动创建TCP连接后台运行
     *
     * @param device 设备实体
     * @author KevenPotter
     * @date 2026-07-14 11:49:10
     */
    public void addDevice(StandardDevice device) {
        if (!running) {
            log.error("{} {} Simulator is not running, cannot add device {}", SIM_TIP_ICON, SIM_PROJECT_NAME, device.getDeviceId());
            return;
        }
        String targetDeviceId = device.getDeviceId();
        // 判断设备是否已存在
        boolean exists = tcpClientList.stream()
                .anyMatch(client -> client.getDeviceId().equals(targetDeviceId));
        if (exists) {
            log.warn("{} {} Device {} already exists, skip creation", SIM_TIP_ICON, SIM_PROJECT_NAME, targetDeviceId);
            return;
        }
        // 1. 同步更新底层配置列表
        config.addSimulatorDevice(device);
        try {
            // 2. 创建TCP客户端并启动，复用原有创建逻辑
            TcpClient tcpClient = new TcpClient(config.getServerIP(), config.getServerPort(), device);
            tcpClient.start();
            tcpClientList.add(tcpClient);
            TimeUnit.MILLISECONDS.sleep(SimulatorConstants.MULTI_DEVICE_START_INTERVAL);
            log.info("{} {} Dynamically add device {} success", SIM_TIP_ICON, SIM_PROJECT_NAME, targetDeviceId);
        } catch (Exception e) {
            log.error("{} {} Failed to dynamically add device {}", SIM_TIP_ICON, SIM_PROJECT_NAME, targetDeviceId, e);
        }
    }

    /**
     * 根据设备ID移除并断开模拟器设备
     *
     * @param deviceId 设备编号
     * @author KevenPotter
     * @date 2026-07-14 11:50:19
     */
    public void removeDevice(String deviceId) {
        // 1. 底层配置移除
        config.removeSimulatorDevice(deviceId);
        // 2. 找到对应客户端关闭销毁
        TcpClient targetClient = null;
        for (TcpClient client : tcpClientList) {
            if (client.getDeviceId().equals(deviceId)) {
                targetClient = client;
                break;
            }
        }
        if (targetClient == null) {
            log.warn("{} {} Device {} not found, skip remove", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId);
            return;
        }
        try {
            targetClient.stop();
            tcpClientList.remove(targetClient);
            log.info("{} {} Dynamically remove device {} success", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId);
        } catch (Exception e) {
            log.error("{} {} Exception when removing device {}", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
        }
    }

}
