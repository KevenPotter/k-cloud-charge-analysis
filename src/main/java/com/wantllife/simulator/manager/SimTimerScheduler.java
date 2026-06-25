package com.wantllife.simulator.manager;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 定时任务调度管理器
 *
 * @author KevenPotter
 * @date 2026-06-25 11:15:36
 */
@Slf4j
public class SimTimerScheduler {

    /** 全局共用定时线程池,统一调度所有定时任务 */
    private final ScheduledExecutorService globalScheduler;
    /** 登录定时器 */
    private ScheduledFuture<?> loginTimer;
    /** 计费验证定时器 */
    private ScheduledFuture<?> billingValidTimer;
    /** 计费模型请求定时器 */
    private ScheduledFuture<?> billingModelTimer;
    /** 心跳定时器 */
    private ScheduledFuture<?> heartbeatTimer;
    /** 实时监测数据定时器 */
    private ScheduledFuture<?> realTimeMonitorTimer;

    /**
     * 构造初始化定时线程池
     *
     * @author KevenPotter
     * @date 2026-06-25 11:18:07
     */
    public SimTimerScheduler() {
        this.globalScheduler = new ScheduledThreadPoolExecutor(5, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 启动登录重试定时器
     * 先停止旧定时器避免重复,再创建新定时器每5秒发送一次登录帧,直到登录成功为止
     *
     * @param task         定时执行业务逻辑
     * @param initialDelay 初始延迟秒
     * @param period       轮询周期秒
     * @param unit         时间单位
     * @author KevenPotter
     * @date 2026-05-28 13:44:21
     */
    public void startLoginTimer(Runnable task, long initialDelay, long period, TimeUnit unit) {
        stopLoginTimer();
        this.loginTimer = globalScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 停止登录重试定时器
     * 安全关闭定时器并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-28 13:45:24
     */
    public void stopLoginTimer() {
        if (loginTimer != null) {
            loginTimer.cancel(false);
            loginTimer = null;
        }
    }

    /**
     * 启动计费验证定时器
     * 先停止旧定时器避免重复,再创建新定时器每3秒发送一次计费模型验证请求,直到计费模型验证请求成功为止
     *
     * @param task         定时执行业务逻辑
     * @param initialDelay 初始延迟秒
     * @param period       轮询周期秒
     * @param unit         时间单位
     * @author KevenPotter
     * @date 2026-05-29 11:07:22
     */
    public void startBillingValidTimer(Runnable task, long initialDelay, long period, TimeUnit unit) {
        stopBillingValidTimer();
        this.billingValidTimer = globalScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 停止计费验证定时器
     * 安全关闭定时器并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-29 11:08:30
     */
    public void stopBillingValidTimer() {
        if (billingValidTimer != null) {
            billingValidTimer.cancel(false);
            billingValidTimer = null;
        }
    }

    /**
     * 启动充电桩计费模型请求定时器
     * 先停止旧定时器避免重复,再创建新定时器每3秒发送一次充电桩计费模型请求,直到充电桩计费模型请求成功为止
     *
     * @param task         定时执行业务逻辑
     * @param initialDelay 初始延迟秒
     * @param period       轮询周期秒
     * @param unit         时间单位
     * @author KevenPotter
     * @date 2026-05-29 11:10:07
     */
    public void startBillingModelTimer(Runnable task, long initialDelay, long period, TimeUnit unit) {
        stopBillingModelTimer();
        this.billingModelTimer = globalScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 停止充电桩计费模型请求定时器
     * 安全关闭定时器并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-29 11:10:49
     */
    public void stopBillingModelTimer() {
        if (billingModelTimer != null) {
            billingModelTimer.cancel(false);
            billingModelTimer = null;
        }
    }

    /**
     * 启动心跳定时发送器
     * 登录成功后调用,每10秒自动发送一次心跳包
     *
     * @param task         定时执行业务逻辑
     * @param initialDelay 初始延迟秒
     * @param period       轮询周期秒
     * @param unit         时间单位
     * @author KevenPotter
     * @date 2026-05-28 13:45:39
     */
    public void startHeartbeatTimer(Runnable task, long initialDelay, long period, TimeUnit unit) {
        stopHeartbeatTimer();
        this.heartbeatTimer = globalScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 停止心跳定时器
     * 安全关闭心跳任务并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-28 13:46:50
     */
    public void stopHeartbeatTimer() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel(false);
            heartbeatTimer = null;
        }
    }

    /**
     * 启动实时监测数据定时发送器
     *
     * @param task         定时执行业务逻辑
     * @param initialDelay 初始延迟秒
     * @param period       轮询周期秒
     * @param unit         时间单位
     * @author KevenPotter
     * @date 2026-06-03 10:37:11
     */
    public void startRealTimeMonitorTimer(Runnable task, long initialDelay, long period, TimeUnit unit) {
        stopRealTimeMonitorTimer();
        this.realTimeMonitorTimer = globalScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * 停止实时监测数据定时器
     * 安全关闭实时上报任务并释放资源
     *
     * @author KevenPotter
     * @date 2026-06-03 10:36:20
     */
    public void stopRealTimeMonitorTimer() {
        if (realTimeMonitorTimer != null) {
            realTimeMonitorTimer.cancel(false);
            realTimeMonitorTimer = null;
        }
    }

    /**
     * 停止所有定时器(重连/关闭时使用)
     *
     * @author KevenPotter
     * @date 2026-05-29 11:12:40
     */
    public void stopAllTimers() {
        stopLoginTimer();
        stopBillingValidTimer();
        stopBillingModelTimer();
        stopHeartbeatTimer();
        stopRealTimeMonitorTimer();
    }

    /**
     * 销毁调度器,关闭线程池释放资源,处理器销毁时调用
     *
     * @author KevenPotter
     * @date 2026-06-25 11:23:50
     */
    public void shutdownScheduler() {
        stopAllTimers();
        if (!globalScheduler.isShutdown()) {
            globalScheduler.shutdown();
        }
    }
}
