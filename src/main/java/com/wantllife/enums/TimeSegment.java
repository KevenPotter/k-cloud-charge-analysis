package com.wantllife.enums;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 时段类型枚举
 *
 * @author KevenPotter
 * @date 2026-06-22 15:26:20
 */
public enum TimeSegment {

    /** 尖时段 */
    SHARP,
    /** 峰时段 */
    PEAK,
    /** 平时段 */
    FLAT,
    /** 谷时段 */
    VALLEY;

    /** 时间格式化器,统一解析HH:mm格式时段字符串 */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 多段分时统一判定入口
     * 根据传入的多段尖/峰/平/谷区间列表,匹配目标时间所属时段类型
     * 判定优先级:谷 > 平 > 峰 > 尖;无任何区间匹配时默认返回【平时段FLAT】
     * 支持单类型多段不连续区间、跨零点区间（如22:00-07:00）
     *
     * @param time         待判定带日期的完整时间点
     * @param sharpRanges  尖时段区间集合，单条格式 HH:mm-HH:mm，允许多条不连续分段
     * @param peakRanges   峰时段区间集合，单条格式 HH:mm-HH:mm，允许多条不连续分段
     * @param flatRanges   平时段区间集合，单条格式 HH:mm-HH:mm，允许多条不连续分段
     * @param valleyRanges 谷时段区间集合，单条格式 HH:mm-HH:mm，允许多条不连续分段
     * @return TimeSegment 匹配成功返回对应时段枚举；无匹配返回FLAT
     * @author KevenPotter
     * @date 2026-07-13 15:24:32
     */
    public static TimeSegment getTimeSegment(LocalDateTime time, List<String> sharpRanges, List<String> peakRanges, List<String> flatRanges, List<String> valleyRanges) {
        // 判定顺序可按需调整
        for (String range : Optional.ofNullable(valleyRanges).orElseGet(ArrayList::new)) {
            if (isInTimeRange(time, range)) return VALLEY;
        }
        for (String range : Optional.ofNullable(flatRanges).orElseGet(ArrayList::new)) {
            if (isInTimeRange(time, range)) return FLAT;
        }
        for (String range : Optional.ofNullable(peakRanges).orElseGet(ArrayList::new)) {
            if (isInTimeRange(time, range)) return PEAK;
        }
        for (String range : Optional.ofNullable(sharpRanges).orElseGet(ArrayList::new)) {
            if (isInTimeRange(time, range)) return SHARP;
        }
        return FLAT;
    }

    /**
     * 判断带日期的时间点是否落在单条时分区间内(左闭右开)
     * 支持跨零点区间(起始分钟 > 结束分钟,例23:00-05:00)
     * 转换时分至当日总分钟数值对比,避免频繁创建LocalTime提升性能
     *
     * @param targetTime 待判断完整时间（含日期）
     * @param timeRange  时分区间字符串，固定格式 HH:mm-HH:mm
     * @return true=时间在区间内；false=不在区间
     * @author KevenPotter
     * @date 2026-07-13 15:24:55
     */
    private static boolean isInTimeRange(LocalDateTime targetTime, String timeRange) {
        if (timeRange == null || !timeRange.contains("-")) {
            return false;
        }
        String[] split = timeRange.split("-");
        String startStr = split[0];
        String endStr = split[1];

        int startMin = parseTimeToMin(startStr);
        int endMin = parseTimeToMin(endStr);
        if (startMin == -1 || endMin == -1) {
            return false;
        }
        int targetMin = targetTime.getHour() * 60 + targetTime.getMinute();

        if (startMin > endMin) {
            // 跨天区间
            return targetMin >= startMin || targetMin < endMin;
        } else {
            return targetMin >= startMin && targetMin < endMin;
        }
    }

    /**
     * 将HH:mm格式时间字符串转换为当日总分钟数
     * 示例:00:00→0、07:30→450、23:59→1439
     *
     * @param hhmm 标准时分字符串，格式HH:mm
     * @return int 当日从0点累计总分钟
     * @author KevenPotter
     * @date 2026-07-13 15:26:00
     */
    private static int parseTimeToMin(String hhmm) {
        String[] arr = hhmm.split(":");
        if (arr.length != 2) {
            return -1;
        }
        try {
            int h = Integer.parseInt(arr[0]);
            int m = Integer.parseInt(arr[1]);
            return h * 60 + m;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

}
