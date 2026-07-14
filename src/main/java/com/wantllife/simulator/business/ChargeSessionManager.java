package com.wantllife.simulator.business;

import com.wantllife.domain.vo.StandardBillingModel;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /** 单次最大充电分钟,由外部配置传入 */
    private final int maxChargeMinutes;
    /** 每分钟充电度数：0.5度/分钟 */
    private static final BigDecimal PER_MIN_ELE = new BigDecimal("0.5");
    /** 金额、电量计算保留小数位数：4位 */
    private static final int SCALE_4 = 4;
    /** 四舍五入模式 */
    private static final RoundingMode ROUND_HALF_UP = RoundingMode.HALF_UP;
    /** 缓存每一分钟对应的分时类型 key=充电分钟起始时刻 */
    @SuppressWarnings("FieldMayBeFinal")
    private Map<LocalDateTime, TimeSegment> minuteSegCache;
    /** 缓存每一分钟对应的电费 key=充电分钟起始时刻 */
    @SuppressWarnings("FieldMayBeFinal")
    private Map<LocalDateTime, BigDecimal> minuteCostCache;

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
    private int remainingMinutes;
    /** 累计充电度数 */
    @Getter
    private BigDecimal chargingDegree = BigDecimal.ZERO;
    /** 已充金额 */
    @Getter
    private BigDecimal chargedAmount = BigDecimal.ZERO;

    /** 损耗比例 */
    private Integer lossRatio = 0;
    /** 定时调度器,用于启停实时上报定时任务 */
    private final SimTimerScheduler timerScheduler;
    /** 统一管理4类分时配置 */
    @SuppressWarnings("FieldMayBeFinal")
    private TimeSegmentRate[] rateArr = new TimeSegmentRate[4];

    /**
     * 分时费率封装实体，支持多段不连续时段
     * 数组下标映射：0=尖 SHARP、1=峰 PEAK、2=平 FLAT、3=谷 VALLEY
     */
    private static class TimeSegmentRate {
        // 电价
        BigDecimal eleFee = BigDecimal.ZERO;
        // 服务费
        BigDecimal serviceFee = BigDecimal.ZERO;
        // 多段时段集合 格式["16:00-17:00","22:00-23:00"]
        List<String> timeRanges = new ArrayList<>();
        // 本次充电该类型累计电量
        BigDecimal electric = BigDecimal.ZERO;
    }

    /** 实时报文发送回调,交由上层处理器统一发送字节流 */
    public interface MonitorDataSendCallback {
        /**
         * 实时监测报文就绪,执行发送
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
     * @param timerScheduler   定时任务管理器
     * @param maxChargeMinutes 单次最大充电分钟数
     */
    public ChargeSessionManager(SimTimerScheduler timerScheduler, int maxChargeMinutes) {
        this.timerScheduler = timerScheduler;
        this.maxChargeMinutes = maxChargeMinutes;
        this.minuteSegCache = new HashMap<>();
        this.minuteCostCache = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            rateArr[i] = new TimeSegmentRate();
        }
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
        this.remainingMinutes = maxChargeMinutes;
        this.chargingDegree = BigDecimal.ZERO;
        this.chargedAmount = BigDecimal.ZERO;
        this.lossRatio = 0;
        // 批量重置所有分时电价、时段、累计电量
        for (TimeSegmentRate rate : rateArr) {
            rate.eleFee = BigDecimal.ZERO;
            rate.serviceFee = BigDecimal.ZERO;
            rate.timeRanges.clear();
            rate.electric = BigDecimal.ZERO;
        }
        minuteSegCache.clear();
        minuteCostCache.clear();
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
        this.remainingMinutes = maxChargeMinutes;
        this.chargingDegree = BigDecimal.ZERO;
        this.chargedAmount = BigDecimal.ZERO;

        // 重置本次充电分时累计电量
        for (TimeSegmentRate rate : rateArr) {
            rate.electric = BigDecimal.ZERO;
        }

        this.minuteSegCache.clear();
        this.minuteCostCache.clear();

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
        if (minutes >= maxChargeMinutes) {
            minutes = maxChargeMinutes;
            stopCode = 0x41;
            log.info("充电达到最大时长{}分钟,触发停机结算", maxChargeMinutes);
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
     * 唯一标准设置计费模型方法,支持多段不连续分时
     *
     * @param billingModelList SADBillingModelReq/SAXBillingModeSetReq解析后的全部分时列表
     * @param lossRatio        损耗百分比
     * @author KevenPotter
     * @date 2026-07-13 15:14:05
     */
    public void setBillingModelData(List<StandardBillingModel> billingModelList, Integer lossRatio) {
        if (billingModelList == null) {
            throw new IllegalArgumentException("计费模型列表不能为空");
        }
        // 清空旧时段数据，防止脏数据残留
        for (TimeSegmentRate rate : rateArr) {
            rate.timeRanges.clear();
        }
        this.lossRatio = lossRatio;

        // 按类型归类，填充电价与多段时段
        for (StandardBillingModel model : billingModelList) {
            int typeCode = model.getTimeSlotType();
            int idx;
            switch (typeCode) {
                // 尖
                case 1:
                    idx = 0;
                    break;
                // 峰
                case 2:
                    idx = 1;
                    break;
                // 平
                case 3:
                    idx = 2;
                    break;
                // 谷
                case 4:
                    idx = 3;
                    break;
                default:
                    throw new IllegalArgumentException("非法分时类型:" + typeCode);
            }
            TimeSegmentRate target = rateArr[idx];
            // 同类型多段电价统一,仅第一次赋值
            if (target.eleFee.compareTo(BigDecimal.ZERO) == 0) {
                target.eleFee = model.getElectricityFee();
                target.serviceFee = model.getServiceFee();
            }
            // 追加当前分段
            target.timeRanges.add(model.getStartTime() + "-" + model.getEndTime());
        }
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
                if (minutes >= maxChargeMinutes) {
                    minutes = maxChargeMinutes;
                }

                accumulatedMinutes = (int) minutes;
                remainingMinutes = maxChargeMinutes - accumulatedMinutes;
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
                log.error("{} {} {} StartRealTimeMonitorTimer Exception. DeviceId: {}, TradeNo: {}, LossRatio: {}", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, deviceId, tradeNo, lossRatio, e);
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
        // 1.费用缓存命中直接返回,跳过全部计算逻辑
        if (minuteCostCache.containsKey(loopMin)) {
            return minuteCostCache.get(loopMin);
        }

        TimeSegment seg;
        // 2.分时缓存命中,直接复用已判定时段
        if (minuteSegCache.containsKey(loopMin)) {
            seg = minuteSegCache.get(loopMin);
        } else {
            // 3.无缓存：执行一次分时判定并存入缓存
            List<String> sharpRanges = rateArr[0].timeRanges;
            List<String> peakRanges = rateArr[1].timeRanges;
            List<String> flatRanges = rateArr[2].timeRanges;
            List<String> valleyRanges = rateArr[3].timeRanges;
            boolean hasConfig = !sharpRanges.isEmpty() || !peakRanges.isEmpty()
                    || !flatRanges.isEmpty() || !valleyRanges.isEmpty();
            if (hasConfig) {
                seg = TimeSegment.getTimeSegment(loopMin, sharpRanges, peakRanges, flatRanges, valleyRanges);
            } else {
                seg = TimeSegment.FLAT;
            }
            minuteSegCache.put(loopMin, seg);
        }

        // 4.根据分时获取单价,计算单分钟电费
        BigDecimal unitTotalPrice;
        switch (seg) {
            case SHARP:
                unitTotalPrice = rateArr[0].eleFee.add(rateArr[0].serviceFee);
                break;
            case PEAK:
                unitTotalPrice = rateArr[1].eleFee.add(rateArr[1].serviceFee);
                break;
            case VALLEY:
                unitTotalPrice = rateArr[3].eleFee.add(rateArr[3].serviceFee);
                break;
            default:
                unitTotalPrice = rateArr[2].eleFee.add(rateArr[2].serviceFee);
        }
        BigDecimal singleMinCost = PER_MIN_ELE.multiply(unitTotalPrice).setScale(SCALE_4, ROUND_HALF_UP);
        // 存入费用缓存,下次直接读取
        minuteCostCache.put(loopMin, singleMinCost);
        return singleMinCost;
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
        List<String> sharpRanges = rateArr[0].timeRanges;
        List<String> peakRanges = rateArr[1].timeRanges;
        List<String> flatRanges = rateArr[2].timeRanges;
        List<String> valleyRanges = rateArr[3].timeRanges;
        boolean hasConfig = !sharpRanges.isEmpty() || !peakRanges.isEmpty()
                || !flatRanges.isEmpty() || !valleyRanges.isEmpty();
        if (!hasConfig) {
            long totalMin = ChronoUnit.MINUTES.between(chargeStartTime, chargeEndTime);
            rateArr[2].electric = BigDecimal.valueOf(totalMin).multiply(PER_MIN_ELE);
            return;
        }

        long totalMin = ChronoUnit.MINUTES.between(chargeStartTime, chargeEndTime);
        LocalDateTime currentMin = chargeStartTime;
        for (long i = 0; i < totalMin; i++) {
            // 复用已缓存分时数据,消除重复区间解析
            TimeSegment seg;
            if (minuteSegCache.containsKey(currentMin)) {
                seg = minuteSegCache.get(currentMin);
            } else {
                seg = TimeSegment.getTimeSegment(currentMin, sharpRanges, peakRanges, flatRanges, valleyRanges);
                minuteSegCache.put(currentMin, seg);
            }
            switch (seg) {
                case SHARP:
                    rateArr[0].electric = rateArr[0].electric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                    break;
                case PEAK:
                    rateArr[1].electric = rateArr[1].electric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                    break;
                case FLAT:
                    rateArr[2].electric = rateArr[2].electric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
                    break;
                case VALLEY:
                    rateArr[3].electric = rateArr[3].electric.add(PER_MIN_ELE).setScale(SCALE_4, ROUND_HALF_UP);
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
        TimeSegmentRate sharpRate = rateArr[0];
        TimeSegmentRate peakRate = rateArr[1];
        TimeSegmentRate flatRate = rateArr[2];
        TimeSegmentRate valleyRate = rateArr[3];
        BigDecimal lossRate = new BigDecimal(lossRatio).divide(ratioDivisor, 8, ROUND_HALF_UP);

        // 2. 计算各段金额、损耗电量（统一百分比换算）
        // 尖
        BigDecimal sharpLossEle = sharpRate.electric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal sharpAmt = sharpRate.electric.multiply(sharpRate.eleFee.add(sharpRate.serviceFee)).setScale(SCALE_4, ROUND_HALF_UP);
        // 峰
        BigDecimal peakLossEle = peakRate.electric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal peakAmt = peakRate.electric.multiply(peakRate.eleFee.add(peakRate.serviceFee)).setScale(SCALE_4, ROUND_HALF_UP);
        // 平
        BigDecimal flatLossEle = flatRate.electric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal flatAmt = flatRate.electric.multiply(flatRate.eleFee.add(flatRate.serviceFee)).setScale(SCALE_4, ROUND_HALF_UP);
        // 谷
        BigDecimal valleyLossEle = valleyRate.electric.multiply(lossRate).setScale(SCALE_4, ROUND_HALF_UP);
        BigDecimal valleyAmt = valleyRate.electric.multiply(valleyRate.eleFee.add(valleyRate.serviceFee)).setScale(SCALE_4, ROUND_HALF_UP);

        // 汇总总电量、总损耗、总金额
        BigDecimal totalElectricity = sharpRate.electric.add(peakRate.electric).add(flatRate.electric).add(valleyRate.electric);
        BigDecimal totalLossEle = sharpLossEle.add(peakLossEle).add(flatLossEle).add(valleyLossEle);
        BigDecimal totalAmt = sharpAmt.add(peakAmt).add(flatAmt).add(valleyAmt);

        // 构造交易对象,填充固定模拟默认值
        StandardTradeRecord record = new StandardTradeRecord();
        record.setTradeNo(tradeNo);
        record.setDeviceId(this.deviceId);
        record.setGunNo(this.gunNo);
        record.setStartTime(Timestamp.valueOf(chargeStartTime));
        record.setEndTime(Timestamp.valueOf(chargeEndTime));

        record.setSharpUnitPrice(sharpRate.eleFee);
        record.setSharpElectricity(sharpRate.electric);
        record.setSharpLossElectricity(sharpLossEle);
        record.setSharpAmount(sharpAmt);

        record.setPeakUnitPrice(peakRate.eleFee);
        record.setPeakElectricity(peakRate.electric);
        record.setPeakLossElectricity(peakLossEle);
        record.setPeakAmount(peakAmt);

        record.setFlatUnitPrice(flatRate.eleFee);
        record.setFlatElectricity(flatRate.electric);
        record.setFlatLossElectricity(flatLossEle);
        record.setFlatAmount(flatAmt);

        record.setValleyUnitPrice(valleyRate.eleFee);
        record.setValleyElectricity(valleyRate.electric);
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
