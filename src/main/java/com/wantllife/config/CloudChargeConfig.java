package com.wantllife.config;

import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.PROJECT_NAME;
import static com.wantllife.constant.CloudFastChargingConstants.TIP_ICON;

/**
 * 云快充协议SDK配置中心
 *
 * @author KevenPotter
 * @date 2026-05-20 14:25:33
 */
@Slf4j
public class CloudChargeConfig {

    /*日志开关,默认true(打印日志)*/
    private boolean logOutput = true;

    /**
     * 获取日志输出开关状态
     *
     * @return true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-05-20 16:58:22
     */
    public boolean isLogOutput() {
        return logOutput;
    }

    /**
     * 设置日志输出开关状态
     * 配置变更时会自动打印配置变更日志
     *
     * @param logOutput true:开启日志输出 false:关闭日志输出
     * @author KevenPotter
     * @date 2026-05-20 16:59:54
     */
    public void setLogOutput(boolean logOutput) {
        this.logOutput = logOutput;
        printLogStatus(logOutput);
    }

    /**
     * 打印日志配置状态
     *
     * @param logOutput 当前日志开关状态
     * @author KevenPotter
     * @date 2026-05-20 17:00:05
     */
    private void printLogStatus(boolean logOutput) {
        if (logOutput) {
            log.info("{} {} User configured parse logging as ENABLED ✅", TIP_ICON, PROJECT_NAME);
        } else {
            log.info("{} {} User configured parse logging as DISABLED ❌", TIP_ICON, PROJECT_NAME);
        }
    }
}
