package com.wantllife.analysis.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 地锁数据上送 [0X61]
 *
 * @author KevenPotter
 * @date 2026-04-28 15:24:32
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AYGroundLockDataReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*车位锁状态*/
    private Integer lockStatus;
    /*车位锁状态描述*/
    private String lockStatusDesc;
    /*车位状态*/
    private Integer parkingStatus;
    /*车位状态描述*/
    private String parkingStatusDesc;
    /*地锁电量状态*/
    private Integer batteryStatus;
    /*报警状态*/
    private Integer alarmStatus;
    /*报警状态描述*/
    private String alarmStatusDesc;
    /*预留位*/
    private String reserved;


    /* 有参构造 */
    public AYGroundLockDataReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-04-28 15:28:40
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BIN]
        this.gunNo = data[index++] & 0xFF;
        // 车位锁状态 [1字节] [BIN]
        this.lockStatus = data[index++] & 0xFF;
        this.lockStatusDesc = parseLockStatusDesc(this.lockStatus);
        // 车位状态 [1字节] [BIN]
        this.parkingStatus = data[index++] & 0xFF;
        this.parkingStatusDesc = parseParkingStatusDesc(this.parkingStatus);
        // 地锁电量状态 [1字节] [BIN]
        this.batteryStatus = data[index++] & 0xFF;
        // 报警状态 [1字节] [BIN]
        this.alarmStatus = data[index++] & 0xFF;
        this.alarmStatusDesc = parseAlarmStatusDesc(this.alarmStatus);
        // 预留位 [4字节] [BIN]
        byte[] reserveBytes = new byte[4];
        System.arraycopy(data, index, reserveBytes, 0, 4);
        this.reserved = HexUtil.encodeHexStr(reserveBytes).toUpperCase();
    }

    /**
     * 解析车位锁状态描述
     *
     * @author KevenPotter
     * @date 2026-04-28 15:29:38
     */
    private static String parseLockStatusDesc(Integer lockStatus) {
        switch (lockStatus) {
            case 0x00:
                return "未到位状态";
            case 0x55:
                return "升锁到位状态";
            case 0xFF:
                return "降锁到位状态";
            default:
                return "未知状态";
        }
    }

    /**
     * 解析车位状态描述
     *
     * @author KevenPotter
     * @date 2026-04-28 15:30:27
     */
    private static String parseParkingStatusDesc(Integer parkingStatus) {
        switch (parkingStatus) {
            case 0x00:
                return "无车辆";
            case 0xFF:
                return "停放车辆";
            default:
                return "未知状态";
        }
    }

    /**
     * 解析报警状态描述
     *
     * @author KevenPotter
     * @date 2026-04-28 15:29:38
     */
    private static String parseAlarmStatusDesc(Integer alarmStatus) {
        switch (alarmStatus) {
            case 0x00:
                return "正常无报警";
            case 0x55:
                return "摇臂升降异常(未到位)";
            case 0xFF:
                return "待机状态摇臂破坏";
            default:
                return "未知状态";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 16:17:40
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x61】 {} 地锁数据上送 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x61】 {} 地锁数据上送 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x61】 {} 地锁数据上送 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x61】 {} 地锁数据上送 车锁状态    lockStatusDesc               : {}", deviceId, lockStatusDesc);
        log.info("🟢 【0x61】 {} 地锁数据上送 车位状态    parkingStatusDesc            : {}", deviceId, parkingStatusDesc);
        log.info("🟢 【0x61】 {} 地锁数据上送 电量状态    batteryStatus                : {}", deviceId, batteryStatus);
        log.info("🟢 【0x61】 {} 地锁数据上送 报警状态    alarmStatusDesc              : {}", deviceId, alarmStatusDesc);
        log.info("🟢 【0x61】 {} 地锁数据上送 预留位值    reserved                     : {}", deviceId, reserved);
        System.out.println();
    }

}
