package com.wantllife.simulator.process;

import cn.hutool.core.util.HexUtil;
import com.wantllife.domain.vo.StandardBillingModel;
import com.wantllife.domain.vo.StandardDevice;
import com.wantllife.domain.vo.StandardRealTimeMonitor;
import com.wantllife.domain.vo.StandardTradeRecord;
import com.wantllife.enums.TimeSegment;
import com.wantllife.simulator.client.TcpClient;
import com.wantllife.simulator.manager.SimTimerScheduler;
import com.wantllife.simulator.req.*;
import com.wantllife.simulator.res.*;
import com.wantllife.simulator.state.DeviceState;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    /** 交易流水号 */
    private String tradeNo;
    /** 是否正在充电中 */
    private boolean isCharging = false;
    /** 充电开始时间 */
    private LocalDateTime chargeStartTime;
    /** 充电结束时间 */
    private LocalDateTime chargeEndTime;
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

    /** 实时监测数据[0x13指令]是否已经发送过初始化 */
    private boolean initRealTimeSent = false;

    /* 计费模型 */
    /** 尖 */
    private BigDecimal sharpEleFee = BigDecimal.ZERO;
    private BigDecimal sharpServiceFee = BigDecimal.ZERO;
    private String sharpTimeRange;
    /** 峰 */
    private BigDecimal peakEleFee = BigDecimal.ZERO;
    private BigDecimal peakServiceFee = BigDecimal.ZERO;
    private String peakTimeRange;
    /** 平 */
    private BigDecimal flatEleFee = BigDecimal.ZERO;
    private BigDecimal flatServiceFee = BigDecimal.ZERO;
    private String flatTimeRange;
    /** 谷 */
    private BigDecimal valleyEleFee = BigDecimal.ZERO;
    private BigDecimal valleyServiceFee = BigDecimal.ZERO;
    private String valleyTimeRange;
    /** 损耗比例 */
    private Integer lossRatio = 0;
    private BigDecimal sharpElectric = BigDecimal.ZERO;
    private BigDecimal peakElectric = BigDecimal.ZERO;
    private BigDecimal flatElectric = BigDecimal.ZERO;
    private BigDecimal valleyElectric = BigDecimal.ZERO;

    /** 金额、电量计算保留小数位数：4位 */
    private static final int SCALE_4 = 4;
    /** 四舍五入模式 */
    private static final RoundingMode ROUND_HALF_UP = RoundingMode.HALF_UP;
    /** 每分钟充电度数：0.5度/分钟 */
    private static final BigDecimal PER_MIN_ELE = new BigDecimal("0.5");
    /** 定时任务调度管理器，接管所有定时任务生命周期 */
    private final SimTimerScheduler timerScheduler = new SimTimerScheduler();

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
                    StandardRealTimeMonitor currentMonitor = isCharging
                            ? fakeChargingRealTimeMonitor(tradeNo, deviceId, gunNo, accumulatedMinutes, remainingMinutes, chargingDegree, chargedAmount)
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
                    if (!isCharging) {
                        sendMessage(SAOStartChargeRes.buildCommand(startChargeReq, 1, 0));
                        // 开始充电 → 启动实时数据上传
                        startCharging(startChargeReq.getTradeNo());
                    } else {
                        sendMessage(SAOStartChargeRes.buildCommand(startChargeReq, 0, 2));
                    }
                    break;
                // 模拟器运营平台远程停机
                case SIM_UP_STOP_CHARGE:
                    SAPStopChargeReq stopChargeReq = new SAPStopChargeReq(data, rawHexMsg);
                    sendMessage(SAPStopChargeRes.buildCommand(stopChargeReq));
                    String tmpChargeTradeNo = this.tradeNo;
                    if (isCharging) {
                        LocalDateTime now = LocalDateTime.now();
                        long minutes = ChronoUnit.MINUTES.between(chargeStartTime, now);
                        int stopCode = 0x40;
                        // 判断是否超时停机
                        if (minutes >= TOTAL_CHARGE_MINUTES) {
                            minutes = TOTAL_CHARGE_MINUTES;
                            stopCode = 0x41;
                            log.info("充电达到最大时长{}分钟,触发停机结算", TOTAL_CHARGE_MINUTES);
                        }
                        // 统一一次计算电量金额
                        chargingDegree = BigDecimal.valueOf(minutes).multiply(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                        BigDecimal totalMoney = BigDecimal.ZERO;
                        LocalDateTime loopMin = chargeStartTime;
                        for (long i = 0; i < minutes; i++) {
                            totalMoney = totalMoney.add(getSingleMinuteCost(loopMin));
                            loopMin = loopMin.plusMinutes(1);
                        }
                        chargedAmount = totalMoney;
                        stopCharging();
                        StandardTradeRecord tradeRecord = buildTradeRecord(tmpChargeTradeNo, stopCode);
                        sendMessage(SAQTradeRecordRes.buildCommand(tradeRecord));
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
                    // 开始充电 → 启动实时数据上传
                    startCharging(parallelStartChargeReq.getTradeNo());
                    break;

            }
        } catch (Exception e) {
            log.error("{} {} {} Message Process Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId);
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
        this.chargeEndTime = null;
        this.accumulatedMinutes = 0;
        this.remainingMinutes = TOTAL_CHARGE_MINUTES;
        this.chargingDegree = BigDecimal.ZERO;
        this.chargedAmount = BigDecimal.ZERO;

        this.sharpElectric = BigDecimal.ZERO;
        this.peakElectric = BigDecimal.ZERO;
        this.flatElectric = BigDecimal.ZERO;
        this.valleyElectric = BigDecimal.ZERO;

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
        this.chargeEndTime = LocalDateTime.now();
        timerScheduler.stopRealTimeMonitorTimer();
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
        timerScheduler.stopLoginTimer();

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
            timerScheduler.stopBillingValidTimer();
            currentState = DeviceState.WAIT_BILLING_MODEL;
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
        if (currentState != DeviceState.WAIT_BILLING_MODEL) return;

        timerScheduler.stopBillingModelTimer();
        currentState = DeviceState.READY;
        startHeartbeatTimer();

        // 1.先拿到当前应答对象
        List<StandardBillingModel> modelList = billingModelReq.getBillingModelList();

        // 2.直接取四段电价、服务费、损耗比例
        this.sharpEleFee = billingModelReq.getSharpEleFee();
        this.sharpServiceFee = billingModelReq.getSharpServiceFee();
        this.peakEleFee = billingModelReq.getPeakEleFee();
        this.peakServiceFee = billingModelReq.getPeakServiceFee();
        this.flatEleFee = billingModelReq.getFlatEleFee();
        this.flatServiceFee = billingModelReq.getFlatServiceFee();
        this.valleyEleFee = billingModelReq.getValleyEleFee();
        this.valleyServiceFee = billingModelReq.getValleyServiceFee();
        this.lossRatio = billingModelReq.getLossRatio();
        // 3.遍历列表匹配类型,赋值时段字符串 "HH:mm-HH:mm"
        for (StandardBillingModel item : modelList) {
            String timeStr = item.getStartTime() + "-" + item.getEndTime();
            switch (item.getTimeSlotType()) {
                case 1:
                    this.sharpTimeRange = timeStr;
                    break;
                case 2:
                    this.peakTimeRange = timeStr;
                    break;
                case 3:
                    this.flatTimeRange = timeStr;
                    break;
                case 4:
                    this.valleyTimeRange = timeStr;
                    break;
            }
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
        currentState = DeviceState.WAIT_LOGIN;
        loginReq = null;
        platformBillingModeId = null;
        initRealTimeSent = false;
        this.isCharging = false;
        this.tradeNo = null;
        this.accumulatedMinutes = 0;
        this.remainingMinutes = TOTAL_CHARGE_MINUTES;

        this.sharpEleFee = BigDecimal.ZERO;
        this.sharpServiceFee = BigDecimal.ZERO;
        this.peakEleFee = BigDecimal.ZERO;
        this.peakServiceFee = BigDecimal.ZERO;
        this.flatEleFee = BigDecimal.ZERO;
        this.flatServiceFee = BigDecimal.ZERO;
        this.valleyEleFee = BigDecimal.ZERO;
        this.valleyServiceFee = BigDecimal.ZERO;
        this.lossRatio = 0;
        this.sharpTimeRange = null;
        this.peakTimeRange = null;
        this.flatTimeRange = null;
        this.valleyTimeRange = null;
        this.sharpElectric = BigDecimal.ZERO;
        this.peakElectric = BigDecimal.ZERO;
        this.flatElectric = BigDecimal.ZERO;
        this.valleyElectric = BigDecimal.ZERO;
        this.chargeEndTime = null;

        // 关闭所有旧定时器
        timerScheduler.stopAllTimers();
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
                if (currentState == DeviceState.WAIT_LOGIN) {
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
                if (currentState == DeviceState.WAIT_BILLING_VALID) {
                    Long billingModeId = platformBillingModeId != null ? platformBillingModeId : 1L;
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
                if (currentState == DeviceState.WAIT_BILLING_MODEL) {
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
                if (currentState == DeviceState.READY) {
                    sendMessage(SABHeartbeatRes.buildCommand(loginReq, gunNo, 0));
                }
            } catch (Exception e) {
                log.error("{} {} {} StartHeartbeatTimer Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
            }
        };
        timerScheduler.startHeartbeatTimer(heartbeatTask, TIMER_HEARTBEAT_SECOND, TIMER_HEARTBEAT_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 启动实时监测数据定时发送器
     *
     * @author KevenPotter
     * @date 2026-06-03 10:37:11
     */
    private void startRealTimeMonitorTimer() {
        Runnable monitorTask = () -> {
            try {
                if (!isCharging) return;

                LocalDateTime now = LocalDateTime.now();
                // 真实已充电总分钟
                long minutes = ChronoUnit.MINUTES.between(chargeStartTime, now);

                // 越界保护,最多充120分钟
                if (minutes >= TOTAL_CHARGE_MINUTES) {
                    minutes = TOTAL_CHARGE_MINUTES;
                }

                accumulatedMinutes = (int) minutes;
                remainingMinutes = TOTAL_CHARGE_MINUTES - accumulatedMinutes;
                // 固定每分钟用电量0.5度(30kW直流桩典型值)
                chargingDegree = BigDecimal.valueOf(minutes).multiply(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);

                // 动态分时计算实时总金额
                BigDecimal totalMoney = BigDecimal.ZERO;
                // 逐分钟回溯计算每一分钟费用
                LocalDateTime loopMin = chargeStartTime;
                for (long i = 0; i < minutes; i++) {
                    BigDecimal minuteCost = getSingleMinuteCost(loopMin);
                    totalMoney = totalMoney.add(minuteCost);
                    loopMin = loopMin.plusMinutes(1);
                }
                chargedAmount = totalMoney;

                // 组装并上报实时报文
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

            } catch (Exception e) {
                log.error("{} {} {} StartRealTimeMonitorTimer Exception", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, e);
            }
        };
        timerScheduler.startRealTimeMonitorTimer(monitorTask, 0, TIMER_REAL_TIME_MONITOR_SECOND, TimeUnit.SECONDS);
    }

    /**
     * 计算指定时刻单分钟电费
     *
     * @param loopMin 当前分钟时刻
     * @return 该分钟电费金额
     * @author KevenPotter
     * @date 2026-06-23 10:20:30
     */
    private BigDecimal getSingleMinuteCost(LocalDateTime loopMin) {
        TimeSegment seg;
        if (sharpTimeRange != null && peakTimeRange != null && flatTimeRange != null && valleyTimeRange != null) {
            seg = TimeSegment.getTimeSegment(loopMin, sharpTimeRange, peakTimeRange, flatTimeRange, valleyTimeRange);
        } else {
            seg = TimeSegment.FLAT;
        }

        BigDecimal unitTotalPrice;
        switch (seg) {
            case SHARP:
                unitTotalPrice = sharpEleFee.add(sharpServiceFee);
                break;
            case PEAK:
                unitTotalPrice = peakEleFee.add(peakServiceFee);
                break;
            case VALLEY:
                unitTotalPrice = valleyEleFee.add(valleyServiceFee);
                break;
            default:
                unitTotalPrice = flatEleFee.add(flatServiceFee);
        }

        return PER_MIN_ELE.multiply(unitTotalPrice);
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
     * 回溯整段充电起止时间.按每分钟0.5度电,动态匹配尖峰平谷区间累加分时用电量
     * <p>
     * 兜底逻辑:未收到0x0A计费模型、时段字符串为空时,全部电量归入平时段
     * 遍历充电每一分钟,逐分钟判定所属时段,累加对应时段用电量
     *
     * @author KevenPotter
     * @date 2026-06-22 15:49:37
     */
    private void splitElectricByTimeSegment() {
        if (chargeStartTime == null || chargeEndTime == null) {
            return;
        }
        if (sharpTimeRange == null || peakTimeRange == null || flatTimeRange == null || valleyTimeRange == null) {
            long totalMin = ChronoUnit.MINUTES.between(chargeStartTime, chargeEndTime);
            this.flatElectric = BigDecimal.valueOf(totalMin).multiply(PER_MIN_ELE);
            return;
        }

        long totalMin = ChronoUnit.MINUTES.between(chargeStartTime, chargeEndTime);
        LocalDateTime currentMin = chargeStartTime;
        for (long i = 0; i < totalMin; i++) {
            TimeSegment seg = TimeSegment.getTimeSegment(currentMin, sharpTimeRange, peakTimeRange, flatTimeRange, valleyTimeRange);
            switch (seg) {
                case SHARP:
                    sharpElectric = sharpElectric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                    break;
                case PEAK:
                    peakElectric = peakElectric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                    break;
                case FLAT:
                    flatElectric = flatElectric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                    break;
                case VALLEY:
                    valleyElectric = valleyElectric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                    break;
            }
            currentMin = currentMin.plusMinutes(1);
        }
    }

    /**
     * 生成结算交易记录实体,核心结算逻辑
     * <p>
     * 1.调用分时电量分摊,统计尖/峰/平/谷四段用电量
     * 2.按公式计算各时段电费、线损损耗电量
     * 单段电费 = 时段用电量 × (时段电价 + 时段服务费)
     * 单段损耗 = 时段用电量 × (损耗百分比 / 100)
     * 3.汇总总电量、总损耗、总费用,组装StandardTradeRecord交易对象
     *
     * @param stopReasonCode 停机原因编码,对应GBT-27930停机原因定义
     * @return 完整结算交易记录, 用于0x3B交易记录上行报文
     * @author KevenPotter
     * @date 2026-06-22 15:51:32
     */
    private StandardTradeRecord buildTradeRecord(String tradeNo, Integer stopReasonCode) {
        // 1. 先分摊分时电量
        splitElectricByTimeSegment();

        BigDecimal ratioDivisor = new BigDecimal("100");
        BigDecimal lossRate = new BigDecimal(lossRatio).divide(ratioDivisor, 8, RoundingMode.HALF_UP);

        // 2. 计算各段金额、损耗电量（统一百分比换算）
        // 尖
        BigDecimal sharpLossEle = sharpElectric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal sharpAmt = sharpElectric.multiply(sharpEleFee.add(sharpServiceFee)).setScale(SCALE_4, ROUND_HALF_UP);
        // 峰
        BigDecimal peakLossEle = peakElectric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal peakAmt = peakElectric.multiply(peakEleFee.add(peakServiceFee)).setScale(SCALE_4, ROUND_HALF_UP);
        // 平
        BigDecimal flatLossEle = flatElectric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal flatAmt = flatElectric.multiply(flatEleFee.add(flatServiceFee)).setScale(SCALE_4, ROUND_HALF_UP);
        // 谷
        BigDecimal valleyLossEle = valleyElectric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal valleyAmt = valleyElectric.multiply(valleyEleFee.add(valleyServiceFee)).setScale(SCALE_4, ROUND_HALF_UP);

        // 汇总总电量、总损耗、总金额
        BigDecimal totalElectricity = sharpElectric.add(peakElectric).add(flatElectric).add(valleyElectric);
        BigDecimal totalLossEle = sharpLossEle.add(peakLossEle).add(flatLossEle).add(valleyLossEle);
        BigDecimal totalAmt = sharpAmt.add(peakAmt).add(flatAmt).add(valleyAmt);

        // 构造交易对象,填充固定模拟默认值
        StandardTradeRecord record = new StandardTradeRecord();
        record.setTradeNo(tradeNo);
        record.setDeviceId(this.deviceId);
        record.setGunNo(this.gunNo);
        record.setStartTime(java.sql.Timestamp.valueOf(chargeStartTime));
        record.setEndTime(java.sql.Timestamp.valueOf(chargeEndTime));

        record.setSharpUnitPrice(sharpEleFee);
        record.setSharpElectricity(sharpElectric);
        record.setSharpLossElectricity(sharpLossEle);
        record.setSharpAmount(sharpAmt);

        record.setPeakUnitPrice(peakEleFee);
        record.setPeakElectricity(peakElectric);
        record.setPeakLossElectricity(peakLossEle);
        record.setPeakAmount(peakAmt);

        record.setFlatUnitPrice(flatEleFee);
        record.setFlatElectricity(flatElectric);
        record.setFlatLossElectricity(flatLossEle);
        record.setFlatAmount(flatAmt);

        record.setValleyUnitPrice(valleyEleFee);
        record.setValleyElectricity(valleyElectric);
        record.setValleyLossElectricity(valleyLossEle);
        record.setValleyAmount(valleyAmt);

        record.setElectricityStart(BigDecimal.ZERO);
        record.setElectricityEnd(totalElectricity);
        record.setTotalElectricity(totalElectricity);
        record.setTotalLossElectricity(totalLossEle);
        record.setTotalAmount(totalAmt);

        record.setVinCode("");
        record.setTradeIdentifier(1);
        record.setTradeTime(java.sql.Timestamp.valueOf(chargeEndTime));
        record.setStopReason(stopReasonCode);
        record.setPhysicalCardNo("0000000000000000");

        return record;
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
