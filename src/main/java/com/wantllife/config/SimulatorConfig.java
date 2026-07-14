package com.wantllife.config;

import com.wantllife.domain.vo.StandardDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 云快充设备模拟器配置
 * <p>
 * 配置TCP服务器地址、端口、模拟设备编号列表
 *
 * @author KevenPotter
 * @date 2026-05-26 13:37:21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorConfig {

    /** TCP服务器IP地址 */
    private String serverIP;
    /** TCP服务器端口号 */
    private int serverPort;
    /** 模拟设备编号列表 */
    private List<StandardDevice> simulatorDeviceList;

    /**
     * 添加单台设备
     *
     * @param device 设备
     * @return 返回当前配置对象
     * @author KevenPotter
     * @date 2026-07-14 11:48:30
     */
    public SimulatorConfig addSimulatorDevice(StandardDevice device) {
        if (this.simulatorDeviceList == null) {
            this.simulatorDeviceList = new CopyOnWriteArrayList<>();
        }
        this.simulatorDeviceList.add(device);
        return this;
    }

    /**
     * 根据设备编号删除设备
     *
     * @param deviceId 设备编号
     * @return 返回当前配置对象
     * @author KevenPotter
     * @date 2026-07-14 11:48:56
     */
    public SimulatorConfig removeSimulatorDevice(String deviceId) {
        if (this.simulatorDeviceList == null) {
            return this;
        }
        this.simulatorDeviceList.removeIf(d -> d.getDeviceId().equals(deviceId));
        return this;
    }
}
