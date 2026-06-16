package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardGroundLockData;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_GROUND_LOCK_DATA;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 地锁数据上送 [0X61]
 *
 * @author KevenPotter
 * @date 2026-06-12 16:33:04
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAYGroundLockDataRes extends FrameHeader {

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

    /**
     * 构建下发指令
     *
     * @param groundLockData 地锁数据上送
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-12 16:33:15
     */
    public static byte[] buildCommand(StandardGroundLockData groundLockData) {
        SAYGroundLockDataRes res = new SAYGroundLockDataRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_GROUND_LOCK_DATA);
        res.setDeviceId(groundLockData.getDeviceId());
        res.setGunNo(groundLockData.getGunNo());
        res.setLockStatus(groundLockData.getLockStatus());
        res.setLockStatusDesc(parseLockStatusDesc(groundLockData.getLockStatus()));
        res.setParkingStatus(groundLockData.getParkingStatus());
        res.setParkingStatusDesc(parseParkingStatusDesc(groundLockData.getParkingStatus()));
        res.setBatteryStatus(groundLockData.getBatteryStatus());
        res.setAlarmStatus(groundLockData.getAlarmStatus());
        res.setAlarmStatusDesc(parseAlarmStatusDesc(groundLockData.getAlarmStatus()));
        res.setReserved("00000000");

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, false);

        // 记录日志
        if (CloudChargeHolder.isSimulatorLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-06-12 16:33:37
     */
    private byte[] buildBody() {
        byte[] body = new byte[16];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BIN]
        body[7] = this.gunNo.byteValue();
        // 车位锁状态 [1字节] [BIN]
        body[8] = this.lockStatus.byteValue();
        // 车位状态 [1字节] [BIN]
        body[9] = this.parkingStatus.byteValue();
        // 地锁电量状态 [1字节] [BIN]
        body[10] = this.batteryStatus.byteValue();
        // 报警状态 [1字节] [BIN]
        body[11] = this.alarmStatus.byteValue();
        // 预留位 [4字节] [BIN]
        byte[] reserveBytes = HexUtil.decodeHex(this.reserved);
        System.arraycopy(reserveBytes, 0, body, 12, 4);

        return body;
    }

    /**
     * 解析车位锁状态描述
     *
     * @author KevenPotter
     * @date 2026-06-12 16:38:54
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
     * @date 2026-06-12 16:39:07
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
     * @date 2026-06-12 16:39:20
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
     * @date 2026-06-12 16:33:59
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x61】 {} 地锁数据上送  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x61】 {} 地锁数据上送  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x61】 {} 地锁数据上送  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0x61】 {} 地锁数据上送  车锁状态    lockStatusDesc               : {}", PURPLE + deviceId + RESET, lockStatusDesc);
        log.info("🚀 【0x61】 {} 地锁数据上送  车位状态    parkingStatusDesc            : {}", PURPLE + deviceId + RESET, parkingStatusDesc);
        log.info("🚀 【0x61】 {} 地锁数据上送  电量状态    batteryStatus                : {}", PURPLE + deviceId + RESET, batteryStatus);
        log.info("🚀 【0x61】 {} 地锁数据上送  报警状态    alarmStatusDesc              : {}", PURPLE + deviceId + RESET, alarmStatusDesc);
        log.info("🚀 【0x61】 {} 地锁数据上送  预留位值    reserved                     : {}", PURPLE + deviceId + RESET, reserved);
        System.out.println();
    }
}
