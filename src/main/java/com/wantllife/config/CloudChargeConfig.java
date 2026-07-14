package com.wantllife.config;

import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.*;

/**
 * 云快充协议SDK配置中心
 *
 * @author KevenPotter
 * @date 2026-05-20 14:25:33
 */
@Slf4j
public class CloudChargeConfig {

    /** 解析器-日志开关,默认true(打印日志) */
    private boolean analysisLogOutput = true;
    /** 解析器-心跳日志开关,默认true(打印日志) */
    private boolean analysisHeartbeatLogOutput = true;
    /** 模拟器-日志开关,默认true(打印日志) */
    private boolean simulatorLogOutput = true;
    /** 模拟器-心跳日志开关,默认true(打印日志) */
    private boolean simulatorHeartbeatLogOutput = true;
    /** 单次模拟最大充电时长(分钟)，默认120 */
    private int maxChargeMinutes = 120;

    /**
     * 获取解析器-日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-05-20 16:58:22
     */
    public boolean isAnalysisLogOutput() {
        return analysisLogOutput;
    }

    /**
     * 设置解析器-日志输出开关状态
     * 配置变更时会自动打印配置变更日志
     *
     * @param analysisLogOutput true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-05-20 16:59:54
     */
    public void setAnalysisLogOutput(boolean analysisLogOutput) {
        this.analysisLogOutput = analysisLogOutput;
        printAnalysisLogStatus(analysisLogOutput);
    }

    /**
     * 打印解析器-日志配置状态
     *
     * @param analysisLogOutput 当前解析器-日志开关状态
     * @author KevenPotter
     * @date 2026-05-20 17:00:05
     */
    private void printAnalysisLogStatus(boolean analysisLogOutput) {
        if (analysisLogOutput) {
            log.info("{} {} User configured analysis logging as ENABLED ✅", TIP_ICON, PROJECT_NAME);
        } else {
            log.info("{} {} User configured analysis logging as DISABLED ❌", TIP_ICON, PROJECT_NAME);
        }
    }

    /**
     * 获取解析器-心跳日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:03:20
     */
    public boolean isAnalysisHeartbeatLogOutput() {
        return analysisHeartbeatLogOutput;
    }

    /**
     * 设置解析器-心跳日志输出开关状态
     * 配置变更时会自动打印配置变更日志
     *
     * @param analysisHeartbeatLogOutput true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:03:51
     */
    public void setAnalysisHeartbeatLogOutput(boolean analysisHeartbeatLogOutput) {
        this.analysisHeartbeatLogOutput = analysisHeartbeatLogOutput;
        printAnalysisHeartbeatLogStatus(analysisHeartbeatLogOutput);
    }

    /**
     * 打印解析器-心跳日志配置状态
     *
     * @param analysisHeartbeatLogOutput 当前解析器-心跳日志开关状态
     * @author KevenPotter
     * @date 2026-06-03 11:04:19
     */
    private void printAnalysisHeartbeatLogStatus(boolean analysisHeartbeatLogOutput) {
        if (analysisHeartbeatLogOutput) {
            log.info("{} {} User configured analysis heartbeat logging as ENABLED ✅", TIP_ICON, PROJECT_NAME);
        } else {
            log.info("{} {} User configured analysis heartbeat logging as DISABLED ❌", TIP_ICON, PROJECT_NAME);
        }
    }

    /**
     * 获取模拟器-日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:04:25
     */
    public boolean isSimulatorLogOutput() {
        return simulatorLogOutput;
    }

    /**
     * 设置模拟器-日志输出开关状态
     * 配置变更时会自动打印配置变更日志
     *
     * @param simulatorLogOutput true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:05:38
     */
    public void setSimulatorLogOutput(boolean simulatorLogOutput) {
        this.simulatorLogOutput = simulatorLogOutput;
        printSimulatorLogStatus(simulatorLogOutput);
    }

    /**
     * 打印模拟器-日志配置状态
     *
     * @param simulatorLogOutput 当前模拟器-日志开关状态
     * @author KevenPotter
     * @date 2026-06-03 11:05:49
     */
    private void printSimulatorLogStatus(boolean simulatorLogOutput) {
        if (simulatorLogOutput) {
            log.info("{} {} User configured simulator logging as ENABLED ✅", SIM_TIP_ICON, SIM_PROJECT_NAME);
        } else {
            log.info("{} {} User configured simulator logging as DISABLED ❌", SIM_TIP_ICON, SIM_PROJECT_NAME);
        }
    }

    /**
     * 获取模拟器-心跳日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:06:29
     */
    public boolean isSimulatorHeartbeatLogOutput() {
        return simulatorHeartbeatLogOutput;
    }

    /**
     * 设置模拟器-心跳日志输出开关状态
     * 配置变更时会自动打印配置变更日志
     *
     * @param simulatorHeartbeatLogOutput true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-06-03 11:06:43
     */
    public void setSimulatorHeartbeatLogOutput(boolean simulatorHeartbeatLogOutput) {
        this.simulatorHeartbeatLogOutput = simulatorHeartbeatLogOutput;
        printSimulatorHeartbeatLogStatus(simulatorHeartbeatLogOutput);
    }

    /**
     * 打印模拟器-心跳日志配置状态
     *
     * @param simulatorHeartbeatLogOutput 当前模拟器-心跳日志开关状态
     * @author KevenPotter
     * @date 2026-06-03 11:07:00
     */
    private void printSimulatorHeartbeatLogStatus(boolean simulatorHeartbeatLogOutput) {
        if (simulatorHeartbeatLogOutput) {
            log.info("{} {} User configured simulator heartbeat logging as ENABLED ✅", SIM_TIP_ICON, SIM_PROJECT_NAME);
        } else {
            log.info("{} {} User configured simulator heartbeat logging as DISABLED ❌", SIM_TIP_ICON, SIM_PROJECT_NAME);
        }
    }

    /**
     * 获取单次模拟最大充电时长
     *
     * @return 单次模拟最大充电时长(分钟)
     * @author KevenPotter
     * @date 2026-07-14 10:52:12
     */
    public int getMaxChargeMinutes() {
        return maxChargeMinutes;
    }

    /**
     * 设置单次模拟最大充电时长
     *
     * @param maxChargeMinutes 单次模拟最大充电时长(分钟)
     * @author KevenPotter
     * @date 2026-07-14 10:52:37
     */
    public void setMaxChargeMinutes(int maxChargeMinutes) {
        this.maxChargeMinutes = maxChargeMinutes;
    }

}