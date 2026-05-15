package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_LOCK_UP_DOWN;


/**
 * 遥控地锁升锁与降锁 [0X62]
 *
 * @author KevenPotter
 * @date 2026-04-28 16:11:55
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AZLockUpDownRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*升/降地锁(0.降锁 1.升锁)*/
    private Integer upOrDown;
    /*预留位*/
    private String reserved;


    /**
     * 构建下发指令
     *
     * @param deviceId 设备编号
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-28 16:12:40
     */
    public static byte[] buildCommand(String deviceId, Integer gunNo, Integer upOrDown) {
        AZLockUpDownRes res = new AZLockUpDownRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_LOCK_UP_DOWN);
        res.setDeviceId(deviceId);
        res.setGunNo(gunNo);
        res.setUpOrDown(upOrDown);
        res.setReserved("00000000");

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body);

        // 记录日志
        res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-28 16:13:50
     */
    private byte[] buildBody() {
        byte[] body = new byte[13];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BIN]
        body[7] = (byte) (this.gunNo & 0xFF);
        // 升/降地锁命令 [1字节] [BIN] 0x55=升锁 0xFF=降锁
        byte lockCmd = (byte) (this.upOrDown == 1 ? 0x55 : 0xFF);
        body[8] = lockCmd;
        // 预留位 [4字节] [BIN] 固定填0
        body[9] = 0x00;
        body[10] = 0x00;
        body[11] = 0x00;
        body[12] = 0x00;
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 16:21:37
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x62】 {} 遥控地锁升锁与降锁 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x62】 {} 遥控地锁升锁与降锁 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x62】 {} 遥控地锁升锁与降锁 枪口编号    gunNo                : {}", deviceId, gunNo);
        log.info("🔶 【0x62】 {} 遥控地锁升锁与降锁 升降地锁    upOrDown             : {}", deviceId, upOrDown == 0 ? "降锁" : "升锁");
        log.info("🔶 【0x62】 {} 遥控地锁升锁与降锁 预留位值    reserved             : {}", deviceId, reserved);
        System.out.println();
    }

}
