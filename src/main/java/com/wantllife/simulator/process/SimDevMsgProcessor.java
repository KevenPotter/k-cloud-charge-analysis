package com.wantllife.simulator.process;

import cn.hutool.core.util.HexUtil;
import com.wantllife.domain.vo.StandardBillingModel;
import com.wantllife.domain.vo.StandardDevice;
import com.wantllife.domain.vo.StandardRealTimeMonitor;
import com.wantllife.domain.vo.StandardTradeRecord;
import com.wantllife.simulator.business.ChargeSessionManager;
import com.wantllife.simulator.client.TcpClient;
import com.wantllife.simulator.manager.SimTimerScheduler;
import com.wantllife.simulator.req.*;
import com.wantllife.simulator.res.*;
import com.wantllife.simulator.state.DeviceState;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.wantllife.constant.CloudFastChargingConstants.*;
import static com.wantllife.constant.SimulatorConstants.*;
import static com.wantllife.simulator.fake.FakeData.fakeChargingRealTimeMonitor;
import static com.wantllife.simulator.fake.FakeData.fakeInitRealTimeMonitor;

/**
 * 默认消息处理器
 * 实现:解析下行指令 → 自动构造上行应答
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
    /** 定时任务调度管理器,接管所有定时任务生命周期 */
    private final SimTimerScheduler timerScheduler;
    /** 充电会话管理器,接管全部充电启停、计费、结算业务 */
    private final ChargeSessionManager chargeSessionManager;
    /** 设备状态管理器,统一托管所有运行状态与标记 */
    private final DeviceStateHolder deviceStateHolder;

    /**
     * 无参构造，初始化定时调度与充电会话管理器
     *
     * @author KevenPotter
     * @date 2026-07-02 15:56:08
     */
    public SimDevMsgProcessor() {
        this.timerScheduler = new SimTimerScheduler();
        this.deviceStateHolder = new DeviceStateHolder();
        this.chargeSessionManager = new ChargeSessionManager(timerScheduler);
        this.chargeSessionManager.setMonitorDataSendCallback(this::sendMessage);
    }

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
        deviceStateHolder.bindDeviceId(this.deviceId);
        chargeSessionManager.initDeviceInfo(deviceId, gunNo);
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
                    if (!deviceStateHolder.isInitRealTimeSent() && deviceStateHolder.isCurrentState(DeviceState.READY)) {
                        deviceStateHolder.markInitRealTimeSent(true);
                        sendMessage(SAERealTimeMonitorRes.buildCommand(fakeInitRealTimeMonitor(deviceId, gunNo)));
                    }
                    new SABHeartbeatReq(data, rawHexMsg);
                    break;
                // 模拟器计费模型验证请求应答
                case SIM_UP_BILLING_MODE_VALID:
                    handleBillingValidReply(new SACBillingModeValidReq(data, rawHexMsg));
                    break;
                // 模拟器计费模型请求应答
                case SIM_UP_BILLING_MODE:
                    SADBillingModelReq billingModelReq = new SADBillingModelReq(data, rawHexMsg);
                    handleBillingModelReply(billingModelReq);
                    break;
                // 模拟器读取实时监测数据
                case SIM_UP_REAL_TIME_MONITOR:
                    new SAERealTimeMonitorReq(data, rawHexMsg);
                    StandardRealTimeMonitor currentMonitor = chargeSessionManager.isCharging() ?
                            fakeChargingRealTimeMonitor(
                                    chargeSessionManager.getTradeNo(),
                                    deviceId,
                                    gunNo,
                                    chargeSessionManager.getAccumulatedMinutes(),
                                    chargeSessionManager.getRemainingMinutes(),
                                    chargeSessionManager.getChargingDegree(),
                                    chargeSessionManager.getChargedAmount()
                            )
                            : fakeInitRealTimeMonitor(deviceId, gunNo);
                    sendMessage(SAERealTimeMonitorRes.buildCommand(currentMonitor));
                    break;
                // 模拟器运营平台确认启动充电
                case SIM_UP_REQUEST_CHARGING:
                    new SANRequestChargingReq(data, rawHexMsg);
                    break;
                // 模拟器运营平台远程控制启机
                case SIM_UP_START_CHARGE:
                    SAOStartChargeReq startChargeReq = new SAOStartChargeReq(data, rawHexMsg);
                    if (!chargeSessionManager.isCharging()) {
                        sendMessage(SAOStartChargeRes.buildCommand(startChargeReq, 1, 0));
                        // 充电业务移交会话管理器
                        chargeSessionManager.startCharge(startChargeReq.getTradeNo());
                    } else {
                        sendMessage(SAOStartChargeRes.buildCommand(startChargeReq, 0, 2));
                    }
                    break;
                // 模拟器运营平台远程停机
                case SIM_UP_STOP_CHARGE:
                    SAPStopChargeReq stopChargeReq = new SAPStopChargeReq(data, rawHexMsg);
                    sendMessage(SAPStopChargeRes.buildCommand(stopChargeReq));
                    String tmpChargeTradeNo = chargeSessionManager.getTradeNo();
                    if (chargeSessionManager.isCharging()) {
                        StandardTradeRecord tradeRecord = chargeSessionManager.stopCharge(tmpChargeTradeNo);
                        if (tradeRecord != null) {
                            sendMessage(SAQTradeRecordRes.buildCommand(tradeRecord));
                        }
                    }
                    break;
                // 模拟器交易记录确认
                case SIM_UP_TRADE_RECORD:
                    new SAQTradeRecordReq(data, rawHexMsg, this.deviceId);
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
                    SAZLockUpDownReq lockUpDownReq = new SAZLockUpDownReq(data, rawHexMsg);
                    sendMessage(SAZLockUpDownRes.buildCommand(lockUpDownReq));
                    break;
                // 模拟器远程重启
                case SIM_UP_REBOOT:
                    SBARebootReq rebootReq = new SBARebootReq(data, rawHexMsg);
                    sendMessage(SBARebootRes.buildCommand(rebootReq));
                    // 远程重启:断开连接 → ?秒后重连
                    tcpClient.restart(DELAY_REBOOT_SECOND);
                    break;
                // 模拟器远程更新
                case SIM_UP_UPGRADE:
                    SBBUpgradeReq upgradeReq = new SBBUpgradeReq(data, rawHexMsg);
                    sendMessage(SBBUpgradeRes.buildCommand(upgradeReq));
                    // 远程升级:断开连接 → ?秒重连
                    tcpClient.restart(DELAY_UPGRADE_SECOND);
                    break;
                // 模拟器运营平台确认并充启动充电
                case SIM_UP_APPLY_PARALLEL_CHARGING:
                    new SBCApplyParallelChargingReq(data, rawHexMsg);
                    break;
                // 模拟器运营平台远程控制并充启机
                case SIM_UP_PARALLEL_START_CHARGE:
                    SBDParallelStartChargeReq parallelStartChargeReq = new SBDParallelStartChargeReq(data, rawHexMsg);
                    sendMessage(SBDParallelStartChargeRes.buildCommand(parallelStartChargeReq));
                    // 充电业务移交会话管理器
                    chargeSessionManager.startCharge(parallelStartChargeReq.getTradeNo());
                    break;

            }
        } catch (Exception e) {
            log.error("{} {} {} Message Process Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
        }
    }

    /**
     * 处理登录应答
     *
     * @author KevenPotter
     * @date 2026-05-29 11:02:18
     */
    private void handleLoginReply(SAALoginReq req) {
        if (!deviceStateHolder.isCurrentState(DeviceState.WAIT_LOGIN)) return;
        if (req.getLoginResult() != 0) return;

        deviceStateHolder.setLoginReq(req);
        timerScheduler.stopLoginTimer();

        // 进入计费验证状态
        deviceStateHolder.switchState(DeviceState.WAIT_BILLING_VALID);
        startBillingValidTimer();
    }

    /**
     * 处理计费验证应答
     *
     * @author KevenPotter
     * @date 2026-05-29 11:02:40
     */
    private void handleBillingValidReply(SACBillingModeValidReq req) {
        if (!deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_VALID)) return;

        // 保存平台下发的计费编码
        deviceStateHolder.setPlatformBillingModeId(req.getBillingModeId());

        // 验证一致 → 进入下一阶段
        if (req.getBillingModeValidResult() == 0) {
            timerScheduler.stopBillingValidTimer();
            deviceStateHolder.switchState(DeviceState.WAIT_BILLING_MODEL);
            startBillingModelTimer();
        }
    }

    /**
     * 处理计费模型应答
     *
     * @param billingModelReq 计费模型请求应答
     * @author KevenPotter
     * @date 2026-05-29 11:03:43
     */
    private void handleBillingModelReply(SADBillingModelReq billingModelReq) {
        if (!deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_MODEL)) return;

        timerScheduler.stopBillingModelTimer();
        deviceStateHolder.switchState(DeviceState.READY);
        startHeartbeatTimer();

        // 1.先拿到当前应答对象
        List<StandardBillingModel> modelList = billingModelReq.getBillingModelList();

        // 2.遍历填充时段字符串
        String sharpTime = null, peakTime = null, flatTime = null, valleyTime = null;
        for (StandardBillingModel item : modelList) {
            String timeStr = item.getStartTime() + "-" + item.getEndTime();
            switch (item.getTimeSlotType()) {
                case 1:
                    sharpTime = timeStr;
                    break;
                case 2:
                    peakTime = timeStr;
                    break;
                case 3:
                    flatTime = timeStr;
                    break;
                case 4:
                    valleyTime = timeStr;
                    break;
            }
        }
        // 3.一次性完成电价、服务费、时段、损耗全部赋值
        chargeSessionManager.setBillingModelData(
                billingModelReq.getSharpEleFee(), billingModelReq.getSharpServiceFee(), sharpTime,
                billingModelReq.getPeakEleFee(), billingModelReq.getPeakServiceFee(), peakTime,
                billingModelReq.getFlatEleFee(), billingModelReq.getFlatServiceFee(), flatTime,
                billingModelReq.getValleyEleFee(), billingModelReq.getValleyServiceFee(), valleyTime,
                billingModelReq.getLossRatio());
    }

    /**
     * TCP连接成功回调
     * 由TcpClient连接建立后调用,重置状态并开始登录
     *
     * @author KevenPotter
     * @date 2026-05-27 17:02:34
     */
    public void onConnected() {
        // 重置所有状态
        deviceStateHolder.resetAllState();

        // 关闭所有旧定时器
        timerScheduler.stopAllTimers();
        // 重连清空充电会话
        chargeSessionManager.resetAllChargeData();
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
        Runnable loginTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.WAIT_LOGIN)) {
                    sendMessage(SAALoginRes.buildCommand(tcpClient.getDevice()));
                }
            } catch (Exception e) {
                log.error("{} {} {} StartLoginTimer Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
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
    private void startBillingValidTimer() {
        Runnable billingValidTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_VALID)) {
                    Long billingModeId = deviceStateHolder.getPlatformBillingModeId() != null ? Long.valueOf(deviceStateHolder.getPlatformBillingModeId()) : 1L;
                    sendMessage(SACBillingModeValidRes.buildCommand(deviceId, billingModeId));
                }
            } catch (Exception e) {
                log.error("{} {} {} StartBillingValidTimer Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
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
    private void startBillingModelTimer() {
        Runnable billingModelTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_MODEL)) {
                    sendMessage(SADBillingModelRes.buildCommand(deviceId));
                }
            } catch (Exception e) {
                log.error("{} {} {} StartBillingModelTimer Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
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
    private void startHeartbeatTimer() {
        Runnable heartbeatTask = () -> {
            try {
                if (deviceStateHolder.isCurrentState(DeviceState.READY)) {
                    SAALoginReq loginReq = deviceStateHolder.getLoginReq();
                    if (loginReq != null) {
                        sendMessage(SABHeartbeatRes.buildCommand(loginReq, gunNo, 0));
                    }
                }
            } catch (Exception e) {
                log.error("{} {} {} StartHeartbeatTimer Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
            }
        };
        timerScheduler.startHeartbeatTimer(heartbeatTask, TIMER_HEARTBEAT_SECOND, TIMER_HEARTBEAT_SECOND, TimeUnit.SECONDS);
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
            outputStream = null;
            tcpClient.closeSocket();
        }
    }

    /**
     * 销毁处理器,关闭定时线程池,释放资源
     *
     * @author KevenPotter
     * @date 2026-06-23 14:29:30
     */
    public void destroy() {
        // 先停止所有单个定时任务
        timerScheduler.shutdownScheduler();
    }

    /**
     * 链路断开时清理定时任务,不销毁线程池,用于自动重连、远程重启、升级
     *
     * @author KevenPotter
     * @date 2026-06-25 13:56:02
     */
    public void clearAllTimers() {
        timerScheduler.stopAllTimers();
    }

}
