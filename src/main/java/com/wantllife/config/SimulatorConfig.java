package com.wantllife.config;

import com.wantllife.domain.vo.StandardDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    /*TCP服务器IP地址*/
    private String serverIP;
    /*TCP服务器端口号*/
    private int serverPort;
    /*模拟设备编号列表*/
    private List<StandardDevice> simulatorDeviceList;
}
