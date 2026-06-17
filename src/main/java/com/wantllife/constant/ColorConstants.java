package com.wantllife.constant;

/**
 * 颜色拾取常量
 *
 * @author KevenPotter
 * @date 2026-05-28 11:00:07
 */
public final class ColorConstants {

    /**
     * 私有构造函数，禁止实例化
     *
     * @author KevenPotter
     * @date 2026-05-28 11:05:40
     */
    private ColorConstants() {
    }

    // ===================== 【控制台颜色常量】 =====================

    /** 重置(恢复默认颜色,必须加) */
    public static final String RESET = "\u001B[0m";

    /*
     * 常规颜色
     */
    /** 黑色 */
    public static final String BLACK = "\u001B[30m";
    /** 红色 */
    public static final String RED = "\u001B[31m";
    /** 绿色 */
    public static final String GREEN = "\u001B[32m";
    /** 黄色 */
    public static final String YELLOW = "\u001B[33m";
    /** 蓝色 */
    public static final String BLUE = "\u001B[34m";
    /** 紫色 */
    public static final String PURPLE = "\u001B[35m";
    /** 青色 */
    public static final String CYAN = "\u001B[36m";
    /** 灰色 */
    public static final String GRAY = "\u001B[37m";

    /*
     * 高亮色(更鲜艳)
     */
    /** 黑色 */
    public static final String BRIGHT_BLACK = "\u001B[90m";
    /** 红色 */
    public static final String BRIGHT_RED = "\u001B[91m";
    /** 绿色 */
    public static final String BRIGHT_GREEN = "\u001B[92m";
    /** 黄色 */
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    /** 蓝色 */
    public static final String BRIGHT_BLUE = "\u001B[94m";
    /** 紫色 */
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    /** 青色 */
    public static final String BRIGHT_CYAN = "\u001B[96m";
    /** 灰色 */
    public static final String BRIGHT_GRAY = "\u001B[97m";
}
