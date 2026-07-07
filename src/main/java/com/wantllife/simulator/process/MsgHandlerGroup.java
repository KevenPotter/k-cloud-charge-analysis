package com.wantllife.simulator.process;

import com.wantllife.domain.vo.StandardBillingModel;
import com.wantllife.domain.vo.StandardRealTimeMonitor;
import com.wantllife.domain.vo.StandardTradeRecord;
import com.wantllife.simulator.business.ChargeSessionManager;
import com.wantllife.simulator.req.*;
import com.wantllife.simulator.res.*;
import com.wantllife.simulator.state.DeviceState;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

import static com.wantllife.constant.SimulatorConstants.DELAY_REBOOT_SECOND;
import static com.wantllife.constant.SimulatorConstants.DELAY_UPGRADE_SECOND;
import static com.wantllife.simulator.fake.FakeData.fakeChargingRealTimeMonitor;
import static com.wantllife.simulator.fake.FakeData.fakeInitRealTimeMonitor;

/**
 * 下行报文统一处理聚合类
 * <p>
 * 收拢当前设备所有帧类型的业务解析、判断、上行应答逻辑
 *
 * @author KevenPotter
 * @date 2026-07-07 11:58:20
 */
@Slf4j
public class MsgHandlerGroup {

    /** 设备状态管理器,统一托管所有运行状态与标记 */
    private final DeviceStateHolder deviceStateHolder;
    /** 设备定时任务门面,收拢全部业务定时创建逻辑 */
    private final DeviceTimerFacade timerFacade;
    /** 充电会话管理器,接管全部充电启停、计费、结算业务 */
    private final ChargeSessionManager chargeSessionManager;
    /** 当前设备编号 */
    private final String deviceId;
    /** 当前设备枪号 */
    private final Integer gunNo;
    /** 报文发送回调,所有定时任务上行报文统一调用该出口 */
    private final Consumer<byte[]> sendMsgCallback;

    /**
     * 全参构造，一次性注入设备全部业务依赖上下文
     *
     * @param deviceStateHolder 设备状态容器
     * @param timerFacade       定时任务门面
     * @param chargeSessionMgr  充电业务管理器
     * @param deviceId          设备编号
     * @param gunNo             设备枪号
     * @param sendMsgCallback   报文发送回调
     * @author KevenPotter
     * @date 2026-07-07 20:15:20
     */
    public MsgHandlerGroup(
            DeviceStateHolder deviceStateHolder, DeviceTimerFacade timerFacade, ChargeSessionManager chargeSessionMgr,
            String deviceId, Integer gunNo,
            Consumer<byte[]> sendMsgCallback) {
        this.deviceStateHolder = deviceStateHolder;
        this.timerFacade = timerFacade;
        this.chargeSessionManager = chargeSessionMgr;
        this.deviceId = deviceId;
        this.gunNo = gunNo;
        this.sendMsgCallback = sendMsgCallback;
    }

    /**
     * 处理登录应答
     *
     * @author KevenPotter
     * @date 2026-05-29 11:02:18
     */
    public void handleLogin(SAALoginReq req) {
        if (!deviceStateHolder.isCurrentState(DeviceState.WAIT_LOGIN)) return;
        if (req.getLoginResult() != 0) return;

        deviceStateHolder.setLoginReq(req);
        timerFacade.timerScheduler.stopLoginTimer();

        // 进入计费验证状态
        deviceStateHolder.switchState(DeviceState.WAIT_BILLING_VALID);
        timerFacade.startBillingValidTimer();
    }

    /**
     * 处理计费验证应答
     *
     * @author KevenPotter
     * @date 2026-05-29 11:02:40
     */
    public void handleBillingValid(SACBillingModeValidReq req) {
        if (!deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_VALID)) return;

        // 保存平台下发的计费编码
        deviceStateHolder.setPlatformBillingModeId(req.getBillingModeId());

        // 验证一致 → 进入下一阶段
        if (req.getBillingModeValidResult() == 0) {
            timerFacade.timerScheduler.stopBillingValidTimer();
            deviceStateHolder.switchState(DeviceState.WAIT_BILLING_MODEL);
            timerFacade.startBillingModelTimer();
        }
    }

    /**
     * 处理计费模型应答
     *
     * @param req 计费模型请求应答
     * @author KevenPotter
     * @date 2026-05-29 11:03:43
     */
    public void handleBillingModel(SADBillingModelReq req) {
        if (!deviceStateHolder.isCurrentState(DeviceState.WAIT_BILLING_MODEL)) return;

        timerFacade.timerScheduler.stopBillingModelTimer();
        deviceStateHolder.switchState(DeviceState.READY);
        timerFacade.startHeartbeatTimer();

        // 1.先拿到当前应答对象
        List<StandardBillingModel> modelList = req.getBillingModelList();

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
                req.getSharpEleFee(), req.getSharpServiceFee(), sharpTime,
                req.getPeakEleFee(), req.getPeakServiceFee(), peakTime,
                req.getFlatEleFee(), req.getFlatServiceFee(), flatTime,
                req.getValleyEleFee(), req.getValleyServiceFee(), valleyTime,
                req.getLossRatio());
    }

    /**
     * 心跳下行帧处理
     * 首次就绪后推送一次初始化实时监测报文
     *
     * @author KevenPotter
     * @date 2026-07-07 13:20:25
     */
    public void handleHeartbeat() {
        if (!deviceStateHolder.isInitRealTimeSent() && deviceStateHolder.isCurrentState(DeviceState.READY)) {
            deviceStateHolder.markInitRealTimeSent(true);
            byte[] cmd = SAERealTimeMonitorRes.buildCommand(fakeInitRealTimeMonitor(deviceId, gunNo));
            sendMsgCallback.accept(cmd);
        }
    }

    /**
     * 实时监测查询下行帧处理
     * 组装当前充电/空载实时数据并回复上行
     *
     * @author KevenPotter
     * @date 2026-07-07 13:21:08
     */
    public void handleRealTimeQuery() {
        StandardRealTimeMonitor monitor;
        if (chargeSessionManager.isCharging()) {
            monitor = fakeChargingRealTimeMonitor(
                    chargeSessionManager.getTradeNo(),
                    deviceId,
                    gunNo,
                    chargeSessionManager.getAccumulatedMinutes(),
                    chargeSessionManager.getRemainingMinutes(),
                    chargeSessionManager.getChargingDegree(),
                    chargeSessionManager.getChargedAmount()
            );
        } else {
            monitor = fakeInitRealTimeMonitor(deviceId, gunNo);
        }
        byte[] cmd = SAERealTimeMonitorRes.buildCommand(monitor);
        sendMsgCallback.accept(cmd);
    }

    /**
     * 启动充电下行帧处理
     * 未充电则回复允许并启动充电会话，已充电返回占用
     *
     * @param req 启动充电请求实体
     * @author KevenPotter
     * @date 2026-07-07 13:21:47
     */
    public void handleStartCharge(SAOStartChargeReq req) {
        if (!chargeSessionManager.isCharging()) {
            sendMsgCallback.accept(SAOStartChargeRes.buildCommand(req, 1, 0));
            chargeSessionManager.startCharge(req.getTradeNo());
        } else {
            sendMsgCallback.accept(SAOStartChargeRes.buildCommand(req, 0, 2));
        }
    }

    /**
     * 停止充电下行帧处理
     * 终止充电会话，生成交易记录并上行推送
     *
     * @param req 停止充电请求实体
     * @author KevenPotter
     * @date 2026-07-07 13:22:15
     */
    public void handleStopCharge(SAPStopChargeReq req) {
        sendMsgCallback.accept(SAPStopChargeRes.buildCommand(req));
        String tradeNo = chargeSessionManager.getTradeNo();
        if (chargeSessionManager.isCharging()) {
            StandardTradeRecord record = chargeSessionManager.stopCharge(tradeNo);
            if (record != null) {
                sendMsgCallback.accept(SAQTradeRecordRes.buildCommand(record));
            }
        }
    }

    /**
     * 远程重启指令处理
     * 回复上行报文并触发客户端延迟重连
     *
     * @param req       重启请求实体
     * @param tcpClient 外部传入tcp客户端实例
     * @author KevenPotter
     * @date 2026-07-07 13:23:48
     */
    public void handleReboot(SBARebootReq req, com.wantllife.simulator.client.TcpClient tcpClient) {
        sendMsgCallback.accept(SBARebootRes.buildCommand(req));
        tcpClient.restart(DELAY_REBOOT_SECOND);
    }

    /**
     * 远程升级指令处理
     * 回复上行报文并触发客户端延迟重连
     *
     * @param req       升级请求实体
     * @param tcpClient 外部传入tcp客户端实例
     * @author KevenPotter
     * @date 2026-07-07 13:23:59
     */
    public void handleUpgrade(SBBUpgradeReq req, com.wantllife.simulator.client.TcpClient tcpClient) {
        sendMsgCallback.accept(SBBUpgradeRes.buildCommand(req));
        tcpClient.restart(DELAY_UPGRADE_SECOND);
    }

    /**
     * 并行充电启动指令处理
     * 回复上行并开启并行充电会话
     *
     * @param req 并行充电请求实体
     * @author KevenPotter
     * @date 2026-07-07 13:25:09
     */
    public void handleParallelStartCharge(SBDParallelStartChargeReq req) {
        sendMsgCallback.accept(SBDParallelStartChargeRes.buildCommand(req));
        chargeSessionManager.startCharge(req.getTradeNo());
    }

    /**
     * 确认申请充电下行帧处理
     * 当前版本无额外业务逻辑，仅预留帧接收入口
     *
     * @author KevenPotter
     * @date 2026-07-07 13:32:30
     */
    public void handleRequestCharge() {
    }

    /**
     * 交易记录确认下行帧处理
     * 仅接收设备编号，当前版本无额外业务逻辑，预留扩展入口
     *
     * @param devId 当前操作设备编号
     * @author KevenPotter
     * @date 2026-07-07 13:32:50
     */
    public void handleTradeRecordConfirm(String devId) {
    }

    /**
     * 账户余额更新下行帧处理
     * 组装余额更新上行应答并回复服务端
     *
     * @param req 余额更新下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:10:40
     */
    public void handleBalanceUpdate(SARBalanceUpdateReq req) {
        sendMsgCallback.accept(SARBalanceUpdateRes.buildCommand(req));
    }

    /**
     * 离线卡数据同步下行帧处理
     * 组装同步应答报文上行回复
     *
     * @param req 离线卡同步下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:11:00
     */
    public void handleOfflineCardSync(SASOfflineCardSyncReq req) {
        sendMsgCallback.accept(SASOfflineCardSyncRes.buildCommand(req));
    }

    /**
     * 离线卡数据清除下行帧处理
     * 组装清除操作应答报文上行回复
     *
     * @param req 离线卡清除下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:11:20
     */
    public void handleOfflineCardClear(SATOfflineCardClearReq req) {
        sendMsgCallback.accept(SATOfflineCardClearRes.buildCommand(req));
    }

    /**
     * 离线卡信息查询下行帧处理
     * 组装查询应答报文上行回复服务端
     *
     * @param req 离线卡查询下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:11:40
     */
    public void handleOfflineCardQuery(SAUOfflineCardQueryReq req) {
        sendMsgCallback.accept(SAUOfflineCardQueryRes.buildCommand(req));
    }

    /**
     * 充电桩工作参数配置下行帧处理
     * 配置完成后返回应答报文
     *
     * @param req 工作参数设置下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:12:00
     */
    public void handleWorkParamSet(SAVWorkingParamsReq req) {
        sendMsgCallback.accept(SAVWorkingParamsRes.buildCommand(req));
    }

    /**
     * 设备时间同步下行帧处理
     * 同步时间完成后回复上行确认报文
     *
     * @param req 时间同步下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:12:20
     */
    public void handleTimeSync(SAWTimeSyncReq req) {
        sendMsgCallback.accept(SAWTimeSyncRes.buildCommand(req));
    }

    /**
     * 计费规则参数设置下行帧处理
     * 参数写入完成后回复确认上行报文
     *
     * @param req 计费模型设置下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:12:40
     */
    public void handleBillingModeSet(SAXBillingModeSetReq req) {
        sendMsgCallback.accept(SAXBillingModeSetRes.buildCommand(req));
    }

    /**
     * 地锁升降控制下行帧处理
     * 执行锁控动作并回复操作结果报文
     *
     * @param req 地锁控制下行请求实体
     * @author KevenPotter
     * @date 2026-07-07 14:13:00
     */
    public void handleLockControl(SAZLockUpDownReq req) {
        sendMsgCallback.accept(SAZLockUpDownRes.buildCommand(req));
    }

    /**
     * 并行充电申请确认下行帧处理
     * 当前版本无业务逻辑，预留扩展入口
     *
     * @author KevenPotter
     * @date 2026-07-07 14:13:20
     */
    public void handleParallelApplyCharge() {
    }
}
