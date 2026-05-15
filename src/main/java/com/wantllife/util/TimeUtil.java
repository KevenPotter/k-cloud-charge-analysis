package com.wantllife.util;

import cn.hutool.core.util.HexUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 *
 * @author KevenPotter
 * @date 2026-05-15 09:31:05
 */
public class TimeUtil {

    /**
     * CP56Time2a时间解析
     *
     * @author KevenPotter
     * @date 2026-04-27 15:20:30
     */
    public static String parseCP56Time(byte[] timeBytes) {
        if (timeBytes == null || timeBytes.length != 7) {
            return "";
        }

        // 转16进制字符串(直接按原始顺序，不反转！！！)
        String hex = HexUtil.encodeHexStr(timeBytes).toUpperCase();

        int millis = Integer.parseInt(hex.substring(0, 2), 16) | (Integer.parseInt(hex.substring(2, 4), 16) << 8);
        int minute = Integer.parseInt(hex.substring(4, 6), 16) & 0x3F;
        int hour = Integer.parseInt(hex.substring(6, 8), 16) & 0x1F;
        int day = Integer.parseInt(hex.substring(8, 10), 16) & 0x1F;
        int month = Integer.parseInt(hex.substring(10, 12), 16) & 0x0F;
        int year = 2000 + (Integer.parseInt(hex.substring(12, 14), 16) & 0x7F);

        int second = millis / 1000;
        millis = millis % 1000;

        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d",
                year, month, day, hour, minute, second, millis);
    }

    /**
     * 转换P56Time2a时间
     *
     * @author KevenPotter
     * @date 2026-04-28 13:53:48
     */
    public static String transformCP56Time(String timeStr) {
        // 时间格式化模板
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime time = LocalDateTime.parse(timeStr, formatter);
        // 提取各个字段
        int year = time.getYear() - 2000;                   // CP56时间是从2000开始算
        int month = time.getMonthValue();
        int day = time.getDayOfMonth();
        int hour = time.getHour();
        int minute = time.getMinute();
        int second = time.getSecond();
        int millis = time.getNano() / 1_000_000;            // 毫秒
        // 组合成 CP56 毫秒值（2字节）
        int millisTotal = second * 1000 + millis;
        // 按协议拼接 7 个字节（低位在前，高位在后）
        byte[] cp56Bytes = new byte[7];
        cp56Bytes[0] = (byte) (millisTotal & 0xFF);         // 毫秒低字节
        cp56Bytes[1] = (byte) ((millisTotal >> 8) & 0xFF);  // 毫秒高字节
        cp56Bytes[2] = (byte) minute;                       // 分钟
        cp56Bytes[3] = (byte) hour;                         // 小时
        cp56Bytes[4] = (byte) day;                          // 日期
        cp56Bytes[5] = (byte) month;                        // 月
        cp56Bytes[6] = (byte) year;                         // 年（+2000）
        // 转成大写16进制字符串（和你原来格式一致）
        return HexUtil.encodeHexStr(cp56Bytes).toUpperCase();
    }
}
