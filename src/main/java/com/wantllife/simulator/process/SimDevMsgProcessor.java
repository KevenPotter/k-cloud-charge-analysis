package com.wantllife.simulator.process;

import cn.hutool.core.util.HexUtil;
import com.wantllife.domain.vo.StandardDevice;
import com.wantllife.domain.vo.StandardRealTimeMonitor;
import com.wantllife.simulator.client.TcpClient;
import com.wantllife.simulator.req.*;
import com.wantllife.simulator.res.*;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.wantllife.constant.CloudFastChargingConstants.*;
import static com.wantllife.constant.SimulatorConstants.*;
import static com.wantllife.simulator.fake.FakeData.fakeChargingRealTimeMonitor;
import static com.wantllife.simulator.fake.FakeData.fakeInitRealTimeMonitor;

/**
 * 默认消息处理器
 * 实现：解析下行指令 → 自动构造上行应答
 * 用户可自行继承此类扩展自定义逻辑
 *
 * @author KevenPotter
 * @date 2026-05-26 13:44:41
 */
@Slf4j
public class SimDevMsgProcessor {

    /** TCP输出流:用于向服务器发送报文 */
    private OutputStream outputStream;
    /** 当前设备对应的TCP客户端 */
    private TcpClient tcpClient;
    /** 当前设备编号 */
    private String deviceId;
    /** 当前设备枪号 */
    private Integer gunNo;
    /** 交易流水号 */
    private String tradeNo;
    /** 是否正在充电中 */
    private boolean isCharging = false;
    /** 充电开始时间 */
    private LocalDateTime chargeStartTime;
    /** 总充电时间(固定120分钟,后续可改) */
    public static final int TOTAL_CHARGE_MINUTES = 120;
    /** 累计充电分钟数 */
    private int accumulatedMinutes = 0;
    /** 剩余充电分钟数 */
    private int remainingMinutes = TOTAL_CHARGE_MINUTES;
    /** 累计充电度数 */
    private BigDecimal chargingDegree = BigDecimal.ZERO;
    /** 已充金额 */
    private BigDecimal chargedAmount = BigDecimal.ZERO;

    /** 设备状态机(独立枚举) */
    private DeviceState currentState = DeviceState.WAIT_LOGIN;
    /** 登录请求对象(用于心跳) */
    private SAALoginReq loginReq;
    /** 平台下发的计费模型编码 */
    private Integer platformBillingModeId;

    /** 登录定时器 */
    private ScheduledExecutorService loginTimer;
    /** 计费验证定时器 */
    private ScheduledExecutorService billingValidTimer;
    /** 计费模型请求定时器 */
    private ScheduledExecutorService billingModelTimer;
    /** 心跳定时器 */
    private ScheduledExecutorService heartbeatTimer;
    /** 实时监测数据定时器 */
    private ScheduledExecutorService realTimeMonitorTimer;

    /** 实时监测数据[0x13指令]是否已经发送过初始化 */
    private boolean initRealTimeSent = false;

    /**
     * 绑定TCP输出流与客户端实例
     * 由TcpClient连接成功后调用
     *
     * @param device       设备信息
     * @param outputStream 输出流
     * @param tcpClient    TCP客户端
     * @author KevenPotter
     * @date 2026-05-27 17:00:00
     */
    public void bindOutputStream(StandardDevice device, OutputStream outputStream, TcpClient tcpClient) {
        this.deviceId = device.getDeviceId();
        this.gunNo = device.getGunNum();
        this.outputStream = outputStream;
        this.tcpClient = tcpClient;
    }

    /**
     * 处理服务器下行报文
     *
     * @param data 原始报文字节数组
     * @author KevenPotter
     * @date 2026-05-27 17:01:20
     */
    public void process(byte[] data) {
        try {
            // 1. 基础校验
            if (data.length < 6) return;

            // 2. 原始报文
            String rawHexMsg = HexUtil.encodeHexStr(data).toUpperCase();

            // 3. 获取帧类型
            byte frameType = data[5];

            switch (frameType) {
                // 模拟器登录认证应答
                case SIM_UP_LOGIN:
                    handleLoginReply(new SAALoginReq(data, rawHexMsg));
                    break;
                // 模拟器心跳包应答
                case SIM_UP_HEARTBEAT:
                    // 心跳第一次发送后 → 上传一次初始化0x13
                    if (!initRealTimeSent && currentState == DeviceState.READY) {
                        initRealTimeSent = true;
                        sendMessage(SAERealTimeMonitorRes.buildCommand(fakeInitRealTimeMonitor(deviceId, gunNo)));
                    }
                    SABHeartbeatReq heartbeatReq = new SABHeartbeatReq(data, rawHexMsg);
                    break;
                // 模拟器计费模型验证请求应答
                case SIM_UP_BILLING_MODE_VALID:
                    handleBillingValidReply(new SACBillingModeValidReq(data, rawHexMsg));
                    break;
                // 模拟器计费模型请求应答
                case SIM_UP_BILLING_MODE:
                    SADBillingModelReq billingModelReq = new SADBillingModelReq(data, rawHexMsg);
                    handleBillingModelReply();
                    break;
                // 模拟器读取实时监测数据
                case SIM_UP_REAL_TIME_MONITOR:
                    SAERealTimeMonitorReq realTimeMonitorReq = new SAERealTimeMonitorReq(data, rawHexMsg);
                    StandardRealTimeMonitor currentMonitor = isCharging
                            ? fakeChargingRealTimeMonitor(tradeNo, deviceId, gunNo, accumulatedMinutes, remainingMinutes, chargingDegree, chargedAmount)
                            : fakeInitRealTimeMonitor(deviceId, gunNo);
                    sendMessage(SAERealTimeMonitorRes.buildCommand(currentMonitor));
                    break;
                // 模拟器运营平台确认启动充电
                case SIM_UP_REQUEST_CHARGING:
                    break;
                // 模拟器运营平台远程控制启机
                case SIM_UP_START_CHARGE:
                    SAOStartChargeReq startChargeReq = new SAOStartChargeReq(data, rawHexMsg);
                    sendMessage(SAOStartChargeRes.buildCommand(startChargeReq));
                    // 开始充电 → 启动实时数据上传
                    startCharging(startChargeReq.getTradeNo());
                    break;
                // 模拟器运营平台远程停机
                case SIM_UP_STOP_CHARGE:
                    SAPStopChargeReq stopChargeReq = new SAPStopChargeReq(data, rawHexMsg);
                    sendMessage(SAPStopChargeRes.buildCommand(stopChargeReq));
                    stopCharging();
                    break;
                // 模拟器交易记录确认
                case SIM_UP_TRADE_RECORD:
                    break;
                // 模拟器远程账户余额更新
                case SIM_UP_BALANCE_UPDATE:
                    SARBalanceUpdateReq balanceUpdateReq = new SARBalanceUpdateReq(data, rawHexMsg);
                    sendMessage(SARBalanceUpdateRes.buildCommand(balanceUpdateReq));
                    break;
                // 模拟器离线卡数据同步
                case SIM_UP_OFFLINE_CARD_SYNC:
                    SASOfflineCardSyncReq offlineCardSyncReq = new SASOfflineCardSyncReq(data, rawHexMsg);
                    sendMessage(SASOfflineCardSyncRes.buildCommand(offlineCardSyncReq));
                    break;
                // 模拟器离线卡数据清除
                case SIM_UP_OFFLINE_CARD_CLEAR:
                    SATOfflineCardClearReq offlineCardClearReq = new SATOfflineCardClearReq(data, rawHexMsg);
                    sendMessage(SATOfflineCardClearRes.buildCommand(offlineCardClearReq));
                    break;
                // 模拟器离线卡数据查询
                case SIM_UP_OFFLINE_CARD_QUERY:
                    SAUOfflineCardQueryReq offlineCardQueryReq = new SAUOfflineCardQueryReq(data, rawHexMsg);
                    sendMessage(SAUOfflineCardQueryRes.buildCommand(offlineCardQueryReq));
                    break;
                // 模拟器充电桩工作参数设置
                case SIM_UP_WORKING_PARAMS:
                    SAVWorkingParamsReq workingParamsReq = new SAVWorkingParamsReq(data, rawHexMsg);
                    sendMessage(SAVWorkingParamsRes.buildCommand(workingParamsReq));
                    break;
                // 模拟器对时设置
                case SIM_UP_TIME_SYNC:
                    SAWTimeSyncReq timeSyncReq = new SAWTimeSyncReq(data, rawHexMsg);
                    sendMessage(SAWTimeSyncRes.buildCommand(timeSyncReq));
                    break;
                // 模拟器计费模型设置
                case SIM_UP_BILLING_MODE_SET:
                    SAXBillingModeSetReq billingModeSetReq = new SAXBillingModeSetReq(data, rawHexMsg);
                    sendMessage(SAXBillingModeSetRes.buildCommand(billingModeSetReq));
                    break;
                // 模拟器遥控地锁升锁与降锁
                case SIM_UP_LOCK_UP_DOWN:
                    break;
                // 模拟器远程重启
                case SIM_UP_REBOOT:
                    break;
                // 模拟器远程更新
                case SIM_UP_UPGRADE:
                    break;
                // 模拟器运营平台确认并充启动充电
                case SIM_UP_APPLY_PARALLEL_CHARGING:
                    break;
                // 模拟器运营平台远程控制并充启机
                case SIM_UP_PARALLEL_START_CHARGE:
                    break;

            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 开始充电
     *
     * @author KevenPotter
     * @date 2026-06-02 16:04:30
     */
    private void startCharging(String tradeNo) {
        this.tradeNo = tradeNo;
        this.isCharging = true;
        this.chargeStartTime = LocalDateTime.now();
        this.accumulatedMinutes = 0;
        this.remainingMinutes = TOTAL_CHARGE_MINUTES;
        this.chargingDegree = BigDecimal.ZERO;
        this.chargedAmount = BigDecimal.ZERO;
        startRealTimeMonitorTimer();
    }

    /**
     * 停止充电
     *
     * @author KevenPotter
     * @date 2026-06-02 16:04:40
     */
    private void stopCharging() {
        this.isCharging = false;
        this.tradeNo = null;
        stopRealTimeMonitorTimer();
    }

    /**
     * 处理登录应答
     *
     * @author KevenPotter
     * @date 2026-05-29 11:02:18
     */
    private void handleLoginReply(SAALoginReq req) {
        if (currentState != DeviceState.WAIT_LOGIN) return;
        if (req.getLoginResult() != 0) return;

        this.loginReq = req;
        stopLoginTimer();

        // 进入计费验证状态
        currentState = DeviceState.WAIT_BILLING_VALID;
        startBillingValidTimer();
    }

    /**
     * 处理计费验证应答
     *
     * @author KevenPotter
     * @date 2026-05-29 11:02:40
     */
    private void handleBillingValidReply(SACBillingModeValidReq req) {
        if (currentState != DeviceState.WAIT_BILLING_VALID) return;

        // 保存平台下发的计费编码
        this.platformBillingModeId = req.getBillingModeId();

        // 验证一致 → 进入下一阶段
        if (req.getBillingModeValidResult() == 0) {
            stopBillingValidTimer();
            currentState = DeviceState.WAIT_BILLING_MODEL;
            startBillingModelTimer();
        }
    }

    /**
     * 处理计费模型应答
     *
     * @author KevenPotter
     * @date 2026-05-29 11:03:43
     */
    private void handleBillingModelReply() {
        if (currentState != DeviceState.WAIT_BILLING_MODEL) return;

        stopBillingModelTimer();
        currentState = DeviceState.READY;
        startHeartbeatTimer();
    }

    /**
     * TCP连接成功回调
     * 由TcpClient连接建立后调用,重置状态并开始登录
     *
     * @param deviceId 设备编号
     * @author KevenPotter
     * @date 2026-05-27 17:02:34
     */
    public void onConnected(String deviceId) {
        // 重置所有状态
        currentState = DeviceState.WAIT_LOGIN;
        loginReq = null;
        platformBillingModeId = null;
        initRealTimeSent = false;
        this.isCharging = false;
        this.tradeNo = null;
        this.accumulatedMinutes = 0;
        this.remainingMinutes = TOTAL_CHARGE_MINUTES;
        // 关闭所有旧定时器
        stopAllTimers();
        // 启动登录
        startLoginTimer();
    }

    /**
     * 启动登录重试定时器
     * 先停止旧定时器避免重复,再创建新定时器每5秒发送一次登录帧,直到登录成功为止
     *
     * @author KevenPotter
     * @date 2026-05-28 13:44:21
     */
    private void startLoginTimer() {
        stopLoginTimer();
        loginTimer = new ScheduledThreadPoolExecutor(1);
        loginTimer.scheduleAtFixedRate(() -> {
            try {
                if (currentState == DeviceState.WAIT_LOGIN) {
                    sendMessage(SAALoginRes.buildCommand(tcpClient.getDevice()));
                }
            } catch (Exception ignored) {
            }
        }, 0, TIMER_LOGIN_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 停止登录重试定时器
     * 安全关闭定时器并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-28 13:45:24
     */
    private void stopLoginTimer() {
        if (loginTimer != null) {
            loginTimer.shutdownNow();
            loginTimer = null;
        }
    }

    /**
     * 启动计费验证定时器
     * 先停止旧定时器避免重复,再创建新定时器每3秒发送一次计费模型验证请求,直到计费模型验证请求成功为止
     *
     * @author KevenPotter
     * @date 2026-05-29 11:07:22
     */
    private void startBillingValidTimer() {
        stopBillingValidTimer();
        billingValidTimer = new ScheduledThreadPoolExecutor(1);
        billingValidTimer.scheduleAtFixedRate(() -> {
            try {
                if (currentState == DeviceState.WAIT_BILLING_VALID) {
                    long billingModeId = platformBillingModeId != null ? platformBillingModeId : 1L;
                    sendMessage(SACBillingModeValidRes.buildCommand(deviceId, billingModeId));
                }
            } catch (Exception ignored) {
            }
        }, 0, TIMER_BILLING_MODE_VALID_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 停止计费验证定时器
     * 安全关闭定时器并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-29 11:08:30
     */
    private void stopBillingValidTimer() {
        if (billingValidTimer != null) {
            billingValidTimer.shutdownNow();
            billingValidTimer = null;
        }
    }

    /**
     * 启动充电桩计费模型请求定时器
     * 先停止旧定时器避免重复,再创建新定时器每3秒发送一次充电桩计费模型请求,直到充电桩计费模型请求成功为止
     *
     * @author KevenPotter
     * @date 2026-05-29 11:10:07
     */
    private void startBillingModelTimer() {
        stopBillingModelTimer();
        billingModelTimer = new ScheduledThreadPoolExecutor(1);
        billingModelTimer.scheduleAtFixedRate(() -> {
            try {
                if (currentState == DeviceState.WAIT_BILLING_MODEL) {
                    sendMessage(SADBillingModelRes.buildCommand(deviceId));
                }
            } catch (Exception ignored) {
            }
        }, 0, TIMER_BILLING_MODE_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 停止充电桩计费模型请求定时器
     * 安全关闭定时器并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-29 11:10:49
     */
    private void stopBillingModelTimer() {
        if (billingModelTimer != null) {
            billingModelTimer.shutdownNow();
            billingModelTimer = null;
        }
    }

    /**
     * 启动心跳定时发送器
     * 登录成功后调用,每10秒自动发送一次心跳包
     *
     * @author KevenPotter
     * @date 2026-05-28 13:45:39
     */
    private void startHeartbeatTimer() {
        stopHeartbeatTimer();
        heartbeatTimer = new ScheduledThreadPoolExecutor(1);
        heartbeatTimer.scheduleAtFixedRate(() -> {
            try {
                if (currentState == DeviceState.READY) {
                    sendMessage(SABHeartbeatRes.buildCommand(loginReq, gunNo, 0));
                }
            } catch (Exception ignored) {
            }
        }, TIMER_HEARTBEAT_SECOND, TIMER_HEARTBEAT_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 停止心跳定时器
     * 安全关闭心跳任务并释放资源
     *
     * @author KevenPotter
     * @date 2026-05-28 13:46:50
     */
    private void stopHeartbeatTimer() {
        if (heartbeatTimer != null) {
            heartbeatTimer.shutdownNow();
            heartbeatTimer = null;
        }
    }

    /**
     * 启动实时监测数据定时发送器
     *
     * @author KevenPotter
     * @date 2026-06-03 10:37:11
     */
    private void startRealTimeMonitorTimer() {
        stopRealTimeMonitorTimer();
        realTimeMonitorTimer = new ScheduledThreadPoolExecutor(1);
        realTimeMonitorTimer.scheduleAtFixedRate(() -> {
            try {
                if (!isCharging) return;

                // 真实时间计算
                long minutes = Duration.between(chargeStartTime, LocalDateTime.now()).toMinutes();

                // 越界保护
                if (minutes >= TOTAL_CHARGE_MINUTES) {
                    minutes = TOTAL_CHARGE_MINUTES;
                }

                // 自动计算
                accumulatedMinutes = (int) minutes;
                remainingMinutes = TOTAL_CHARGE_MINUTES - accumulatedMinutes;
                chargingDegree = BigDecimal.valueOf(accumulatedMinutes * 0.5);  // 每分钟0.5度
                chargedAmount = BigDecimal.valueOf(accumulatedMinutes * 0.8);  // 每分钟0.8元

                // 发送
                StandardRealTimeMonitor monitor = fakeChargingRealTimeMonitor(
                        tradeNo,
                        deviceId,
                        gunNo,
                        accumulatedMinutes,
                        remainingMinutes,
                        chargingDegree,
                        chargedAmount
                );
                sendMessage(SAERealTimeMonitorRes.buildCommand(monitor));

            } catch (Exception ignored) {
            }
        }, 0, TIMER_REAL_TIME_MONITOR_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 停止实时监测数据定时器
     * 安全关闭心跳任务并释放资源
     *
     * @author KevenPotter
     * @date 2026-06-03 10:36:20
     */
    private void stopRealTimeMonitorTimer() {
        if (realTimeMonitorTimer != null) {
            realTimeMonitorTimer.shutdownNow();
            realTimeMonitorTimer = null;
        }
    }

    /**
     * 停止所有定时器(重连/关闭时使用)
     *
     * @author KevenPotter
     * @date 2026-05-29 11:12:40
     */
    private void stopAllTimers() {
        stopLoginTimer();
        stopBillingValidTimer();
        stopBillingModelTimer();
        stopHeartbeatTimer();
        stopRealTimeMonitorTimer();
    }

    /**
     * 发送字节数组报文
     *
     * @param data 报文字节数组
     * @author KevenPotter
     * @date 2026-05-26 16:03:12
     */
    public void sendMessage(byte[] data) {
        try {
            if (outputStream != null) {
                outputStream.write(data);
                outputStream.flush();
            }
        } catch (Exception e) {
            tcpClient.stop();
        }
    }

}
