package com.wantllife.util;

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

}
