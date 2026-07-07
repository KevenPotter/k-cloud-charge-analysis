package com.wantllife.simulator.process;

import com.wantllife.simulator.client.TcpClient;
import com.wantllife.simulator.manager.SimTimerScheduler;
import com.wantllife.simulator.req.SAALoginReq;
import com.wantllife.simulator.res.SAALoginRes;
import com.wantllife.simulator.res.SABHeartbeatRes;
import com.wantllife.simulator.res.SACBillingModeValidRes;
import com.wantllife.simulator.res.SADBillingModelRes;
import com.wantllife.simulator.state.DeviceState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_PROJECT_NAME;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_TIP_ICON;
import static com.wantllife.constant.SimulatorConstants.*;

/**
 * 设备定时任务门面类
 * <p>
 * 收拢单台设备全部业务定时创建逻辑,作为SimTimerScheduler上层业务封装层
 * 底层SimTimerScheduler只负责线程池与任务启停,本类封装所有业务Runnable、状态判断、报文构造、日志
 * 统一持有单设备专属上下文,消除主处理器分散的定时业务代码,定时问题仅需查询本文件
 *
 * @author KevenPotter
 * @date 2026-07-07 10:45:30
 */
@Slf4j
public class DeviceTimerFacade {

    /** 定时任务调度管理器,接管所有定时任务生命周期 */
    public final SimTimerScheduler timerScheduler;
    /** 设备状态管理器,统一托管所有运行状态与标记 */
    private final DeviceStateHolder deviceStateHolder;
    /** 当前设备编号 */
    private final String deviceId;
    /** 当前设备枪号 */
    private final Integer gunNo;
    /** 当前设备对应的TCP客户端 */
    private final TcpClient tcpClient;
    /** 报文发送回调,所有定时任务上行报文统一调用该出口 */
    private final Consumer<byte[]> sendMsgCallback;

    /**
     * 全参构造，一次性注入单设备全部依赖上下文
     *
     * @param timerScheduler    底层定时调度器
     * @param deviceStateHolder 设备状态持有管理器
     * @param deviceId          设备编号
     * @param gunNo             设备枪号
     * @param tcpClient         TCP客户端实例
     * @param sendMsgCallback   报文发送回调函数
     * @author KevenPotter
     * @date 2026-07-07 10:47:36
     */
    public DeviceTimerFacade(
            SimTimerScheduler timerScheduler, DeviceStateHolder deviceStateHolder,
            String deviceId, Integer gunNo,
            TcpClient tcpClient, Consumer<byte[]> sendMsgCallback) {
        this.timerScheduler = timerScheduler;
        this.deviceStateHolder = deviceStateHolder;
        this.deviceId = deviceId;
        this.gunNo = gunNo;
        this.tcpClient = tcpClient;
        this.sendMsgCallback = sendMsgCallback;
    }

    /**
     * 启动登录重试定时器
     * 先停止旧定时器避免重复,再创建新定时器每5秒发送一次登录帧,直到登录成功为止
     *
     * @author KevenPotter
     * @date 2026-05-28 13:44:21
     */
    public void startLoginTimer() {
        Runnable loginTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.WAIT_LOGIN)) {
                    byte[] loginCmd = SAALoginRes.buildCommand(tcpClient.getDevice());
                    sendMsgCallback.accept(loginCmd);
                }
            } catch (Exception e) {
                log.error("{} {} {} StartLoginTimer Task Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
            }
        };
        timerScheduler.startLoginTimer(loginTask, 0, TIMER_LOGIN_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 启动计费验证定时器
     * 先停止旧定时器避免重复,再创建新定时器每3秒发送一次计费模型验证请求,直到计费模型验证请求成功为止
     *
     * @author KevenPotter
     * @date 2026-05-29 11:07:22
     */
    public void startBillingValidTimer() {
        Runnable billingValidTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_VALID)) {
                    Long billingModeId = deviceStateHolder.getPlatformBillingModeId() != null
                            ? Long.valueOf(deviceStateHolder.getPlatformBillingModeId())
                            : 1L;
                    byte[] validCmd = SACBillingModeValidRes.buildCommand(deviceId, billingModeId);
                    sendMsgCallback.accept(validCmd);
                }
            } catch (Exception e) {
                log.error("{} {} {} StartBillingValidTimer Task Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
            }
        };
        timerScheduler.startBillingValidTimer(billingValidTask, 0, TIMER_BILLING_MODE_VALID_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 启动充电桩计费模型请求定时器
     * 先停止旧定时器避免重复,再创建新定时器每3秒发送一次充电桩计费模型请求,直到充电桩计费模型请求成功为止
     *
     * @author KevenPotter
     * @date 2026-05-29 11:10:07
     */
    public void startBillingModelTimer() {
        Runnable billingModelTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_MODEL)) {
                    byte[] modelCmd = SADBillingModelRes.buildCommand(deviceId);
                    sendMsgCallback.accept(modelCmd);
                }
            } catch (Exception e) {
                log.error("{} {} {} StartBillingModelTimer Task Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
            }
        };
        timerScheduler.startBillingModelTimer(billingModelTask, 0, TIMER_BILLING_MODE_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 启动心跳定时发送器
     * 登录成功后调用,每10秒自动发送一次心跳包
     *
     * @author KevenPotter
     * @date 2026-05-28 13:45:39
     */
    public void startHeartbeatTimer() {
        Runnable heartbeatTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.READY)) {
                    SAALoginReq loginReq = deviceStateHolder.getLoginReq();
                    if (loginReq != null) {
                        byte[] heartbeatCmd = SABHeartbeatRes.buildCommand(loginReq, gunNo, 0);
                        sendMsgCallback.accept(heartbeatCmd);
                    }
                }
            } catch (Exception e) {
                log.error("{} {} {} StartHeartbeatTimer Task Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
            }
        };
        timerScheduler.startHeartbeatTimer(heartbeatTask, TIMER_HEARTBEAT_SECOND, TIMER_HEARTBEAT_SECOND, TimeUnit.SECONDS);
    }
}
