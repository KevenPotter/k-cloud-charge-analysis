package com.wantllife.enums;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
     * 动态时段判定入口.
     * <p>
     * 根据0x0A下发四段区间,判断指定时间归属尖/峰/平/谷
     * 自动支持跨天区间,例如22:00-07:00谷段
     *
     * @param now         待判定时间点
     * @param sharpRange  尖时段区间，格式 HH:mm-HH:mm
     * @param peakRange   峰时段区间，格式 HH:mm-HH:mm
     * @param flatRange   平时段区间，格式 HH:mm-HH:mm
     * @param valleyRange 谷时段区间，格式 HH:mm-HH:mm
     * @return TimeSegment 匹配到的时段类型,无匹配默认返回谷时段
     * @author KevenPotter
     * @date 2026-06-22 15:46:20
     */
    public static TimeSegment getTimeSegment(LocalDateTime now, String sharpRange, String peakRange, String flatRange, String valleyRange) {
        LocalTime nowTime = now.toLocalTime();
        if (isInTimeRange(nowTime, sharpRange)) return SHARP;
        if (isInTimeRange(nowTime, peakRange)) return PEAK;
        if (isInTimeRange(nowTime, flatRange)) return FLAT;
        return VALLEY;
    }

    /**
     * 内部工具方法:判断单个时刻是否落在指定时间区间内
     * <p>
     * 兼容两种场景:同天区间、跨零点区间（start > end）
     *
     * @param target   待判断时刻
     * @param rangeStr 区间字符串,格式必须为 HH:mm-HH:mm
     * @return true:在区间内 false:不在/格式异常/入参为空
     * @author KevenPotter
     * @date 2026-06-22 15:47:35
     */
    private static boolean isInTimeRange(LocalTime target, String rangeStr) {
        if (rangeStr == null || !rangeStr.contains("-")) return false;
        String[] arr = rangeStr.split("-");
        if (arr.length != 2) return false;
        LocalTime start, end;
        try {
            start = LocalTime.parse(arr[0], TIME_FORMAT);
            end = LocalTime.parse(arr[1], TIME_FORMAT);
        } catch (Exception e) {
            return false;
        }
        // 跨天场景:起始时间大于结束时间,如22:00 ~ 07:00
        if (start.isAfter(end)) {
            return target.isAfter(start) || target.isBefore(end);
        } else {
            // 普通同天区间
            return !target.isBefore(start) && !target.isAfter(end);
        }
    }

}
