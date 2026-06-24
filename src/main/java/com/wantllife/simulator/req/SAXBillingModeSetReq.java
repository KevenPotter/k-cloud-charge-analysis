package com.wantllife.simulator.req;

import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardBillingModel;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 计费模型设置 [0X58]
 *
 * @author KevenPotter
 * @date 2026-06-05 09:57:34
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAXBillingModeSetReq extends FrameHeader {

    /** 尖 */
    private static final int SHARP = 0;
    /** 峰 */
    private static final int PEAK = 1;
    /** 平 */
    private static final int FLAT = 2;
    /** 谷 */
    private static final int VALLEY = 3;

    /** 设备编号 */
    private String deviceId;
    /** 计费模型编码 */
    private Long billingModeId;
    /** 尖电费费率 */
    private BigDecimal sharpEleFee;
    /** 尖服务费费率 */
    private BigDecimal sharpServiceFee;
    /** 峰费电费费率 */
    private BigDecimal peakEleFee;
    /** 峰服务费费率 */
    private BigDecimal peakServiceFee;
    /** 平电费费率 */
    private BigDecimal flatEleFee;
    /** 平服务费费率 */
    private BigDecimal flatServiceFee;
    /** 谷电费费率 */
    private BigDecimal valleyEleFee;
    /** 谷服务费费率 */
    private BigDecimal valleyServiceFee;
    /** 计损比例 */
    private Integer lossRatio;
    /** 时段费率 */
    private byte[] timeSlotRates = new byte[48];
    /** 计费模型列表 */
    List<StandardBillingModel> billingModelList = new ArrayList<>();

    /* 有参构造 */
    public SAXBillingModeSetReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        if (CloudChargeHolder.isSimulatorLogOutput()) log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-06-05 09:57:55
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 计费模型编号 [2字节] [BCD]
        String billingModeIdStr = StringUtil.bcd2String(data, index, 2);
        this.setBillingModeId(Long.parseLong(billingModeIdStr));
        index += 2;
        // 尖电费费率 [4字节] [BIN]
        this.setSharpEleFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 尖服务费费率 [4字节] [BIN]
        this.setSharpServiceFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 峰电费费率 [4字节] [BIN]
        this.setPeakEleFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 峰服务费费率 [4字节] [BIN]
        this.setPeakServiceFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 平电费费率 [4字节] [BIN]
        this.setFlatEleFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 平服务费费率 [4字节] [BIN]
        this.setFlatServiceFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 谷电费费率 [4字节] [BIN]
        this.setValleyEleFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 谷服务费费率 [4字节] [BIN]
        this.setValleyServiceFee(bin4ToBigDecimal(data, index));
        index += 4;
        // 计损比例 [1字节] [BIN]
        this.setLossRatio(data[index] & 0xFF);
        index += 1;
        // 时段费率 [48字节] [BIN]
        System.arraycopy(data, index, this.timeSlotRates, 0, 48);
        index += 48;
        parseDynamicTimeSlots();
    }

    /**
     * 将4字节BIN码(低位在前)转换为BigDecimal类型的费率
     * 精确到5位小数,符合云快充协议规范
     *
     * @param data  原始数据
     * @param index 数据角标
     * @return BigDecimal类型的费率
     * @author KevenPotter
     * @date 2026-06-05 10:01:14
     */
    private BigDecimal bin4ToBigDecimal(byte[] data, int index) {
        long value = ((long) data[index] & 0xFF)
                | ((long) data[index + 1] & 0xFF) << 8
                | ((long) data[index + 2] & 0xFF) << 16
                | ((long) data[index + 3] & 0xFF) << 24;
        return BigDecimal.valueOf(value, 5).setScale(5, RoundingMode.HALF_UP);
    }

    /**
     * 动态拆解48个时段费率
     * 根据48个时隙自动解析尖峰平谷，并合并跨天时段
     *
     * @author KevenPotter
     * @date 2026-06-05 10:00:55
     */
    private void parseDynamicTimeSlots() {
        // 1.拆分所有连续段
        List<Segment> segments = new ArrayList<>();
        Byte currentType = null;
        int start = 0;
        for (int i = 0; i < 48; i++) {
            byte t = timeSlotRates[i];
            if (currentType == null) {
                currentType = t;
                start = i;
            } else if (currentType != t) {
                segments.add(new Segment(currentType, start, i - 1));
                currentType = t;
                start = i;
            }
        }
        if (currentType != null) {
            segments.add(new Segment(currentType, start, 47));
        }

        // 2.按类型分组
        Map<Integer, List<Segment>> group = new HashMap<>();
        for (Segment s : segments) {
            group.computeIfAbsent(s.type, k -> new ArrayList<>()).add(s);
        }

        // 3.合并跨天：首尾同类型 → 合并
        List<StandardBillingModel> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Segment>> entry : group.entrySet()) {
            int type = entry.getKey();
            List<Segment> list = entry.getValue();
            // 判断是否跨天：首段从0开始，尾段到47结束
            boolean cross = list.size() >= 2
                    && list.get(0).start == 0
                    && list.get(list.size() - 1).end == 47;
            if (cross) {
                // 合并成：尾段start → 首段end+1
                Segment first = list.get(0);
                Segment last = list.get(list.size() - 1);
                String startTime = slotToTime(last.start);
                String endTime = slotToTime(first.end + 1);
                result.add(buildModel(type, startTime, endTime));
            } else {
                // 普通：只取第一段（同一类型只会一段）
                Segment s = list.get(0);
                String startTime = slotToTime(s.start);
                String endTime = slotToTime(s.end + 1);
                result.add(buildModel(type, startTime, endTime));
            }
        }

        // 4.保证4类都存在（缺的补空）
        ensureType(result, SHARP);
        ensureType(result, PEAK);
        ensureType(result, FLAT);
        ensureType(result, VALLEY);

        // 5.赋值给最终列表
        billingModelList = result.stream()
                .sorted(Comparator.comparingInt(StandardBillingModel::getTimeSlotType))
                .collect(Collectors.toList());
    }

    /**
     * 补全缺失类型
     * 确保尖峰平谷四种类型始终存在，避免解析缺失报错
     *
     * @param list 解析后的时段列表
     * @param type 时段类型 0尖1峰2平3谷
     * @author KevenPotter
     * @date 2026-06-05 10:00:58
     */
    private void ensureType(List<StandardBillingModel> list, int type) {
        boolean exist = list.stream().anyMatch(m -> m.getTimeSlotType() == type + 1);
        if (!exist) {
            list.add(buildModel(type, "00:00", "00:00"));
        }
    }

    /**
     * 构建单个计费时段模型
     *
     * @param type      时段类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 标准计费模型
     * @author KevenPotter
     * @date 2026-06-05 10:00:26
     */
    private StandardBillingModel buildModel(int type, String startTime, String endTime) {
        StandardBillingModel m = new StandardBillingModel();
        m.setStrategyId(billingModeId);
        m.setTimeSlotType(type + 1);
        switch (type) {
            case SHARP:
                m.setTimeSlotName("尖");
                m.setElectricityFee(sharpEleFee);
                m.setServiceFee(sharpServiceFee);
                break;
            case PEAK:
                m.setTimeSlotName("峰");
                m.setElectricityFee(peakEleFee);
                m.setServiceFee(peakServiceFee);
                break;
            case FLAT:
                m.setTimeSlotName("平");
                m.setElectricityFee(flatEleFee);
                m.setServiceFee(flatServiceFee);
                break;
            case VALLEY:
                m.setTimeSlotName("谷");
                m.setElectricityFee(valleyEleFee);
                m.setServiceFee(valleyServiceFee);
                break;
            default:
                m.setTimeSlotName("未知");
        }
        m.setStartTime(startTime);
        m.setEndTime(endTime);
        return m;
    }

    /**
     * 时段序号转换为时间字符串
     * 0 → 00:00
     * 1 → 00:30
     * ...
     * 47 → 23:30
     *
     * @param slot 时段序号 0-47
     * @return HH:mm 格式时间
     * @author KevenPotter
     * @date 2026-06-05 09:59:36
     */
    private String slotToTime(int slot) {
        if (slot >= 48) slot = 0;
        int hour = slot / 2;
        int minute = (slot % 2) * 30;
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-05 09:58:11
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇑ 【0x58】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 计费模型设置  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 计费模型设置  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 计费模型设置  计费编码    billingModeId                : %s\n", devLabel, billingModeId));
        sb.append(String.format("👩‍🚀%s 计费模型设置  尖电费率    sharpEleFee                  : %s\n", devLabel, sharpEleFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  尖服费率    sharpServiceFee              : %s\n", devLabel, sharpServiceFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  峰电费率    peakEleFee                   : %s\n", devLabel, peakEleFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  峰服费率    peakServiceFee               : %s\n", devLabel, peakServiceFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  平电费率    flatEleFee                   : %s\n", devLabel, flatEleFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  平服费率    flatServiceFee               : %s\n", devLabel, flatServiceFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  谷电费率    valleyEleFee                 : %s\n", devLabel, valleyEleFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  谷服费率    valleyServiceFee             : %s\n", devLabel, valleyServiceFee));
        sb.append(String.format("👩‍🚀%s 计费模型设置  计损比例    lossRatio                    : %s\n", devLabel, lossRatio));

        for (StandardBillingModel mode : billingModelList) {
            switch (mode.getTimeSlotType()) {
                case 1:
                    sb.append(String.format("👩‍🚀%s 计费模型设置  尖时间段    sharpTime                    : %s-%s\n", devLabel, mode.getStartTime(), mode.getEndTime()));
                    break;
                case 2:
                    sb.append(String.format("👩‍🚀%s 计费模型设置  峰时间段    peakTime                     : %s-%s\n", devLabel, mode.getStartTime(), mode.getEndTime()));
                    break;
                case 3:
                    sb.append(String.format("👩‍🚀%s 计费模型设置  平时间段    flatTime                     : %s-%s\n", devLabel, mode.getStartTime(), mode.getEndTime()));
                    break;
                case 4:
                    sb.append(String.format("👩‍🚀%s 计费模型设置  谷时间段    valleyTime                   : %s-%s\n", devLabel, mode.getStartTime(), mode.getEndTime()));
                    break;
            }
        }
        log.info(sb.toString());
    }

    /**
     * 内部时段片段实体
     * 用于临时存储连续时段的类型、起始、结束序号
     *
     * @author KevenPotter
     * @date 2026-06-05 10:02:50
     */
    private static class Segment {
        /*时段类型 0.尖 1.峰 2.平 3.谷*/
        int type;
        /*起始时隙序号0-47*/
        int start;
        /*结束时隙序号0-47*/
        int end;

        Segment(int type, int start, int end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }
    }
}
