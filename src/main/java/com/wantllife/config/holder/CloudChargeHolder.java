package com.wantllife.config.holder;

import com.wantllife.config.CloudChargeConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 云快充SDK全局配置持有器
 * 统一持有全局生效配置实例，提供快捷读取入口
 *
 * @author KevenPotter
 * @date 2026-05-20 15:47:22
 */
@Slf4j
public class CloudChargeHolder {

    /** 全局默认配置实例 */
    private static CloudChargeConfig GLOBAL_CONFIG = new CloudChargeConfig();

    /**
     * 设置全局配置
     * 由使用者在@Bean中调用覆盖默认配置
     *
     * @author KevenPotter
     * @date 2026-05-20 15:48:36
     */
    public static void setGlobalConfig(CloudChargeConfig config) {
        CloudChargeHolder.GLOBAL_CONFIG = config;
    }

    /**
     * 获取解析器-日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-05-20 15:49:13
     */
    public static boolean isAnalysisLogOutput() {
        return GLOBAL_CONFIG.isAnalysisLogOutput();
    }

    /**
     * 获取解析器-心跳日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:07:26
     */
    public static boolean isAnalysisHeartbeatLogOutput() {
        return GLOBAL_CONFIG.isAnalysisHeartbeatLogOutput();
    }

    /**
     * 获取模拟器-日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:07:45
     */
    public static boolean isSimulatorLogOutput() {
        return GLOBAL_CONFIG.isSimulatorLogOutput();
    }

    /**
     * 获取模拟器-心跳日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:07:58
     */
    public static boolean isSimulatorHeartbeatLogOutput() {
        return GLOBAL_CONFIG.isSimulatorHeartbeatLogOutput();
    }

}
