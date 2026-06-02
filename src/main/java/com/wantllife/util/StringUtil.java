package com.wantllife.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 字符串工具类
 *
 * @author KevenPotter
 * @date 2026-05-14 17:10:54
 */
public class StringUtil {

    /**
     * BCD转字符串
     *
     * @author KevenPotter
     * @date 2026-05-14 17:18:20
     */
    public static String bcd2String(byte[] data, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + len; i++) {
            sb.append((data[i] >> 4) & 0x0F);
            sb.append(data[i] & 0x0F);
        }
        return sb.toString();
    }

    /**
     * 字符串转BCD码
     *
     * @author KevenPotter
     * @date 2026-05-14 17:19:55
     */
    public static byte[] string2bcd(String str) {
        int len = str.length();
        byte[] bcd = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 高4位
            int high = str.charAt(i) - '0';
            // 低4位
            int low = str.charAt(i + 1) - '0';
            // 合并成一个字节
            bcd[i / 2] = (byte) ((high << 4) | (low & 0x0F));
        }
        return bcd;
    }

    /**
     * 生成交易流水号
     * 格式：桩号(7字节) + 枪号(1字节) + 年月日时分秒(6字节) + 自增序号(2字节)
     * 示例：520106001090420120240415121160
     *
     * @param deviceId 桩编号，如 "52010600109042"
     * @param gunNo    枪号，如 1
     * @return 交易流水号，32位纯数字字符串（可用于BCD编码）
     * @author KevenPotter
     * @date 2026-06-01 17:22:36
     */
    public static String generateSerial(String deviceId, Integer gunNo) {
        // 1. 桩号 (7字节 = 14位，不足补0)
        String paddedDeviceId = String.format("%14s", deviceId).replace(' ', '0');
        // 2. 枪号 (1字节 = 2位十进制，不足补0)
        String gunNoDec = String.format("%02d", gunNo);
        // 3. 年月日时分秒 (6字节 = 12位十进制，格式：yyMMddHHmmss)
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        // 4. 自增序号 (2字节 = 4位十进制，0000-9999)
        int seq = (int) (System.currentTimeMillis() % 10000);
        String seqNum = String.format("%04d", seq);
        return paddedDeviceId + gunNoDec + timeStr + seqNum;
    }

}
