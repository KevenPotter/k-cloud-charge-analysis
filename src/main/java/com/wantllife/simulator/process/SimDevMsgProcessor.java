package com.wantllife.simulator.process;

import cn.hutool.core.util.HexUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.domain.vo.StandardDevice;
import com.wantllife.simulator.business.ChargeSessionManager;
import com.wantllife.simulator.client.TcpClient;
import com.wantllife.simulator.manager.SimTimerScheduler;
import com.wantllife.simulator.req.*;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;

import static com.wantllife.constant.CloudFastChargingConstants.*;

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
    /** 设备定时任务门面,收拢全部业务定时创建逻辑 */
    private DeviceTimerFacade timerFacade;
    /** 报文业务处理聚合类,收拢所有下行帧完整业务逻辑 */
    private MsgHandlerGroup msgHandlerGroup;

    /**
     * 无参构造，初始化定时调度与充电会话管理器
     *
     * @author KevenPotter
     * @date 2026-07-02 15:56:08
     */
    public SimDevMsgProcessor() {
        this.timerScheduler = new SimTimerScheduler();
        this.deviceStateHolder = new DeviceStateHolder();
        int maxMin = CloudChargeHolder.getGlobalConfig().getMaxChargeMinutes();
        this.chargeSessionManager = new ChargeSessionManager(timerScheduler, maxMin);
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
        this.timerFacade = new DeviceTimerFacade(
                this.timerScheduler,
                this.deviceStateHolder,
                this.deviceId,
                this.gunNo,
                this.tcpClient,
                this::sendMessage
        );
        this.msgHandlerGroup = new MsgHandlerGroup(
                this.deviceStateHolder,
                this.timerFacade,
                this.chargeSessionManager,
                this.deviceId,
                this.gunNo,
                this::sendMessage
        );
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
                    SAALoginReq loginReq = new SAALoginReq(data, rawHexMsg);
                    msgHandlerGroup.handleLogin(loginReq);
                    break;
                // 模拟器心跳包应答
                case SIM_UP_HEARTBEAT:
                    new SABHeartbeatReq(data, rawHexMsg);
                    msgHandlerGroup.handleHeartbeat();
                    break;
                // 模拟器计费模型验证请求应答
                case SIM_UP_BILLING_MODE_VALID:
                    msgHandlerGroup.handleBillingValid(new SACBillingModeValidReq(data, rawHexMsg));
                    break;
                // 模拟器计费模型请求应答
                case SIM_UP_BILLING_MODE:
                    msgHandlerGroup.handleBillingModel(new SADBillingModelReq(data, rawHexMsg));
                    break;
                // 模拟器读取实时监测数据
                case SIM_UP_REAL_TIME_MONITOR:
                    new SAERealTimeMonitorReq(data, rawHexMsg);
                    msgHandlerGroup.handleRealTimeQuery();
                    break;
                // 模拟器运营平台确认启动充电
                case SIM_UP_REQUEST_CHARGING:
                    new SANRequestChargingReq(data, rawHexMsg);
                    msgHandlerGroup.handleRequestCharge();
                    break;
                // 模拟器运营平台远程控制启机
                case SIM_UP_START_CHARGE:
                    msgHandlerGroup.handleStartCharge(new SAOStartChargeReq(data, rawHexMsg));
                    break;
                // 模拟器运营平台远程停机
                case SIM_UP_STOP_CHARGE:
                    msgHandlerGroup.handleStopCharge(new SAPStopChargeReq(data, rawHexMsg));
                    break;
                // 模拟器交易记录确认
                case SIM_UP_TRADE_RECORD:
                    new SAQTradeRecordReq(data, rawHexMsg, this.deviceId);
                    msgHandlerGroup.handleTradeRecordConfirm(this.deviceId);
                    break;
                // 模拟器远程账户余额更新
                case SIM_UP_BALANCE_UPDATE:
                    SARBalanceUpdateReq balanceReq = new SARBalanceUpdateReq(data, rawHexMsg);
                    msgHandlerGroup.handleBalanceUpdate(balanceReq);
                    break;
                // 模拟器离线卡数据同步
                case SIM_UP_OFFLINE_CARD_SYNC:
                    SASOfflineCardSyncReq syncReq = new SASOfflineCardSyncReq(data, rawHexMsg);
                    msgHandlerGroup.handleOfflineCardSync(syncReq);
                    break;
                // 模拟器离线卡数据清除
                case SIM_UP_OFFLINE_CARD_CLEAR:
                    SATOfflineCardClearReq clearReq = new SATOfflineCardClearReq(data, rawHexMsg);
                    msgHandlerGroup.handleOfflineCardClear(clearReq);
                    break;
                // 模拟器离线卡数据查询
                case SIM_UP_OFFLINE_CARD_QUERY:
                    SAUOfflineCardQueryReq queryReq = new SAUOfflineCardQueryReq(data, rawHexMsg);
                    msgHandlerGroup.handleOfflineCardQuery(queryReq);
                    break;
                // 模拟器充电桩工作参数设置
                case SIM_UP_WORKING_PARAMS:
                    SAVWorkingParamsReq paramReq = new SAVWorkingParamsReq(data, rawHexMsg);
                    msgHandlerGroup.handleWorkParamSet(paramReq);
                    break;
                // 模拟器对时设置
                case SIM_UP_TIME_SYNC:
                    SAWTimeSyncReq timeReq = new SAWTimeSyncReq(data, rawHexMsg);
                    msgHandlerGroup.handleTimeSync(timeReq);
                    break;
                // 模拟器计费模型设置
                case SIM_UP_BILLING_MODE_SET:
                    SAXBillingModeSetReq setReq = new SAXBillingModeSetReq(data, rawHexMsg);
                    msgHandlerGroup.handleBillingModeSet(setReq);
                    break;
                // 模拟器遥控地锁升锁与降锁
                case SIM_UP_LOCK_UP_DOWN:
                    SAZLockUpDownReq lockReq = new SAZLockUpDownReq(data, rawHexMsg);
                    msgHandlerGroup.handleLockControl(lockReq);
                    break;
                // 模拟器远程重启
                case SIM_UP_REBOOT:
                    SBARebootReq rebootReq = new SBARebootReq(data, rawHexMsg);
                    msgHandlerGroup.handleReboot(rebootReq, this.tcpClient);
                    break;
                // 模拟器远程更新
                case SIM_UP_UPGRADE:
                    SBBUpgradeReq upgradeReq = new SBBUpgradeReq(data, rawHexMsg);
                    msgHandlerGroup.handleUpgrade(upgradeReq, this.tcpClient);
                    break;
                // 模拟器运营平台确认并充启动充电
                case SIM_UP_APPLY_PARALLEL_CHARGING:
                    new SBCApplyParallelChargingReq(data, rawHexMsg);
                    msgHandlerGroup.handleParallelApplyCharge();
                    break;
                // 模拟器运营平台远程控制并充启机
                case SIM_UP_PARALLEL_START_CHARGE:
                    SBDParallelStartChargeReq parallelReq = new SBDParallelStartChargeReq(data, rawHexMsg);
                    msgHandlerGroup.handleParallelStartCharge(parallelReq);
                    break;

            }
        } catch (Exception e) {
            log.error("{} {} {} Message Process Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
        }
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
        timerFacade.startLoginTimer();
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
