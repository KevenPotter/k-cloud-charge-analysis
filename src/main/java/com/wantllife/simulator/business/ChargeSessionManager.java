package com.wantllife.simulator.business;

import com.wantllife.domain.vo.StandardRealTimeMonitor;
import com.wantllife.domain.vo.StandardTradeRecord;
import com.wantllife.enums.TimeSegment;
import com.wantllife.simulator.fake.FakeData;
import com.wantllife.simulator.manager.SimTimerScheduler;
import com.wantllife.simulator.res.SAERealTimeMonitorRes;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_PROJECT_NAME;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_TIP_ICON;
import static com.wantllife.constant.SimulatorConstants.TIMER_REAL_TIME_MONITOR_SECOND;

/**
 * 充电会话管理器
 * 管控单次充电全生命周期:启充、实时计量、分时计费、停机结算、电量分摊、生成结算单
 *
 * @author KevenPotter
 * @date 2026-07-01 11:30:20
 */
@Slf4j
public class ChargeSessionManager {

    /** 当前设备编号 */
    private String deviceId;
    /** 当前设备枪号 */
    private Integer gunNo;
    /** 当前充电交易流水号 */
    @Getter
    private String tradeNo;

    /** 总充电时间(固定120分钟,后续可改) */
    public static final int TOTAL_CHARGE_MINUTES = 120;
    /** 每分钟充电度数：0.5度/分钟 */
    private static final BigDecimal PER_MIN_ELE = new BigDecimal("0.5");
    /** 金额、电量计算保留小数位数：4位 */
    private static final int SCALE_4 = 4;
    /** 四舍五入模式 */
    private static final RoundingMode ROUND_HALF_UP = RoundingMode.HALF_UP;

    /** 是否正在充电中 */
    @Getter
    private boolean isCharging = false;
    /** 充电开始时间 */
    private LocalDateTime chargeStartTime;
    /** 充电结束时间 */
    private LocalDateTime chargeEndTime;
    /** 累计充电分钟数 */
    @Getter
    private int accumulatedMinutes = 0;
    /** 剩余充电分钟数 */
    @Getter
    private int remainingMinutes = TOTAL_CHARGE_MINUTES;
    /** 累计充电度数 */
    @Getter
    private BigDecimal chargingDegree = BigDecimal.ZERO;
    /** 已充金额 */
    @Getter
    private BigDecimal chargedAmount = BigDecimal.ZERO;

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
    /** 尖时段累计电量 */
    private BigDecimal sharpElectric = BigDecimal.ZERO;
    /** 峰时段累计电量 */
    private BigDecimal peakElectric = BigDecimal.ZERO;
    /** 平时段累计电量 */
    private BigDecimal flatElectric = BigDecimal.ZERO;
    /** 谷时段累计电量 */
    private BigDecimal valleyElectric = BigDecimal.ZERO;

    /** 定时调度器,用于启停实时上报定时任务 */
    private final SimTimerScheduler timerScheduler;

    /** 实时报文发送回调，交由上层处理器统一发送字节流 */
    public interface MonitorDataSendCallback {
        /**
         * 实时监测报文就绪，执行发送
         *
         * @param data 上行报文字节数组
         * @author KevenPotter
         * @date 2026-07-01 11:44:26
         */
        void sendMessage(byte[] data);
    }

    @Setter
    private MonitorDataSendCallback monitorDataSendCallback;

    /**
     * 构造注入定时调度器
     *
     * @param timerScheduler 定时任务管理器
     */
    public ChargeSessionManager(SimTimerScheduler timerScheduler) {
        this.timerScheduler = timerScheduler;
    }

    /**
     * 初始化设备标识信息
     *
     * @param deviceId 设备编号
     * @param gunNo    枪号
     * @author KevenPotter
     * @date 2026-07-01 11:44:18
     */
    public void initDeviceInfo(String deviceId, Integer gunNo) {
        this.deviceId = deviceId;
        this.gunNo = gunNo;
    }

    /**
     * 连接重连/设备复位,清空全部充电会话数据与电价配置
     *
     * @author KevenPotter
     * @date 2026-07-01 11:46:51
     */
    public void resetAllChargeData() {
        // 清空会话状态
        this.tradeNo = null;
        this.isCharging = false;
        this.chargeStartTime = null;
        this.chargeEndTime = null;
        this.accumulatedMinutes = 0;
        this.remainingMinutes = TOTAL_CHARGE_MINUTES;
        this.chargingDegree = BigDecimal.ZERO;
        this.chargedAmount = BigDecimal.ZERO;

        // 清空分时累计电量
        this.sharpElectric = BigDecimal.ZERO;
        this.peakElectric = BigDecimal.ZERO;
        this.flatElectric = BigDecimal.ZERO;
        this.valleyElectric = BigDecimal.ZERO;

        // 清空电价配置
        this.sharpEleFee = BigDecimal.ZERO;
        this.sharpServiceFee = BigDecimal.ZERO;
        this.sharpTimeRange = null;
        this.peakEleFee = BigDecimal.ZERO;
        this.peakServiceFee = BigDecimal.ZERO;
        this.peakTimeRange = null;
        this.flatEleFee = BigDecimal.ZERO;
        this.flatServiceFee = BigDecimal.ZERO;
        this.flatTimeRange = null;
        this.valleyEleFee = BigDecimal.ZERO;
        this.valleyServiceFee = BigDecimal.ZERO;
        this.valleyTimeRange = null;
        this.lossRatio = 0;

        // 停止实时上报定时
        timerScheduler.stopRealTimeMonitorTimer();
    }

    /**
     * 开始充电
     *
     * @param tradeNo 本次交易流水号
     * @author KevenPotter
     * @date 2026-07-01 11:47:30
     */
    public void startCharge(String tradeNo) {
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
     * @param tradeNo 本次交易流水号
     * @return 结算交易记录对象, 无充电则返回null
     * @author KevenPotter
     * @date 2026-07-02 1:44:36
     */
    public StandardTradeRecord stopCharge(String tradeNo) {
        if (!isCharging) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(chargeStartTime, now);
        int stopCode = 0x40;
        // 超时达到最大时长强制停机
        if (minutes >= TOTAL_CHARGE_MINUTES) {
            minutes = TOTAL_CHARGE_MINUTES;
            stopCode = 0x41;
            log.info("充电达到最大时长{}分钟,触发停机结算", TOTAL_CHARGE_MINUTES);
        }

        chargingDegree = BigDecimal.valueOf(minutes).multiply(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal totalMoney = BigDecimal.ZERO;
        LocalDateTime loopMin = chargeStartTime;
        for (long i = 0; i < minutes; i++) {
            totalMoney = totalMoney.add(getSingleMinuteCost(loopMin));
            loopMin = loopMin.plusMinutes(1);
        }
        chargedAmount = totalMoney;

        // 修改状态、结束会话
        this.isCharging = false;
        this.tradeNo = null;
        this.chargeEndTime = now;
        timerScheduler.stopRealTimeMonitorTimer();

        return buildTradeRecord(tradeNo, stopCode);
    }

    /**
     * 设置计费模型全套电价、时段、损耗配置
     *
     * @param sharpEleFee      尖电价
     * @param sharpServiceFee  尖服务费
     * @param sharpTimeRange   尖时段
     * @param peakEleFee       峰电价
     * @param peakServiceFee   峰服务费
     * @param peakTimeRange    峰时段
     * @param flatEleFee       平电价
     * @param flatServiceFee   平服务费
     * @param flatTimeRange    平时段
     * @param valleyEleFee     谷电价
     * @param valleyServiceFee 谷服务费
     * @param valleyTimeRange  谷时段
     * @param lossRatio        损耗比例
     * @author KevenPotter
     * @date 2026-07-02 15:45:38
     */
    public void setBillingModelData(
            BigDecimal sharpEleFee, BigDecimal sharpServiceFee, String sharpTimeRange,
            BigDecimal peakEleFee, BigDecimal peakServiceFee, String peakTimeRange,
            BigDecimal flatEleFee, BigDecimal flatServiceFee, String flatTimeRange,
            BigDecimal valleyEleFee, BigDecimal valleyServiceFee, String valleyTimeRange,
            Integer lossRatio) {
        this.sharpEleFee = sharpEleFee;
        this.sharpServiceFee = sharpServiceFee;
        this.sharpTimeRange = sharpTimeRange;

        this.peakEleFee = peakEleFee;
        this.peakServiceFee = peakServiceFee;
        this.peakTimeRange = peakTimeRange;

        this.flatEleFee = flatEleFee;
        this.flatServiceFee = flatServiceFee;
        this.flatTimeRange = flatTimeRange;

        this.valleyEleFee = valleyEleFee;
        this.valleyServiceFee = valleyServiceFee;
        this.valleyTimeRange = valleyTimeRange;

        this.lossRatio = lossRatio;
    }

    /**
     * 启动实时监测数据定时发送器
     *
     * @author KevenPotter
     * @date 2026-07-02 15:48:51
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
                    totalMoney = totalMoney.add(getSingleMinuteCost(loopMin));
                    loopMin = loopMin.plusMinutes(1);
                }
                chargedAmount = totalMoney;

                // 组装并上报实时报文
                StandardRealTimeMonitor monitor = FakeData.fakeChargingRealTimeMonitor(
                        tradeNo, deviceId, gunNo,
                        accumulatedMinutes, remainingMinutes,
                        chargingDegree, chargedAmount
                );
                byte[] resBytes = SAERealTimeMonitorRes.buildCommand(monitor);
                if (monitorDataSendCallback != null) {
                    monitorDataSendCallback.sendMessage(resBytes);
                }
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
     * @date 2026-07-02 15:51:11
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
     * 回溯整段充电起止时间.按每分钟0.5度电,动态匹配尖峰平谷区间累加分时用电量
     * <p>
     * 兜底逻辑:未收到0x0A计费模型、时段字符串为空时,全部电量归入平时段
     * 遍历充电每一分钟,逐分钟判定所属时段,累加对应时段用电量
     *
     * @author KevenPotter
     * @date 2026-07-02 15:52:44
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
     * @date 2026-07-02 15:53:27
     */
    private StandardTradeRecord buildTradeRecord(String tradeNo, Integer stopReasonCode) {
        // 1. 先分摊分时电量
        splitElectricByTimeSegment();

        BigDecimal ratioDivisor = new BigDecimal("100");
        BigDecimal lossRate = new BigDecimal(lossRatio).divide(ratioDivisor, 8, ROUND_HALF_UP);

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
        record.setStartTime(Timestamp.valueOf(chargeStartTime));
        record.setEndTime(Timestamp.valueOf(chargeEndTime));

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

}
